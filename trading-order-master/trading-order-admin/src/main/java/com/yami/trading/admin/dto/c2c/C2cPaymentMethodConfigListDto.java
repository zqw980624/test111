package com.yami.trading.admin.dto.c2c;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel
public class C2cPaymentMethodConfigListDto implements Serializable {

    private  String uuid;

    /**
     * 支付方式类型：0其它/1银行卡/2虚拟货币/3微信/4支付宝/5PayPal/6西联汇款/7SWIFT国际汇款
     */

    @ApiModelProperty("* 支付方式类型：0其它/1银行卡/2虚拟货币/3微信/4支付宝/5PayPal/6西联汇款/7SWIFT国际汇款")
    private String methodType;


    @ApiModelProperty("* 支付方式类型名称")
    private String methodTypeName;


    /**
     * 支付方式名称
     */
    @ApiModelProperty("支付方式名称")
    private String methodName;

    /**
     * 支付方式图片
     */

    @ApiModelProperty("支付方式图片")
    private String methodImg;

    private String methodImgUrl;


    /**
     * 参数名1（配关键数据：如微信账号、支付宝账号、银行卡号、虚拟货币地址等等）
     */
    @ApiModelProperty("参数名1（配关键数据：如微信账号、支付宝账号、银行卡号、虚拟货币地址等等）")
    private String paramName1;

    /**
     * 参数名2
     */
    private String paramName2;

    /**
     * 参数名3
     */
    private String paramName3;

    /**
     * 参数名4
     */
    private String paramName4;

    /**
     * 参数名5
     */
    private String paramName5;

    /**
     * 参数名6
     */
    private String paramName6;

    /**
     * 参数名7
     */
    private String paramName7;

    /**
     * 参数名8
     */
    private String paramName8;

    /**
     * 参数名9
     */
    private String paramName9;

    /**
     * 参数名10
     */
    private String paramName10;

    /**
     * 参数名11
     */
    private String paramName11;

    /**
     * 参数名12
     */
    private String paramName12;

    /**
     * 参数名13
     */
    private String paramName13;

    /**
     * 参数名14
     */
    private String paramName14;

    /**
     * 参数名15
     */
    private String paramName15;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
