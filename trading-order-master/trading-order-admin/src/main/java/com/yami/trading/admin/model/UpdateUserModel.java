package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UpdateUserModel {

    @ApiModelProperty("用户id")
    private  String  userId;

    @ApiModelProperty("登录权限")
    private  boolean loginAuthority;

    @ApiModelProperty("提现权限")
    private  boolean withdrawAuthority;


    @ApiModelProperty("是否业务锁定")
    private  boolean enabled;

    @ApiModelProperty("备注")
    private   String remarks;

}
