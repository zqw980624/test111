package com.yami.trading.bean.data.domain;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeDetails {
    private String symbol;
    private String trade_unique_id;
    private int trade_volume;
    private int side;
    private double current;
    private String currentStr;
    private double chg;
    private int level;
    private int trade_session;
    private String trade_type;
    private double percent;
    private long timestamp;
}
