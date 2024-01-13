package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel
public class WithdrawalLimitFlowModel {

    @ApiModelProperty("限额")
    private BigDecimal moneyWithdraw;


    @ApiModelProperty("userid")
    @NotBlank
    private  String userId;
}
