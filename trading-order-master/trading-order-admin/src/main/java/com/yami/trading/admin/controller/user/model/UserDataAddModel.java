package com.yami.trading.admin.controller.user.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UserDataAddModel {


    @ApiModelProperty("用户名")
    @NotBlank
    private  String username;


    @ApiModelProperty("登录密码")
    @NotBlank
    private String  password;

    @ApiModelProperty("上级用户或上级代理商UID(选填)")
    private  String parentsUseCode;


    @ApiModelProperty("登录权限")
    private  boolean loginAuthority;


    @ApiModelProperty("是否锁定")
    private  boolean enabled;


    @ApiModelProperty("备注")
    private  String remarks;


}
