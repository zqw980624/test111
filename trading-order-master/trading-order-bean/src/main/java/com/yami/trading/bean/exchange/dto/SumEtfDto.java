package com.yami.trading.bean.exchange.dto;

import lombok.Data;

@Data
public class SumEtfDto {


    private  double profitLoss=0;  //盈亏
    private double toDayProfitLoss=0; //今日盈亏
    private double sumPrice=0;  //总资产
    private double sumVolume=0; //可用


}
