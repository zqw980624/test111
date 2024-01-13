package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel
public class WithdrawDto  implements Serializable {

    private  String id;

    @ApiModelProperty("用户名 ")
    private String userName;
    private String names;
    @ApiModelProperty("uid ")
    private  String userCode;

    @ApiModelProperty("账号类型 ")
    private  String  rolename;

    @ApiModelProperty("推荐人")
    private  String userNameParent;

    /**
     * 订单号
     */

    @ApiModelProperty("订单号")
    private String orderNo;
    /**
     * 提现数量
     */
    @ApiModelProperty("提现数量")
    private BigDecimal amount;

    /**
     * 到账数量
     */
    @ApiModelProperty("到账数量")
    private BigDecimal volume;

    /**
     * 订单手续费，USDT。
     */
    @ApiModelProperty("订单手续费")
    private BigDecimal amountFee;

    /**
     *  状态 0 待审核 1 成功 2 失败
     */
    @ApiModelProperty("状态 0 处理中 1 已处理 2 驳回，")
    private int status = 0;

    /**
     * 提现货币 CNY USD
     */

    @ApiModelProperty("提现货币 CNY USD")
    private String currency;

    /**
     * 错误信息
     */
    @ApiModelProperty("驳回原因")
    private String failureMsg;


    /**
     * 审核操作时间
     */
    @ApiModelProperty("审核操作时间")
    private Date reviewTime;


    @ApiModelProperty("创建时间")
    private Date createTime;



    /**
     * 收款方式 bank 银行卡 alipay 支付宝 weixin 微信 paypal PayPal western 西联汇款 swift
     * SWIFT国际汇款,USDT
     *
     */
    @ApiModelProperty("提现币链")
    private String method;

    private String bank;
    /**
     * 二维码
     */
    @ApiModelProperty("二维码")
    private String qdcode;

    /**
     * USDT地址
     */
    @ApiModelProperty("USDT地址")
    private String address;


    @ApiModelProperty("hash")
    private  String tx;

    private  String remarks;

    private  String account;
}
