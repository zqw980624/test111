package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel
public class ApplyWithdrawModel {
    @ApiModelProperty("资金密码")
    private String safeword;
    @ApiModelProperty("提现金额")
    @Min(0)
    private BigDecimal amount;
    @ApiModelProperty("渠道 USDT,BTC,ETH")
    @NotBlank
    private String channel;
    @ApiModelProperty("提现转出地址")
    @NotBlank
    private String address;
}
