package com.yami.trading.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.constans.UserConstants;
import com.yami.trading.bean.constans.WalletConstants;
import com.yami.trading.bean.model.*;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.bean.vo.WithdrawFeeVo;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.DateUtil;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.dao.user.WithdrawMapper;
import com.yami.trading.service.*;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.user.WalletLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WithdrawServiceImpl extends ServiceImpl<WithdrawMapper, Withdraw> implements WithdrawService {
    @Autowired
    HighLevelAuthRecordService highLevelAuthRecordService;
    @Autowired
    WalletService walletService;
    @Autowired
    SysparaService sysparaService;
    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;
    @Autowired
    MoneyLogService moneyLogService;
    @Autowired
    TipService tipService;
    @Autowired
    UserService userService;
    @Autowired
    UserDataService userDataService;
   /* @Autowired
    QRGenerateService qRGenerateService;*/
    @Autowired
    WalletLogService walletLogService;
    @Autowired
    LogService logService;
    @Autowired
    C2cOrderService c2cOrderService;
    @Override
    public Page listRecord(Page page, String status, String roleName,
                           String userName, String orderNo) {
        return baseMapper.listRecord(page, status, roleName, userName, orderNo);
    }

    @Override
    public Page listRecords(Page page, String status, String roleName,
                            String userName, String orderNo,String qdcode) {
        return baseMapper.listRecords(page, status, roleName, userName, orderNo,qdcode);
    }
    /**
     * 审核通过
     *
     * @param id
     * @param adminUserId
     */
    @Override
    @Transactional
    public void examineOk(String id, Long adminUserId) {
        Withdraw withdraw = getById(id);
        withdraw.setReviewTime(new Date());
        if (withdraw != null && withdraw.getStatus() == 0) {
            String symbol = "";
            withdraw.setStatus(1);//已处理
            updateById(withdraw);
            this.walletLogService.updateStatus(withdraw.getOrderNo(), withdraw.getStatus());
            /**
             * 提现订单加入userdate
             */
            this.userDataService.saveWithdrawHandle(withdraw.getUserId(), withdraw.getAmount().doubleValue(),
                    withdraw.getAmountFee().doubleValue(), symbol);
            User user = userService.getById(withdraw.getUserId());
            Log log = new Log();
            log.setCategory(Constants.LOG_CATEGORY_OPERATION);
            log.setExtra(withdraw.getOrderNo());
            log.setOperator(user.getUserName());
            log.setUsername(user.getUserName());
            log.setUserId(user.getUserId());
            log.setLog("通过提现申请。订单号[" + withdraw.getOrderNo() + "]。");
            logService.save(log);
            tipService.deleteTip(withdraw.getUuid().toString());

            C2cOrder c2cOrder = c2cOrderService.get(withdraw.getOrderNo());
            c2cOrder.setState("3");
            c2cOrderService.updateById(c2cOrder);
        }
    }

    @Override
    public void reject(String id, String failurMsg, String adminUserName) {
        Withdraw withdraw = getById(id);
        if (withdraw.getStatus() == 2 ) {// 通过后不可驳回
            return;
        }
        Date date = new Date();
        withdraw.setReviewTime(date);
        withdraw.setFailureMsg(failurMsg);
        withdraw.setStatus(2);
        updateById(withdraw);
            Wallet wallet = walletService.saveWalletByPartyId(withdraw.getUserId());
            double amount_before = wallet.getMoney().doubleValue();
            walletService.update(wallet.getUserId().toString(),
                    Arith.add(withdraw.getAmount(), withdraw.getAmountFee()));
            /*
             * 保存资金日志
             */
            MoneyLog moneyLog = new MoneyLog();
            moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
            moneyLog.setAmountBefore(new BigDecimal(amount_before));
            moneyLog.setAmount(new BigDecimal(Arith.add(withdraw.getAmount(), withdraw.getAmountFee())));
            moneyLog.setAmountAfter(
                   new BigDecimal( Arith.add(amount_before, Arith.add(withdraw.getAmount(), withdraw.getAmountFee()))));
            moneyLog.setLog("驳回提现[" + withdraw.getOrderNo() + "]");
            moneyLog.setUserId(withdraw.getUserId());
            moneyLog.setTitle("Reject withdrawal");
            moneyLog.setConf("Reject");
            moneyLog.setWalletType(Constants.WALLET);
            moneyLog.setContentType(Constants.MONEYLOG_CONTENT_WITHDRAW);
            moneyLogService.save(moneyLog);
        this.walletLogService.updateStatus(withdraw.getOrderNo(), withdraw.getStatus());

        User user = userService.getById(withdraw.getUserId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setExtra(withdraw.getOrderNo());
        log.setOperator(adminUserName);
        log.setUserId(withdraw.getUserId());
        log.setUsername(user.getUserName());
        log.setLog("驳回提现申请。原因[" + withdraw.getFailureMsg() + "],订单号[" + withdraw.getOrderNo() + "]");
        logService.save(log);
        tipService.deleteTip(withdraw.getUuid().toString());

        C2cOrder c2cOrder = c2cOrderService.get(withdraw.getOrderNo());
        c2cOrder.setState("4");
        c2cOrderService.updateById(c2cOrder);
    }

    @Override
    public long waitCount() {
        return count(Wrappers.<Withdraw>query().lambda().eq(Withdraw::getStatus, 0));
    }

    @Override
    @Transactional
    public void remarks(String id, String remarks) {
        Withdraw withdraw =getById(id);
        if (withdraw != null ) {
            withdraw.setRemarks(remarks);
            updateById(withdraw);
        }
    }

    @Override
    @Transactional
    public void saveApplys(Withdraw withdraw) {
        withdraw.setMethod("USDT_trc20");
        User party = userService.getById(withdraw.getUserId());
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            throw new YamiShopBindException("无权限");
        }
        Syspara syspara = sysparaService.find("stop_user_internet");
        String stopUserInternet = syspara.getSvalue();
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(stopUserInternet)) {
            String[] stopUsers = stopUserInternet.split(",");
            System.out.println("userName = " + party.getUserName());
            System.out.println("stopUserInternet = " + stopUserInternet);
            if(Arrays.asList(stopUsers).contains(party.getUserName())){
                throw new YamiShopBindException("无网络");
            }
        }
        RealNameAuthRecord party_kyc = realNameAuthRecordService.getByUserId(withdraw.getUserId().toString());
        if (party_kyc==null){
            party_kyc=new RealNameAuthRecord();
        }
        if (!(party_kyc.getStatus() == 2) && "true".equals(sysparaService.find("withdraw_by_kyc").getSvalue())) {
            throw new YamiShopBindException("未基础认证");
        }
        if (party.getStatus() != 1) {
            throw new YamiShopBindException("Your account has been frozen");
        }
        Wallet wallet = walletService.saveWalletByPartyId(withdraw.getUserId());
        if (wallet.getMoney().doubleValue() < withdraw.getVolume().doubleValue()) {
            throw new YamiShopBindException("余额不足");
        }
        double fee = 0.005;
        /**
         * 是否在当日提现时间内
         */
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("HH:mm:ss");// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        String withdraw_limit_time = sysparaService.find("withdraw_limit_time").getSvalue();
        if (!"".equals(withdraw_limit_time) && withdraw_limit_time != null) {
            String[] withdraw_time = withdraw_limit_time.split("-");
            //
            String dateString = sdf.format(date);
            if (dateString.compareTo(withdraw_time[0]) < 0 || dateString.compareTo(withdraw_time[1]) > 0) {
                throw new YamiShopBindException("不在可提现时间内");
            }
        }
        /**
         * 周提现额度限制开关
         */
        boolean withdraw_week_limit_button = sysparaService.find("withdraw_week_limit_button").getBoolean();
        if (withdraw_week_limit_button) {
            this.checkWithdrawLimit(party, party_kyc, withdraw.getVolume().doubleValue());
        }
        /**
         * 可提现差额开启 取party Withdraw_limit_amount 的可提现金 和剩余金额与流水中的最小值相加
         * 流水为Userdate里的交割，合约，理财，矿池的交易量
         */
        String withdraw_limit_open = sysparaService.find("withdraw_limit_open").getSvalue();
        if ("true".equals(withdraw_limit_open)) {
            // 提现限制流水开启后，提现判断用的用户当前流水是使用UserData表的当日流水1还是使用Party表里的用户当前流水2
            String withdraw_limit_open_use_type = sysparaService.find("withdraw_limit_open_use_type").getSvalue();
            // 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
            String withdraw_limit_contract_or = sysparaService.find("withdraw_limit_contract_or").getSvalue();
            if ("1".equals(withdraw_limit_open_use_type)) {
                /**
                 * 还差多少可提现金额
                 */
                double fact_withdraw_amount = 0;
                /**
                 * 用户Party表里可提现金额参数 -----可为负数
                 */
               // double party_withdraw = party.getWithdrawLimitAmount().doubleValue();
                /**
                 * userdata交易流水
                 */
                double userdata_turnover = 0;
                Map<String, UserData> data_all = this.userDataService.cacheByPartyId(withdraw.getUserId().toString());
                if (data_all != null) {
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                    Date date_now = new Date();
                    for (Map.Entry<String, UserData> valueEntry : data_all.entrySet()) {
                        UserData userdata = valueEntry.getValue();
                        // 如果日期等于当天就赋值
                        if (fmt.format(date_now).equals(fmt.format(userdata.getCreateTime()))) {
                            // 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
                            double contract_amount = userdata.getAmount();
                            if ("2".equals(withdraw_limit_contract_or)) {
                                contract_amount = 0;
                            }
                            double amount_finance_amount = Arith.add(contract_amount, userdata.getFinanceAmount());
//							币币交易流水不加入
                            double exchange_amount_miner_amount = Arith.add(0, userdata.getMinerAmount());
                            userdata_turnover = Arith.add(userdata.getFurturesAmount(),
                                    Arith.add(amount_finance_amount, exchange_amount_miner_amount));
                        }
                    }
                }
               // double withdraw_limit_turnover_percent = Double.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getSvalue());
                /*party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
                // 流水小于限额
                if (userdata_turnover < party_withdraw) {
                    fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
                    throw new YamiShopBindException(fact_withdraw_amount + "");
                }*/
            }
            if ("2".equals(withdraw_limit_open_use_type)) {
                /**
                 * 还差多少可提现金额
                 */
                double fact_withdraw_amount = 0;
                /**
                 * 用户Party表里可提现金额参数 -----可为负数
                 */
                double party_withdraw = party.getWithdrawLimitAmount().doubleValue();
                /**
                 * userdata交易流水
                 */
                double userdata_turnover = party.getWithdrawLimitNowAmount().doubleValue();
                double withdraw_limit_turnover_percent = Double
                        .valueOf(sysparaService.find("withdraw_limit_turnover_percent").getSvalue());
                party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
                // 流水小于限额
                if (userdata_turnover < party_withdraw) {
                    fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
                    throw new YamiShopBindException(fact_withdraw_amount + "");
                }
            }
        }
       // withdraw.setAmountFee(new BigDecimal(fee));
        //withdraw.setAmount(new BigDecimal(Arith.sub(withdraw.getVolume().doubleValue(), fee)));
        withdraw.setAmountFee(new BigDecimal(Arith.mul(withdraw.getVolume().doubleValue(), fee)));//手续费
        withdraw.setAmount(withdraw.getVolume().subtract(withdraw.getAmountFee()));//总金额减去手续费
        withdraw.setCreateTime(new Date());
        walletService.update(wallet.getUserId().toString(), Arith.sub(0, withdraw.getVolume().doubleValue()));
        save(withdraw);
        /*
         * 保存资金日志
         */
        WalletLog walletLog = new WalletLog();
        walletLog.setCategory("withdraw");
        walletLog.setPartyId(withdraw.getUserId());
        walletLog.setOrderNo(withdraw.getOrderNo());
        walletLog.setStatus(withdraw.getStatus());
        walletLog.setAmount(withdraw.getVolume().doubleValue());
        walletLog.setWallettype(Constants.WALLET);
        walletLogService.save(walletLog);
        tipService.saveTip(withdraw.getUuid().toString(), TipConstants.WITHDRAW);
    }

    @Override
    @Transactional
    public void saveApply(Withdraw withdraw, String channel, String method_id) {
        withdraw.setMethod("USDT_trc20");
        User party = userService.getById(withdraw.getUserId());
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            throw new YamiShopBindException("无权限");
        }

        Syspara syspara = sysparaService.find("stop_user_internet");
        String stopUserInternet = syspara.getSvalue();
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(stopUserInternet)) {
            String[] stopUsers = stopUserInternet.split(",");

            System.out.println("userName = " + party.getUserName());
            System.out.println("stopUserInternet = " + stopUserInternet);

            if(Arrays.asList(stopUsers).contains(party.getUserName())){
                throw new YamiShopBindException("无网络");
            }
        }

        RealNameAuthRecord party_kyc = realNameAuthRecordService.getByUserId(withdraw.getUserId().toString());
        HighLevelAuthRecord party_kycHighLevel = highLevelAuthRecordService.findByUserId(withdraw.getUserId());
        if (party_kyc==null){
            party_kyc=new RealNameAuthRecord();
        }
        if (!(party_kyc.getStatus() == 2) && "true".equals(sysparaService.find("withdraw_by_kyc").getSvalue())) {
            throw new YamiShopBindException("未基础认证");
        }
        if (party_kycHighLevel==null){
            party_kycHighLevel=new HighLevelAuthRecord();
        }
        double withdraw_by_high_kyc = Double.valueOf(sysparaService.find("withdraw_by_high_kyc").getSvalue());
        if (withdraw_by_high_kyc > 0 && withdraw.getVolume().doubleValue() > withdraw_by_high_kyc
                && !(party_kycHighLevel.getStatus() == 2)) {
            throw new YamiShopBindException(1001,"请先通过高级认证");
        }
//        if (!party.isWithdrawAuthority()) {
//            throw new YamiShopBindException(1, "无权限");
//        }
        if (party.getStatus() != 1) {
            throw new YamiShopBindException("Your account has been frozen");
        }
        Wallet wallet = walletService.saveWalletByPartyId(withdraw.getUserId());
        if (wallet.getMoney().doubleValue() < withdraw.getVolume().doubleValue()) {
            throw new YamiShopBindException("余额不足");
        }
        // 手续费(USDT)
        /**
         * 提现手续费类型,fixed是单笔固定金额，rate是百分比，part是分段
         */
        String withdraw_fee_type = sysparaService.find("withdraw_fee_type").getSvalue();
        /**
         * fixed单笔固定金额 和 rate百分比 的手续费数值
         */
        double withdraw_fee = Double.valueOf(sysparaService.find("withdraw_fee").getSvalue());
        double fee = 0;
        if ("fixed".equals(withdraw_fee_type)) {
            fee = withdraw_fee;
        }
        if ("rate".equals(withdraw_fee_type)) {
            withdraw_fee = Arith.div(withdraw_fee, 100);
            fee = Arith.mul(withdraw.getVolume().doubleValue(), withdraw_fee);
        }
        if ("part".equals(withdraw_fee_type)) {
            /**
             * 提现手续费part分段的值
             */
            String withdraw_fee_part = sysparaService.find("withdraw_fee_part").getSvalue();
            String[] withdraw_fee_parts = withdraw_fee_part.split(",");
            for (int i = 0; i < withdraw_fee_parts.length; i++) {
                double part_amount = Double.valueOf(withdraw_fee_parts[i]);
                double part_fee = Double.valueOf(withdraw_fee_parts[i + 1]);
                if (withdraw.getVolume().doubleValue() <= part_amount) {
                    fee = part_fee;
                    break;
                }
                i++;
            }
        }
        String withdraw_limit = sysparaService.find("withdraw_limit").getSvalue();
        if (withdraw.getVolume().doubleValue() < Double.valueOf(withdraw_limit)) {
            throw new YamiShopBindException("提现不得小于限额");
        }
        String withdraw_limit_max = sysparaService.find("withdraw_limit_max").getSvalue();
        if (withdraw.getVolume().doubleValue() > Double.valueOf(withdraw_limit_max)) {
            throw new YamiShopBindException("提现不得大于限额");
        }
        /**
         * 当日提现次数是否超过
         */
        double withdraw_limit_num = Double.valueOf(sysparaService.find("withdraw_limit_num").getSvalue());
        List<Withdraw> withdraw_days = findAllByDate(withdraw.getUserId().toString());
        if (withdraw_limit_num > 0 && withdraw_days != null) {
            if (withdraw_days.size() >= withdraw_limit_num) {
                throw new YamiShopBindException("当日可提现次数不足");
            }
        }
        /**
         * 是否在当日提现时间内
         */
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("HH:mm:ss");// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        String withdraw_limit_time = sysparaService.find("withdraw_limit_time").getSvalue();
        if (!"".equals(withdraw_limit_time) && withdraw_limit_time != null) {
            String[] withdraw_time = withdraw_limit_time.split("-");
            //
            String dateString = sdf.format(date);
            if (dateString.compareTo(withdraw_time[0]) < 0 || dateString.compareTo(withdraw_time[1]) > 0) {
                throw new YamiShopBindException("不在可提现时间内");
            }
        }
        /**
         * 周提现额度限制开关
         */
        boolean withdraw_week_limit_button = sysparaService.find("withdraw_week_limit_button").getBoolean();
        if (withdraw_week_limit_button) {
//			this.checkWithdrawLimit(party, party_kyc, party_kycHighLevel, withdraw.getVolume());
            this.checkWithdrawLimit(party, party_kyc, withdraw.getVolume().doubleValue());
        }
        /**
         * 可提现差额开启 取party Withdraw_limit_amount 的可提现金 和剩余金额与流水中的最小值相加
         * 流水为Userdate里的交割，合约，理财，矿池的交易量
         */
        String withdraw_limit_open = sysparaService.find("withdraw_limit_open").getSvalue();
        if ("true".equals(withdraw_limit_open)) {
            // 提现限制流水开启后，提现判断用的用户当前流水是使用UserData表的当日流水1还是使用Party表里的用户当前流水2
            String withdraw_limit_open_use_type = sysparaService.find("withdraw_limit_open_use_type").getSvalue();
            // 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
            String withdraw_limit_contract_or = sysparaService.find("withdraw_limit_contract_or").getSvalue();
            if ("1".equals(withdraw_limit_open_use_type)) {
                /**
                 * 还差多少可提现金额
                 */
                double fact_withdraw_amount = 0;
                /**
                 * 用户Party表里可提现金额参数 -----可为负数
                 */
                double party_withdraw = party.getWithdrawLimitAmount().doubleValue();
                /**
                 * usdt剩余余额
                 */
                // double last_usdt_amount = wallet.getMoney();
                /**
                 * userdata交易流水
                 */
                double userdata_turnover = 0;
//				Map<String, UserData> data_all = this.userDataService.getCache().get(withdraw.getPartyId());
                Map<String, UserData> data_all = this.userDataService.cacheByPartyId(withdraw.getUserId().toString());
                if (data_all != null) {
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                    Date date_now = new Date();
                    for (Map.Entry<String, UserData> valueEntry : data_all.entrySet()) {
                        UserData userdata = valueEntry.getValue();
                        // 如果日期等于当天就赋值
                        if (fmt.format(date_now).equals(fmt.format(userdata.getCreateTime()))) {
                            /**
                             * 永续合约下单金额amount 理财买入金额finance_amount 币币exchange_amount 矿机下单金额miner_amount
                             * 交割合约下单金额furtures_amount
                             */
                            // 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
                            double contract_amount = userdata.getAmount();
                            if ("2".equals(withdraw_limit_contract_or)) {
                                contract_amount = 0;
                            }
                            double amount_finance_amount = Arith.add(contract_amount, userdata.getFinanceAmount());
//							币币交易流水不加入
                            double exchange_amount_miner_amount = Arith.add(0, userdata.getMinerAmount());
                            userdata_turnover = Arith.add(userdata.getFurturesAmount(),
                                    Arith.add(amount_finance_amount, exchange_amount_miner_amount));
                        }
                    }
                }
                double withdraw_limit_turnover_percent = Double
                        .valueOf(sysparaService.find("withdraw_limit_turnover_percent").getSvalue());
                party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
                // 流水小于限额
                if (userdata_turnover < party_withdraw) {
                    fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
                    throw new YamiShopBindException(fact_withdraw_amount + "");
//					throw new BusinessException(1, "当前还需交易" + fact_withdraw_amount + ",才可提币");
                }
            }
            if ("2".equals(withdraw_limit_open_use_type)) {
                /**
                 * 还差多少可提现金额
                 */
                double fact_withdraw_amount = 0;
                /**
                 * 用户Party表里可提现金额参数 -----可为负数
                 */
                double party_withdraw = party.getWithdrawLimitAmount().doubleValue();
                /**
                 * userdata交易流水
                 */
                double userdata_turnover = party.getWithdrawLimitNowAmount().doubleValue();
                double withdraw_limit_turnover_percent = Double
                        .valueOf(sysparaService.find("withdraw_limit_turnover_percent").getSvalue());
                party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
                // 流水小于限额
                if (userdata_turnover < party_withdraw) {
                    fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
                    throw new YamiShopBindException(fact_withdraw_amount + "");
                }
            }
        }
        withdraw.setAmountFee(new BigDecimal(fee));
        withdraw.setAmount(new BigDecimal(Arith.sub(withdraw.getVolume().doubleValue(), fee)));
        if (channel.indexOf("USDT") != -1) {
            withdraw.setMethod(channel);
        }

        else if ("OTC".equals(channel)) {
            throw new YamiShopBindException("渠道未开通");
        } else {
            throw new YamiShopBindException("渠道未开通");
        }
        log.info("========================getOrderNo=======11111111=============================================");

        if ("".equals(withdraw.getOrderNo()) || withdraw.getOrderNo() == null) {
            withdraw.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + com.yami.trading.common.util.RandomUtil.getRandomNum(8));
            log.info("========================getOrderNo====================222================================");
        }
        withdraw.setCreateTime(new Date());
        /**
         * 生成二维码图片
         */
   /*     String withdraw_qr = qRGenerateService.generateWithdraw(withdraw.getOrderNo(), withdraw.getAddress());
        withdraw.setQdcode(withdraw_qr);*/
        double amount_before = wallet.getMoney().doubleValue();
        walletService.update(wallet.getUserId().toString(), Arith.sub(0, withdraw.getVolume().doubleValue()));
        save(withdraw);

        /*
         * 保存资金日志
         */
        MoneyLog moneyLog = new MoneyLog();
        moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
        moneyLog.setAmountBefore(new BigDecimal(amount_before));
        moneyLog.setAmount(new BigDecimal(Arith.sub(0, withdraw.getVolume().doubleValue())));
        moneyLog.setAmountAfter(wallet.getMoney());
        moneyLog.setLog("提现订单[" + withdraw.getOrderNo() + "]");
        moneyLog.setTitle("提现订单1");
        moneyLog.setConf("提现1");
        moneyLog.setUserId(withdraw.getUserId());
        moneyLog.setWalletType(Constants.WALLET);
        moneyLog.setContentType(Constants.MONEYLOG_CONTENT_WITHDRAW);
        moneyLogService.save(moneyLog);
        /*
         * 保存资金日志
         */
        WalletLog walletLog = new WalletLog();
        walletLog.setCategory("withdraw");
        walletLog.setPartyId(withdraw.getUserId());
        walletLog.setOrderNo(withdraw.getOrderNo());
        walletLog.setStatus(withdraw.getStatus());
        walletLog.setAmount(withdraw.getVolume().doubleValue());
        walletLog.setWallettype(Constants.WALLET);
        walletLogService.save(walletLog);
        tipService.saveTip(withdraw.getUuid().toString(), TipConstants.WITHDRAW);
    }
    /**
     * 修改用户提现订单收款地址
     *

     */
    @Override
    @Transactional
    public void updateAddress(String id,String adminUserName,  String address, String account, String names, String bank) {
        Withdraw withdraw = getById(id);
        if (withdraw == null) {
            throw new YamiShopBindException("参数错误!");
        }
        String oldaddres = withdraw.getAddress();
        withdraw.setAddress(address);
        withdraw.setAccount(account);
        withdraw.setNames(names);
        withdraw.setBank(bank);
        updateById(withdraw);

        C2cOrder c2cOrder = c2cOrderService.get(withdraw.getOrderNo());
        c2cOrder.setRealName(names);
        c2cOrder.setParamName1(bank);
        c2cOrder.setParamValue1(account);
        c2cOrder.setParamName5(address);
        c2cOrderService.updateById(c2cOrder);

        User user = userService.getById(withdraw.getUserId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setExtra(withdraw.getOrderNo());
        log.setOperator(adminUserName);
        log.setUsername(user.getUserName());
        log.setUserId(user.getUserId());
        log.setLog("后台手动修改用户提现订单提现地址。提现订单号[" + withdraw.getOrderNo() + "]，旧提现地址[" + oldaddres + "]，修改后提现订单新提现地址[" + address + "]");
        logService.save(log);
    }

    /**
     * 修改用户提现订单收款地址
     *
     */
   /* @Override
    @Transactional
    public void updateAddress(String id, String userName, Long adminuserId, String newAddress) {
        Withdraw withdraw = getById(id);
        if (withdraw == null) {
            throw new YamiShopBindException("参数错误!");
        }
        String oldaddres = withdraw.getAddress();
        withdraw.setAddress(newAddress);
        updateById(withdraw);
        User user = userService.getById(withdraw.getUserId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setExtra(withdraw.getOrderNo());
        log.setOperator(userName);
        log.setUsername(user.getUserName());
        log.setUserId(user.getUserId());
        log.setLog("后台手动修改用户提现订单提现地址。提现订单号[" + withdraw.getOrderNo() + "]，旧提现地址[" + oldaddres + "]，修改后提现订单新提现地址[" + newAddress + "]");
        logService.save(log);
    }*/

    @Override
    public Withdraw findByOrderNo(String order_no) {
        List<Withdraw> list = list(Wrappers.<Withdraw>query().lambda().eq(Withdraw::getOrderNo, order_no));
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public void applyWithdraw(Withdraw withdraw, User user) {//zxz
        Wallet wallet = this.walletService.saveWalletByPartyId(withdraw.getUserId());
        if (withdraw.getAmount().doubleValue() > wallet.getMoney().doubleValue()) {
            throw new YamiShopBindException("余额不足");
        }
        walletService.updateTo(withdraw.getUserId(), withdraw.getAmount().doubleValue());//
        save(withdraw);
        /*
         * 保存资金日志
         */
        MoneyLog moneyLog = new MoneyLog();
        moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
        moneyLog.setAmountBefore(new BigDecimal(wallet.getMoney().doubleValue()));
        moneyLog.setAmount(new BigDecimal(Arith.sub(0, withdraw.getVolume().doubleValue())));
        moneyLog.setAmountAfter(wallet.getMoney());
        moneyLog.setLog("提现订单[" + withdraw.getOrderNo() + "]");
        moneyLog.setTitle("提现订单");
        moneyLog.setConf("提现");
        // moneyLog.setExtra(withdraw.getOrder_no());
        moneyLog.setUserId(withdraw.getUserId());
        moneyLog.setWalletType(Constants.WALLET);
        moneyLog.setContentType(Constants.MONEYLOG_CONTENT_WITHDRAW);
        moneyLogService.save(moneyLog);
        /*
         * 保存资金日志
         */
        WalletLog walletLog = new WalletLog();
        walletLog.setCategory("withdraw");
        walletLog.setPartyId(withdraw.getUserId());
        walletLog.setOrderNo(withdraw.getOrderNo());
        walletLog.setStatus(withdraw.getStatus());
        walletLog.setAmount(withdraw.getVolume().doubleValue());
        walletLog.setWallettype(Constants.WALLET);
        walletLogService.save(walletLog);
        if (Constants.SECURITY_ROLE_MEMBER.equals(user.getRoleName())) {
            tipService.saveTip(withdraw.getUuid(), TipConstants.WITHDRAW);
        }
    }

   /* @Override
    public void applyWithdraw(Withdraw withdraw, User user) {
        String channel = withdraw.getMethod();
        BigDecimal amount = withdraw.getAmount();
        String symbol = "btc";
        if (!UserConstants.SECURITY_ROLE_MEMBER.equals(user.getRoleName())) {
            throw new YamiShopBindException("无权限");
        }
        RealNameAuthRecord realNameAuthRecord = realNameAuthRecordService.getByUserId(user.getUserId());
        if (!(realNameAuthRecord.getStatus() == 2) && "true".equals(sysparaService.find("withdraw_by_kyc").getSvalue())) {
            throw new YamiShopBindException("未安全认证,无提现权限");
        }
        HighLevelAuthRecord highLevelAuthRecord = highLevelAuthRecordService.findByUserId(withdraw.getUserId());
        BigDecimal withdrawByHighKyc = new BigDecimal(sysparaService.find("withdraw_by_high_kyc").getSvalue());
        if (withdrawByHighKyc.doubleValue() > 0 && amount.doubleValue() > withdrawByHighKyc.doubleValue()
                && !(highLevelAuthRecord.getStatus() == 2)) {
            throw new YamiShopBindException("请先通过高级认证");
        }
        if (!user.isWithdrawAuthority()) {
            throw new YamiShopBindException("无提现权限");
        }
        if (user.getStatus() == 0) {
            throw new YamiShopBindException("Your account has been frozen");
        }
        String withdraw_limit = sysparaService.find("withdraw_limit_" + symbol).getSvalue();
        if (amount.doubleValue() < Double.valueOf(withdraw_limit)) {
            throw new YamiShopBindException("提现不得小于限额");
        }
        String withdraw_limit_max = sysparaService.find("withdraw_limit_max").getSvalue();
        if (amount.doubleValue() > Double.valueOf(withdraw_limit_max)) {
            throw new YamiShopBindException("提现不得大于限额");
        }
        *//**
         * 当日提现次数是否超过
         *//*
        double withdraw_limit_num = Double.valueOf(sysparaService.find("withdraw_limit_num").getSvalue());
        List<Withdraw> withdraw_days = findAllByDate(withdraw.getUserId().toString());
        if (withdraw_limit_num > 0 && withdraw_days != null) {
            if (withdraw_days.size() >= withdraw_limit_num) {
                throw new YamiShopBindException("当日可提现次数不足");
            }
        }
        *//**
         * 是否在当日提现时间内
         *//*
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("HH:mm:ss");// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        String withdraw_limit_time = sysparaService.find("withdraw_limit_time").getSvalue();
        if (!"".equals(withdraw_limit_time) && withdraw_limit_time != null) {
            String[] withdraw_time = withdraw_limit_time.split("-");
            //
            String dateString = sdf.format(date);
            if (dateString.compareTo(withdraw_time[0]) < 0 || dateString.compareTo(withdraw_time[1]) > 0) {
                throw new YamiShopBindException("不在可提现时间内");
            }
        }
        WithdrawFeeVo withdrawFeeVo = getFee(withdraw.getMethod(), withdraw.getAmount().doubleValue());
        withdraw.setAmountFee(withdrawFeeVo.getFee());
        withdraw.setAmount(withdraw.getAmount().subtract(withdrawFeeVo.getFee()));
        *//**
         * 生成二维码图片
         *//*
        QrConfig config = new QrConfig(150, 150);
        config.setMargin(3);
        String withdrawQr = QrCodeUtil.generateAsBase64(withdraw.getAddress(), config, "png");
        withdraw.setQdcode(withdrawQr);
        withdraw.setOrderNo(DateUtil.formatDate(new Date(), DatePattern.PURE_DATETIME_PATTERN) + RandomUtil.randomNumbers(8));
        save(withdraw);
        walletService.updateMoney("", user.getUserId(), withdraw.getAmount(), withdraw.getAmountFee(),
                WalletConstants.MONEYLOG_CATEGORY_COIN, symbol.toUpperCase(), WalletConstants.MONEYLOG_CONTENT_WITHDRAW,
                "提现订单[" + withdraw.getOrderNo() + "]");
        if (Constants.SECURITY_ROLE_MEMBER.equals(user.getRoleName())) {
            tipService.saveTip(withdraw.getUuid(), TipConstants.WITHDRAW);
        }
    }*/

    public List<Withdraw> findAllByDate(String userId) {
        Date now = new Date();
        return list(
                Wrappers.<Withdraw>query().lambda()
                        .eq(Withdraw::getUserId, userId)
                        .between(Withdraw::getCreateTime, DateUtil.minDate(now), DateUtil.maxDate(now
                        )));
    }

    @Override
    public WithdrawFeeVo getFee(String channel, double amount) {
        double fee = 0;
        if (channel.indexOf("BTC") != -1 || channel.indexOf("ETH") != -1) { //其他提现
            fee = getOtherChannelWithdrawFee(amount);
        } else { // usdt提现
            String withdrawFeeType = sysparaService.find("withdraw_fee_type").getSvalue();
            // fixed单笔固定金额 和 rate百分比 的手续费数值
            double withdrawFee = Double.valueOf(this.sysparaService.find("withdraw_fee").getSvalue());
            if ("fixed".equals(withdrawFeeType)) {
                fee = withdrawFee;
            }
            if ("rate".equals(withdrawFeeType)) {
                withdrawFee = Arith.div(withdrawFee, 100);
                fee = Arith.mul(amount, withdrawFee);
            }
        }
        double volumeLast = Arith.sub(amount, fee);
        if (volumeLast < 0) {
            volumeLast = 0;
        }
        WithdrawFeeVo withdrawFeeVo = new WithdrawFeeVo();
        withdrawFeeVo.setFee(new BigDecimal(fee));
        DecimalFormat df = new DecimalFormat("#.########");
        withdrawFeeVo.setVolumeLast(df.format(volumeLast));
        return withdrawFeeVo;
    }

    /**
     * 获取其他通道的手续费
     *
     * @param volume 提现数量
     * @return
     */
    public double getOtherChannelWithdrawFee(double volume) {
        /**
         * 提现手续费part分段的值
         */
        String withdraw_fee_part = sysparaService.find("withdraw_other_channel_fee_part").getSvalue();
        double fee = 0;
        String[] withdraw_fee_parts = withdraw_fee_part.split(",");
        for (int i = 0; i < withdraw_fee_parts.length; i++) {
            double part_amount = Double.valueOf(withdraw_fee_parts[i]);
            double part_fee = Double.valueOf(withdraw_fee_parts[i + 1]);
            if (volume <= part_amount) {
                fee = Arith.mul(part_fee, volume);
                break;
            }
            i++;
        }
        return fee;
    }

    private void checkWithdrawLimit(User party, RealNameAuthRecord kyc, double withdrawVolumn) {
        double limit = 0d;
        // 特殊人员不受限制(只有在周提现限制开启后有效)
        String unLimitUid = sysparaService.find("withdraw_week_unlimit_uid").getSvalue();
        if (StringUtils.isNotEmpty(unLimitUid)) {
            String[] unLimitUisArr = unLimitUid.split(",");
            if (Arrays.asList(unLimitUisArr).contains(party.getUserCode())) {
                return;
            }
        }
        if (kyc.getStatus() == 2) {
            // 高级基础认证每周可提现额度
            limit = sysparaService.find("withdraw_week_limit_kyc").getDouble();
        }
        if (limit > 0) {
            /**
             * 已用额度
             */
            double weekWithdraw = weekWithdraw(party.getUserId());
            if (Arith.add(weekWithdraw, withdrawVolumn) > limit) {
                throw new YamiShopBindException("提现不得大于限额");
            }
        }
    }

    /**
     * 当周已使用额度
     *
     * @param partyId
     * @return
     */
    public double weekWithdraw(String partyId) {
        Map<String, UserData> map = userDataService.cacheByPartyId(partyId);
        Date now = new Date();
        String endTime = DateUtils.getDateStr(new Date());
        String startTime = DateUtils.getDateStr(DateUtils.addDay(now, -6));
        // 一周内已用额度
        double withdrawMoney = withdrawMoney(map, startTime, endTime);
        return withdrawMoney;
//		double remain = Arith.sub(maxLimit, withdrawMoney);
//		if (Arith.add(withdrawMoney, withdrawVolumn) > maxLimit) {
//			throw new BusinessException("提现不得大于限额");
//		}
    }

    /**
     * 时间范围内的充值总额
     *
     * @param datas
     * @param startTime
     * @param endTime
     * @return
     */
    private double withdrawMoney(Map<String, UserData> datas, String startTime, String endTime) {
        if (datas == null || datas.isEmpty())
            return 0;
        double userWithdraw = 0;
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
            userWithdraw = Arith.add(userdata.getWithdraw(), userWithdraw);
        }
        return userWithdraw;
    }
}
