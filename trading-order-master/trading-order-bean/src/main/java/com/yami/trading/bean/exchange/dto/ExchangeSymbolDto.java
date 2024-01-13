package com.yami.trading.bean.exchange.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeSymbolDto {
    /**
     * 名称
     */
    private  String  name;
    /**
     * 币
     */
    private  String partyId;
    private  String  pid;
    private  String symbol;
    private  double volume; //可用
    private  double marketValue;//市值
    private double openPrice; //开盘价

    private  BigDecimal quantity; //数量
    private  double positionVolume;//持仓数
    private  double price;  //成本
    private  double currentPrice;//现价

    private  double profitLoss=0;  //总盈亏

    private double toDayProfitLoss=0; //今日盈亏

    private  double profitLossPercentage; //总盈亏百分比

    private  double toDayProfitLossPercentage; //今日盈亏百分比

}
