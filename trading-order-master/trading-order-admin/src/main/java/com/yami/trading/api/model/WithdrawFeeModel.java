package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class WithdrawFeeModel  {


    @ApiModelProperty("渠道 USDT,OTC")
    private  String channel;


    @ApiModelProperty("提币数量")
    private  double amount;

}
