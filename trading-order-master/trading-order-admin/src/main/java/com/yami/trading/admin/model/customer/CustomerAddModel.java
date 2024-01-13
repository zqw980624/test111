package com.yami.trading.admin.model.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CustomerAddModel {

    @ApiModelProperty("用户名")
    @NotBlank
    private String userName;

    @ApiModelProperty("备注")
    private String remarks;

    @ApiModelProperty("密码")
    @NotBlank
    private String password;

    @ApiModelProperty("资金密码")
    @NotBlank
    private String safePassword;
    @ApiModelProperty("用户名")
    private String autoAnswer;

    @ApiModelProperty("登录权限  true 开启 false 禁用")
    private boolean enabled;

    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private String loginSafeword;

    @ApiModelProperty("超级管理员谷歌验证码")
    private String superGoogleAuthCode;

}
