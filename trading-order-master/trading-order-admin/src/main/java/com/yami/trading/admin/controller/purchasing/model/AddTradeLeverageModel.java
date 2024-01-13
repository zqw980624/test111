package com.yami.trading.admin.controller.purchasing.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel
public class AddTradeLeverageModel {

    @NotBlank
    @ApiModelProperty("申购id")
    private  String id;

    @ApiModelProperty("倍数")
    private BigDecimal  multiple;
}
