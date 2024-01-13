package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class OrderCancelModel {

    @NotBlank
    private  String  orderNo;


    @NotBlank
    @ApiModelProperty("取消理由")
    private  String reason;

}
