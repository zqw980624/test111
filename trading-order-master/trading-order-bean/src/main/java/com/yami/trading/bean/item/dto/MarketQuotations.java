package com.yami.trading.bean.item.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 行情数据
 */
@Data
@ApiModel
public class MarketQuotations {

    private String symbol;
    @ApiModelProperty("产品名称")
    private String name;
    @ApiModelProperty("调整值")
    private String adjustValue;
    /**
     * 原值
     */
    @ApiModelProperty("原值")
    private String newPrice;
    /**
     * 调整后
     */
    @ApiModelProperty("调整后")
    private String afterValue;

}
