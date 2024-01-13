package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ChangeLoginPasswordModel {

    @ApiModelProperty("旧登录密码 记得加密")
    @NotBlank
    private String oldPassword;


    @ApiModelProperty("新登录密码 记得加密")
    @NotBlank
    private  String newPassword;
}
