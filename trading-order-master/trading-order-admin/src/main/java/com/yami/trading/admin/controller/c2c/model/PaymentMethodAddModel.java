package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class PaymentMethodAddModel {

    @ApiModelProperty("用户UID")
    private  String userCode;

    @ApiModelProperty("支付方式模板")
    //@NotBlank
    private  String methodConfigId;


    @ApiModelProperty("真实姓名")
    private  String realName;

    private  String methodName;
    private  String paramName1;
    private  String paramName2;
    private  String paramName3;
    private  String paramName4;
    private  String paramValue1;
    private  String paramValue2;
    private  String paramValue3;
    private  String paramValue4;
    private  String paramValue5;
    private  String paramValue6;
    private  String paramValue7;
    private  String paramValue8;
    private  String paramValue9;
    private  String paramValue10;
    private  String paramValue11;
    private  String paramValue12;
    private  String paramValue13;
    private  String paramValue14;
    private  String paramValue15;

    @ApiModelProperty("支付二维码")

    private  String qrcode;

    @ApiModelProperty("备注")
    private  String remark;

    @ApiModelProperty("资金密码")
   // @NotBlank
    private  String loginSafeword;

}
