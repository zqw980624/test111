package com.yami.trading.admin.controller.customer.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class BindGooleAuthCodeModel {

    @ApiModelProperty("谷歌密钥")
    @NotBlank
    private String googleAuthSecret;


    @ApiModelProperty("谷歌验证码")
    @NotBlank
    private String googleAuthCode;


    @ApiModelProperty("超级谷歌验证码")
    @NotBlank
    private  String rootGoogleCode;

    @ApiModelProperty("用户id")
    @NotBlank
    private  String  userId;
}
