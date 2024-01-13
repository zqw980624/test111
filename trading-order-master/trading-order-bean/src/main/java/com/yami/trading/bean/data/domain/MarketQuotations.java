package com.yami.trading.bean.data.domain;

import lombok.Data;

/**
 * 行情数据
 */
@Data
public class MarketQuotations {
    private String symbol;
    private String name;
    private String adjustValue;
    /**
     * 原值
     */
    private String newPrice;
    /**
     * 调整后
     */
    private String afterValue;

}
