package com.yami.trading.bean.item.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 */
@Data
@ApiModel
public class MarketQuotationsAdjust {
    @ApiModelProperty("币对")
    private String symbol;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("调整值/累计修正值")
    private String adjustValue;
    /**
     * 原值
     */
    @ApiModelProperty("原值")
    private String newPrice;
    /**
     * 调整后
     */
    @ApiModelProperty("调整后值")
    private String afterValue;
    @ApiModelProperty("待生效的值")
    private String delayValue;
    @ApiModelProperty("生效趋势，多少s后生效")
    private String delaySecond;
    private String pips;

}
