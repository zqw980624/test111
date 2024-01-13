package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cOrderCancelModel {

    @ApiModelProperty("订单号")
    private String order_no;

    @ApiModelProperty("备注")
    private  String remark;
}
