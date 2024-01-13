package com.yami.trading.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class NameLableDto {


    @ApiModelProperty("值")
    private  String value;

    @ApiModelProperty("描述")
    private  String lable;
}
