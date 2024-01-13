package com.yami.trading.service.c2c.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.c2c.C2cAdvert;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.c2c.C2cUser;
import com.yami.trading.bean.constans.WalletConstants;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.model.*;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.*;
import com.yami.trading.dao.c2c.C2cOrderMapper;
import com.yami.trading.dao.c2c.UserBankMapper;
import com.yami.trading.service.*;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.c2c.C2cPaymentMethodService;
import com.yami.trading.service.c2c.C2cUserService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.impl.WalletServiceImpl;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.user.WalletLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.yami.trading.dao.user.WalletMapper;
@Service
@Slf4j
public class C2cOrderServiceImpl extends ServiceImpl<C2cOrderMapper, C2cOrder> implements C2cOrderService {

    @Autowired
    WalletService walletService;

    @Autowired
    C2cPaymentMethodService c2cPaymentMethodService;

    @Autowired
    RechargeBlockchainOrderService rechargeBlockchainOrderService;

    @Autowired
    SysparaService sysparaService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MoneyLogService moneyLogService;

    @Autowired
    TipService tipService;

    @Autowired
    UserService userService;

    @Autowired
    WalletLogService walletLogService;

    @Autowired
    C2cAdvertService c2cAdvertService;

    @Autowired
    LogService logService;

    @Autowired
    C2cUserService c2cUserService;

    @Autowired
    UserDataService userDataService;
    @Autowired
    UserBankMapper userBankMapper;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @Autowired
    ItemService itemService;

    @Autowired
    DataService dataService;

    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    HighLevelAuthRecordService highLevelAuthRecordService;
    @Autowired
    WalletMapper walletMapper;

    @Override
    public long getTodayCancelOrderCount(String userId) {

        return count(Wrappers.<C2cOrder>query().lambda().eq(C2cOrder::getPartyId, userId).eq(C2cOrder::getState, 4));
    }

    @Override
    /*
     * 查询未完结订单数量，根据广告ID
     */
    public Long findNoEndingOrdersCountByAdvertId(String c2cAdvertId) {
        return count(Wrappers.<C2cOrder>query().lambda().eq(C2cOrder::getC2cAdvertId, c2cAdvertId).in(C2cOrder::getState, "0", "1", "2", "5"));
    }


    /**
     * 订单放行
     */
    public void saveOrderPass(C2cOrder c2cOrder) {
        DecimalFormat df = new DecimalFormat("#.########");
        C2cAdvert c2cAdvert = this.c2cAdvertService.getById(c2cOrder.getC2cAdvertId());
        if (null == c2cAdvert) {
            throw new YamiShopBindException("广告不存在");
        }
        double symbolClose = c2cAdvert.getSymbolClose();
        if (C2cOrder.DIRECTION_BUY.equals(c2cOrder.getDirection())) {
            // 买币
            // 给用户账户添加相应的币种数量
            double amountBefore = 0d;
            double amountAfter = 0d;
            if ("usdt".equalsIgnoreCase(c2cOrder.getSymbol())) {
                Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
                amountBefore = wallet.getMoney().doubleValue();
                amountAfter = Double.valueOf(df.format(Arith.add(wallet.getMoney().doubleValue(), c2cOrder.getCoinAmount()))).doubleValue();
                this.walletService.update(c2cOrder.getPartyId().toString(), c2cOrder.getCoinAmount());
            } else {
                WalletExtend walletExtend = this.walletService.saveExtendByPara(c2cOrder.getPartyId(), c2cOrder.getSymbol());
                amountBefore = walletExtend.getAmount();
                amountAfter = Double.valueOf(df.format(Arith.add(walletExtend.getAmount(), c2cOrder.getCoinAmount()))).doubleValue();
                this.walletService.updateExtend(c2cOrder.getPartyId().toString(), c2cOrder.getSymbol(), c2cOrder.getCoinAmount());
            }
            // 保存资金日志
            MoneyLog moneyLog = new MoneyLog();
            moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_C2C);
            moneyLog.setAmount_before(new BigDecimal(amountBefore));
            moneyLog.setAmount(new BigDecimal(c2cOrder.getCoinAmount()));
            moneyLog.setAmount_after(new BigDecimal(amountAfter));
            moneyLog.setLog("c2c订单购买放行，订单号[" + c2cOrder.getOrderNo() + "]");
            moneyLog.setTitle("股票购买审核通过");
            moneyLog.setConf("购买");
            moneyLog.setUserId(c2cOrder.getPartyId());
            moneyLog.setWallet_type(c2cOrder.getSymbol());
            moneyLog.setSymbol(c2cOrder.getSymbol());
            moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_OTC_BUY);
            moneyLogService.save(moneyLog);
            // usdt价值=币种数量*行情价
            if ("usdt".equalsIgnoreCase(c2cOrder.getSymbol())) {
                userDataService.saveRechargeHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), c2cOrder.getSymbol());
            } else {
                this.userDataService.saveRechargeHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), c2cOrder.getSymbol());
            }
        } else if (C2cOrder.DIRECTION_SELL.equals(c2cOrder.getDirection())) {
            // 卖币
            if (C2cAdvertLock.add(c2cOrder.getC2cAdvertId())) {
                c2cAdvert.setCoinAmount(Double.valueOf(df.format(Arith.add(c2cAdvert.getCoinAmount(), c2cOrder.getCoinAmount()))).doubleValue());
                c2cAdvert.setDeposit(Double.valueOf(df.format(Arith.add(c2cAdvert.getDeposit(), c2cOrder.getAmountUsdt()))).doubleValue());
                this.c2cAdvertService.updateById(c2cAdvert);
            }
            // usdt价值=币种数量*行情价
            if ("usdt".equalsIgnoreCase(c2cOrder.getSymbol())) {
                this.userDataService.saveWithdrawHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), 0d, "usdt");
            } else {
                this.userDataService.saveWithdrawHandle(c2cOrder.getPartyId(), Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), symbolClose))).doubleValue(), 0d, c2cOrder.getSymbol());
            }
        }
        // 订单完成
        c2cOrder.setState("3");
        c2cOrder.setHandleTime(new Date());
        updateById(c2cOrder);
        this.updateNofinishOrderCount(c2cOrder);
        C2cUser c2cUser = this.c2cUserService.getById(c2cOrder.getC2cUserId());
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        if (C2cOrder.DIRECTION_BUY.equals(c2cOrder.getDirection())) {
            // 买币
            c2cUser.setBuyAmount(c2cUser.getBuyAmount() + c2cOrder.getAmountUsdt());
            c2cUser.setBuySuccessOrders(c2cUser.getBuySuccessOrders() + 1);
        } else if (C2cOrder.DIRECTION_SELL.equals(c2cOrder.getDirection())) {
            // 卖币
            c2cUser.setSellAmount(c2cUser.getSellAmount() + c2cOrder.getAmountUsdt());
            c2cUser.setSellSuccessOrders(c2cUser.getSellSuccessOrders() + 1);
        }
        c2cUser.setTotalAmount(c2cUser.getTotalAmount() + c2cOrder.getAmountUsdt());
        c2cUser.setTotalSuccessOrders(c2cUser.getTotalSuccessOrders() + 1);
        if (C2cOrder.DIRECTION_SELL.equals(c2cOrder.getDirection())) {
            // 卖币
            // 用户出售承兑商付款 相当于给承兑商账户充值了保证金
            c2cUser.setDepositOpen(Double.valueOf(df.format(Arith.add(c2cUser.getDepositOpen(), c2cOrder.getAmountUsdt()))).doubleValue());
        }
        this.c2cUserService.updateById(c2cUser);
        this.tipService.deleteTip(c2cOrder.getUuid().toString());
        //this.c2cSendMessageByState(c2cOrder, "3");
    }

    @Override
    public void savePass(C2cOrder c2cOrder, String safeword, String operator_username) {
        if ("3".equals(c2cOrder.getState())) {
            throw new YamiShopBindException("订单已完成，无法放行");
        }
        if ("4".equals(c2cOrder.getState())) {
            throw new YamiShopBindException("订单已取消，无法放行");
        }
        saveOrderPass(c2cOrder);
        User order_user = userService.getById(c2cOrder.getPartyId());
        this.saveLog(order_user.getUserName(), operator_username, "订单放行", c2cOrder.getPartyId().toString());
    }

    @Override
    public Map<String, Object> detail(C2cOrder order) {

        Map<String, String> pmtMap = this.c2cAdvertService
                .getC2cSyspara("c2c_payment_method_type");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", order.getUuid());
        result.put("party_id", order.getPartyId());
        result.put("c2c_user_id", order.getC2cUserId());
        result.put("c2c_advert_id", order.getC2cAdvertId());
        result.put("payment_method_id", order.getPaymentMethodId());
        result.put("order_type", order.getOrderType());
        result.put("order_no", order.getOrderNo());
        result.put("state", order.getState());
        result.put("c2c_user_type", order.getC2cUserType());
        result.put("c2c_user_code", order.getC2cUserCode());
        result.put("c2c_user_nick_name", order.getC2cUserNickName());
        result.put("c2c_user_party_id", order.getC2cUserPartyId());
        result.put("c2c_user_party_code", order.getC2cUserPartyCode());
        result.put("c2c_user_party_name", order.getC2cUserPartyName());
        result.put("direction", order.getDirection());
        result.put("currency", order.getCurrency());
        result.put("symbol", order.getSymbol().toUpperCase());
        result.put("pay_rate", order.getPayRate());
        result.put("symbol_value", order.getSymbolValue());
        result.put("coin_amount", order.getCoinAmount());
        result.put("expire_time", order.getExpireTime());
        result.put("amount", order.getAmount());
        result.put("method_type", order.getMethodType());
        String methodType = String.valueOf(order.getMethodType());
        result.put("method_type_name", pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
        result.put("method_name", order.getMethodName());
        result.put("method_img", awsS3OSSFileService.getUrl(order.getMethodImg()));
        result.put("real_name", order.getRealName());
        result.put("param_name1", order.getParamName1());
        result.put("param_value1", order.getParamValue1());
        result.put("param_name2", order.getParamName2());
        result.put("param_value2", order.getParamValue2());
        result.put("param_name3", order.getParamName3());
        result.put("param_value3", order.getParamValue3());
        result.put("param_name4", order.getParamName4());
        result.put("param_value4", order.getParamValue4());
        result.put("param_name5", order.getParamName5());
        result.put("param_value5", order.getParamValue5());
        result.put("param_name6", order.getParamName6());
        result.put("param_value6", order.getParamValue6());
        result.put("param_name7", order.getParamName7());
        result.put("param_value7", order.getParamValue7());
        result.put("param_name8", order.getParamName8());
        result.put("param_value8", order.getParamValue8());
        result.put("param_name9", order.getParamName9());
        result.put("param_value9", order.getParamValue9());
        result.put("param_name10", order.getParamName10());
        result.put("param_value10", order.getParamValue10());
        result.put("param_name11", order.getParamName11());
        result.put("param_value11", order.getParamValue11());
        result.put("param_name12", order.getParamName12());
        result.put("param_value12", order.getParamValue12());
        result.put("param_name13", order.getParamName13());
        result.put("param_value13", order.getParamValue13());
        result.put("param_name14", order.getParamName14());
        result.put("param_value14", order.getParamValue14());
        result.put("param_name15", order.getParamName15());
        result.put("param_value15", order.getParamValue15());
        result.put("qrcode", order.getQrcode());
        result.put("remark", order.getRemark());
        result.put("img", awsS3OSSFileService.getUrl(order.getImg()));

        result.put("create_time", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("handle_time", DateUtils.format(order.getHandleTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("close_time", DateUtils.format(order.getCloseTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("pay_time", DateUtils.format(order.getPayTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("cancel_time", DateUtils.format(order.getCancelTime(), DateUtils.DF_yyyyMMddHHmmss));
        return result;
    }

    /**
     * 订单取消
     *
     * @param role: user/用户；manager/承兑商管理员、root和admin；timer/订单自动取消定时器；
     */
    public void saveOrderCancel(C2cOrder c2cOrder, String role) {

        DecimalFormat df = new DecimalFormat("#.########");
        if ("4".equals(c2cOrder.getState())) {
            throw new YamiShopBindException("该订单已取消");
        }
        if ("3".equals(c2cOrder.getState())) {
            throw new YamiShopBindException("该订单已完成");
        }
        if ("recharge".equals(c2cOrder.getDirection())) {
            // 充值
        } else if ("withdraw".equals(c2cOrder.getDirection())) {
            // 提现
            // 用户钱包退还
            Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
            double amountBefore = wallet.getMoney().doubleValue();
            double amountAfter = Double.valueOf(df.format(Arith.add(wallet.getMoney().doubleValue(), c2cOrder.getCoinAmount()))).doubleValue();
            this.walletService.update(c2cOrder.getPartyId().toString(), c2cOrder.getCoinAmount());
            // 保存 资金日志
           /* MoneyLog moneyLog = new MoneyLog();
            moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD);
            moneyLog.setAmountBefore(new BigDecimal(amountBefore));
            moneyLog.setAmount(new BigDecimal(c2cOrder.getCoinAmount()));
            moneyLog.setAmountAfter(new BigDecimal(amountAfter));
            moneyLog.setLog("银行卡订单取消，订单号[" + c2cOrder.getOrderNo() + "]");
            moneyLog.setTitle("银行卡提现订单取消");//银行卡提现订单取消
            moneyLog.setConf("提现订单取消");//提现订单取消
            moneyLog.setUserId(c2cOrder.getPartyId());
            moneyLog.setWalletType(c2cOrder.getSymbol());
            moneyLog.setSymbol(c2cOrder.getSymbol());
            moneyLog.setContentType(Constants.MONEYLOG_CONTENT_BANK_CARD_ORDER_CANCEL);
            this.moneyLogService.save(moneyLog);*/
            // 保存 充提记录
            WalletLog walletLog = new WalletLog();
            walletLog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD_WITHDRAW);
            walletLog.setPartyId(c2cOrder.getPartyId());
            walletLog.setOrderNo(c2cOrder.getOrderNo());
            walletLog.setStatus(Integer.valueOf(c2cOrder.getState()).intValue());
            walletLog.setAmount(c2cOrder.getCoinAmount());
            walletLog.setWallettype(c2cOrder.getSymbol());
           // moneyLog.setSymbol(c2cOrder.getSymbol());
            this.walletLogService.save(walletLog);
        }
        c2cOrder.setState("4");
        c2cOrder.setCancelTime(new Date());
        updateById(c2cOrder);
        this.updateNofinishOrderCount(c2cOrder);
        this.tipService.deleteTip(c2cOrder.getUuid().toString());
        // 取消次数加1
        String partyIdCancel = "";
        if ("user".equals(role)) {
            partyIdCancel = c2cOrder.getPartyId();
        }
        if (!"".equals(partyIdCancel)) {
            Map<String, Integer> map = (Map<String, Integer>) redisTemplate.opsForValue().get(RedisKeys.C2C_ORDER_CANCEL_DAY_TIMES);
            if (null == map) {
                map = new ConcurrentHashMap<String, Integer>();
            }
            if (null == map.get(partyIdCancel)) {
                map.put(partyIdCancel, 1);
            } else {
                map.put(partyIdCancel, map.get(partyIdCancel) + 1);
            }
            redisTemplate.opsForValue().set(RedisKeys.C2C_ORDER_CANCEL_DAY_TIMES, map);
        }
    }

    @Override
    public void orderCancel(String id, String reason) {

    }

    /*
     * 用户未结束订单数量（0未付款）减1
     */
    public void updateNofinishOrderCount(C2cOrder entity) {

        Map<String, Long> ocMap = (Map<String, Long>) redisTemplate.opsForValue().get(RedisKeys.C2C_NOFINISH_ORDER_COUNT);
        if (null == ocMap) {
            ocMap = new ConcurrentHashMap<String, Long>();
        }
        Long count = ocMap.get(entity.getPartyId());
        if (null == count) {
            ocMap.put(entity.getPartyId(), 0L);
        } else {
            ocMap.put(entity.getPartyId(), count - 1 <= 0 ? 0 : count - 1);
        }
        redisTemplate.opsForValue().set(RedisKeys.C2C_NOFINISH_ORDER_COUNT, ocMap);
    }

    /**
     * 手动放行
     */
    @Override
    @Transactional
    public void manualRelease(C2cOrder c2cOrder, String operator_username) {
        if ("3".equals(c2cOrder.getState())) {
            throw new YamiShopBindException("订单已完成，无法放行");
        }
        if ("4".equals(c2cOrder.getState())) {
            throw new YamiShopBindException("订单已取消，无法放行");
        }
        if ("recharge".equals(c2cOrder.getDirection())) {
            // 充值
            // 给用户账户添加相应的币种数量
            Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
            double amountBefore = wallet.getMoney().doubleValue();//用户余额
            // 保存 资金日志
            MoneyLog moneyLog = new MoneyLog();
            moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD);
            moneyLog.setAmount_before(new BigDecimal(amountBefore));
            moneyLog.setAmount(new BigDecimal(c2cOrder.getCoinAmount()));
            moneyLog.setLog("银行卡订单充值放行，订单号[" + c2cOrder.getOrderNo() + "]");
            moneyLog.setTitle("Bank card recharge");//银行卡订单充值
            moneyLog.setConf("recharge");//充值
            moneyLog.setUserId(c2cOrder.getPartyId());
            moneyLog.setWalletType(c2cOrder.getSymbol());
            moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_BANK_CARD_RECHARGE);
            this.moneyLogService.save(moneyLog);
            // 保存 充提记录
            WalletLog walletLog = new WalletLog();
            walletLog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD_RECHARGE);
            walletLog.setPartyId(c2cOrder.getPartyId());
            walletLog.setOrderNo(c2cOrder.getOrderNo());
            walletLog.setStatus(Integer.valueOf(c2cOrder.getState()).intValue());
            walletLog.setAmount(c2cOrder.getCoinAmount());
            walletLog.setWallettype(c2cOrder.getSymbol());
            walletLog.setCreateTime(new Date());
            this.walletLogService.save(walletLog);
            this.userDataService.saveRechargeHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), c2cOrder.getSymbol());
        } else if ("withdraw".equals(c2cOrder.getDirection())) {
            // 提现
            this.userDataService.saveWithdrawHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), 0d, "usdt");
        }

        // 订单完成
        c2cOrder.setState("3");
        c2cOrder.setHandleTime(new Date());
        updateById(c2cOrder);
        this.updateNofinishOrderCount(c2cOrder);
        this.tipService.deleteTip(c2cOrder.getUuid().toString());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_BANK_CARD_RW);
        log.setUsername(operator_username);
        log.setUserId(c2cOrder.getPartyId());
        log.setOperator(operator_username);
        log.setLog("订单放行");
        log.setCreateTime(new Date());
        logService.save(log);
    }

    @Override
    public Page pagedQuery(long pageNo, long pageSize, String status, String orderNo, String userCode, String rolename,
                           String c2cUserCode, String c2cUserType, String c2cUserPartyCode, List<String> direction, String loginPartyId) {
        Page page = new Page(pageNo, pageSize);
        return baseMapper.pagedC2cQuery(page, status, orderNo, userCode, rolename, c2cUserCode, c2cUserType, c2cUserPartyCode, direction);
    }

    public void saveOpen(C2cOrder c2cOrder, String remark) {
        log.error("saveOpen start:" + remark);
        C2cAdvert c2cAdvert = this.c2cAdvertService.getById(c2cOrder.getC2cAdvertId());
        if (null == c2cAdvert) {
            throw new YamiShopBindException("广告不存在");
        }

        if (1 != c2cAdvert.getOnSale()) {
            throw new YamiShopBindException("广告已下架");
        }

        User party = userService.getById(c2cOrder.getPartyId());
        if (null == party) {
            throw new YamiShopBindException("用户信息不存在");
        }

        C2cUser c2cUser = this.c2cUserService.getById(c2cAdvert.getC2cUserId());
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        User c2cParty = userService.getById(c2cOrder.getC2cUserPartyId());

        if (null == c2cParty) {
            throw new YamiShopBindException("承兑商的用户信息不存在");
        }

        C2cPaymentMethod method = this.c2cPaymentMethodService.get(c2cOrder.getPaymentMethodId());
        if (null == method) {
            throw new YamiShopBindException("支付方式不存在");
        }

        if (C2cAdvert.DIRECTION_SELL.equals(c2cAdvert.getDirection())) {

            if (!party.isWithdrawAuthority()) {
                throw new YamiShopBindException("无权限");
            }

            this.checkSellAuth(c2cOrder.getPartyId());
            this.checkSellNum(c2cOrder.getPartyId());

            if (!method.getPartyId().equals(c2cOrder.getPartyId())) {
                throw new YamiShopBindException("支付方式不匹配该用户");
            }
        } else {

            if (!method.getPartyId().equals(c2cUser.getC2cUserPartyId())) {
                throw new YamiShopBindException("支付方式不匹配该承兑商");
            }
        }
        c2cOrder.setC2cUserType(c2cUser.getC2cUserType());
        c2cOrder.setC2cUserCode(c2cUser.getC2cUserCode());
        c2cOrder.setC2cUserNickName(c2cUser.getNickName());
        c2cOrder.setC2cUserHeadImg(c2cUser.getHeadImg());
        c2cOrder.setC2cUserPartyId(c2cUser.getC2cUserPartyId());
        c2cOrder.setC2cUserPartyCode(c2cParty.getUserCode());
        c2cOrder.setC2cUserPartyName(c2cParty.getUserName());
        c2cOrder.setDirection(c2cAdvert.getDirection());
        c2cOrder.setCurrency(c2cAdvert.getCurrency());
        c2cOrder.setSymbol(c2cAdvert.getSymbol());
        c2cOrder.setPayRate(c2cAdvert.getPayRate());
        c2cOrder.setSymbolValue(c2cAdvert.getSymbolValue());
        c2cOrder.setExpireTime(c2cAdvert.getExpireTime());
        c2cOrder.setMethodType(method.getMethodType());
        c2cOrder.setMethodName(method.getMethodName());
        c2cOrder.setMethodImg(method.getMethodImg());
        c2cOrder.setRealName(method.getRealName());
        c2cOrder.setParamName1(method.getParamName1());
        c2cOrder.setParamValue1(method.getParamValue1());
        c2cOrder.setParamName2(method.getParamName2());
        c2cOrder.setParamValue2(method.getParamValue2());
        c2cOrder.setParamName3(method.getParamName3());
        c2cOrder.setParamName4(method.getParamName4());
        c2cOrder.setParamValue4(method.getParamValue4());
        c2cOrder.setCreateTime(new Date());
        c2cOrder.setHandleTime(null);
        c2cOrder.setCloseTime(DateUtils.addMinute(c2cOrder.getCreateTime(), c2cOrder.getExpireTime()));
        c2cOrder.setPayTime(null);
        c2cOrder.setCancelTime(null);

        // 币种单价
        double symbolValue = c2cAdvert.getSymbolValue();
        if (C2cAdvert.DIRECTION_BUY.equals(c2cAdvert.getDirection())) {
            // 买币
            this.saveBuy(c2cOrder, c2cAdvert, symbolValue, remark);
        } else if (C2cAdvert.DIRECTION_SELL.equals(c2cAdvert.getDirection())) {
            // 卖币
            this.saveSell(c2cOrder, c2cAdvert, symbolValue, remark);
        } else {
            throw new YamiShopBindException("买卖方式不正确");
        }

        this.tipService.saveNewTip(c2cOrder.getUuid().toString(), TipConstants.C2C_ORDER, remark);
        //  this.c2cSendMessageByState(c2cOrder, "0");
        log.error("saveOpen end:" + remark);
    }


    /**
     * 检测用户可卖币次数
     */
    public void checkSellNum(String partyId) {
        // 当日提现次数是否超过
        Object obj = this.sysparaService.find("c2c_sell_limit_num");
        if (null != obj) {
            double c2c_sell_limit_num = Double.valueOf(this.sysparaService.find("c2c_sell_limit_num").getSvalue());
            List<C2cOrder> c2cOrders = this.findByPartyIdAndToday(partyId, C2cAdvert.DIRECTION_SELL, null);

            if (c2c_sell_limit_num > 0 && c2cOrders != null) {
                if (c2cOrders.size() >= c2c_sell_limit_num) {
                    throw new BusinessException(1, "当日可提现次数不足");
                }
            }
        }
    }

    /**
     * 检测用户可卖币权限
     */
    public void checkSellAuth(String partyId) {
        RealNameAuthRecord party_kyc = realNameAuthRecordService.getByUserId(partyId);
        Object objKyc = this.sysparaService.find("c2c_sell_by_kyc");
        if (null != objKyc) {
            if (!(party_kyc.getStatus() == 2) && "true".equals(this.sysparaService.find("c2c_sell_by_kyc").getSvalue())) {
                throw new BusinessException(401, "无权限");
            }
        }
        HighLevelAuthRecord party_kycHighLevel = highLevelAuthRecordService.findByUserId(partyId);
        Object objKycHigh = this.sysparaService.find("c2c_sell_by_high_kyc");
        if (null != objKycHigh) {
            if (!(party_kycHighLevel.getStatus() == 2) && "true".equals(this.sysparaService.find("c2c_sell_by_high_kyc").getSvalue())) {
                throw new BusinessException(1, "请先通过高级认证");
            }
        }
    }

    private void saveSell(C2cOrder c2cOrder, C2cAdvert c2cAdvert, double symbol_value, String remark) {
        log.error("saveSell start::" + remark);
        DecimalFormat df = new DecimalFormat("#.########");

        if (C2cOrder.ORDER_TYPE_BY_AMOUNT.equals(c2cOrder.getOrderType())) {
            // 按支付金额支付

            // 币种数量 = 支付金额/币种单价
            double coin_amount = Double.valueOf(df.format(Arith.div(c2cOrder.getAmount(), symbol_value)));
            c2cOrder.setCoinAmount(coin_amount);
            c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(coin_amount, c2cAdvert.getSymbolClose()))).doubleValue());
        } else {
            // 按币种数量支付
            c2cOrder.setAmount(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), symbol_value))));
            c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), c2cAdvert.getSymbolClose()))).doubleValue());
        }
        log.error("sell save start::" + remark);
        this.save(c2cOrder);
        log.error("save save end::" + remark);
        // 买入金额需要在区间内
        if (c2cOrder.getAmount() > c2cAdvert.getInvestmentMax() || c2cOrder.getAmount() < c2cAdvert.getInvestmentMin()) {
            throw new YamiShopBindException("金额不在购买区间");
        }

        double amountBefore = 0d;
        double amountAfter = 0d;

        if ("usdt".equalsIgnoreCase(c2cAdvert.getSymbol())) {

            Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
            if (c2cOrder.getCoinAmount() > wallet.getMoney().doubleValue()) {
                throw new YamiShopBindException("用户剩余数量不足");
            }

            amountBefore = wallet.getMoney().doubleValue();
            amountAfter = Double.valueOf(df.format(Arith.sub(wallet.getMoney().doubleValue(), c2cOrder.getCoinAmount()))).doubleValue();
            this.walletService.update(c2cOrder.getPartyId(), Arith.sub(0, c2cOrder.getCoinAmount()));
        } else {

            WalletExtend walletExtend = this.walletService.saveExtendByPara(c2cOrder.getPartyId(), c2cOrder.getSymbol());

            if (c2cOrder.getCoinAmount() > walletExtend.getAmount()) {
                throw new YamiShopBindException("用户剩余数量不足");
            }

            amountBefore = walletExtend.getAmount();
            amountAfter = Double.valueOf(df.format(Arith.sub(walletExtend.getAmount(), c2cOrder.getCoinAmount()))).doubleValue();
            this.walletService.updateExtend(c2cOrder.getPartyId(), c2cOrder.getSymbol(), Arith.sub(0, c2cOrder.getCoinAmount()));
        }

        // 保存资金日志
        MoneyLog moneylog = new MoneyLog();
        moneylog.setCategory(Constants.MONEYLOG_CATEGORY_C2C);
        moneylog.setAmount_before(new BigDecimal(amountBefore));
        moneylog.setAmount(new BigDecimal(Arith.sub(0, c2cOrder.getCoinAmount())));
        moneylog.setAmount_after(new BigDecimal(amountAfter));
        moneylog.setLog("c2c卖币，币种[" + c2cOrder.getSymbol() + "]，订单号[" + c2cOrder.getOrderNo() + "]");
        moneylog.setTitle("卖股票1");
        moneylog.setConf("卖股票1");
        moneylog.setUserId(c2cOrder.getPartyId());
        moneylog.setWalletType(c2cOrder.getSymbol());
        moneylog.setContent_type(Constants.MONEYLOG_CONTENT_C2C_SELL);
        moneyLogService.save(moneylog);

        c2cAdvert.setSortIndex(0);
        this.c2cAdvertService.updateById(c2cAdvert);
        log.error("saveSell end::" + remark);
    }

    private void saveBuy(C2cOrder c2cOrder, C2cAdvert c2cAdvert, double symbol_value, String remark) {
        log.error("saveBuy start:" + remark);
        DecimalFormat df = new DecimalFormat("#.########");

        if (C2cOrder.ORDER_TYPE_BY_AMOUNT.equals(c2cOrder.getOrderType())) {
            // 按支付金额支付

            // 币种数量 = 支付金额/币种单价
            double coin_amount = Double.valueOf(df.format(Arith.div(c2cOrder.getAmount(), symbol_value))).doubleValue();

            if (coin_amount > c2cAdvert.getCoinAmount()) {
                throw new YamiShopBindException("该广告剩余数量不足");
            }

            c2cOrder.setCoinAmount(coin_amount);
            c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(coin_amount, c2cAdvert.getSymbolClose()))).doubleValue());
            c2cAdvert.setCoinAmount(Double.valueOf(df.format(Arith.sub(c2cAdvert.getCoinAmount(), coin_amount))).doubleValue());
            c2cAdvert.setDeposit(Double.valueOf(df.format(Arith.sub(c2cAdvert.getDeposit(), c2cOrder.getAmountUsdt()))).doubleValue());
        } else {
            // 按币种数量支付

            if (c2cOrder.getCoinAmount() > c2cAdvert.getCoinAmount()) {
                throw new YamiShopBindException("该广告剩余数量不足");
            }

            c2cOrder.setAmount(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), symbol_value))).doubleValue());
            c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), c2cAdvert.getSymbolClose()))).doubleValue());
            c2cAdvert.setCoinAmount(Double.valueOf(df.format(Arith.sub(c2cAdvert.getCoinAmount(), c2cOrder.getCoinAmount()))).doubleValue());
            c2cAdvert.setDeposit(Double.valueOf(df.format(Arith.sub(c2cAdvert.getDeposit(), c2cOrder.getAmountUsdt()))).doubleValue());
        }
        log.error("buy save start:" + remark);
        this.save(c2cOrder);
        log.error("buy save end:" + remark);
        // 买入金额需要在区间内
        if (c2cOrder.getAmount() > c2cAdvert.getInvestmentMax() || c2cOrder.getAmount() < c2cAdvert.getInvestmentMin()) {
            throw new YamiShopBindException("金额不在购买区间");
        }

        c2cAdvert.setSortIndex(0);
        this.c2cAdvertService.updateById(c2cAdvert);
        log.error("saveBuy end:" + remark);
    }


    @Override
    public Page pagedBankCardOrderQuery(Page page, List<String> direction, String state, String userCode, String roleName, String orderNo) {
        return baseMapper.pagedBankCardOrderQuery(page, direction, state, userCode, roleName,
                orderNo);
    }

    @Override
    public Page pagedQuery(int pageNo, int pageSize, String direction, String state, String loginPartyId) {

        Page page = new Page(pageNo, pageSize);
        baseMapper.pagedQuery(page, direction, state, loginPartyId);
        // 金额默认保留2位
        DecimalFormat df = new DecimalFormat("#.##");
        // 币种默认保留8位
        DecimalFormat dfCoin = new DecimalFormat("#.########");
        for (Map<String, Object> data : (List<Map<String, Object>>) page.getRecords()) {
            data.put("symbol_value", df.format(data.get("symbol_value")));
            data.put("coin_amount", dfCoin.format(data.get("coin_amount")));
            data.put("amount", dfCoin.format(data.get("amount")));
//			if(data.containsKey("symbol")){
//				data.put("symbol", data.get("symbol").toString().toUpperCase());
//			}else{
//				data.put("symbol", "");
//			}
        }
        return page;
    }

    public List<C2cOrder> findByPartyIdAndToday(String partyId, String direction, String state) {

        Date now = new Date();
        if (StringUtils.isEmptyString(state)) {
            List<C2cOrder> list = list(Wrappers.<C2cOrder>query().lambda().eq(C2cOrder::getPartyId, partyId).eq(C2cOrder::getDirection, direction)
                    .between(C2cOrder::getCreateTime, DateUtil.minDate(now), DateUtil.maxDate(now)));
            return list;
        } else {
            List<C2cOrder> list = list(Wrappers.<C2cOrder>query().lambda().eq(C2cOrder::getPartyId, partyId).eq(C2cOrder::getDirection, direction)
                    .between(C2cOrder::getCreateTime, DateUtil.minDate(now), DateUtil.maxDate(now)).eq(C2cOrder::getState, state));
            return list;
        }
    }

    @Override
    public C2cOrder get(String order_no) {
        List<C2cOrder> list = list(Wrappers.<C2cOrder>query().lambda().eq(C2cOrder::getOrderNo, order_no));
        if (list.size() > 0) {
            return (C2cOrder) list.get(0);
        }
        return null;
    }

    public List<C2cPaymentMethod> getOrderPayments(String order_no, boolean c2cOrderFlag) {

        C2cOrder c2cOrder = get(order_no);
        if (null == c2cOrder) {
            throw new YamiShopBindException("订单不存在");
        }
        List<String> typeList = new ArrayList<>();
        if (c2cOrderFlag) {
            C2cAdvert c2cAdvert = c2cAdvertService.getById(c2cOrder.getC2cAdvertId());
            if (null == c2cAdvert) {
                throw new YamiShopBindException("广告不存在");
            }
            String[] types = c2cAdvert.getPayType().split(",");
            typeList = Arrays.asList(types);

        }

        List<C2cPaymentMethod> list = new ArrayList<C2cPaymentMethod>();
        Map<String, C2cPaymentMethod> methodMap = this.c2cPaymentMethodService.getByPartyId(c2cOrder.getPartyId());
        for (String key : methodMap.keySet()) {
            C2cPaymentMethod method = methodMap.get(key);
            if (null != method) {
                list.add(method);
            }
        }
        C2cPaymentMethod cpm = this.c2cPaymentMethodService.get(c2cOrder.getPaymentMethodId());
        List<C2cPaymentMethod> listRet = new ArrayList<C2cPaymentMethod>();
        // 先添加订单记录的支付方式
        for (C2cPaymentMethod pay : list) {
            if (c2cOrder.getPaymentMethodId().equals(pay.getUuid())) {
                listRet.add(pay);
            }
        }
        // 再添加与订单记录的支付方式类型相同的支付方式
        for (C2cPaymentMethod pay : list) {
            if (!c2cOrder.getPaymentMethodId().equals(pay.getUuid())) {
                //if (cpm.getMethodConfigId().equals(pay.getMethodConfigId())) {
                    listRet.add(pay);
               // }
            }
        }
        if (!c2cOrderFlag) {
            // 最后添加与广告匹配的支付方式
            for (C2cPaymentMethod pay : list) {
                //if (!c2cOrder.getPaymentMethodId().equals(pay.getUuid()) && !cpm.getMethodConfigId().equals(pay.getMethodConfigId())) {
                   // if (typeList.contains(pay.getMethodConfigId())) {
                        listRet.add(pay);
                 //   }
              //  }
            }
        }
        for (int i = 0; i < listRet.size(); i++) {
            C2cPaymentMethod method = listRet.get(i);
            String methodType = String.valueOf(method.getMethodType());
            Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
            method.setMethodTypeName(pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
        }
        return listRet;
    }

    @Override
    public void saveOrderPay(String order_no, String safeword, String operator_username, String payment_method_id_order_pay) {

        C2cOrder c2cOrder = get(order_no);
        if (null == c2cOrder) {
            throw new YamiShopBindException("订单不存在");
        }
        if (!Arrays.asList("0", "2").contains(c2cOrder.getState())) {
            throw new YamiShopBindException("订单不处于待支付或申诉中状态，无法转账");
        }
        C2cPaymentMethod method = this.c2cPaymentMethodService.get(payment_method_id_order_pay);
        // 更新最终的支付方式
        c2cOrder.setPaymentMethodId(method.getUuid());
        c2cOrder.setMethodType(method.getMethodType());
        c2cOrder.setMethodName(method.getMethodName());
        c2cOrder.setMethodImg(method.getMethodImg());
        c2cOrder.setRealName(method.getRealName());
        c2cOrder.setParamName1(method.getParamName1());
        c2cOrder.setParamValue1(method.getParamValue1());
        c2cOrder.setParamName2(method.getParamName2());
        c2cOrder.setParamValue2(method.getParamValue2());
        c2cOrder.setParamName3(method.getParamName3());
        c2cOrder.setParamName4(method.getParamName4());
        c2cOrder.setParamValue4(method.getParamValue4());
        c2cOrder.setState("1");
        c2cOrder.setPayTime(new Date());
        updateById(c2cOrder);
        // 订单完成
        c2cOrder.setHandleTime(new Date());
        updateById(c2cOrder);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_BANK_CARD_RW);
        log.setUsername(operator_username);
        log.setUserId(c2cOrder.getPartyId());
        log.setOperator(operator_username);
        log.setLog("订单转账完成");
        log.setCreateTime(new Date());
        logService.save(log);

    }
    @Override
    public void saveOrderPayPd(String order_no, String safeword, String operator_username, String payment_method_id_order_pay) {

        C2cOrder c2cOrder = get(order_no);
        if (null == c2cOrder) {
            throw new YamiShopBindException("订单不存在");
        }
        if (!Arrays.asList("0", "2").contains(c2cOrder.getState())) {
            throw new YamiShopBindException("订单不处于待支付或申诉中状态，无法转账");
        }

        Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
        if(wallet.getMoney().compareTo(new BigDecimal(c2cOrder.getAmount()))<=0){
            throw new YamiShopBindException("余额不足");
        }
        BigDecimal subtract =wallet.getMoney().subtract( new BigDecimal(c2cOrder.getAmount()));
        Wallet walls = new Wallet();
        walls.setMoney(subtract);
        walls.setUuid(wallet.getUuid());
        walletMapper.updateWall(walls);
        // 订单完成
        c2cOrder.setState("3");
        c2cOrder.setHandleTime(new Date());
        updateById(c2cOrder);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_BANK_CARD_RW);
        log.setUsername(operator_username);
        log.setUserId(c2cOrder.getPartyId());
        log.setOperator(operator_username);
        log.setLog("订单转账完成");
        log.setCreateTime(new Date());
        logService.save(log);

    }
    public void saveLog(String order_username, String operator, String context, String orderPartyId) {

        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_C2C);
        log.setUsername(order_username);
        log.setUserId(orderPartyId);
        log.setOperator(operator);
        log.setLog(context);
        log.setCreateTime(new Date());
        logService.save(log);
    }

    public void saveOpen(C2cOrder c2cOrder, User party) {

        C2cPaymentMethod method = c2cPaymentMethodService.get(c2cOrder.getPaymentMethodId());
        if (null == method) {
            throw new YamiShopBindException("支付方式不存在");
        }
        if ("withdraw".equals(c2cOrder.getDirection())) {
            String partyId = party.getUserId();
            if (!method.getPartyId().equals(partyId)) {
                throw new YamiShopBindException("支付方式不匹配该用户");
            }
        }
        int expireTime = 45;
        Object obj = this.sysparaService.find("bank_card_expire_time");
        if (null != obj) {
            expireTime = Integer.valueOf(this.sysparaService.find("bank_card_expire_time").getSvalue()).intValue();
        }
        c2cOrder.setExpireTime(expireTime);
        c2cOrder.setMethodType(method.getMethodType());
        c2cOrder.setMethodName(method.getMethodName());
        c2cOrder.setMethodImg(method.getMethodImg());
        c2cOrder.setRealName(method.getRealName());
        c2cOrder.setParamName1(method.getParamName1());
        c2cOrder.setParamValue1(method.getParamValue1());
        c2cOrder.setParamName2(method.getParamName2());
        c2cOrder.setParamValue2(method.getParamValue2());
        c2cOrder.setParamName3(method.getParamName3());
        c2cOrder.setParamName4(method.getParamName4());
        c2cOrder.setParamValue4(method.getParamValue4());
        if ("recharge".equals(c2cOrder.getDirection())) {
            // 充值
            this.saveRecharge(c2cOrder, party);
        } else if ("withdraw".equals(c2cOrder.getDirection())) {
            // 提现
            this.saveWithdraw(c2cOrder);
        } else {
            throw new YamiShopBindException("充值或提现不正确");
        }
    }

    public void saveOpenRecharge(C2cOrder c2cOrder, User party) {
        this.save(c2cOrder);
        // 保存 资金日志
        //Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
        //DecimalFormat df = new DecimalFormat("#.########");
        //double amountBefore = wallet.getMoney().doubleValue();
        //double amountAfter = Double.valueOf(df.format(Arith.sub(wallet.getMoney().doubleValue(), c2cOrder.getCoinAmount()))).doubleValue();
      /*  MoneyLog moneylog = new MoneyLog();
        moneylog.setCategory(Constants.MONEYLOG_CONTENT_RECHARGE);
        moneylog.setAmountBefore(new BigDecimal(amountBefore));
        moneylog.setAmount(new BigDecimal(Arith.sub(0, c2cOrder.getCoinAmount())));
        moneylog.setAmount(new BigDecimal(c2cOrder.getAmount()));
        moneylog.setAmountAfter(new BigDecimal(amountAfter));
        moneylog.setContent_type(Constants.MONEYLOG_CATEGORY_BANK_CARD_RECHARGE);
        if(c2cOrder.getOrderType().equals("1")){
            moneylog.setLog("银行卡充值，币种[" + c2cOrder.getSymbol() + "]，订单号[" + c2cOrder.getOrderNo() + "]");
            moneylog.setTitle("Bank card recharge");//银行卡充值
            moneylog.setConf("recharge");//充值
        }else {
            moneylog.setLog("UPi充值，币种[" + c2cOrder.getSymbol() + "]，订单号[" + c2cOrder.getOrderNo() + "]");
            moneylog.setTitle("UPI recharge");//UPi充值
            moneylog.setConf("recharge");
        }

        moneylog.setUserId(c2cOrder.getPartyId());
        moneylog.setWalletType(c2cOrder.getSymbol());
        moneylog.setSymbol(c2cOrder.getSymbol());
        moneyLogService.save(moneylog);*/

        this.tipService.saveTip(c2cOrder.getUuid().toString(), TipConstants.BANK_CARD_ORDER);

    }

    @Transactional
    @Override
    public void saveOpenWithdraw(C2cOrder c2cOrder, User user) {
        DecimalFormat df = new DecimalFormat("#.########");
        Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
        if (c2cOrder.getCoinAmount() > wallet.getMoney().doubleValue()) {
            throw new YamiShopBindException("余额不足");
        }
        double amountBefore = wallet.getMoney().doubleValue();
        double amountAfter = Double.valueOf(df.format(Arith.sub(wallet.getMoney().doubleValue(), c2cOrder.getCoinAmount()))).doubleValue();
        this.walletService.update(c2cOrder.getPartyId(), Arith.sub(0, c2cOrder.getCoinAmount()));
        this.save(c2cOrder);
        this.tipService.saveTip(c2cOrder.getUuid().toString(), TipConstants.BANK_CARD_ORDER);
        // 保存 资金日志
       /* MoneyLog moneylog = new MoneyLog();
        moneylog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD);
        moneylog.setAmountBefore(new BigDecimal(amountBefore));
        moneylog.setAmount(new BigDecimal(Arith.sub(0, c2cOrder.getAmount())));
        //moneylog.setAmount(new BigDecimal(c2cOrder.getAmount()));
        moneylog.setAmountAfter(new BigDecimal(amountAfter));
        moneylog.setLog("银行卡提现，币种[" + c2cOrder.getSymbol() + "]，订单号[" + c2cOrder.getOrderNo() + "]");
        moneylog.setTitle("银行卡提现");
        moneylog.setConf("提现");
        moneylog.setUserId(c2cOrder.getPartyId());
        moneylog.setWalletType(c2cOrder.getSymbol());
        moneylog.setSymbol(c2cOrder.getSymbol());

        moneylog.setContentType(Constants.MONEYLOG_CONTENT_BANK_CARD_WITHDRAW);
        moneyLogService.save(moneylog);*/
        // 保存 充提记录
        WalletLog walletLog = new WalletLog();
        walletLog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD_WITHDRAW);
        walletLog.setPartyId(c2cOrder.getPartyId());
        walletLog.setOrderNo(c2cOrder.getOrderNo());
        walletLog.setStatus(Integer.valueOf(c2cOrder.getState()).intValue());
        walletLog.setAmount(c2cOrder.getCoinAmount());
        walletLog.setWallettype(c2cOrder.getSymbol());
        //moneylog.setSymbol(c2cOrder.getSymbol());

        walletLogService.save(walletLog);
    }

    private void saveWithdraw(C2cOrder c2cOrder) {

        DecimalFormat df = new DecimalFormat("#.########");
        Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
        if (c2cOrder.getCoinAmount() > wallet.getMoney().doubleValue()) {
            throw new YamiShopBindException("余额不足");
        }
        double amountBefore = wallet.getMoney().doubleValue();
        double amountAfter = Double.valueOf(df.format(Arith.sub(wallet.getMoney().doubleValue(), c2cOrder.getCoinAmount()))).doubleValue();
        this.walletService.update(c2cOrder.getPartyId(), Arith.sub(0, c2cOrder.getCoinAmount()));
        this.save(c2cOrder);
        this.tipService.saveTip(c2cOrder.getUuid().toString(), TipConstants.BANK_CARD_ORDER);
        // 保存 资金日志
        MoneyLog moneylog = new MoneyLog();
        moneylog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD);
        moneylog.setAmountBefore(new BigDecimal(amountBefore));
        moneylog.setAmount(new BigDecimal(Arith.sub(0, c2cOrder.getCoinAmount())));
        moneylog.setAmountAfter(new BigDecimal(amountAfter));
        moneylog.setLog("银行卡提现，币种[" + c2cOrder.getSymbol() + "]，订单号[" + c2cOrder.getOrderNo() + "]");
        moneylog.setTitle("银行卡提现");
        moneylog.setConf("提现");
        moneylog.setUserId(c2cOrder.getPartyId());
        moneylog.setWalletType(c2cOrder.getSymbol());
        moneylog.setSymbol(c2cOrder.getSymbol());

        moneylog.setContentType(Constants.MONEYLOG_CONTENT_BANK_CARD_WITHDRAW);
        moneyLogService.save(moneylog);
        // 保存 充提记录
        WalletLog walletLog = new WalletLog();
        walletLog.setCategory(Constants.MONEYLOG_CATEGORY_BANK_CARD_WITHDRAW);
        walletLog.setPartyId(c2cOrder.getPartyId());
        walletLog.setOrderNo(c2cOrder.getOrderNo());
        walletLog.setStatus(Integer.valueOf(c2cOrder.getState()).intValue());
        walletLog.setAmount(c2cOrder.getCoinAmount());
        walletLog.setWallettype(c2cOrder.getSymbol());
        moneylog.setSymbol(c2cOrder.getSymbol());

        walletLogService.save(walletLog);
    }

    private void saveRecharge(C2cOrder c2cOrder, User party) {
        // 充值申请中的订单是否只能唯一：1唯一，2不限制
        double recharge_only_one = Double.valueOf(this.sysparaService.find("recharge_only_one").getSvalue());
        // 用户未完成USDT订单
        List<RechargeBlockchainOrder> orders = rechargeBlockchainOrderService.findByPartyIdAndSucceeded(party.getUserId(), 0);
        if (null != orders && 1 == recharge_only_one) {
            throw new YamiShopBindException("提交失败，当前有未处理USDT订单");
        }
        // 用户未结束银行卡订单数量
        Long nofinishOrderCount = this.getNofinishOrderCount(c2cOrder.getPartyId());
        if (null != nofinishOrderCount && 0 != nofinishOrderCount.longValue() && 1 == recharge_only_one) {
            throw new YamiShopBindException("提交失败，当前有未处理银行卡订单");
        }
        double recharge_limit_min = Double.valueOf(this.sysparaService.find("recharge_limit_min").getSvalue());
        double recharge_limit_max = Double.valueOf(this.sysparaService.find("recharge_limit_max").getSvalue());
        if (c2cOrder.getCoinAmount() < recharge_limit_min) {
            log.info(c2cOrder.getCoinAmount() + "====================================" + recharge_limit_min);
            throw new YamiShopBindException("充值数量不得小于最小限额");
        }
        if (c2cOrder.getCoinAmount() > recharge_limit_max) {
            throw new YamiShopBindException("充值数量不得大于最大限额");
        }
        this.save(c2cOrder);
        this.tipService.saveTip(c2cOrder.getUuid().toString(), TipConstants.BANK_CARD_ORDER);
    }


    public List<C2cOrder> getByPayId(String payId) {
        return list(Wrappers.<C2cOrder>query().lambda().eq(C2cOrder::getPaymentMethodId, payId).in(C2cOrder::getState, 0, 1));
    }


    /*
     * 获取 用户未结束订单数量
     */
    @Override
    public Long getNofinishOrderCount(String partyId) {

        Map<String, Long> ocMap = (Map<String, Long>) redisTemplate.opsForValue().get(RedisKeys.C2C_NOFINISH_ORDER_COUNT);
        if (null == ocMap) {
            return 0L;
        }
        Long count = ocMap.get(partyId);
        if (null == count) {
            return 0L;
        } else {
            return count;
        }
    }

}
