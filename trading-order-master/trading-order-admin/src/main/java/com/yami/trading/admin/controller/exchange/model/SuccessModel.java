package com.yami.trading.admin.controller.exchange.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class SuccessModel {

    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private  String loginSafeword;


    @ApiModelProperty("订单号")
    @NotBlank
    private String orderNo;

}
