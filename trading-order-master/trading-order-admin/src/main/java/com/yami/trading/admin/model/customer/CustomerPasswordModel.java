package com.yami.trading.admin.model.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CustomerPasswordModel {


    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("密码")
    @NotBlank
    private String password;

    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private String loginSafeword;

    @ApiModelProperty("超级管理员谷歌验证码")
    private long superGoogleAuthCode;

}
