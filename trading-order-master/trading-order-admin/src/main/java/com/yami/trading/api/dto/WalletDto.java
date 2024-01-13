package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class WalletDto {



    @ApiModelProperty("现金")
    private BigDecimal money =new BigDecimal(0);

    @ApiModelProperty("锁定金额")
    private BigDecimal lockMoney =new BigDecimal(0);


    @ApiModelProperty("冻结金额")
    private BigDecimal freezeMoney =new BigDecimal(0);
}
