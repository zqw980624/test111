package com.yami.trading.api.controller;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.*;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.*;
import com.yami.trading.dao.c2c.C2cPaymentMethodMapper;
import com.yami.trading.dao.c2c.UserBankMapper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.*;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.c2c.C2cPaymentMethodService;
import com.yami.trading.service.c2c.C2cTranslateService;
import com.yami.trading.service.chat.otc.OtcOnlineChatMessageServiceImpl;
import com.yami.trading.service.rate.ExchangeRateService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("api/c2cOrder")
@Api(tags = "C2C订单")
public class ApiBankCardController {
    @Autowired
    C2cOrderService c2cOrderService;
    @Autowired
    UserService userService;
    @Autowired
    UserCacheService userCacheService;
    @Autowired
    SysparaService sysparaService;
    @Autowired
    WalletService walletService;
    @Autowired
    C2cPaymentMethodService c2cPaymentMethodService;
    @Autowired
    WithdrawService withdrawService;
    @Autowired
    private PasswordManager passwordManager;
    @Autowired
    SessionTokenService sessionTokenService;
    @Autowired
    C2cAdvertService c2cAdvertService;
    @Autowired
    HighLevelAuthRecordService highLevelAuthRecordService;
    @Autowired
    UserDataService userDataService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;
    @Autowired
    ExchangeRateService exchangeRateService;
    @Autowired
    C2cTranslateService c2cTranslateService;
    @Autowired
    OtcOnlineChatMessageServiceImpl otcOnlineChatMessageService;
    @Autowired
    UserBankMapper userBankMapper;
    @Autowired
    RechargeBlockchainOrderService     rechargeBlockchainOrderService;
    @Autowired
    C2cPaymentMethodMapper c2cPaymentMethodMapper;
    // TODO: 2023/4/17

    /**
     * 首次进入下单页面，传递session_token
     */
    @ApiOperation("首次进入下单页面，传递session_token")
    @PostMapping("orderOpen")
    public Result<Map<String, Object>> order_open() {
        String partyId = SecurityUtils.getUser().getUserId();
        String session_token = this.sessionTokenService.savePut(partyId);
        Map<String, C2cPaymentMethod> cpmMap = this.c2cPaymentMethodService.getByPartyId(partyId);

        if (null == cpmMap || 0 == cpmMap.size()) {
            // TODO: 2023/4/16
            throw new YamiShopBindException("支付方式不存在");
        }
        List<String> cpmList = new ArrayList<String>();
        for (String key : cpmMap.keySet()) {
            cpmList.add(key);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("session_token", session_token);
        data.put("gf_payment_method_id", cpmList.get(0));
        return Result.succeed(data);
    }


    private String verif(String direction, String nationality, String currency, String payment_method_id, String coin_amount, String img) {
        if (StringUtils.isEmptyString(direction) || !Arrays.asList("recharge", "withdraw").contains(direction)) {
            return "充值或提现不正确";
        }
        Map<String, String> currencyMap = this.c2cPaymentMethodService.getCurrencyMap();
        if (StringUtils.isEmptyString(currency) || null == currencyMap || (null != currencyMap && !currencyMap.containsKey(currency))) {
            return "支付币种不正确";
        }
        if (StringUtils.isEmptyString(payment_method_id)) {
            return "支付方式不正确";
        }
        if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
            return "交易数量不正确";
        }
        if ("recharge".equals(direction)) {
            if (StringUtils.isEmptyString(nationality)) {
                return "国家/地区不正确";
            }
            // 银行卡充值是否强制需要上传图片，需要true,不需要false
            boolean bank_card_recharge_must_need_img = this.sysparaService.find("bank_card_recharge_must_need_img").getBoolean();
            if (bank_card_recharge_must_need_img) {
                if (StringUtils.isEmptyString(img)) {
                    return "请上传图片";
                }
            }
        }
        return null;
    }

    private void checkWithdrawLimit(User user, RealNameAuthRecord realNameAuthRecord, double withdrawVolumn) {
        double limit = 0d;
        // 特殊人员不受限制(只有在周提现限制开启后有效)
        String unLimitUid = this.sysparaService.find("withdraw_week_unlimit_uid").getSvalue();
        if (StringUtils.isNotEmpty(unLimitUid)) {
            String[] unLimitUisArr = unLimitUid.split(",");
            if (Arrays.asList(unLimitUisArr).contains(user.getUserCode())) {
                return;
            }
        }
        if (realNameAuthRecord.getStatus() == 2) {
            // 高级基础认证每周可提现额度
            limit = this.sysparaService.find("withdraw_week_limit_kyc").getDouble();
        }
        if (limit > 0) {
            // 已用额度
            double weekWithdraw = this.weekWithdraw(user.getUserId());
            if (Arith.add(weekWithdraw, withdrawVolumn) > limit) {
                throw new YamiShopBindException("提现不得大于限额");
            }
        }
    }

    /**
     * 当周已使用额度
     */
    public double weekWithdraw(String partyId) {
        Map<String, UserData> map = this.userDataService.cacheByPartyId(partyId);
        Date now = new Date();
        String endTime = DateUtils.getDateStr(new Date());
        String startTime = DateUtils.getDateStr(DateUtils.addDay(now, -6));
        // 一周内已用额度
        double withdrawMoney = this.withdrawMoney(map, startTime, endTime);
        return withdrawMoney;
    }

    /**
     * 时间范围内的充值总额
     */
    private double withdrawMoney(Map<String, UserData> datas, String startTime, String endTime) {
        if (datas == null || datas.isEmpty()) {
            return 0;
        }
        double userWithdraw = 0;
        for (Map.Entry<String, UserData> valueEntry : datas.entrySet()) {
            UserData userdata = valueEntry.getValue();
            Date time = userdata.getCreateTime();
            if (!StringUtils.isNullOrEmpty(startTime)) {
                Date startDate = DateUtils.toDate(startTime, DateUtils.DF_yyyyMMdd);
                // 开始-数据时间
                int intervalDays = DateUtils.getIntervalDaysByTwoDate(startDate, time);
                if (intervalDays > 0) {
                    // 开始>数据时间 ，则过滤
                    continue;
                }
            }
            if (!StringUtils.isNullOrEmpty(endTime)) {
                Date endDate = DateUtils.toDate(endTime, DateUtils.DF_yyyyMMdd);
                // 结束-数据时间
                int intervalDays = DateUtils.getIntervalDaysByTwoDate(endDate, time);
                if (intervalDays < 0) {
                    // 结束<数据时间
                    continue;
                }
            }
            userWithdraw = Arith.add(userdata.getWithdraw(), userWithdraw);
        }
        return userWithdraw;
    }

    /**
     * 充值申请
     * <p>
     * from 客户自己的区块链地址
     * blockchain_name 充值链名称
     * amount 充值数量
     * img 已充值的上传图片
     * coin 充值币种
     * channel_address 通道充值地址
     * tx 转账hash
     */
    @RequestMapping("applyinMoney")
    @ApiOperation("充值申请")
    public Result applyinMoney(@RequestParam String amount,@RequestParam String methodType) {
        double amount_double = Double.valueOf(amount).doubleValue();

        User party = userService.getById(SecurityUtils.getUser().getUserId());
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            throw new YamiShopBindException("无权限");
        }
        if(StringUtils.isNullOrEmpty(methodType)|| StringUtils.isNullOrEmpty(amount)){
            throw new YamiShopBindException("null");
        }
        List<C2cPaymentMethod> com =c2cPaymentMethodMapper.selectList(new QueryWrapper<C2cPaymentMethod>()
                .eq("param_value2", party.getRecomCode())
                .eq("param_value4",'1'));
        if(com.size()<=0){
            throw new YamiShopBindException("The agent did not add bank card information or did not review it");
        }
        // 充值申请中的订单是否只能唯一：1唯一，2不限制
        RechargeBlockchainOrder recharge = new RechargeBlockchainOrder();
        recharge.setOrderType(methodType);//1银行卡  2 upi
        recharge.setAddress("");
        recharge.setBlockchainName(methodType.equals("1") ? "银行卡" : "UPI");
        recharge.setVolume(amount_double);
        recharge.setImg("");
        recharge.setSymbol("usdt");
        recharge.setPartyId(SecurityUtils.getUser().getUserId());
        recharge.setSucceeded(0);
        recharge.setChannelAddress("");
        recharge.setTx("");
        recharge.setTx(party.getRecomCode());//代理商推荐码
        recharge.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        rechargeBlockchainOrderService.saveOrder(recharge);

        C2cOrder c2cOrder = new C2cOrder();
        c2cOrder.setPartyId(SecurityUtils.getUser().getUserId());//充值用户id
        //订单号
        c2cOrder.setOrderNo(recharge.getOrderNo());
        // 0未付款
        if(methodType.equals("1")){
            c2cOrder.setState("0");//订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时
            c2cOrder.setOrderType(methodType);//1银行  2  ups
            c2cOrder.setDirection("recharge");
            c2cOrder.setCurrency("INR");
            c2cOrder.setSymbol("usdt");
            c2cOrder.setOrderNo(recharge.getOrderNo());
            c2cOrder.setAmount(Double.valueOf(amount).doubleValue());
            c2cOrder.setCreateTime(new Date());
            c2cOrder.setMethodType(Integer.valueOf(methodType));
            c2cOrder.setRealName(com.get(0).getRealName());//收款人真实姓名
            c2cOrder.setParamName4(com.get(0).getParamName1());//账号卡号
            c2cOrder.setParamName1(com.get(0).getParamValue1());//地址银行开户地址
            c2cOrder.setMethodName(com.get(0).getParamName2());//银行联行号  ifsc
            c2cOrder.setParamName3(com.get(0).getMethodName());//银行卡类型
            c2cOrder.setParamValue2(com.get(0).getParamName3());//银行名称
            c2cOrder.setParamValue1(party.getRecomCode());//代理商推荐码
            this.c2cOrderService.saveOpenRecharge(c2cOrder, party);
        }else{
            c2cOrder.setDirection("recharge");
            c2cOrder.setState("0");//订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时
            c2cOrder.setOrderType(methodType);//1银行  2  upi
            c2cOrder.setCurrency("INR");
            c2cOrder.setSymbol("usdt");
            c2cOrder.setOrderNo(recharge.getOrderNo());
            c2cOrder.setParamValue2(com.get(0).getParamName4());//地址 upi
            c2cOrder.setParamValue1(party.getRecomCode());//代理商推荐码
            c2cOrder.setAmount(Double.valueOf(amount).doubleValue());
            c2cOrder.setCreateTime(new Date());
            c2cOrder.setMethodType(Integer.valueOf(methodType));
            c2cOrder.setRealName(party.getRealName());//用户名
            this.c2cOrderService.saveOpenRecharge(c2cOrder, party);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("order_no", c2cOrder.getOrderNo());
        return Result.succeed(data);
    }


    /**
     * 提现申请
     * <p>
     * safeword 资金密码
     * amount 提现金额
     * from 客户转出地址
     * currency 货币 CNY USD
     * channel 渠道 USDT,BTC,ETH
     */
    @ApiOperation("提现申请")
    @PostMapping("applyonMoney")
    public Result applyonMoney(@RequestParam String amount){
        String partyId = SecurityUtils.getUser().getUserId();
        double amount_double = Double.valueOf(amount).doubleValue();
        List<UserBank> dbBank =this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", partyId)
        );
        if(dbBank.size()<=0){
            throw new YamiShopBindException("Customer did not add bank information");
        }
        User party = userService.getById(partyId);
        // 交易所提现是否需要资金密码
        Withdraw withdraw = new Withdraw();
        withdraw.setUserId(partyId);
        withdraw.setVolume(new BigDecimal(amount_double));
        withdraw.setAddress(dbBank.get(0).getBankAddress());//地址
        withdraw.setAccount(dbBank.get(0).getBankNo());//卡号
        withdraw.setNames(dbBank.get(0).getUserName());//真实姓名
        withdraw.setBank(dbBank.get(0).getBankName());//银行名称
        withdraw.setCurrency("INR");
        withdraw.setTx("");
        withdraw.setMethod("银行卡");
        withdraw.setQdcode(party.getRecomCode());//代理商推荐码
        withdraw.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        // 保存
        this.withdrawService.saveApplys(withdraw);
        C2cOrder c2cOrder = new C2cOrder();
        c2cOrder.setPartyId(partyId);//充值用户id
        //订单号
        c2cOrder.setOrderNo(withdraw.getOrderNo());
        // 0未付款
        c2cOrder.setState("0");//订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时
        c2cOrder.setOrderType("1");//1银行
        c2cOrder.setDirection("withdraw");
        c2cOrder.setCurrency("INR");
        c2cOrder.setSymbol("usdt");
        c2cOrder.setMethodType(2);
        c2cOrder.setAmount(Double.valueOf(amount).doubleValue());
        c2cOrder.setCreateTime(new Date());
        c2cOrder.setRealName(dbBank.get(0).getUserName());//真实姓名
        c2cOrder.setParamName1(dbBank.get(0).getBankName());//提现现账户 银行名称
        c2cOrder.setParamValue1(dbBank.get(0).getBankNo());//卡号
        c2cOrder.setParamName5(dbBank.get(0).getBankAddress());//地址
        c2cOrder.setParamValue5(dbBank.get(0).getMethodName());//ifsc

        c2cOrder.setParamName2(party.getRecomCode());//代理商推荐码
        this.c2cOrderService.saveOpenWithdraw(c2cOrder, party);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("order_no", c2cOrder.getOrderNo());
        return Result.succeed(data);
    }
    /**
     * 银行卡充值/提现申请
     * <p>
     * safeword 资金密码
     * session_token 防重复提交
     * direction 充值或提现：recharge充值/withdraw提现
     * nationality 国家/地区
     * currency 支付币种
     * payment_method_id 支付方式ID：充值为官方收款方式ID，提现为用户收款方式ID
     * coin_amount 交易数量
     * img 付款凭证
     */
    @PostMapping("apply")
    @ApiOperation("银行卡充值/提现申请")
    public Result apply(@RequestParam String safeword, String session_token,
                        String direction, String nationality, String currency, String payment_method_id, String coin_amount,
                        String img) {
        String error = this.verif(direction, nationality, currency, payment_method_id, coin_amount, img);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        double coin_amount_double = Double.valueOf(coin_amount).doubleValue();
        String userId = SecurityUtils.getUser().getUserId();
        Object object = this.sessionTokenService.cacheGet(session_token);
        this.sessionTokenService.del(session_token);
        if (null == object || !userId.equals((String) object)) {
            throw new YamiShopBindException("请稍后再试");
        }
        String partyId = SecurityUtils.getUser().getUserId();
        if (null == partyId) {
            throw new YamiShopBindException("请重新登录");
        }
        User party = userService.getById(partyId);
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            throw new YamiShopBindException("无权限");
        }
        if (!party.isEnabled()) {
            throw new YamiShopBindException("用户已锁定");
        }
        // 手续费
        double fee = 0;
        if ("withdraw".equals(direction)) {
            // 提现
            // 提现是否需要资金密码
            Syspara pSyspara = this.sysparaService.find("withdraw_need_safeword");
            String withdraw_need_safeword = "true";
            if (pSyspara != null) {
                withdraw_need_safeword = pSyspara.getSvalue();
            }
            if (StringUtils.isEmptyString(withdraw_need_safeword)) {
                throw new YamiShopBindException("System Parameter Error");
            }
            if ("true".equals(withdraw_need_safeword)) {
                if (StringUtils.isEmptyString(safeword)) {
                    throw new YamiShopBindException("The fund password cannot be blank");
                }
                if (safeword.length() < 6 || safeword.length() > 12) {
                    throw new YamiShopBindException("The fund password must be 6-12 digits");
                }
                userService.checkLoginSafeword(userId, safeword);
            }
            RealNameAuthRecord party_kyc = realNameAuthRecordService.getByUserId(partyId);
            if (party_kyc == null) {
                party_kyc = new RealNameAuthRecord();
            }
            if (!(party_kyc.getStatus() == 2) && "true".equals(this.sysparaService.find("withdraw_by_kyc").getSvalue())) {
                throw new YamiShopBindException("无权限");
            }
            HighLevelAuthRecord party_kycHighLevel = highLevelAuthRecordService.findByUserId(partyId);
            if (party_kycHighLevel == null) {
                party_kycHighLevel = new HighLevelAuthRecord();
            }
            double withdraw_by_high_kyc = Double.valueOf(this.sysparaService.find("withdraw_by_high_kyc").getSvalue());
            if (withdraw_by_high_kyc > 0 && coin_amount_double > withdraw_by_high_kyc && !(party_kycHighLevel.getStatus() == 2)) {
                throw new YamiShopBindException("请先通过高级认证");
            }
            // 手续费(USDT) 提现手续费类型，fixed是单笔固定金额，rate是百分比，part是分段
            String withdraw_fee_type = this.sysparaService.find("withdraw_fee_type").getSvalue();
            // fixed单笔固定金额 和 rate百分比 的手续费数值
            double withdraw_fee = Double.valueOf(this.sysparaService.find("withdraw_fee").getSvalue());
            if ("fixed".equals(withdraw_fee_type)) {
                fee = withdraw_fee;
            }
            if ("rate".equals(withdraw_fee_type)) {
                withdraw_fee = Arith.div(withdraw_fee, 100);
                fee = Arith.mul(coin_amount_double, withdraw_fee);
            }
            if ("part".equals(withdraw_fee_type)) {
                // 提现手续费part分段的值
                String withdraw_fee_part = this.sysparaService.find("withdraw_fee_part").getSvalue();
                String[] withdraw_fee_parts = withdraw_fee_part.split(",");
                for (int i = 0; i < withdraw_fee_parts.length; i++) {
                    double part_amount = Double.valueOf(withdraw_fee_parts[i]);
                    double part_fee = Double.valueOf(withdraw_fee_parts[i + 1]);
                    if (coin_amount_double <= part_amount) {
                        fee = part_fee;
                        break;
                    }
                    i++;
                }
            }
            String withdraw_limit = this.sysparaService.find("withdraw_limit").getSvalue();
            if (coin_amount_double < Double.valueOf(withdraw_limit)) {
                throw new YamiShopBindException("提现不得小于限额");
            }
            String withdraw_limit_max = this.sysparaService.find("withdraw_limit_max").getSvalue();
            if (coin_amount_double > Double.valueOf(withdraw_limit_max)) {
                throw new YamiShopBindException("提现不得大于限额");
            }
            // 检测银行卡用户当日提现次数
            Object obj = this.sysparaService.find("bank_card_withdraw_limit_num");
            if (null != obj) {
                double bank_card_withdraw_limit_num = Double.valueOf(this.sysparaService.find("bank_card_withdraw_limit_num").getSvalue());
                List<C2cOrder> c2cOrders = null;

                //  this.c2cOrderService.findByPartyIdAndToday(partyId, C2cOrder.DIRECTION_WITHDRAW, null);

                if (bank_card_withdraw_limit_num > 0 && c2cOrders != null) {
                    if (c2cOrders.size() >= bank_card_withdraw_limit_num) {
                        throw new YamiShopBindException("当日可提现次数不足");
                    }
                }
            }
            // 是否在当日提现时间内
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("HH:mm:ss");
            Date date = new Date();
            String withdraw_limit_time = this.sysparaService.find("withdraw_limit_time").getSvalue();
            if (StringUtils.isNotEmpty(withdraw_limit_time)) {
                String[] withdraw_time = withdraw_limit_time.split("-");
                String dateString = sdf.format(date);
                if (dateString.compareTo(withdraw_time[0]) < 0 || dateString.compareTo(withdraw_time[1]) > 0) {
                    throw new YamiShopBindException("不在可提现时间内");
                }
            }
            // 周提现额度限制开关
            boolean withdraw_week_limit_button = this.sysparaService.find("withdraw_week_limit_button").getBoolean();
            if (withdraw_week_limit_button) {
                this.checkWithdrawLimit(party, party_kyc, coin_amount_double);
            }
            // 可提现差额开启，取party Withdraw_limit_amount 的可提现金 和剩余金额与流水中的最小值相加
            // 流水为Userdate里的交割，合约，理财，矿池的交易量
            String withdraw_limit_open = this.sysparaService.find("withdraw_limit_open").getSvalue();
            if ("true".equals(withdraw_limit_open)) {
                // 提现限制流水开启后，提现判断用的用户当前流水是使用UserData表的当日流水1还是使用Party表里的用户当前流水2
                String withdraw_limit_open_use_type = this.sysparaService.find("withdraw_limit_open_use_type").getSvalue();
                // 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
                String withdraw_limit_contract_or = this.sysparaService.find("withdraw_limit_contract_or").getSvalue();
                if ("1".equals(withdraw_limit_open_use_type)) {
                    // 还差多少可提现金额
                    double fact_withdraw_amount = 0;
                    // 用户Party表里可提现金额参数 -----可为负数
                    double party_withdraw = party.getWithdrawLimitAmount().doubleValue();
                    // userdata交易流水
                    double userdata_turnover = 0;
                    Map<String, UserData> data_all = this.userDataService.cacheByPartyId(partyId);
                    if (data_all != null) {
                        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                        Date date_now = new Date();
                        for (Map.Entry<String, UserData> valueEntry : data_all.entrySet()) {
                            UserData userdata = valueEntry.getValue();
                            // 如果日期等于当天就赋值
                            if (fmt.format(date_now).equals(fmt.format(userdata.getCreateTime()))) {
                                // 永续合约下单金额amount 理财买入金额finance_amount 股票exchange_amount 矿机下单金额miner_amount 交割合约下单金额furtures_amount
                                // 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
                                double contract_amount = userdata.getAmount();
                                if ("2".equals(withdraw_limit_contract_or)) {
                                    contract_amount = 0;
                                }
                                double amount_finance_amount = Arith.add(contract_amount, userdata.getFinanceAmount());
                                double exchange_amount_miner_amount = Arith.add(0, userdata.getMinerAmount());
                                // 股票交易流水不加入
                                userdata_turnover = Arith.add(userdata.getFurturesAmount(), Arith.add(amount_finance_amount, exchange_amount_miner_amount));
                            }
                        }
                    }
                    double withdraw_limit_turnover_percent = Double.valueOf(this.sysparaService.find("withdraw_limit_turnover_percent").getSvalue());
                    party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
                    // 流水小于限额
                    if (userdata_turnover < party_withdraw) {
                        fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
                        throw new YamiShopBindException("流水小于限额");
                    }
                }
                if ("2".equals(withdraw_limit_open_use_type)) {
                    // 还差多少可提现金额
                    double fact_withdraw_amount = 0;
                    // 用户Party表里可提现金额参数 -----可为负数
                    double party_withdraw = party.getWithdrawLimitAmount().doubleValue();
                    // userdata交易流水
                    double userdata_turnover = party.getWithdrawLimitNowAmount().doubleValue();
                    double withdraw_limit_turnover_percent = Double.valueOf(this.sysparaService.find("withdraw_limit_turnover_percent").getSvalue());
                    party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
                    // 流水小于限额
                    if (userdata_turnover < party_withdraw) {
                        fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
                        throw new YamiShopBindException(fact_withdraw_amount + "");
                    }
                }
            }
        } else {
            // 充值
        }
        // 每日银行卡订单取消最大次数
        int orderCancelDayTimes = 0;
        Map<String, Integer> map = (Map<String, Integer>) redisTemplate.opsForValue().get(RedisKeys.C2C_ORDER_CANCEL_DAY_TIMES);
        if (null != map && null != map.get(partyId)) {
            orderCancelDayTimes = map.get(partyId);
        }
        Object obj1 = this.sysparaService.find("bank_card_order_cancel_day_times");
        if (null != obj1) {
            if (orderCancelDayTimes >= Integer.valueOf(this.sysparaService.find("bank_card_order_cancel_day_times").getSvalue()).intValue()) {
                throw new YamiShopBindException("今日取消订单次数太多了，请明日再试");
            }
        }
        DecimalFormat df = new DecimalFormat("#.########");
        ExchangeRate ex = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT, currency.toUpperCase());
        if (null == ex) {
            throw new YamiShopBindException("支付币种不正确");
        }
        C2cOrder c2cOrder = new C2cOrder();
        c2cOrder.setPartyId(party.getUserId());
        c2cOrder.setPaymentMethodId(payment_method_id);
        c2cOrder.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        // 0未付款
        c2cOrder.setState("0");
        c2cOrder.setNationality(nationality);
        c2cOrder.setImg(img);
        c2cOrder.setDirection(direction);
        c2cOrder.setCurrency(currency);
        c2cOrder.setSymbol("usdt");
        c2cOrder.setSymbolValue(ex.getRata().doubleValue());
        c2cOrder.setCoinAmountFee(Double.valueOf(df.format(fee)).doubleValue());
        c2cOrder.setCoinAmount(Double.valueOf(df.format(Arith.sub(coin_amount_double, c2cOrder.getCoinAmountFee()))).doubleValue());
        c2cOrder.setAmount(Double.valueOf(df.format(Arith.mul(ex.getRata().doubleValue(), coin_amount_double))).doubleValue());
        c2cOrder.setCreateTime(new Date());
        c2cOrder.setHandleTime(null);
        c2cOrder.setCancelTime(null);
        this.c2cOrderService.saveOpen(c2cOrder, party);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("order_no", c2cOrder.getOrderNo());
        return Result.succeed(data);
    }

    @ApiOperation("取消订单")
    @PostMapping("orderCancel")
    public Result orderCancel(String order_no, String remark) {
        User party = userService.getById(SecurityUtils.getUser().getUserId());
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            throw new YamiShopBindException("无权限");
        }
        if (!party.isEnabled()) {
            throw new YamiShopBindException("用户已锁定");
        }
        C2cOrder order = this.c2cOrderService.get(order_no);
        if (null == order || !order.getPartyId().equals(SecurityUtils.getUser().getUserId())) {
           // throw new YamiShopBindException("订单不存在");
        }
        order.setRemark(remark);
        // 用户不能取消提现
        if (SecurityUtils.getUser().getUserId().equals(order.getPartyId())) {
            if ("withdraw".equals(order.getDirection())) {
                throw new YamiShopBindException("用户不能取消提现");
            }
            this.c2cOrderService.saveOrderCancel(order, "user");
        }
        return Result.succeed(null);
    }

    /**
     * 获取 银行卡订单 详情
     */
    @GetMapping("get")
    @ApiOperation("获取 银行卡订单 详情")
    public Result get(@RequestParam String order_no, @RequestParam String language) {
        C2cOrder c2cOrder = this.c2cOrderService.get(order_no);

        if (null == c2cOrder) {
            throw new YamiShopBindException("订单不存在");
        }
        List<String> nos = new ArrayList<String>();
        nos.add(c2cOrder.getOrderNo());
        if (StringUtils.isNotEmpty(c2cOrder.getMethodImg())) {
            String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + c2cOrder.getMethodImg();
            c2cOrder.setMethodImg(path);
        }

        // 多语言
        c2cOrder = this.c2cTranslateService.translateOrder(c2cOrder, language);
        long nowTimestamp = (new Date()).getTime();
        long createTimestamp = c2cOrder.getCreateTime().getTime();
        long autoCancelSeconds = 0;
        if ("0".equals(c2cOrder.getState())) {
            autoCancelSeconds = c2cOrder.getExpireTime() * 60 * 1000 - (nowTimestamp - createTimestamp);
        }
        c2cOrder.setAutoCancelTimeRemain((int) (autoCancelSeconds <= 0 ? 0 : autoCancelSeconds / 1000));
        return Result.succeed(c2cOrder);
    }

    /**
     *
     */
    @ApiOperation("获取 支付币种（法币） 列表")
    @RequestMapping("currency")
    public Result currency() {
        Map<String, String> pmtMap = c2cPaymentMethodService.getC2cSyspara("bank_card_currency");
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        List<ExchangeRate> exchangeRateList = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT);
        if (!CollectionUtils.isEmpty(exchangeRateList)) {
            for (ExchangeRate er : exchangeRateList) {
                if (pmtMap.keySet().contains(er.getCurrency())) {
                    Map<String, Object> erMap = new HashMap<String, Object>();
                    erMap.put("out_or_in", er.getOutOrIn());
                    erMap.put("rate", er.getRata());
                    erMap.put("currency", er.getCurrency());
                    erMap.put("name", er.getName());
                    erMap.put("currency_symbol", er.getCurrencySymbol());
                    data.add(erMap);
                }
            }
        }
        return Result.succeed(data);
    }

    /**
     * 获取 银行卡订单 列表
     */
    @GetMapping("list")
    @ApiOperation("获取 银行卡订单 列表")
    public Result list(String page_no, String direction, String state) {
        String partyId = SecurityUtils.getUser().getUserId();
        if (null == partyId) {
            throw new YamiShopBindException("请重新登录");
        }
        if (StringUtils.isNullOrEmpty(page_no)) {
            page_no = "1";
        }
        if (!StringUtils.isInteger(page_no)) {
            throw new YamiShopBindException("页码不是整数");
        }
        if (Integer.valueOf(page_no).intValue() <= 0) {
            throw new YamiShopBindException("页码不能小于等于0");
        }
        int page_no_int = Integer.valueOf(page_no).intValue();
        if (StringUtils.isNotEmpty(direction) && !Arrays.asList("recharge", "withdraw").contains(direction)) {
            throw new YamiShopBindException("充值或提现不正确");
        }
        if (StringUtils.isNotEmpty(state) && !Arrays.asList("0", "3", "4").contains(state)) {
            throw new YamiShopBindException("订单状态不正确");
        }
        Page page = this.c2cOrderService.pagedQuery(page_no_int, 20, direction, state, partyId);
        if (null == page) {
            return Result.succeed(new ArrayList<Map<String, Object>>());
        } else {
            List<String> nos = new ArrayList<String>();
            for (Map<String, Object> map : (List<Map<String, Object>>) page.getRecords()) {
                nos.add(map.get("order_no").toString());
            }
            Map<String, Integer> unreadMsgs = this.otcOnlineChatMessageService.unreadMsgsApi(nos);
            for (Map<String, Object> map : (List<Map<String, Object>>) page.getRecords()) {
                String orderNo = map.get("order_no").toString();
                if (unreadMsgs.containsKey(orderNo)) {
                    map.put("unread_msg", unreadMsgs.get(orderNo));
                }
            }
            return Result.succeed(page.getRecords());
        }
    }
}
