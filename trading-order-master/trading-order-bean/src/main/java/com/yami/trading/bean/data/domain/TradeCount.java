package com.yami.trading.bean.data.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradeCount {
    @ApiModelProperty("价格")
    private String price;
    @ApiModelProperty("交易数量")
    private  Integer count;
}
