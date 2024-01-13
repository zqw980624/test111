package com.yami.trading.service.user.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserData;
import com.yami.trading.bean.model.UserDataSum;
import com.yami.trading.bean.model.UserRecom;
import com.yami.trading.bean.user.dto.ChildrenLever;
import com.yami.trading.bean.user.dto.UserBenefitsDto;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.DateUtil;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.dao.user.UserDataMapper;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserDataSumService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserDataServiceImpl extends ServiceImpl<UserDataMapper, UserData> implements UserDataService {
    /**
     * 缓存时间
     */
    private final int CACHE_TIME = 60 * 60 * 25;
    private final String CACHE_KEY_START = "usercode:";
    @Autowired
    UserService userService;
    @Autowired
    UserRecomService userRecomService;
    @Autowired
    UserDataSumService userDataSumService;

    @Autowired
    private DataService dataService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private String REDIS_KEY = "userdata:";

    private UserData findBydate(String userId, Date date) {

        Date startDate = DateUtil.minDate(date);
        ;
        Date endDate = DateUtil.maxDate(date);
        return getOne(Wrappers.<UserData>query().lambda().between(UserData::getCreateTime, startDate, endDate)
                .eq(UserData::getUserId, userId));
    }

    public Map<String, UserData> cacheByPartyId(String partyId) {

        Object obj = redisTemplate.opsForValue().get(REDIS_KEY + partyId);
        if (obj == null) {
            return null;
        }
        return null;
    }

    @Override
    public Page listUserGenefits(Page page, Date startTime, Date endTime, String userName,
                                 List<String> children) {

        baseMapper.listUserGenefits(page, startTime, endTime, userName, children);
        compute(page.getRecords());
        return page;


    }

    /**
     * 获取今日充值的人数
     *
     * @return
     */
    @Override
    public long countTodayRechargeUser() {

        Date now = new Date();
        return count(Wrappers.<UserData>query().lambda().between(UserData::getCreateTime, DateUtil.minDate(now),
                DateUtil.maxDate(now)).gt(UserData::getRecharge, 0));
    }

    @Override
    public UserBenefitsDto daySumData(Date startTime, Date endTime, List<String> userIds) {

        return baseMapper.daySumData(startTime, endTime, userIds);
    }


    @Override
    public UserBenefitsDto daySumDatas(Date startTime, Date endTime, List<String> userIds,String userCodes) {

        return baseMapper.daySumDatas(startTime, endTime, userIds,userCodes);
    }

    /**
     * 矿机利息
     */
    @Override
    public void saveGiftMoneyHandle(String partyId, double amount) {
        User user = userService.getById(partyId);
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName())) {
            guest = true;
        }

        if (guest) {
            return;
        }

        UserData userData = new UserData();
        userData.setRolename(user.getRoleName());
        userData.setCreateTime(new Date());
        userData.setUserId(user.getUserId());
        userData.setGiftMoney(amount);
        saveUserData(userData);

    }

    @Override
    public List<Map<String, UserData>> cacheByPartyIds(List<String> partyIds) {

        if (CollectionUtils.isEmpty(partyIds)) return new LinkedList<Map<String, UserData>>();
        List<Map<String, UserData>> result = new LinkedList<Map<String, UserData>>();
        for (String id : partyIds) {
            result.add(cacheByPartyId(id));
        }
        return result;
    }

    @Override
    public void saveRegister(String userId) {
        User user = userService.getById(userId);
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }
        UserRecom userRecom = userRecomService.findByPartyId(userId);
        if (userRecom == null) {
            return;
        }
        List<UserRecom> parents = this.userRecomService.getParents(userId);
        int loop = 4;
        for (int i = 0; i < parents.size(); i++) {
            User party_parent = userService.getById(parents.get(i).getRecomUserId());
            if (Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRoleName()) && loop > 0) {
                UserData userData_reco = new UserData();
                userData_reco.setRolename(party_parent.getRoleName());
                userData_reco.setCreateTime(new Date());
                userData_reco.setUserId(parents.get(i).getRecomUserId());
                userData_reco.setRecoNum(1);
                save(userData_reco);
                UserDataSum userDataSum = saveBySum(parents.get(i).getRecomUserId());
                userDataSum.setRecoNum(userDataSum.getRecoNum() + 1);
                userDataSumService.updateById(userDataSum);
                loop--;
            }
        }
    }

    /**
     * 根据partyId获取UserDataSum
     */
    public UserDataSum saveBySum(String partyId) {

        List list = userDataSumService.getByUserId(partyId);
        if (list.size() > 0) {
            return (UserDataSum) list.get(0);
        }
        UserDataSum userDataSum = new UserDataSum();
        userDataSum.setUserId(partyId);
        userDataSumService.save(userDataSum);
        return userDataSum;
    }

    @Override
    public Page userAll(Page page, Date startTime, Date endTime) {

        Page page1 = baseMapper.userAll(page, startTime, endTime);
        compute(page1.getRecords(), false);
        return page1;
    }

    @Override
    public Map sumAll(Date startTime, Date endTime) {

        Map map = baseMapper.sumAll(startTime, endTime);
        List<Map> maps = new ArrayList<>();
        maps.add(map);
        compute(maps, true);
        return maps.get(0);
    }

    /**
     * 统计的数据存在空时，不统计总额
     *
     * @param data
     * @return
     */
    private boolean dataExistNull(Map<String, Object> data) {

        if (null == data.get("recharge_withdrawal_fee")) return false;
        if (null == data.get("order_income")) return false;
        if (null == data.get("fee")) return false;
        if (null == data.get("finance_income")) return false;
        if (null == data.get("exchange_fee")) return false;
        if (null == data.get("exchange_income")) return false;
        if (null == data.get("furtures_fee")) return false;
        if (null == data.get("furtures_income")) return false;
        return true;
    }

    private void compute(List<Map<String, Object>> datas) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(datas)) return;
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
            if (null == data.get("reco_num")) {
                data.put("reco_num", 0);
            }

            if (null != data.get("order_income"))
                data.put("order_income", Arith.sub(0, new Double(data.get("order_income").toString())));//订单收益负数
            if (null != data.get("finance_income"))
                data.put("finance_income", Arith.sub(0, new Double(data.get("finance_income").toString())));//理财收益负数
            if (null != data.get("exchange_income"))
                data.put("exchange_income", 0);//币币收益负数
            if (null != data.get("furtures_income"))
                data.put("furtures_income", Arith.sub(0, new Double(data.get("furtures_income").toString())));//交割收益负数
            if (null != data.get("miner_income"))
                data.put("miner_income", Arith.sub(0, new Double(data.get("miner_income").toString())));// 矿机收益负数
            if (null != data.get("exchange_lever_order_income"))
                data.put("exchange_lever_order_income", Arith.sub(0, new Double(data.get("exchange_lever_order_income").toString())));// 币币收益负数

            if (!dataExistNull(data)) continue;
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
    /**
     * 计算 统计时
     *
     * @param datas
     * @param isSum
     */
    private void compute(List<Map> datas, boolean isSum) {

        if (org.apache.commons.collections.CollectionUtils.isEmpty(datas)) return;
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
                data.put("order_income", Arith.sub(0, new Double(data.get("order_income").toString())));//订单收益负数
            if (null != data.get("finance_income"))
                data.put("finance_income", Arith.sub(0, new Double(data.get("finance_income").toString())));//理财收益负数
            if (null != data.get("exchange_income"))
                data.put("exchange_income", 0);//币币收益负数
            if (null != data.get("furtures_income"))
                data.put("furtures_income", Arith.sub(0, new Double(data.get("furtures_income").toString())));//交割收益负数
            if (null != data.get("miner_income"))
                data.put("miner_income", Arith.sub(0, new Double(data.get("miner_income").toString())));// 矿机收益负数
            if (null != data.get("exchange_lever_order_income"))
                data.put("exchange_lever_order_income", Arith.sub(0, new Double(data.get("exchange_lever_order_income").toString())));// 币币收益负数
            if (!dataExistNull(data)) continue;
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
            data.put("recharge_btc", new BigDecimal(data.get("recharge_btc").toString()).setScale(8, RoundingMode.FLOOR).toPlainString());//订单收益负数
        }
    }

    @Override
    public void saveUserData(UserData entity) {
        try {
            UserData db = findBydate(entity.getUserId(), entity.getCreateTime());
            if (db != null) {
                db.setRechargeDapp(Arith.add(db.getRechargeDapp(), entity.getRechargeDapp()));
                db.setWithdrawDapp(Arith.add(db.getWithdrawDapp(), entity.getWithdrawDapp()));
                db.setRecharge(Arith.add(db.getRecharge(), entity.getRecharge()));
                db.setRechargeEth(Arith.add(db.getRechargeEth(), entity.getRechargeEth()));
                db.setRechargeUsdt(Arith.add(db.getRechargeUsdt(), entity.getRechargeUsdt()));
                db.setRechargeBtc(Arith.add(db.getRechargeBtc(), entity.getRechargeBtc()));
                db.setRechargeHt(Arith.add(db.getRechargeHt(), entity.getRechargeHt()));
                db.setRechargeLtc(Arith.add(db.getRechargeLtc(), entity.getRechargeLtc()));
                // 充值返佣
                db.setRechargeRecom(Arith.add(db.getRechargeRecom(), entity.getRechargeRecom()));
                db.setWithdrawAll(Arith.add(db.getWithdrawAll(), entity.getWithdrawAll()));
                db.setWithdraw(Arith.add(db.getWithdraw(), entity.getWithdraw()));
                db.setWithdrawEth(Arith.add(db.getWithdrawEth(), entity.getWithdrawEth()));
                db.setWithdrawBtc(Arith.add(db.getWithdrawBtc(), entity.getWithdrawBtc()));
                db.setAmount(Arith.add(db.getAmount(), entity.getAmount()));
                db.setFee(Arith.add(db.getFee(), entity.getFee()));
                db.setOrderIncome(Arith.add(db.getOrderIncome(), entity.getOrderIncome()));
                db.setFinanceAmount(Arith.add(db.getFinanceAmount(), entity.getFinanceAmount()));
                db.setFinanceIncome(Arith.add(db.getFinanceIncome(), entity.getFinanceIncome()));
                db.setExchangeAmount(Arith.add(db.getExchangeAmount(), entity.getExchangeAmount()));
                db.setExchangeFee(Arith.add(db.getExchangeFee(), entity.getExchangeFee()));
                db.setExchangeIncome(Arith.add(db.getExchangeIncome(), entity.getExchangeIncome()));
                db.setCoinIncome(Arith.add(db.getCoinIncome(), entity.getCoinIncome()));
                db.setFurturesAmount(Arith.add(db.getFurturesAmount(), entity.getFurturesAmount()));
                db.setFurturesFee(Arith.add(db.getFurturesFee(), entity.getFurturesFee()));
                db.setFurturesIncome(Arith.add(db.getFurturesIncome(), entity.getFurturesIncome()));
                db.setRecoNum(db.getRecoNum() + entity.getRecoNum());
                db.setRechargeWithdrawalFee(
                        Arith.add(db.getRechargeWithdrawalFee(), entity.getRechargeWithdrawalFee()));
                db.setGiftMoney(Arith.add(db.getGiftMoney(), entity.getGiftMoney()));
                db.setMinerAmount(Arith.add(db.getMinerAmount(), entity.getMinerAmount()));
                db.setMinerIncome(Arith.add(db.getMinerIncome(), entity.getMinerIncome()));
                // 质押2.0
                db.setGalaxyAmount(Arith.add(db.getGalaxyAmount(), entity.getGalaxyAmount()));
                db.setGalaxyIncome(Arith.add(db.getGalaxyIncome(), entity.getGalaxyIncome()));
                db.setThirdRechargeAmount(Arith.add(db.getThirdRechargeAmount(), entity.getThirdRechargeAmount()));
                db.setHoldingMoney(Arith.add(db.getHoldingMoney(), entity.getHoldingMoney()));
                db.setTransferInMoney(Arith.add(db.getTransferInMoney(), entity.getTransferInMoney()));
                db.setTransferOutMoney(Arith.add(db.getTransferOutMoney(), entity.getTransferOutMoney()));
                db.setExchangeLeverAmount(Arith.add(db.getExchangeLeverAmount(), entity.getExchangeLeverAmount()));
                db.setExchangeLeverFee(Arith.add(db.getExchangeLeverFee(), entity.getExchangeLeverFee()));
                db.setExchangeLeverOrderIncome(Arith.add(db.getExchangeLeverOrderIncome(), entity.getExchangeLeverOrderIncome()));
                updateById(db);
            } else {
                save(entity);
            }
        } catch (Exception e) {
            log.error("saveUserData 异常", e);
        }
    }

    public void put(String userId, Map<String, UserData> map_party) {

        redisTemplate.opsForValue().set(REDIS_KEY + userId, map_party);
    }

    @Override
    public List<Map<String, UserData>> findByPartyIds(List<String> children) {
        List<UserData> users = list(Wrappers.<UserData>query().lambda().in(UserData::getUserId, children));
        List<Map<String, UserData>> mapList = new ArrayList<>();
        for (UserData userData : users) {
            Map<String, UserData> map = new HashMap<>();
            map.put(userData.getUserId(), userData);
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 资金盘定制化需求，等盘口下架可以删除
     */

    @Override
    public List<Map<String, Object>> getChildrenLevelPagedForGalaxy(int pageNo, int pageSize, String partyId, Integer levelNum) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ChildrenLever children = cacheChildrenLever4(partyId);
        // 获取子代partyId
        List<String> level = new ArrayList<String>();
        if (levelNum == 1) {
            level = children.getLever1();
        }
        if (levelNum == 2) {
            level = children.getLever2();
        }
        if (levelNum == 3) {
            level = children.getLever3();
        }
        if (level == null || level.isEmpty()) {
            return list;
        }
        Page<User> page = new Page(pageNo, pageSize);
        userService.page(page, Wrappers.<User>query().lambda().in(User::getUserId, level));
        Map<String, Object> element_map = new HashMap<String, Object>();
        for (int i = 0; i < page.getRecords().size(); i++) {
            User user = page.getRecords().get(i);
            element_map.put("username", user.getUserName());
            element_map.put("partyId", user.getUserId());
            String childrenPartyId = element_map.get("partyId").toString();
            ChildrenLever childrenLever = cacheChildrenLever4(childrenPartyId);
            // 获取子代partyId
            List<String> level_children = new ArrayList<String>();
            if (levelNum == 1) {
                level_children = childrenLever.getLever1();
            }
            if (levelNum == 2) {
                level_children = childrenLever.getLever2();
            }
            if (levelNum == 3) {
                level_children = childrenLever.getLever3();
            }
            element_map.put("reco_sum", level_children.size());
            // list里面的总业绩
            Map<String, UserData> map = cacheByPartyId(childrenPartyId);
            double sum = 0;
            if (null != map && map.size() > 0) {
                for (UserData userData : map.values()) {
                    sum += userData.getGalaxyIncome();
                }
            }
            element_map.put("recharge_sum", sum);
            list.add(element_map);
        }
        return list;
    }

    @Override
    public ChildrenLever cacheChildrenLever4(String partyId) {

        ChildrenLever childrenLever = new ChildrenLever();
        /**
         * lever1
         */
        List<UserRecom> userrecom_lever1 = userRecomService.findRecoms(partyId);
        for (int i = 0; i < userrecom_lever1.size(); i++) {
            childrenLever.getLever1().add(userrecom_lever1.get(i).getUserId().toString());
        }
        /**
         * lever2
         */
        if (childrenLever.getLever1().size() == 0) {
            return childrenLever;
        }
        for (int i = 0; i < childrenLever.getLever1().size(); i++) {
            List<UserRecom> userrecom_lever2 = userRecomService.findRecoms(childrenLever.getLever1().get(i));
            for (int j = 0; j < userrecom_lever2.size(); j++) {
                childrenLever.getLever2().add(userrecom_lever2.get(j).getUserId().toString());
            }
        }
        /**
         * lever3
         */
        if (childrenLever.getLever2().size() == 0) {
            return childrenLever;
        }
        for (int i = 0; i < childrenLever.getLever2().size(); i++) {
            List<UserRecom> userrecom_lever3 = userRecomService.findRecoms(childrenLever.getLever2().get(i));
            for (int j = 0; j < userrecom_lever3.size(); j++) {
                childrenLever.getLever3().add(userrecom_lever3.get(j).getUserId().toString());
            }
        }
        return childrenLever;
    }

    public void saveWithdrawHandle(String partyId, double amount, double amount_fee, String symbol) {
        System.out.println("saveWithdrawHandle -> partyId:" + partyId);
        User user = userService.getById(partyId);
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }
        UserData userData = new UserData();
        userData.setRolename(user.getRoleName());
        userData.setCreateTime(new Date());
        userData.setUserId(partyId);
        if (StringUtils.isEmpty(symbol) || "usdt".equals(symbol)) {
            userData.setWithdraw(amount);
            userData.setRechargeWithdrawalFee(amount_fee);
            userData.setWithdrawAll(amount);
        }
        saveUserData(userData);
    }

    @Override
    public void saveRechargeHandle(String partyId, double amount, String symbol) {

        User party = userService.getById(partyId);
        if ("USDT_DAPP".equals(symbol)) {
            UserData userData = new UserData();
            userData.setRolename(party.getRoleName());
            userData.setCreateTime(new Date());
            userData.setUserId(partyId);
            userData.setRechargeDapp(amount);
            save(userData);
            return;
        }
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(party.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }
        UserData userData = new UserData();
        userData.setRolename(party.getRoleName());
        userData.setCreateTime(new Date());
        userData.setUserId(partyId);
        userData.setRecharge(amount);
        userData.setRechargeUsdt(amount);
        saveUserData(userData);
    }

    public void saveClose(ContractOrder order) {

        User user = userService.getById(order.getPartyId());
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName()) || Constants.SECURITY_ROLE_TEST.equals(user.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }
        UserData userData = new UserData();
        userData.setRolename(userData.getRolename());
        userData.setCreateTime(new Date());
        userData.setUserId(order.getPartyId());
        userData.setAmount(order.getDepositOpen().doubleValue());
        if (order.getAmountClose() == null) {
            order.setAmountClose(BigDecimal.ZERO);
        }
        if (order.getAmountClose().compareTo(BigDecimal.ZERO) < 0) {
            order.setAmountClose(BigDecimal.ZERO);
        }
        userData.setFee(order.getFee().doubleValue());
        userData.setOrderIncome(order.getAmountClose().subtract(order.getDepositOpen()).doubleValue());
        saveUserData(userData);
    }

    @Override
    public void saveSellYd(ExchangeApplyOrder order, String pid) {
        User user = userService.getById(order.getPartyId());
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName()) || Constants.SECURITY_ROLE_TEST.equals(user.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }
        Object results = redisTemplate.opsForValue().get("ydTask" + pid);
        JSONObject msgObject = JSONUtil.parseObj(results);
        double close = 1;
        if (msgObject != null) {
            close = Double.parseDouble(msgObject.getStr("last"));
        } else {
            throw new YamiShopBindException("参数错误");
        }
        UserData userData = new UserData();
        userData.setRolename(user.getRoleName());
        userData.setCreateTime(new Date());
        userData.setUserId(order.getPartyId());
        userData.setExchangeAmount(Arith.mul(close, order.getVolume()));
        userData.setExchangeFee(Arith.mul(close, order.getFee()));
        userData.setExchangeIncome(0);
        saveUserData(userData);
    }

    @Override
    public void saveSell(ExchangeApplyOrder order) {
        User user = userService.getById(order.getPartyId());
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName()) || Constants.SECURITY_ROLE_TEST.equals(user.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }
        List<Realtime> realtime_list = this.dataService.realtime(order.getSymbol());

        Realtime realtime = null;
        if (realtime_list.size() > 0) {
            realtime = realtime_list.get(0);
        } else {
            throw new YamiShopBindException("系统错误，请稍后重试");
        }
        UserData userData = new UserData();
        userData.setRolename(user.getRoleName());
        userData.setCreateTime(new Date());
        userData.setUserId(order.getPartyId());
        userData.setExchangeAmount(Arith.mul(realtime.getClose().doubleValue(), order.getVolume()));
        userData.setExchangeFee(Arith.mul(realtime.getClose().doubleValue(), order.getFee()));
        userData.setExchangeIncome(0);
        saveUserData(userData);
    }

    @Override
    public void saveBuy(ExchangeApplyOrder order) {
        User user = userService.getById(order.getPartyId());
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName()) || Constants.SECURITY_ROLE_TEST.equals(user.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }
        UserData userData = new UserData();
        userData.setRolename(user.getRoleName());
        userData.setCreateTime(new Date());
        userData.setUserId(order.getPartyId());
        userData.setExchangeAmount(order.getVolume());
        userData.setExchangeFee(0);
        saveUserData(userData);
    }

    @Override
    public void saveBuyYd(ExchangeApplyOrder order) {

        User user = userService.getById(order.getPartyId());
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(user.getRoleName()) || Constants.SECURITY_ROLE_TEST.equals(user.getRoleName())) {
            guest = true;
        }
        if (guest) {
            return;
        }

        UserData userData = new UserData();
        userData.setRolename(user.getRoleName());
        userData.setCreateTime(new Date());
        userData.setUserId(order.getPartyId());
        userData.setExchangeAmount(order.getVolume());
        userData.setExchangeFee(0);
        saveUserData(userData);
    }

    /**
     * 交割合约平仓
     */
    @Override
    public void saveFuturesClose(FuturesOrder order) {

        User user = userService.getById(order.getPartyId());
        String roleName = user.getRoleName();
        boolean guest = false;
        if (Constants.SECURITY_ROLE_GUEST.equals(roleName) || Constants.SECURITY_ROLE_TEST.equals(roleName)) {
            guest = true;
        }
        if (guest) {
            return;
        }
        User party = userService.getById(order.getPartyId());
        UserData userData = new UserData();
        userData.setRolename(party.getRealName());
        userData.setCreateTime(new Date());
        userData.setUserId(order.getPartyId());
        userData.setFurturesAmount(order.getVolume());
        userData.setFurturesFee(order.getFee());
        userData.setFurturesIncome(order.getProfit());
        saveUserData(userData);

    }

}
