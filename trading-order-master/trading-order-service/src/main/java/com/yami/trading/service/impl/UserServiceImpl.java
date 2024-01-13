package com.yami.trading.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.constans.UserConstants;
import com.yami.trading.bean.model.*;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.bean.syspara.dto.SysparasDto;
import com.yami.trading.bean.user.dto.AgentUserDto;
import com.yami.trading.bean.user.dto.UserDataDto;
import com.yami.trading.bean.user.dto.UserDto;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.*;
import com.yami.trading.dao.user.SmsLogMapper;
import com.yami.trading.dao.user.UserMapper;
import com.yami.trading.service.IdentifyingCodeTimeWindowService;
import com.yami.trading.service.MoneyLogService;
import com.yami.trading.service.OnlineUserService;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.*;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.syspara.SysparaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    UserRecomService userRecomService;
    @Autowired
    WalletService walletService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    SysparaService sysparaService;
    @Autowired
    LogService logService;
    @Autowired
    MoneyLogService moneyLogService;
    @Autowired
    AgentService agentService;
    @Autowired
    @Lazy
    private UserDataService userDataService;
    /**
     * 图片验证key，保证前后一致性
     */
    private Map<String, String> imageCodeCache = new ConcurrentHashMap<String, String>();
    @Autowired
    OnlineUserService onlineUserService;
    @Autowired
    WalletExtendService walletExtendService;
    @Autowired
    IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService;
    @Autowired(required = false)
    @Qualifier("dataService")
    private DataService dataService;
    @Autowired
    SmsLogMapper smsLogMapper;
    @Autowired
    private SmsLogService smsLogService;

    @Override
    public boolean checkLoginSafeword(User user, String loginSafeword) {
        return passwordEncoder.matches(loginSafeword, user.getSafePassword());
    }

    @Override
    public Page<UserDto> listUser(Page page, List<String> roleNames, String userCode, String userName, List<String> checkedList) {
        return baseMapper.listUser(page, roleNames, userCode, userName, checkedList);
    }

    @Override
    public Page<UserDataDto> listUserAndRecom(Page page, List<String> roleNames, String userCode, String userName, String lastIp, List<String> checkedList) {
        return baseMapper.listUserAndRecom(page, roleNames, userCode,
                userName, lastIp, checkedList);
    }

    @Override
    public boolean checkLoginSafeword(String userId, String loginSafeword) {
        User user = getById(userId);
        if (user == null) {
            throw new YamiShopBindException("用户不存在!");
        }
        return checkLoginSafeword(user, loginSafeword);
    }

    @Override
    public void updateAgent(String userId, boolean operaAuthority, boolean loginAuthority) {
        String roleName = operaAuthority ? Constants.SECURITY_ROLE_AGENT : Constants.SECURITY_ROLE_AGENTLOW;
        User user = getById(userId);
        user.setStatus(loginAuthority ? 1 : 0);
        user.setRealName(roleName);
        updateById(user);
    }

    @Override
    public User cacheUserBy(String userId) {
        return null;
    }

    @Override
    public long countToDay() {
        Date now = new Date();
        return count(Wrappers.<User>query().lambda().between(User::getCreateTime, DateUtil.minDate(now),
                DateUtil.maxDate(now)).eq(User::getRoleName, Constants.SECURITY_ROLE_MEMBER));
    }
    @Override
    public long countToDays(String userCode) {
        Date now = new Date();
        return count(Wrappers.<User>query().lambda().between(User::getCreateTime, DateUtil.minDate(now),
                DateUtil.maxDate(now)).eq(User::getRoleName, Constants.SECURITY_ROLE_MEMBER)
                .eq(StringUtils.isNotEmpty(userCode), User::getRecomCode, userCode));
    }

    /**
     * 根据已验证的邮箱获取Party对象
     *
     * @param email 电子邮件
     * @return 用户对象
     */
    @Override
    public User findPartyByVerifiedEmail(String email) {
        if (null == email) return null;
        List<User> list = list(Wrappers.<User>query().lambda().eq(User::getUserMail, email).eq(User::isMailBind, true));
        return list.size() > 0 ? list.get(0) : null;
    }

    public void savePhone(String phone, String partyId) {
        /**
         * party
         */
        User party = getById(partyId);
        party.setUserMobile(phone);
        party.setUserMobileBind(true);
        // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
        // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
        // 如：级别11表示：新注册的前端显示为VIP1；
        int userLevel = party.getUserLevel();
//        party.setUserLevel(((int) Math.floor(userLevel / 10)) * 10 + 2);
        updateById(party);
    }

    @Override
    public void logout(String userId) {
        if (StringUtils.isNullOrEmpty(userId)) {
            return;
        }
        onlineUserService.del(userId);
    }

    @Override
    public Page getAgentAllStatistics(long current, long size, String startTime, String endTime, String userName,
                                      String targetPartyId) {
        Page<AgentUserDto> page = new Page(current, size);
        List children = null;
        if (!StringUtils.isNullOrEmpty(targetPartyId)) {
            children = userRecomService.findRecomsToPartyId(targetPartyId);
            if (children.size() == 0) {
                return new Page();
            }
        }
        baseMapper.getAgentAllStatistics(page, userName, children);
        /**
         * 页面查询第一层partyId级
         */
        List<String> list_partyId = new ArrayList<String>();
        for (int i = 0; i < page.getRecords().size(); i++) {
            AgentUserDto agentUserDto = page.getRecords().get(i);
            list_partyId.add(agentUserDto.getUserId().toString());
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < list_partyId.size(); i++) {
            int reco_agent = 0;
            log.info(list_partyId.get(i));
            /**
             * 所有子集
             */
            List<String> children_all = this.userRecomService.findChildren(list_partyId.get(i));
            /**
             * 正式用户
             */
            List<String> children_member = new ArrayList<>();
            for (int j = 0; j < children_all.size(); j++) {
                String partyId = children_all.get(j);
                User party = getById(partyId);
                if (Constants.SECURITY_ROLE_AGENT.equals(party.getRoleName()) || Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRoleName())) {
                    reco_agent++;
                } else if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRoleName())) {
                    children_member.add(partyId);
                }
            }
            Map<String, Object> item_result = this.sumUserData(children_member, startTime, endTime);
            item_result.put("reco_agent", reco_agent);
            item_result.put("reco_member", children_member.size());
            item_result.put("partyId", list_partyId.get(i));
            User party = getById(list_partyId.get(i));
            item_result.put("username", party.getUserName());
            item_result.put("UID", party.getUserCode());
            result.add(item_result);
        }
        Page page_result = new Page();
        page_result.setRecords(result);
        compute(page_result.getRecords());// 计算总收益
        return page_result;
    }

    /**
     * 统计的数据存在空时，不统计总额
     *
     * @param data
     * @return
     */
    private boolean dataExistNull(Map<String, Object> data) {
        if (null == data.get("recharge_withdrawal_fee"))
            return false;
        if (null == data.get("order_income"))
            return false;
        if (null == data.get("fee"))
            return false;
        if (null == data.get("finance_income"))
            return false;
        if (null == data.get("exchange_fee"))
            return false;
        if (null == data.get("exchange_income"))
            return false;
        if (null == data.get("furtures_fee"))
            return false;
        if (null == data.get("furtures_income"))
            return false;
        return true;
    }

    private void compute(List<Map<String, Object>> datas) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(datas))
            return;
        Double totle_income = 0d;
        Double totle_fee = 0d;
        Double business_profit = 0d;//交易盈亏
        Double fin_miner_amount = 0d;//理财 矿机 交易额
        Double fin_miner_income = 0d;//理财 矿机 收益
        for (Map<String, Object> data : datas) {
            totle_income = 0d;
            totle_fee = 0d;
            business_profit = 0d;
            fin_miner_amount = 0d;
            fin_miner_income = 0d;
            if (null != data.get("order_income"))
                data.put("order_income", Arith.sub(0, new Double(data.get("order_income").toString())));// 订单收益负数
            if (null != data.get("finance_income"))
                data.put("finance_income", Arith.sub(0, new Double(data.get("finance_income").toString())));// 理财收益负数
            if (null != data.get("exchange_income"))
                data.put("exchange_income", 0);// 币币收益负数
            if (null != data.get("furtures_income"))
                data.put("furtures_income", Arith.sub(0, new Double(data.get("furtures_income").toString())));// 交割收益负数
            if (null != data.get("miner_income"))
                data.put("miner_income", Arith.sub(0, new Double(data.get("miner_income").toString())));// 矿机收益负数
            if (null != data.get("exchange_lever_order_income"))
                data.put("exchange_lever_order_income", Arith.sub(0, new Double(data.get("exchange_lever_order_income").toString())));// 币币收益负数
            if (!dataExistNull(data))
                continue;
            totle_income = Arith.add(totle_income, new Double(data.get("recharge_withdrawal_fee").toString()));
            totle_income = Arith.add(totle_income, new Double(data.get("order_income").toString()));
            totle_income = Arith.add(totle_income, new Double(data.get("fee").toString()));
            totle_income = Arith.add(totle_income, new Double(data.get("finance_income").toString()));
            totle_income = Arith.add(totle_income, new Double(data.get("exchange_fee").toString()));
            totle_income = Arith.add(totle_income, new Double(0));
            totle_income = Arith.add(totle_income, new Double(data.get("furtures_fee").toString()));
            totle_income = Arith.add(totle_income, new Double(data.get("furtures_income").toString()));
            totle_income = Arith.add(totle_income, new Double(data.get("miner_income").toString()));
            totle_income = Arith.add(totle_income, new Double(data.get("exchange_lever_order_income").toString()));
            data.put("totle_income", totle_income);
            totle_fee = Arith.add(totle_fee, new Double(data.get("recharge_withdrawal_fee").toString()));
            totle_fee = Arith.add(totle_fee, new Double(data.get("fee").toString()));
            totle_fee = Arith.add(totle_fee, new Double(data.get("exchange_fee").toString()));
            totle_fee = Arith.add(totle_fee, new Double(data.get("furtures_fee").toString()));
            totle_fee = Arith.add(totle_fee, new Double(data.get("exchange_lever_fee").toString()));
            data.put("totle_fee", totle_fee);
            business_profit = Arith.add(business_profit, new Double(data.get("order_income").toString()));
            business_profit = Arith.add(business_profit, new Double(data.get("exchange_income").toString()));
            business_profit = Arith.add(business_profit, new Double(data.get("furtures_income").toString()));
            business_profit = Arith.add(business_profit, new Double(data.get("exchange_lever_order_income").toString()));
            data.put("business_profit", business_profit);
            fin_miner_amount = Arith.add(fin_miner_amount, new Double(data.get("finance_amount").toString()));
            fin_miner_amount = Arith.add(fin_miner_amount, new Double(data.get("miner_amount").toString()));
            data.put("fin_miner_amount", fin_miner_amount);
            fin_miner_income = Arith.add(fin_miner_income, new Double(data.get("finance_income").toString()));
            fin_miner_income = Arith.add(fin_miner_income, new Double(data.get("miner_income").toString()));
            data.put("fin_miner_income", fin_miner_income);
        }
    }

    private List<UserData> filterData(Map<String, UserData> datas, String startTime, String endTime) {
        List<UserData> result = new ArrayList<UserData>();
        for (Map.Entry<String, UserData> valueEntry : datas.entrySet()) {
            UserData userdata = valueEntry.getValue();
            Date time = userdata.getCreateTime();
            if (!StringUtils.isNullOrEmpty(startTime)) {
                Date startDate = DateUtils.toDate(startTime, DateUtils.DF_yyyyMMdd);
                int intervalDays = DateUtils.getIntervalDaysByTwoDate(startDate, time);// 开始-数据时间
                if (intervalDays > 0) // 开始>数据时间 ，则过滤
                    continue;
            }
            if (!StringUtils.isNullOrEmpty(endTime)) {
                Date endDate = DateUtils.toDate(endTime, DateUtils.DF_yyyyMMdd);
                int intervalDays = DateUtils.getIntervalDaysByTwoDate(endDate, time);// 结束-数据时间
                if (intervalDays < 0) // 结束<数据时间
                    continue;
            }
            result.add(userdata);
        }
        return result;
    }

    private Map<String, Object> sumUserData(List<String> children, String startTime, String endTime) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(children)) {//children数据为空时，数据填充,这里操作减少dubbo调用
            return sumData(new HashMap<String, Object>(), new ArrayList<UserData>());
        }
        Map<String, Object> item_result = new HashMap<String, Object>();
        List<Map<String, UserData>> datas = this.userDataService.findByPartyIds(children);
        for (int i = 0; i < datas.size(); i++) {
            Map<String, UserData> data_all = datas.get(i);
            if (data_all == null) {
                continue;
            }
            List<UserData> userdata = filterData(data_all, startTime, endTime);
            item_result = sumData(item_result, userdata);
        }
        if (item_result.isEmpty()) {//item_result数据为空时，数据填充
            item_result = sumData(item_result, new ArrayList<UserData>());
        }
        return item_result;
    }

    private Map<String, Object> sumData(Map<String, Object> item_result, List<UserData> datas) {
        double recharge_dapp = 0;
        double withdraw_dapp = 0;
        double recharge = 0;
        double recharge_usdt = 0;
        double recharge_eth = 0;
        double recharge_btc = 0;
        double recharge_ht = 0;
        double recharge_ltc = 0;
        double withdraw = 0;
        double withdraw_eth = 0;
        double withdraw_btc = 0;
        double recharge_withdrawal_fee = 0;
        double gift_money = 0;
        double balance_amount = 0;
        double amount = 0;
        double fee = 0;
        double order_income = 0;
        double finance_amount = 0;
        double finance_income = 0;
        double exchange_amount = 0;
        double exchange_fee = 0;
        double exchange_income = 0;
        double coin_income = 0;
        double furtures_amount = 0;
        double furtures_fee = 0;
        double furtures_income = 0;
        double miner_income = 0;
        double miner_amount = 0;
        double third_recharge_amount = 0;
        double exchange_lever_amount = 0;
        double exchange_lever_fee = 0;
        double exchange_lever_order_income = 0;
        for (int i = 0; i < datas.size(); i++) {
            UserData data = datas.get(i);
            // 充提
            recharge_dapp = Arith.add(data.getRechargeDapp(), recharge_dapp);
            withdraw_dapp = Arith.add(data.getWithdrawDapp(), withdraw_dapp);
            recharge = Arith.add(data.getRecharge(), recharge);
            recharge_usdt = Arith.add(data.getRechargeUsdt(), recharge_usdt);
            recharge_eth = Arith.add(data.getRechargeEth(), recharge_eth);
            recharge_btc = Arith.add(data.getRechargeBtc(), recharge_btc);
            recharge_ht = Arith.add(data.getRechargeHt(), recharge_ht);
            recharge_ltc = Arith.add(data.getRechargeLtc(), recharge_ltc);
            withdraw = Arith.add(data.getWithdraw(), withdraw);
            withdraw_eth = Arith.add(data.getWithdrawEth(), withdraw_eth);
            withdraw_btc = Arith.add(data.getWithdrawBtc(), withdraw_btc);
            recharge_withdrawal_fee = Arith.add(data.getRechargeWithdrawalFee(), recharge_withdrawal_fee);
            gift_money = Arith.add(data.getGiftMoney(), gift_money);
            balance_amount = Arith.add(Arith.sub(data.getRecharge(), data.getWithdraw()), balance_amount);
            // 永续
            amount = Arith.add(data.getAmount(), amount);
            fee = Arith.add(data.getFee(), fee);
            order_income = Arith.add(data.getOrderIncome(), order_income);
            // 理财
            finance_amount = Arith.add(data.getFinanceAmount(), finance_amount);
            finance_income = Arith.add(data.getFinanceIncome(), finance_income);
            // 币币
            exchange_amount = Arith.add(data.getExchangeAmount(), exchange_amount);
            exchange_fee = Arith.add(data.getExchangeFee(), exchange_fee);
            //exchange_income = Arith.add(data.getExchange_income(), exchange_income);
            exchange_income = 0;
            coin_income = Arith.add(data.getCoinIncome(), coin_income);
            // 交割
            furtures_amount = Arith.add(data.getFurturesAmount(), furtures_amount);
            furtures_fee = Arith.add(data.getFurturesFee(), furtures_fee);
            furtures_income = Arith.add(data.getFurturesIncome(), furtures_income);
            //矿机
            miner_income = Arith.add(data.getMinerIncome(), miner_income);
            miner_amount = Arith.add(data.getMinerAmount(), miner_amount);
            //三方充值货币金额
            third_recharge_amount = Arith.add(data.getThirdRechargeAmount(), third_recharge_amount);
            //币币杠杆
            exchange_lever_amount = Arith.add(data.getExchangeLeverAmount(), exchange_lever_amount);
            exchange_lever_fee = Arith.add(data.getExchangeLeverFee(), exchange_lever_fee);
            exchange_lever_order_income = Arith.add(data.getExchangeLeverOrderIncome(), exchange_lever_order_income);
        }
        if (item_result != null && item_result.size() != 0) {
            // 充提
            item_result.put("recharge_dapp", Arith.add(Double.valueOf(item_result.get("recharge_dapp").toString()), recharge_dapp));
            item_result.put("withdraw_dapp", Arith.add(Double.valueOf(item_result.get("withdraw_dapp").toString()), withdraw_dapp));
            item_result.put("recharge", Arith.add(Double.valueOf(item_result.get("recharge").toString()), recharge));
            item_result.put("recharge_usdt", Arith.add(Double.valueOf(item_result.get("recharge_usdt").toString()), recharge_usdt));
            item_result.put("recharge_eth", Arith.add(Double.valueOf(item_result.get("recharge_eth").toString()), recharge_eth));
            item_result.put("recharge_btc", Arith.add(Double.valueOf(item_result.get("recharge_btc").toString()), recharge_btc));
            item_result.put("recharge_ht", Arith.add(Double.valueOf(item_result.get("recharge_ht").toString()), recharge_ht));
            item_result.put("recharge_ltc", Arith.add(Double.valueOf(item_result.get("recharge_ltc").toString()), recharge_ltc));
            item_result.put("withdraw", Arith.add(Double.valueOf(item_result.get("withdraw").toString()), withdraw));
            item_result.put("withdraw_eth", Arith.add(Double.valueOf(item_result.get("withdraw_eth").toString()), withdraw_eth));
            item_result.put("withdraw_btc", Arith.add(Double.valueOf(item_result.get("withdraw_btc").toString()), withdraw_btc));
            item_result.put("recharge_withdrawal_fee", Arith.add(Double.valueOf(item_result.get("recharge_withdrawal_fee").toString()), recharge_withdrawal_fee));
            item_result.put("gift_money", Arith.add(Double.valueOf(item_result.get("gift_money").toString()), gift_money));
            item_result.put("balance_amount", Arith.add(Double.valueOf(item_result.get("balance_amount").toString()), balance_amount));
            // 永续
            item_result.put("amount", Arith.add(Double.valueOf(item_result.get("amount").toString()), amount));
            item_result.put("fee", Arith.add(Double.valueOf(item_result.get("fee").toString()), fee));
            item_result.put("order_income", Arith.add(Double.valueOf(item_result.get("order_income").toString()), order_income));
            // 理财
            item_result.put("finance_amount", Arith.add(Double.valueOf(item_result.get("finance_amount").toString()), finance_amount));
            item_result.put("finance_income", Arith.add(Double.valueOf(item_result.get("finance_income").toString()), finance_income));
            // 币币
            item_result.put("exchange_amount", Arith.add(Double.valueOf(item_result.get("exchange_amount").toString()), exchange_amount));
            item_result.put("exchange_fee", Arith.add(Double.valueOf(item_result.get("exchange_fee").toString()), exchange_fee));
            //item_result.put("exchange_income", Arith.add(Double.valueOf( item_result.get("exchange_income").toString()),exchange_income));
            item_result.put("exchange_income", 0);
            item_result.put("coin_income", Arith.add(Double.valueOf(item_result.get("coin_income").toString()), coin_income));
            // 交割
            item_result.put("furtures_amount", Arith.add(Double.valueOf(item_result.get("furtures_amount").toString()), furtures_amount));
            item_result.put("furtures_fee", Arith.add(Double.valueOf(item_result.get("furtures_fee").toString()), furtures_fee));
            item_result.put("furtures_income", Arith.add(Double.valueOf(item_result.get("furtures_income").toString()), furtures_income));
            //矿机
            item_result.put("miner_income", Arith.add(Double.valueOf(item_result.get("miner_income").toString()), miner_income));
            item_result.put("miner_amount", Arith.add(Double.valueOf(item_result.get("miner_amount").toString()), miner_amount));
            //三方充值货币金额
            item_result.put("third_recharge_amount", Arith.add(Double.valueOf(item_result.get("third_recharge_amount").toString()), third_recharge_amount));
            //币币杠杆
            item_result.put("exchange_lever_amount", Arith.add(Double.valueOf(item_result.get("exchange_lever_amount").toString()), exchange_lever_amount));
            item_result.put("exchange_lever_fee", Arith.add(Double.valueOf(item_result.get("exchange_lever_fee").toString()), exchange_lever_fee));
            item_result.put("exchange_lever_order_income", Arith.add(Double.valueOf(item_result.get("exchange_lever_order_income").toString()), exchange_lever_order_income));
        } else {
            // 充提
            item_result.put("recharge_dapp", recharge_dapp);
            item_result.put("withdraw_dapp", withdraw_dapp);
            item_result.put("recharge", recharge);
            item_result.put("recharge_usdt", recharge_usdt);
            item_result.put("recharge_eth", recharge_eth);
            item_result.put("recharge_btc", recharge_btc);
            item_result.put("recharge_ht", recharge_ht);
            item_result.put("recharge_ltc", recharge_ltc);
            item_result.put("withdraw", withdraw);
            item_result.put("withdraw_eth", withdraw_eth);
            item_result.put("withdraw_btc", withdraw_btc);
            item_result.put("recharge_withdrawal_fee", recharge_withdrawal_fee);
            item_result.put("gift_money", gift_money);
            item_result.put("balance_amount", balance_amount);
            // 永续
            item_result.put("amount", amount);
            item_result.put("fee", fee);
            item_result.put("order_income", order_income);
            // 理财
            item_result.put("finance_amount", finance_amount);
            item_result.put("finance_income", finance_income);
            // 币币
            item_result.put("exchange_amount", exchange_amount);
            item_result.put("exchange_fee", exchange_fee);
            item_result.put("exchange_income", 0);
            item_result.put("coin_income", coin_income);
            // 交割
            item_result.put("furtures_amount", furtures_amount);
            item_result.put("furtures_fee", furtures_fee);
            item_result.put("furtures_income", furtures_income);
            // 矿机
            item_result.put("miner_income", miner_income);
            item_result.put("miner_amount", miner_amount);
            //三方充值货币金额
            item_result.put("third_recharge_amount", third_recharge_amount);
            //币币杠杆
            item_result.put("exchange_lever_amount", exchange_lever_amount);
            item_result.put("exchange_lever_fee", exchange_lever_fee);
            item_result.put("exchange_lever_order_income", exchange_lever_order_income);
        }
        return item_result;
    }

    @Override
    @Transactional
    public void saveRegisterUsername(String username, String password, String recoUserCode, String safeword) {
        User party_reco = findUserByUserCode(recoUserCode);
//		用户注册是否需要推荐码
        if ("true".equals(sysparaService.find("register_need_usercode").getSvalue())) {
            if (StringUtils.isNotEmpty(recoUserCode)) {
                if (party_reco == null) {
                    throw new YamiShopBindException("请输入正确的推荐码");
                }
                if (Constants.SECURITY_ROLE_TEST.equals(party_reco.getRoleName())) {
                    throw new YamiShopBindException("推荐人无权限推荐");
                }
                if (!party_reco.isEnabled()) {
                    throw new YamiShopBindException("推荐人无权限推荐");
                }
            }
        }
        int userLevel = 1;
        if (null != party_reco) {
            userLevel++;
            userLevel = getUserRecomLevel(userLevel, party_reco.getUserId());
        }
        if (findByUserName(username) != null) {
            throw new YamiShopBindException("用户名重复");
        }
        /**
         * 用户code
         */
        String usercode = getUserCode();
        /**
         * party
         */
        User party = new User();
        party.setUserName(username);
        party.setUserCode(usercode);
        int ever_user_level_num = sysparaService.find("ever_user_level_num").getInteger();
        int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();
//        party.setUserLevel(ever_user_level_num_custom * 10 + ever_user_level_num);
        party.setUserLevel(userLevel);
        party.setSafePassword(passwordEncoder.encode(safeword));
        party.setRoleName(Constants.SECURITY_ROLE_MEMBER);
        party.setLoginPassword(passwordEncoder.encode(password));
        save(party);
        /**
         * usdt账户
         */
        Wallet wallet = new Wallet();
        wallet.setUserId(party.getUserId().toString());
        this.walletService.save(wallet);
        if (party_reco != null) {
            UserRecom userRecom = new UserRecom();
            userRecom.setUserId(party_reco.getUserId());
            userRecom.setRecomUserId(party.getUserId());// 父类partyId
            this.userRecomService.save(userRecom);
            party.setUserRecom(party_reco.getUserId());
            updateById(party);
        }
//        String uuid = UUIDGenerator.getUUID();
//        String partyId = party.getUserId().toString();
//        String partyRecoId = party_reco != null?party_reco.getUserId().toString():"";
//        jdbcTemplate.execute("INSERT INTO T_USER(UUID,PARTY_ID,PARENT_PARTY_ID) VALUES('"+uuid+"','"+partyId+"','"+partyRecoId+"')");
        userDataService.saveRegister(party.getUserId());
        /**
         * 用户注册自动赠送金额 start
         */
        String register_gift_coin = sysparaService.find("register_gift_coin").getSvalue();
        if (!"".equals(register_gift_coin) && register_gift_coin != null) {
            String[] register_gift_coins = register_gift_coin.split(",");
            String gift_symbol = register_gift_coins[0];
            double gift_sum = Double.valueOf(register_gift_coins[1]);
            if ("usdt".equals(gift_symbol)) {
                Wallet walletExtend = this.walletService.saveWalletByPartyId(party.getUserId());
                double amount_before = walletExtend.getMoney().doubleValue();
                if (Arith.add(gift_sum, walletExtend.getMoney().doubleValue()) < 0.0D) {
                    throw new YamiShopBindException("操作失败！修正后账户余额小于0。");
                }
                walletService.update(wallet.getUserId().toString(), gift_sum);
                userDataService.saveGiftMoneyHandle(wallet.getUserId(), gift_sum);
            } else {
                WalletExtend walletExtend = this.walletService.saveExtendByPara(party.getUserId(), gift_symbol);
                double amount_before = walletExtend.getAmount();
                if (Arith.add(gift_sum, walletExtend.getAmount()) < 0.0D) {
                    throw new YamiShopBindException("操作失败！修正后账户余额小于0。");
                }
                walletService.updateExtend(walletExtend.getPartyId().toString(), gift_symbol, gift_sum);
                BigDecimal amount = dataService.realtime(gift_symbol).get(0).getClose().multiply(new BigDecimal(gift_sum)).setScale(2, RoundingMode.HALF_UP);
                userDataService.saveGiftMoneyHandle(wallet.getUserId(), amount.doubleValue());
            }
        }
    }

    public User findPartyByEmail(String email) {
        if (null == email) return null;
        List<User> list = list(Wrappers.<User>query().lambda().eq(User::getUserMail, email));
        return list.size() > 0 ? list.get(0) : null;
    }

    @Transactional
    @Override
    public void saveRegister(String username, String password, String usercode, String safeword, String verifcode) {
        Page<AgentDto> page = new Page(1, 5);
        if (StrUtil.isEmpty(verifcode)) {
            throw new YamiShopBindException("Invitation code not filled in");
        }
        page = agentService.listTotal(page, usercode);//usercode: 100058
        List<AgentDto> list = page.getRecords();
        if (list.size()<=0) {
            throw new YamiShopBindException("Agent recommendation code not found");
        }
        username = username.trim();
        password = password.trim();
        if (!"null".equals(safeword) && !StringUtils.isEmptyString(safeword)) {
            safeword = safeword.trim();
        }
        QueryWrapper<SmsLog> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("mobile_code", verifcode);
        SmsLog smsLog = smsLogMapper.selectOne(itemWrapper);
        if ((smsLog == null)) {
            throw new YamiShopBindException("Incorrect authorization code");
        }
        if( smsLog.getStatus().equals("0")){
            throw new YamiShopBindException("Used");
        }

        User party_reco = findUserByUserCode(usercode);
        if ("true".equals(this.sysparaService.find("register_need_usercode").getSvalue())) {
            if (StringUtils.isNotEmpty(usercode)) {
                if (null == party_reco) {
                    throw new YamiShopBindException("推荐码不正确");
                }
                if (Constants.SECURITY_ROLE_TEST.equals(party_reco.getRoleName())) {
                    throw new YamiShopBindException("推荐人无权限推荐");
                }
                if (!party_reco.isEnabled()) {
                    throw new YamiShopBindException("推荐人无权限推荐");
                }
            }
        }
        if (findByuserPhone(username) != null) {
            throw new YamiShopBindException("Duplicate accounts");
        }
        int userLevel = 1;
        if (null != party_reco) {
            userLevel++;
            userLevel = getUserRecomLevel(userLevel, party_reco.getUserId());
        }
        User party = new User();
        party.setUserName(username);
        party.setUserCode(getUserCode());
        party.setUserMobile(username);
        party.setUserLevel(userLevel);
        party.setRecomCode(usercode);
        party.setLoginPassword(passwordEncoder.encode(password));
        party.setSafePassword(this.passwordEncoder.encode(safeword));
        party.setRoleName(Constants.SECURITY_ROLE_MEMBER);
        save(party);
        this.savePhone(username, party.getUserId().toString());
        // usdt账户
        Wallet wallet = new Wallet();
        wallet.setUserId(party.getUserId().toString());
        this.walletService.save(wallet);
        if (party_reco != null) {
            UserRecom userRecom = new UserRecom();
            userRecom.setUserId(party_reco.getUserId());
            userRecom.setRecomUserId(party.getUserId());
            this.userRecomService.save(userRecom);
            party.setUserRecom(party_reco.getUserId());
            updateById(party);
        }
        this.userDataService.saveRegister(party.getUserId());
        smsLog.setStatus("0");
        smsLogMapper.updateById(smsLog);
    }

   /* @Transactional
    @Override
    public void saveRegister(String username, String password, String usercode, String safeword, String verifcode, String type) {
        Page<AgentDto> page = new Page(1, 5);
        page = agentService.listTotal(page, usercode);
        List<AgentDto> list = page.getRecords();
        if (list.size()<=0) {
            throw new YamiShopBindException("代理商推荐码未找到");
        }
        username = username.trim();
        password = password.trim();
        if (!"null".equals(safeword) && !StringUtils.isEmptyString(safeword)) {
            safeword = safeword.trim();
        }
        User party_reco = findUserByUserCode(usercode);
        String key = username;
        String authcode = identifyingCodeTimeWindowService.getAuthCode(key);
        if ((authcode == null) || (!authcode.equals(verifcode))) {
            throw new YamiShopBindException("验证码不正确");
        }
        if ("true".equals(this.sysparaService.find("register_need_usercode").getSvalue())) {
            if (StringUtils.isNotEmpty(usercode)) {
                if (null == party_reco) {
                    throw new YamiShopBindException("推荐码不正确");
                }
                if (Constants.SECURITY_ROLE_TEST.equals(party_reco.getRoleName())) {
                    throw new YamiShopBindException("推荐人无权限推荐");
                }
                if (!party_reco.isEnabled()) {
                    throw new YamiShopBindException("推荐人无权限推荐");
                }
            }
        }
        if (findByUserName(username) != null) {
            throw new YamiShopBindException("用户名重复");
        }
        int userLevel = 1;
        if (null != party_reco) {
            userLevel++;
            userLevel = getUserRecomLevel(userLevel, party_reco.getUserId());
        }
        User party = new User();
        party.setUserName(username);
        party.setUserCode(getUserCode());
        party.setUserLevel(userLevel);
        party.setRecomCode(usercode);
        party.setLoginPassword(passwordEncoder.encode(password));
        party.setSafePassword(this.passwordEncoder.encode(safeword));
        party.setRoleName(Constants.SECURITY_ROLE_MEMBER);
        save(party);
        if (type.equals("1")) {
            if (StringUtils.isEmptyString(username) || username.length() > 20) {
                throw new YamiShopBindException("请输入正确的手机号码");
            }
            this.savePhone(username, party.getUserId().toString());
        } else {
            // 邮箱注册
            if (!Strings.isEmail(username)) {
                throw new YamiShopBindException("请输入正确的邮箱地址");
            }
            if (findPartyByEmail(username) != null) {
                throw new YamiShopBindException("邮箱已重复");
            }
            this.saveEmail(username, party.getUserId().toString());
        }
        // usdt账户
        Wallet wallet = new Wallet();
        wallet.setUserId(party.getUserId().toString());
        this.walletService.save(wallet);
        if (party_reco != null) {
            UserRecom userRecom = new UserRecom();
            userRecom.setUserId(party_reco.getUserId());
            userRecom.setRecomUserId(party.getUserId());
            this.userRecomService.save(userRecom);
            party.setUserRecom(party_reco.getUserId());
            updateById(party);
        }
        this.userDataService.saveRegister(party.getUserId());
        this.identifyingCodeTimeWindowService.delAuthCode(key);
    }*/

    /**
     * 根据推荐关系获取层级
     *
     * @param userLevel
     * @param userId
     * @return
     */
    private int getUserRecomLevel(int userLevel, String userId) {
        // 查询上级用户
//        UserRecom userRecom = userRecomService.getOne(Wrappers.<UserRecom>lambdaQuery().eq(UserRecom::getUserId, userId));
//        if(null != userRecom) {
//            userLevel ++;
//            return this.getUserRecomLevel(userLevel, userRecom.getRecomUserId());
//        }
        return 0;
    }

    public void saveEmail(String email, String partyId) {
        /**
         * party
         */
        User party = getById(partyId);
        party.setUserMail(email);
        party.setMailBind(true);
        // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
        // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
        // 如：级别11表示：新注册的前端显示为VIP1；
        int userLevel = party.getUserLevel();
//        party.setUserLevel(((int) Math.floor(userLevel / 10)) * 10 + 2);
        updateById(party);
    }

    @Override
    @Transactional
    public User saveAgentUser(String userName, String password, String safePassword, String roleName, String remarks,
                              String userCode, boolean loginAuthority) {
        User user = findByUserName(userName);
        if (user != null) {
            throw new YamiShopBindException("用户名重复");
        }
        User recomUser = null;
        int userLevel = 1;
        //推荐人
        if (StrUtil.isNotBlank(userCode)) {
            recomUser = findUserByUserCode(userCode);
            if (null != recomUser) {
                userLevel++;
                userLevel = getUserRecomLevel(userLevel, recomUser.getUserId());
            }
        }
        user = new User();
        user.setUserName(userName);
        user.setUserCode(getAgentUserCode());
        user.setRemarks(remarks);
        user.setRoleName(roleName);
        user.setLoginPassword(password);
        user.setSafePassword(passwordEncoder.encode(safePassword));
        user.setStatus(loginAuthority ? 1 : 0);
        user.setUserLevel(userLevel);
        save(user);
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getUserId());
        walletService.save(wallet);
        //推荐人
        if (StrUtil.isNotBlank(userCode)) {
//            if ("true".equals(this.sysparaService.find("register_need_usercode").getSvalue())) {
            if (null == recomUser) {
                throw new YamiShopBindException("推荐码不正确");
            }
            if (UserConstants.SECURITY_ROLE_TEST.equals(recomUser.getRoleName())) {
                throw new YamiShopBindException("推荐人无权限推荐");
            }
            if (recomUser.getStatus() == 0) {
                throw new YamiShopBindException("推荐人无权限推荐");
            }
            UserRecom userRecom = new UserRecom();
            userRecom.setUserId(recomUser.getUserId());
            // 父类partyId
            userRecom.setRecomUserId(user.getUserId());
            userRecomService.save(userRecom);
            user.setUserRecom(recomUser.getUserId());
            updateById(user);
            // }
        }
        return user;
    }

    @Override
    @Transactional
    public void updateWallt(String userId, BigDecimal moneyRevise, int accountType, String coinType) {
        User user = getById(userId);
        if (user == null) {
            throw new YamiShopBindException("用户不存在");
        }
        if (accountType == 1) { //充值
        }
        if (accountType == 2) { //扣除
            moneyRevise = moneyRevise.negate();
        }
        walletService.updateMoney("", userId, moneyRevise, new BigDecimal(0), Constants.MONEYLOG_CATEGORY_COIN
                , coinType, accountType == 1 ? Constants.MONEYLOG_CONTENT_RECHARGE : Constants.MONEYLOG_CONTENT_WITHDRAW, "后台修改账号余额");
    }

    public void checkGooleAuthAndSefeword(User user, String googleAuthCode, String loginSafeword) {
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5);
        long t = System.currentTimeMillis();
        boolean flag = ga.check_code(user.getGoogleAuthSecret(), Long.valueOf(googleAuthCode), t);
        if (!flag) {
            throw new YamiShopBindException("谷歌验证码错误!");
        }
        if (!passwordEncoder.matches(loginSafeword, user.getSafePassword())) {
            throw new YamiShopBindException("登录人资金密码错误");
        }
    }

    @Override
    public void restLoginPasswrod(String userId, String password) {
        User user = getById(userId);
        //user.setLoginPassword(decryptPassword(password));
        //user.setLoginPassword(passwordEncoder.encode(password));
       // user.setPassword(passwordEncoder.encode(model.getNewPassword()));
        user.setLoginPassword(passwordEncoder.encode(decryptPassword(password)));
        updateById(user);
    }

    /**
     * 用于aes签名的key，16位
     */
    @Value("${auth.password.signKey:-mall4j-password}")
    public String passwordSignKey;

    public String decryptPassword(String data) {
        AES aes = new AES(passwordSignKey.getBytes(StandardCharsets.UTF_8));
        String decryptStr;
        String decryptPassword;
        try {
            decryptStr = aes.decryptStr(data);
            decryptPassword = decryptStr.substring(13);
        } catch (Exception e) {
            throw new YamiShopBindException("AES解密错误", e);
        }
        return decryptPassword;
    }
    @Override
    public void restSafePassword(String userId, String newSafeword) {
        User user = getById(userId);
        user.setSafePassword(passwordEncoder.encode(newSafeword));
        updateById(user);
    }

    @Override
    public void deleteGooleAuthCode(String userId, String googleAuthCode, String loginSafeword) {
        User user = getById(userId);
        if (user == null) {
            throw new YamiShopBindException("参数错误!");
        }
        if (!user.isGoogleAuthBind()) {
            throw new YamiShopBindException("用户谷歌验证码未绑定!");
        }
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5);
        long t = System.currentTimeMillis();
        boolean flag = ga.check_code(user.getGoogleAuthSecret(), Long.valueOf(googleAuthCode), t);
        if (!flag) {
            throw new YamiShopBindException("谷歌验证码错误!");
        }
        if (!passwordEncoder.matches(loginSafeword, user.getSafePassword())) {
            throw new YamiShopBindException("登录人资金密码错误");
        }
        user.setGoogleAuthBind(false);
        user.setGoogleAuthSecret("");
        updateById(user);
    }

    @Override
    @Transactional
    public void updateWithdrawalLimitFlow(String userId, BigDecimal moneyWithdraw) {
        User user = getById(userId);
        BigDecimal lastAmount = user.getWithdrawLimitAmount();
        if (lastAmount == null) {
            lastAmount = new BigDecimal(0);
        }
        if (moneyWithdraw == null) {
            throw new YamiShopBindException("请填入有效数字");
        }
        BigDecimal resultAmount = lastAmount.add(moneyWithdraw);
        if (moneyWithdraw.doubleValue() < 0) {
            throw new YamiShopBindException("修改后金额不能小于0");
        }
        user.setWithdrawLimitAmount(moneyWithdraw);
        updateById(user);
    }

    @Override
    public User findUserByUserCode(String userCode) {
        return getOne(Wrappers.<User>query().lambda().eq(User::getUserCode, userCode).or().eq(User::getUserId, userCode));
    }

    private String getUserCode() {
        Syspara syspara = sysparaService.find("user_uid_sequence");
        int random = (int) (Math.random() * 3 + 1);
        int user_uid_sequence = syspara.getInteger() + random;
        SysparasDto sysparasDto = new SysparasDto();
        sysparasDto.setUser_uid_sequence(user_uid_sequence + "");
        sysparaService.updateSysparas(sysparasDto);
        String usercode = String.valueOf(user_uid_sequence);
        return usercode;
    }

    private String getAgentUserCode() {
        Syspara syspara = sysparaService.find("agent_uid_sequence");
        int agent_uid_sequence = syspara.getInteger() + 1;
        SysparasDto sysparasDto = new SysparasDto();
        sysparasDto.setAgent_uid_sequence(String.valueOf(agent_uid_sequence));
        sysparaService.updateSysparas(sysparasDto);
        String usercode = String.valueOf(agent_uid_sequence);
        return usercode;
    }

    @Override
    public boolean isOnline(String partyId) {
        Object object = onlineUserService.get(partyId);
        if (object != null) {
            return true;
        }
        return false;
    }

    @Override
    public void online(String partyId) {
        if (StringUtils.isNullOrEmpty(partyId)) {
            return;
        }
        onlineUserService.put(partyId, new Date());
    }
    @Transactional
    @Override
    public void saveUser(String username, String password, boolean login_authority, boolean enabled, String remarks, String operatorUsername, String ip, String parents_usercode) {
        username = username.trim();
        password = password.trim();
        if (findByuserPhone(username) != null) {
            throw new YamiShopBindException("Duplicate accounts");
        }
        /**
         * 用户code
         */
        String usercode = getUserCode();
        int userLevel = 1;
        if (!StringUtils.isNullOrEmpty(parents_usercode)) {
            User party_parents = findUserByUserCode(parents_usercode);
            if (party_parents == null) {
                throw new YamiShopBindException("推荐码不正确");
            }
            userLevel++;
            userLevel = getUserRecomLevel(userLevel, party_parents.getUserId());
        }
        /**
         * party
         */
            User code = findUserByUserCode(usercode);
            if (code != null) {
                //Random random = new Random();
               throw new YamiShopBindException("推荐码已经存在");
               // usercode = usercode+String.valueOf(random.nextInt()).substring(7);
            }

        User party = new User();
        party.setUserName(username);
        party.setUserMobile(username);
        party.setEnabled(enabled);
        party.setStatus(login_authority ? 1 : 0);
        party.setRemarks(remarks);
        party.setUserCode(usercode);
        party.setRecomCode(parents_usercode);
        party.setUserLevel(userLevel);
        party.setSafePassword(passwordEncoder.encode("000000"));
        party.setLoginPassword(passwordEncoder.encode(password));
        party.setRoleName(Constants.SECURITY_ROLE_GUEST);
        save(party);
        if (!StringUtils.isNullOrEmpty(parents_usercode)) {
            User party_parents = findUserByUserCode(parents_usercode);
            if (party_parents == null) {
                throw new YamiShopBindException("推荐码不正确");
            }
            UserRecom userRecom = new UserRecom();
            userRecom.setUserId(party_parents.getUserId());
            // 父类partyId
            userRecom.setRecomUserId(party.getUserId());
            this.userRecomService.save(userRecom);
            party.setUserRecom(party_parents.getUserRecom());
            updateById(party);
        }
        /**
         * usdt账户
         */
        Wallet wallet = new Wallet();
        wallet.setUserId(party.getUserId().toString());
        this.walletService.save(wallet);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setUsername(party.getUserName());
        log.setOperator(operatorUsername);
        log.setLog("ip:" + ip + ",管理员手动新增了演示用户:" + username);
        logService.save(log);
    }

    @Override
    public void saveResetLock(String partyId, double moneyRevise, String safeword, String operatorName, String resetType, String ip, String coinType) {
        double amount_before = 0D;
        double lock_amount_before = 0D;
        double freeze_amount_before = 0D;
        //更改的可用金额
        double changeMoney = 0d;
        //更改的锁定金额
        double lockMoney = 0.0d;
        //更改的冻结金额
        double freezeMoney = 0.0d;
        if ("usdt".equals(coinType)) {
            Wallet wallet = this.walletService.saveWalletByPartyId(partyId);
            amount_before = wallet.getMoney().doubleValue();
            lock_amount_before = wallet.getLockMoney().doubleValue();
            freeze_amount_before = wallet.getFreezeMoney().doubleValue();
            Map<String, Object> map = checkChangeMoney(moneyRevise, resetType, amount_before, lock_amount_before, freeze_amount_before);
            changeMoney = Double.valueOf(map.get("changeMoney").toString());
            lockMoney = Double.valueOf(map.get("lockMoney").toString());
            freezeMoney = Double.valueOf(map.get("freezeMoney").toString());
            walletService.updateWithLockAndFreeze(wallet.getUserId().toString(), changeMoney, lockMoney, freezeMoney);
        } else {
            WalletExtend walletExtend = this.walletService.saveExtendByPara(partyId, coinType);
            amount_before = walletExtend.getAmount();
            lock_amount_before = walletExtend.getLockAmount();
            freeze_amount_before = walletExtend.getFreezeAmount();
            Map<String, Object> map = checkChangeMoney(moneyRevise, resetType, amount_before, lock_amount_before, freeze_amount_before);
            changeMoney = Double.valueOf(map.get("changeMoney").toString());
            lockMoney = Double.valueOf(map.get("lockMoney").toString());
            freezeMoney = Double.valueOf(map.get("freezeMoney").toString());
            walletService.updateExtendWithLockAndFreeze(walletExtend.getPartyId().toString(), coinType, changeMoney, lockMoney, freezeMoney);
        }

        /*
         * 保存账变日志
         */
        MoneyLog moneyLog = new MoneyLog();
        moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
        moneyLog.setAmountBefore(new BigDecimal(amount_before));
        moneyLog.setAmount(new BigDecimal(changeMoney));
        moneyLog.setAmountAfter(new BigDecimal(Arith.add(amount_before, changeMoney)));
        moneyLog.setUserId(partyId);
        moneyLog.setWalletType(coinType.toUpperCase());
        moneyLog.setContentType(Constants.MONEYLOG_CONTENT_SYS_LOCK);
        /**
         * 操作日志
         */
        User user = getById(partyId);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setUsername(user.getUserName());
        log.setOperator(operatorName);
        String logInfo = "";
        String logResetType = "";
        double lockOrFreezeMoney = 0d;
        if ("moneryToLock".equals(resetType)) {//余额转到锁定
            logInfo = "管理员手动操作,可用金额->锁定金额";
            logResetType = "锁定";
            moneyLog.setTitle("锁定金额1");
            moneyLog.setConf("锁定1");
            lockOrFreezeMoney = lockMoney;
            moneyLog.setLog(logInfo);
            moneyLogService.save(moneyLog);
        } else if ("lockToMoney".equals(resetType)) {//锁定转到余额
            logInfo = "管理员手动操作,锁定金额->可用金额";
            logResetType = "锁定";
            moneyLog.setTitle("可用金额2");
            moneyLog.setConf("锁定2");
            lockOrFreezeMoney = lockMoney;
            moneyLog.setLog(logInfo);
            moneyLogService.save(moneyLog);
        } else if ("addLock".equals(resetType)) {
            logInfo = "管理员手动添加锁定金额";
            logResetType = "锁定";
            moneyLog.setTitle("添加锁定金额3");
            moneyLog.setConf("锁定3");
            lockOrFreezeMoney = lockMoney;
            log.setExtra(Constants.MONEYLOG_CONTENT_SYS_MONEY_ADD_LOCK);
        } else if ("subLock".equals(resetType)) {
            logInfo = "管理员手动减少锁定金额";
            logResetType = "锁定";
            moneyLog.setTitle("锁定金额4");
            moneyLog.setConf("锁定4");
            lockOrFreezeMoney = lockMoney;
            log.setExtra(Constants.MONEYLOG_CONTENT_SYS_MONEY_SUB_LOCK);
        } else if ("moneryToFreeze".equals(resetType)) {//余额转到冻结
            logInfo = "管理员手动操作,可用金额->冻结金额";
            logResetType = "冻结";
            lockOrFreezeMoney = freezeMoney;
            moneyLog.setLog(logInfo);
            moneyLog.setTitle("冻结金额5");
            moneyLog.setConf("冻结5");
            moneyLogService.save(moneyLog);
        } else if ("freezeToMoney".equals(resetType)) {//冻结转到余额
            logInfo = "管理员手动操作,冻结金额->可用金额";
            logResetType = "冻结";
            moneyLog.setTitle("可用金额6");
            moneyLog.setConf("冻结6");
            lockOrFreezeMoney = freezeMoney;
            moneyLog.setLog(logInfo);
            moneyLogService.save(moneyLog);
        }
        String logText = MessageFormat.format("ip:{0},{1}。修改币种[{2}],修改可用数量[{3}],修改{4}数量[{5}]", ip, logInfo, coinType, changeMoney, logResetType, lockOrFreezeMoney);
        log.setLog(logText);
        logService.save(log);
    }

    private Map<String, Object> checkChangeMoney(double moneyRevise, String resetType, double amountBefore,
                                                 double lockAmountBefore,
                                                 double freezeAmountBefore) {
        Map<String, Object> map = new HashMap<String, Object>();
        //更改的可用金额
        double changeMoney = 0d;
        //更改的锁定金额
        double lockMoney = 0.0d;
        //更改的冻结金额
        double freezeMoney = 0.0d;
        if (StringUtils.isEmptyString(resetType)) {
            throw new YamiShopBindException("请选择转移类型");
        } else if ("moneryToLock".equals(resetType)) {//余额转到锁定
            if (moneyRevise > amountBefore) {
                throw new YamiShopBindException("操作失败！修正后账户余额小于0。");
            }
            changeMoney = Arith.sub(0, moneyRevise);
            lockMoney = moneyRevise;
        } else if ("lockToMoney".equals(resetType)) {
            if (moneyRevise > lockAmountBefore) {
                throw new YamiShopBindException("操作失败！修正后账户锁定余额小于0。");
            }
            changeMoney = moneyRevise;
            lockMoney = Arith.sub(0, moneyRevise);
        } else if ("addLock".equals(resetType)) {
            changeMoney = 0;
            lockMoney = moneyRevise;
        } else if ("subLock".equals(resetType)) {
            if (moneyRevise > lockAmountBefore) {
                throw new YamiShopBindException("操作失败！修正后账户锁定余额小于0。");
            }
            changeMoney = 0;
            lockMoney = Arith.sub(0, moneyRevise);
        } else if ("moneryToFreeze".equals(resetType)) {//余额转到冻结
            if (moneyRevise > amountBefore) {
                throw new YamiShopBindException("操作失败！修正后账户余额小于0。");
            }
            changeMoney = Arith.sub(0, moneyRevise);
            freezeMoney = moneyRevise;
        } else if ("freezeToMoney".equals(resetType)) {
            if (moneyRevise > freezeAmountBefore) {
                throw new YamiShopBindException("操作失败！修正后账户冻结余额小于0。");
            }
            changeMoney = moneyRevise;
            freezeMoney = Arith.sub(0, moneyRevise);
        } else {
            throw new YamiShopBindException("请选择转移类型");
        }
        map.put("changeMoney", changeMoney);
        map.put("lockMoney", lockMoney);
        map.put("freezeMoney", freezeMoney);
        return map;
    }

    @Override
    @Transactional
    public User registerMobile(String userMobile, String password, String userCode, boolean robot) {
        int userLevel = 1;
        User recomUser = findUserByUserCode(userCode);
        if (StrUtil.isEmpty(userCode)) {
            throw new YamiShopBindException("请输入推荐码");
        }
        if (null == recomUser) {
            throw new YamiShopBindException("请输入正确的推荐码");
        }
        String mobile = userMobile.substring(2);
        if (null != recomUser) {
            userLevel++;
            userLevel = getUserRecomLevel(userLevel, recomUser.getUserId());
        }
        // 手机
       /* if (!isValidPhone(mobile)) {
            throw new YamiShopBindException("手机号格式不正常!");
        }*/
        User user = findByUserMobile(mobile);
        if (user != null) {
            throw new YamiShopBindException("Duplicate accounts!");
        }
        Page<AgentDto> page = new Page(1, 5);
        page = agentService.listTotal(page, userCode);
        List<AgentDto> list = page.getRecords();
        if (list.size()<=0) {
            throw new YamiShopBindException("Agent recommendation code not found");
        }
        user = new User();
        user.setUserName(userMobile);
        if (user == null) {
            throw new YamiShopBindException("注册失败!");
        }
        user.setUserLevel(userLevel);
        user.setSafePassword(passwordEncoder.encode("000000"));
        user.setLoginPassword(password);
        user.setUserMobile(mobile);
        user.setUserName(mobile);
        user.setStatus(1);
        user.setRoleName(robot ? UserConstants.SECURITY_ROLE_ROBOT : UserConstants.SECURITY_ROLE_MEMBER);
        user.setUserRegip(IPHelper.getIpAddr());
        user.setUserLastip(user.getUserRegip());
        user.setUserCode(getUserCode());
        user.setCreateTime(new Date());
        user.setRecomCode(userCode);
        save(user);
        //1.保存钱包记录
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getUserId());
        wallet.setCreateTime(new Date());
        wallet.setMoney(new BigDecimal(0));
        walletService.save(wallet);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_SECURITY);
        log.setLog("用户注册,ip[" + user.getUserRegip() + "]");
        log.setUserId(user.getUserId());
        log.setUsername(user.getUserName());
        logService.save(log);
        if (recomUser != null) {
            //推荐人
            UserRecom userRecom = new UserRecom();
            userRecom.setCreateTime(new Date());
            userRecom.setUserId(recomUser.getUserId());
            userRecom.setRecomUserId(user.getUserId());
            userRecomService.save(userRecom);
            user.setUserRecom(recomUser.getUserId());
            updateById(user);
        }
        userDataService.saveRegister(user.getUserId());
        return user;
    }

    @Override
    @Transactional
    public User registerMail(String userMail, String password, String userCode, boolean robot) {
        int userLevel = 1;
        User recomUser = findUserByUserCode(userCode);
        if (StrUtil.isEmpty(userCode)) {
            throw new YamiShopBindException("请输入推荐码");
        }
        if (null == recomUser) {
            throw new YamiShopBindException("请输入正确的推荐码");
        }
        if (findByuserPhone(userMail) != null) {
            throw new YamiShopBindException("邮箱重复");
        }
        if (null != recomUser) {
            userLevel++;
            userLevel = getUserRecomLevel(userLevel, recomUser.getUserId());
        }
        // 邮箱
        if (!isValidEmail(userMail)) {
            throw new YamiShopBindException("not a valid Email!");
        }
        User user = findByEmail(userMail);
        if (user != null) {
            throw new YamiShopBindException("邮箱已存在!");
        }
        Page<AgentDto> page = new Page(1, 5);
        page = agentService.listTotal(page, userCode);
        List<AgentDto> list = page.getRecords();
        if (list.size()<=0) {
            throw new YamiShopBindException("代理商推荐码未找到");
        }
        user = new User();
        user.setMailBind(true);
        user.setUserMail(userMail);
        if (user == null) {
            throw new YamiShopBindException("注册失败!");
        }
        user.setUserLevel(userLevel);
        user.setSafePassword(passwordEncoder.encode("000000"));
        user.setLoginPassword(password);
        user.setStatus(1);
        user.setUserName(userMail);
        user.setRoleName(robot ? UserConstants.SECURITY_ROLE_ROBOT : UserConstants.SECURITY_ROLE_MEMBER);
        user.setUserRegip(IPHelper.getIpAddr());
        user.setUserLastip(user.getUserRegip());
        user.setUserCode(getUserCode());
        user.setCreateTime(new Date());
        user.setRecomCode(userCode);
        save(user);
        //1.保存钱包记录
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getUserId());
        wallet.setCreateTime(new Date());
        wallet.setMoney(new BigDecimal(0));
        walletService.save(wallet);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_SECURITY);
        log.setLog("用户注册,ip[" + user.getUserRegip() + "]");
        log.setUserId(user.getUserId());
        log.setUsername(user.getUserName());
        logService.save(log);
        if (recomUser != null) {
            //推荐人
            UserRecom userRecom = new UserRecom();
            userRecom.setCreateTime(new Date());
            userRecom.setUserId(recomUser.getUserId());
            userRecom.setRecomUserId(user.getUserId());
            userRecomService.save(userRecom);
            user.setUserRecom(recomUser.getUserId());
            updateById(user);
        }
        userDataService.saveRegister(user.getUserId());
        return user;
    }

    // 手机号校验
    private boolean isValidPhone(String username) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(username).matches();
    }

    // 邮箱校验
    private boolean isValidEmail(String username) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(username)
                .matches();
    }

    // 用户名校验
    private boolean isValidUsername(String username) {
        String regex = "^[A-Za-z]\\w{5,29}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        return m.matches();
    }

    @Override
    public void setSafeword(String userId, String safePassword) {
        User user = getById(userId);
        if (user == null) {
            throw new YamiShopBindException("当前登录账号不存在!");
        }
//        if (StrUtil.isNotBlank(user.getSafePassword())) {
//            throw new YamiShopBindException("资金密码已经设置过了!");
//        }
        user.setSafePassword(safePassword);
        user.setUpdateTime(new Date());
        updateById(user);
    }

    @Override
    public User findByEmail(String email) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUserMail, email));
        return user;
    }

    @Override
    public User findByUserName(String userName) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
        return user;
    }

    @Override
    public User findByuserPhone(String userPhone) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUserMobile, userPhone));
        return user;
    }

    @Override
    public User findByUserMobile(String mobile) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUserMobile, mobile));
    }

    @Override
    public User login(String username, String password) {
        User user = findByUserName(username);
        if (user == null) {
            throw new YamiShopBindException("用户不存在");
        }
        String[] rolesArrty = new String[]{Constants.SECURITY_ROLE_GUEST, Constants.SECURITY_ROLE_MEMBER, Constants.SECURITY_ROLE_TEST};
        if (user == null) {
            throw new YamiShopBindException("登录失败");
        }
        if (!passwordEncoder.matches(password, user.getLoginPassword())) {
            throw new YamiShopBindException("密码不正确");
        }
        user.setUserLasttime(new Date());
        updateById(user);
        return user;
    }

    /**
     * 根据已验证的电话号码获取Party对象
     *
     * @param phone 电话号码
     * @return 用户对象
     */
    @Override
    public User findPartyByVerifiedPhone(String phone) {
        if (null == phone) return null;
        List<User> list = list(Wrappers.<User>query().lambda().eq(User::getUserMobile, phone).eq(User::isUserMobileBind, true));
        return list.size() > 0 ? list.get(0) : null;
    }

    /**
     * 获取用户系统等级： 1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
     */
    public int getUserLevelByAuth(User user) {
        int userLevel = 1;
        if (user.isMailBind() || user.isUserMobileBind() || user.isGoogleAuthBind()) {
            if (user.isRealNameAuthority()) {
                if (user.isHighlevelAuthority()) {
                    userLevel = 4;
                } else {
                    userLevel = 3;
                }
            } else {
                userLevel = 2;
            }
        } else {
            userLevel = 1;
        }
        return userLevel;
    }
}
