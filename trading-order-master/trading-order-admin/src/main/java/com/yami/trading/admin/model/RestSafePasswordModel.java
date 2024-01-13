package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class RestSafePasswordModel {


    @ApiModelProperty("userid")
    @NotBlank
    private  String userId;


    @ApiModelProperty("谷歌验证码")
    @NotBlank
    private  String  googleAuthCode;

    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private  String loginSafeword;

    @ApiModelProperty("新资金密码")
    @NotBlank
    private  String newSafeword;
}
