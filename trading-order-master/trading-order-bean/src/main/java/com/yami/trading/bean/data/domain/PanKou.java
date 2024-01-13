package com.yami.trading.bean.data.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PanKou {
    @ApiModelProperty("卖盘")
    private List<TradeCount> sell;
    @ApiModelProperty("买盘")
    private List<TradeCount> buy;
}
