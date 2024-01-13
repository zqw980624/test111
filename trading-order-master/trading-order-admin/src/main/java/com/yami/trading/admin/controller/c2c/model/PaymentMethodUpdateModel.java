package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class PaymentMethodUpdateModel {

    private  String id;
    private  String uuid;
    @ApiModelProperty("用户UID")
    private  String userCode;

    @ApiModelProperty("支付方式模板")
    private  String methodConfigId;

    @ApiModelProperty("真实姓名")
    private  String realName;
    private  String ParamName1;
    private  String ParamName2;
    private  String ParamName3;
    private  String ParamName4;
    private  String paramValue1;
    private  String paramValue2;
    private  String paramValue3;
    private  String paramValue4;
    private  String paramValue5;

    @ApiModelProperty("支付二维码")

    private  String methodName;
    private  String qrcode;

    @ApiModelProperty("备注")
    private  String remark;

    @ApiModelProperty("资金密码")
    private  String loginSafeword;

}
