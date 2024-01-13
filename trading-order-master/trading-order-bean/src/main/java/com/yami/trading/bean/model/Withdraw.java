package com.yami.trading.bean.model;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("tz_withdraw_order")
public class Withdraw  extends UUIDEntity {


    private  Date createTime;

    private String userId;
    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 订单总金额（必须大于 0），单位为对应币种的最小货币单位，人民币为元。
     */
    private BigDecimal amount;

    /**
     * 订单总金额（必须大于 0），单位为对应币种的最小货币单位，人民币为元。
     */
    private BigDecimal volume;

    /**
     * 订单手续费，USDT。
     */
    private BigDecimal amountFee;

    /**
     * 状态 0 初始状态，未知 1 成功 2 失败，
     */
    private int status = 0;

    /**
     * 提现货币 CNY USD
     */
    private String currency;

    /**
     * 错误信息
     */
    private String failureMsg;


    /**
     * 审核操作时间
     */
    private Date reviewTime;

    private Date timeSettle;

    /**
     * 收款方式 bank 银行卡 alipay 支付宝 weixin 微信 paypal PayPal western 西联汇款 swift
     * SWIFT国际汇款,USDT
     *
     */
    private String method;

    /**
     * 银行卡号
     */
    private String account;
    /**
     * 二维码
     */
    private String qdcode;

    /**
     * 姓名
     */
    private String username;
    /**
     * 真实姓名
     */
    private String names;

    /*
     * 以下是银行卡专用
     */
    /**
     * 银行名称
     */
    private String bank;
    /**
     * 开户行
     */
    private String deposit_bank;

    /**
     * 地址
     */
    private String address;

    /**
     * hash值
     */
    private String tx;
    private String remarks;


}
