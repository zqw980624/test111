package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cUserResetModel {

    @ApiModelProperty("id")
    private  String id;

    @ApiModelProperty("充值 提现")
    private  String recharge_withdraw;

    @ApiModelProperty("资金密码")
    private  String  safe_password;

    private  String money_change;


}
