package com.yami.trading.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class HomeViewDto {
    @ApiModelProperty("今日新增")
    private long todayUserCount = 0;
    @ApiModelProperty("总用户数")
    private long allUserCount = 0;
    @ApiModelProperty("今日充值人数")
    private long todayRechargeUserCount = 0;
    @ApiModelProperty("总收益")
    private BigDecimal totleIncome = new BigDecimal(0);

    @ApiModelProperty("今日收益")
    private BigDecimal todayTotleIncome = new BigDecimal(0);

    @ApiModelProperty("今日USDT账户余额")
    private BigDecimal todayUsdtAmount = new BigDecimal(0);
    @ApiModelProperty("USDT账户余额")
    private BigDecimal sumUsdtAmount = new BigDecimal(0);

    @ApiModelProperty("今日充值金额")
    private BigDecimal recharge = new BigDecimal(0);
    @ApiModelProperty("总充值金额")
    private BigDecimal sumRecharge = new BigDecimal(0);
    @ApiModelProperty("今日提现金额")
    private BigDecimal withdraw = new BigDecimal(0);
    @ApiModelProperty("总提现金额")
    private BigDecimal sumWithdraw = new BigDecimal(0);
    @ApiModelProperty("充提差额")
    private BigDecimal balanceAmount = new BigDecimal(0);
}
