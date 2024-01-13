package com.yami.trading.api.dto;

import lombok.Data;

@Data
public class TradeDetails {
    private String symbol;
    private String trade_unique_id;
    private int trade_volume;
    private int side;
    private double current;
    private double chg;
    private int level;
    private int trade_session;
    private String trade_type;
    private double percent;
    private long timestamp;
}
