package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class AgentAndModel {

    @ApiModelProperty("用户名")
    @NotBlank
    private String userName;

    @ApiModelProperty("密码")
    @NotBlank
    private String password;


    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private String safeword;


    @ApiModelProperty("备注")
    private  String remarks;

    @ApiModelProperty("上级代理商UID(选填)")
    private  String parentsUseCode;



    @ApiModelProperty("登录权限")
    private  boolean loginAuthority;

    @ApiModelProperty("操作权限")
    private  boolean operaAuthority;
}
