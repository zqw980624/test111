package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel
public class ManualReleaseModel {

    @NotBlank
    private  String  orderNo;



    @ApiModelProperty("资金密码")
    @NotBlank
    private  String safeword;}
