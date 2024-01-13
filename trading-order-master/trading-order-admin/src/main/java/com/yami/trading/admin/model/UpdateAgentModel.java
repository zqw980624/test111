package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UpdateAgentModel {

    @NotBlank
    private String id;

    @ApiModelProperty("登录权限")
    private  boolean loginAuthority;

    @ApiModelProperty("操作权限")
    private  boolean operaAuthority;

    @ApiModelProperty("备注")
    private  String remarks;
}
