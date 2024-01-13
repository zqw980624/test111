package com.yami.trading.admin.dto.c2c;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cPaymentMethodTypeDto {

    @ApiModelProperty("ID")
    private  String id;

    @ApiModelProperty("名称")
    private  String name;
}
