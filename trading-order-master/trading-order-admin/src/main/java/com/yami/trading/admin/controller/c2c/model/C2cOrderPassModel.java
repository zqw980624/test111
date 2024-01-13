package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cOrderPassModel {

    @ApiModelProperty("订单号")
    private String order_no;

    @ApiModelProperty("登录资金密码")
    private  String safeword;
}
