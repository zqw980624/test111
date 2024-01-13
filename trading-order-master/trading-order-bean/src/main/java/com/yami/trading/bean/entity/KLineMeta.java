package com.yami.trading.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class KLineMeta implements Serializable {
    String symbol;
    String interval;
    String currency;
    String exchangeTimezone;
    String exchange;
    String micCode;
    String type;
}
