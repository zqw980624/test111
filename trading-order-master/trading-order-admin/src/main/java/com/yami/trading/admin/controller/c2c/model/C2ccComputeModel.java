package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2ccComputeModel {


    @ApiModelProperty("")
    private  String depositTotal;

    private  String currency;

    private  String symbol;

    private  String coinAmount;

    private  String symbolValue;

}
