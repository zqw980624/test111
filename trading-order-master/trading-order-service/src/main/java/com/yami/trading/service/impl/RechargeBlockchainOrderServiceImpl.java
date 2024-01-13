package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.model.*;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.DateUtil;
import com.yami.trading.common.util.RandomUtil;
import com.yami.trading.dao.user.RechargeBlockchainOrderMapper;
import com.yami.trading.service.*;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.user.WalletLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class RechargeBlockchainOrderServiceImpl extends ServiceImpl<RechargeBlockchainOrderMapper, RechargeBlockchainOrder> implements RechargeBlockchainOrderService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    WalletService  walletService;

    @Autowired
    UserService userService;

    @Autowired
    SysparaService sysparaService;

    @Autowired
    WalletLogService walletLogService;

    @Autowired
    ChannelBlockchainService channelBlockchainService;
    @Autowired
    TipService tipService;

    @Autowired
    LogService logService;

    @Autowired
    MoneyLogService moneyLogService;

    @Autowired
    UserDataService userDataService;

    @Autowired
    RechargeBonusService rechargeBonusService;

    @Autowired
    DataService dataService;
    @Autowired
    C2cOrderService c2cOrderService;
    @Override
    public Page pageRecord(Page page, String rolename, String orderNo, String userName, Date startTime, Date endTime,String status) {
        return baseMapper.listRecord(page, rolename, orderNo, userName, startTime, endTime,status);
    }

    @Override
    public Page pageRecords(Page page, String rolename, String orderNo, String userName, Date startTime, Date endTime,String status,String tx) {
        return baseMapper.listRecords(page, rolename, orderNo, userName, startTime, endTime,status,tx);
    }
    @Override
    @Transactional
    public void manualReceipt(String id, BigDecimal amount,String operator_username) {
        RechargeBlockchainOrder recharge = getById(id);
        if (recharge == null) {
            throw new YamiShopBindException("参数错误!");
        }
        User party = userService.getById(recharge.getPartyId());
        if (party == null) {
            throw new YamiShopBindException("用户不存在!");
        }
        if (recharge.getSucceeded() == 1) {
            throw new YamiShopBindException("已操作过了!");
        }
        recharge.setReviewTime(new Date());
        recharge.setSucceeded(1);
        WalletLog walletLog = walletLogService.find(Constants.MONEYLOG_CATEGORY_RECHARGE, recharge.getOrderNo());
        if (amount.doubleValue() != recharge.getVolume()) {
            Log log = new Log();
            log.setCategory(Constants.LOG_CATEGORY_OPERATION);
            log.setExtra(recharge.getOrderNo());
            log.setOperator(operator_username);
            log.setUsername(party.getUserName());
            log.setUserId(recharge.getPartyId());
            log.setCreateTime(new Date());
            log.setLog("管理员修改充值数量，原充值数量["
                    + recharge.getVolume() + "],修改后充值数量[" + amount.doubleValue() + "]。订单号[" + recharge.getOrderNo()+ "]。");

            logService.save(log);
            walletLog.setAmount(amount.doubleValue());
            recharge.setVolume(amount.doubleValue());
        }
        /**
         * 如果是usdt则加入wallet，否则寻找walletExtend里相同币种
         */
        Syspara user_recom_bonus_open = sysparaService.find("user_recom_bonus_open");
            double amount1 = recharge.getVolume();
            Wallet wallet = new Wallet();
            wallet = walletService.saveWalletByPartyId(recharge.getPartyId());
            double amount_before = wallet.getMoney().doubleValue();
            walletService.update(wallet.getUserId(), amount1);
            // 保存资金日志
            MoneyLog moneyLog = new MoneyLog();
            moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
            moneyLog.setAmountBefore(new BigDecimal(amount_before));
            moneyLog.setAmount(new BigDecimal(amount1));
            moneyLog.setAmountAfter(BigDecimal.valueOf(Arith.add(amount_before, amount1)));
        if(recharge.getOrderType().equals("1")){
            moneyLog.setLog("银行卡充值，币种[" + recharge.getSymbol() + "]，订单号[" + recharge.getOrderNo() + "]");
            moneyLog.setTitle("Bank card recharge");//银行卡充值
            moneyLog.setConf("recharge");//充值
        }else {
            moneyLog.setLog("UPi充值，币种[" + recharge.getSymbol() + "]，订单号[" + recharge.getOrderNo() + "]");
            moneyLog.setTitle("UPI recharge");//UPi充值
            moneyLog.setConf("recharge");
        }
            moneyLog.setUserId(recharge.getPartyId());
            moneyLog.setWalletType(Constants.WALLET);
            moneyLog.setContentType(Constants.MONEYLOG_CONTENT_RECHARGE);
            moneyLog.setCreateTime(new Date());
            moneyLogService.save(moneyLog);
            walletLog.setStatus(recharge.getSucceeded());
            walletLogService.updateById(walletLog);
            updateById(recharge);
            /**
             * 给他的代理添加充值记录
             */
            userDataService.saveRechargeHandle(recharge.getPartyId(), recharge.getVolume(), recharge.getSymbol());
            /**
             * 若已开启充值奖励 ，则充值到账后给他的代理用户添加奖金
             */
            if ("true".equals(user_recom_bonus_open.getSvalue())) {
                List<RechargeBlockchainOrder> orders =findByPartyIdAndToday(recharge.getPartyId());
                rechargeBonusService.saveBounsHandle(recharge, 1,orders);
            }
            // 充值到账后给他增加提现流水限制金额 充值到账后，当前流水大于提现限制流水时是否重置提现限制流水并将Party表里的当前流水设置清零，
            // 1不重置，2重置
            String recharge_sucess_reset_withdraw = this.sysparaService.find("recharge_sucess_reset_withdraw").getSvalue();
            if ("1".equals(recharge_sucess_reset_withdraw)) {
                if (party.getWithdrawLimitAmount()==null){
                    party.setWithdrawLimitAmount(new BigDecimal(0));
                }
                if (party.getWithdrawLimitNowAmount()==null){
                    party.setWithdrawLimitNowAmount(new BigDecimal(0));
                }
                party.setWithdrawLimitAmount(new BigDecimal(Arith.add(party.getWithdrawLimitAmount().doubleValue(), amount.doubleValue())));
                if (party.getWithdrawLimitNowAmount().doubleValue() > party.getWithdrawLimitAmount().doubleValue()) {
                    party.setWithdrawLimitNowAmount(new BigDecimal(0));
                }
            }
            if ("2".equals(recharge_sucess_reset_withdraw)) {
                double withdraw_limit_turnover_percent = Double
                        .valueOf(sysparaService.find("withdraw_limit_turnover_percent").getSvalue());
                double party_withdraw = Arith.mul(party.getWithdrawLimitAmount().doubleValue(), withdraw_limit_turnover_percent);
                if (party.getWithdrawLimitNowAmount().doubleValue() >= party_withdraw) {
                    party.setWithdrawLimitAmount(amount);
                    party.setWithdrawLimitNowAmount(new BigDecimal(0));
                } else {
                    party.setWithdrawLimitAmount(new BigDecimal(Arith.add(party.getWithdrawLimitAmount().doubleValue(), amount.doubleValue())));
                }
            }
            userService.updateById(party);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setExtra(recharge.getOrderNo());
        log.setOperator(operator_username);
        log.setUsername(party.getUserName());
        log.setUserId(recharge.getPartyId());
        log.setCreateTime(new Date());
        log.setLog("手动到账一笔充值订单。订单号[" + recharge.getOrderNo() + "]。");
        logService.save(log);
        tipService.deleteTip(recharge.getUuid().toString());

        C2cOrder c2cOrder = c2cOrderService.get(recharge.getOrderNo());
        c2cOrder.setState("3");
        c2cOrderService.updateById(c2cOrder);
    }

    @Override
    public long waitCount() {
        return  count(Wrappers.<RechargeBlockchainOrder>query().lambda().eq(RechargeBlockchainOrder::getSucceeded,0));
    }

    @Override
    public List<RechargeBlockchainOrder> findByPartyIdAndSucceeded(Serializable partyId, int succeeded) {
        List<RechargeBlockchainOrder> list =list(Wrappers.<RechargeBlockchainOrder>query().lambda().eq(RechargeBlockchainOrder::getPartyId,partyId).eq(RechargeBlockchainOrder::getSucceeded,succeeded));
        if (list.size() > 0) {
            return list;
        }
        return null;
    }

    @Override
    public RechargeBlockchainOrder findByOrderNo(String order_no) {
        List<RechargeBlockchainOrder> list= list(Wrappers.<RechargeBlockchainOrder>query().lambda().eq(RechargeBlockchainOrder::getOrderNo,order_no));
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<RechargeBlockchainOrder> findByPartyIdAndToday(String partyId) {
        Date now =new Date();
        List<RechargeBlockchainOrder> list= list(Wrappers.<RechargeBlockchainOrder>query().lambda().eq(RechargeBlockchainOrder::getPartyId,partyId)
                .between(RechargeBlockchainOrder::getCreated,DateUtil.minDate(now),DateUtil.maxDate(now)));

        if (list.size() > 0) {
            return list;
        }
        return null;
    }
    /*@Override
    @Transactional
    public void saveOrders(RechargeBlockchainOrder recharge) {
        recharge.setCreated(new Date());
        log.info(new Gson().toJson(recharge));

        if ("".equals(recharge.getOrderNo()) || recharge.getOrderNo() == null) {
          //  log.info(recharge.getOrderNo() +"==============================================="+recharge_limit_min);

            recharge.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        }

        save(recharge);

        // 保存资金日志
        WalletLog walletLog = new WalletLog();
        walletLog.setCategory(Constants.MONEYLOG_CATEGORY_RECHARGE);
        walletLog.setPartyId(recharge.getPartyId());
        walletLog.setOrderNo(recharge.getOrderNo());
        walletLog.setStatus(recharge.getSucceeded());

        walletLog.setAmount(recharge.getVolume());
        walletLog.setWallettype(recharge.getSymbol());
        walletLog.setCreateTime(new Date());
        walletLogService.save(walletLog);
        tipService.saveTip(recharge.getUuid(), TipConstants.RECHARGE_BLOCKCHAIN);
    }*/

    @Override
    @Transactional
    public void saveOrder(RechargeBlockchainOrder recharge) {
        // 充值申请中的订单是否只能唯一：1唯一，2不限制
        double recharge_only_one = Double.valueOf(sysparaService.find("recharge_only_one").getSvalue());
        // 用户未完成USDT订单
        List<RechargeBlockchainOrder> orders = this.findByPartyIdAndSucceeded(recharge.getPartyId(), 0);
        if (null != orders && 1 == recharge_only_one) {
            throw new YamiShopBindException("提交失败，当前有未处理USDT订单");
        }
        /*if (!"ETH".equals(recharge.getSymbol().toUpperCase())) {
            ChannelBlockchain channel = channelBlockchainService.findByNameAndCoinAndAdd(recharge.getBlockchainName(),
                    recharge.getSymbol(), recharge.getChannelAddress());
            if (channel == null || !recharge.getSymbol().toUpperCase().equals(channel.getCoin().toUpperCase())) {
                throw new YamiShopBindException("充值链错误");
            }
        }*/
        double recharge_limit_min = Double.valueOf(sysparaService.find("recharge_limit_min").getSvalue());
        recharge.setCreated(new Date());
        log.info(new Gson().toJson(recharge));
        if ("".equals(recharge.getOrderNo()) || recharge.getOrderNo() == null) {
            log.info(recharge.getOrderNo() +"==============================================="+recharge_limit_min);
            recharge.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        }
        save(recharge);
        // 保存资金日志
        WalletLog walletLog = new WalletLog();
        walletLog.setCategory(Constants.MONEYLOG_CATEGORY_RECHARGE);
        walletLog.setPartyId(recharge.getPartyId());
        walletLog.setOrderNo(recharge.getOrderNo());
        walletLog.setStatus(recharge.getSucceeded());
        walletLog.setAmount(recharge.getVolume());
        walletLog.setWallettype(recharge.getSymbol());
        walletLog.setCreateTime(new Date());
        walletLogService.save(walletLog);

        tipService.saveTip(recharge.getUuid(), TipConstants.RECHARGE_BLOCKCHAIN);
    }

    /**
     * 驳回申请
     * @param id
     * @param content
     */
    @Override
    @Transactional
    public void refusalApply(String id, String content, String userName) {
        RechargeBlockchainOrder recharge = getById(id);
        if (recharge == null) {
            throw new YamiShopBindException("参数错误!");
        }
        // 通过后不可驳回
        if (recharge.getSucceeded() == 2 || recharge.getSucceeded() == 1) {
            return ;
        }
        Date date = new Date();
        recharge.setReviewTime(date);
        recharge.setSucceeded(2);
        recharge.setDescription(content);
        updateById(recharge);
        WalletLog walletLog = walletLogService.find(Constants.MONEYLOG_CATEGORY_RECHARGE, recharge.getOrderNo());
        walletLog.setStatus(recharge.getSucceeded());
        walletLogService.updateById(walletLog);
        User sec = userService. getById(recharge.getPartyId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setExtra(recharge.getOrderNo());
        log.setUsername(sec.getUserName());
        log.setOperator(userName);
        log.setUserId(recharge.getPartyId());
        log.setLog("管理员驳回一笔充值订单。充值订单号[" + recharge.getOrderNo() + "]，驳回理由[" + recharge.getDescription() + "]。");
        logService.save(log);
        tipService.deleteTip(id);
        C2cOrder c2cOrder = c2cOrderService.get(recharge.getOrderNo());
        c2cOrder.setState("4");
        c2cOrderService.updateById(c2cOrder);
    }
}
