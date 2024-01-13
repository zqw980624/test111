package com.yami.trading.admin.controller.exchange.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class CloseModel {

    @ApiModelProperty("订单号")
    private  String orderNo;
}
