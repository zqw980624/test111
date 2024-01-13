package com.yami.trading.admin.controller.customer.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UnbindGoogleAuthCodeModel {

    @ApiModelProperty("userId")
    private String userId;

    @ApiModelProperty("超级谷歌验证码")
    @NotBlank
    private  String rootGoogleCode;
}
