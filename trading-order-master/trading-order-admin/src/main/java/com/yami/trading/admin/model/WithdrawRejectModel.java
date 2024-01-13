package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class WithdrawRejectModel {


    @ApiModelProperty
    @NotBlank
     private  String id;

    @ApiModelProperty("驳回原因")
    private  String content;
}
