package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class AssetsDto {


    @ApiModelProperty("未结盈利" )
    private BigDecimal moneyContractProfit;
    @ApiModelProperty("账户余额")
    private  BigDecimal total;

    @ApiModelProperty("净值")
    private BigDecimal moneyWallet;
}
