package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cUserDepositModel {

    @ApiModelProperty("承兑商UID")
    private  String  c2cUserCode;

    private  String advertId;
}
