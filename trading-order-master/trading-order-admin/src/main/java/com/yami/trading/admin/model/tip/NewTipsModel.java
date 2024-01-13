package com.yami.trading.admin.model.tip;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class NewTipsModel {


    @ApiModelProperty("时间搓")
    private  long timeStamp;

    @ApiModelProperty("模块")
    private  String model;
}
