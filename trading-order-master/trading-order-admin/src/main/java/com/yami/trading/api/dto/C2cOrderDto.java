package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel
public class C2cOrderDto {

    /**
     * 订单号
     */
    @ApiModelProperty("订单号")
    private String orderNo;


    /**
     * 币种
     */

    @ApiModelProperty("币种")
    private String currency;

    /**
     * 数量
     */
    @ApiModelProperty("数量")
    private BigDecimal amount;



    /**
     * 付款凭证（上传支付详情截图))
     */
    @ApiModelProperty("付款凭证（上传支付详情截图))")
    private String  paymentVoucherImg;


    /**
     * 订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时
     */
    @ApiModelProperty("0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时")
    private int status;
    /**
     * 国家/地区
     */

    @ApiModelProperty("国家/地区")
    private  String country;
    /**
     * 国家/地区代码
     */
    @ApiModelProperty("国家/地区代码")
    private  String countryCode;
    /**
     *  1 充值  2提现
     */
    @ApiModelProperty(" 1 充值  2提现")
    private int orderType;
    /***
     *
     */

    @ApiModelProperty("原因")
    private  String reason;

    /**
     * 处理时间
     */
    @ApiModelProperty("处理时间")
    private Date handleTime;

    /**
     * 真实姓名
     */
    @ApiModelProperty("真实姓名")
    private String realName;
    /**
     * 账号
     */

    @ApiModelProperty("账号")
    private String account;



    @ApiModelProperty("手续费，USDT。")
    private BigDecimal amountFee;

}
