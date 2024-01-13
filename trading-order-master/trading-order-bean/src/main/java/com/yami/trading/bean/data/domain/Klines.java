package com.yami.trading.bean.data.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Klines {

    /**
     * OPEN
     */
    private BigDecimal open;
    /**
     * HIGH
     */
    private BigDecimal high;
    /**
     * LOW
     */
    private BigDecimal low;
    /**
     * CLOSE
     */
    private BigDecimal close;

    /**
     * timestamp
     */
    private Long timestamp;

}
