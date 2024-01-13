package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class AgentRechargeReportDto {


    private  String userId;
    @ApiModelProperty("用户")
    private String userName;
    @ApiModelProperty("UID")
    private String userCode;

    @ApiModelProperty("用户数")
    private int recoMember;
    @ApiModelProperty("代理数")
    private int recoAgent;

    @ApiModelProperty("充值 usdt")
    private BigDecimal rechargeUsdt;

    @ApiModelProperty("充值 eth")
    private BigDecimal rechargeEth;

    @ApiModelProperty("充值 btc")
    private BigDecimal rechargeBtc;

    @ApiModelProperty("提现 usdt")
    private BigDecimal withdraw;
    @ApiModelProperty("提现 eth")
    private BigDecimal withdrawEth;

    @ApiModelProperty("提现 btc")
    private BigDecimal withdrawBtc;

    @ApiModelProperty("交易盈亏")
    private BigDecimal businessProfit;

    @ApiModelProperty("手续费")
    private BigDecimal totleFee;

    @ApiModelProperty("总收益")
    private BigDecimal totleIncome;

}
