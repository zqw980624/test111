package com.yami.trading.huobi.data.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class TimeseriesResult implements Serializable {

	private static final long serialVersionUID = -2654740103586762267L;

	/**
     * 收盘价
     */
    private BigDecimal close;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 时间
     */
    private String date;


    
}
