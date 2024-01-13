package com.yami.trading.admin.controller.exchange.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ResetFreezeModel {

    @ApiModelProperty("用户id")
    private  String id;

    @ApiModelProperty("转移金额")
    private  String moneyRevise;

    @ApiModelProperty("登录人资金密码")
    private String loginSafeword;

    @ApiModelProperty("转移方向")
    private  String resetType;
    @ApiModelProperty("转移币种")
    private  String coinType;
}
