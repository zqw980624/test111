package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class RechargeDto {


    @ApiModelProperty("订单id")
    private  String orderId;
}
