package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ResetUserLoginStateModel {

    @NotBlank
    @ApiModelProperty("登录人谷歌验证码")
    private  String googleAuthCode;


    @ApiModelProperty("资金密码")
    @NotBlank
    private  String loginSafeword;

    @ApiModelProperty("userid")
    @NotBlank
    private  String userId;
}
