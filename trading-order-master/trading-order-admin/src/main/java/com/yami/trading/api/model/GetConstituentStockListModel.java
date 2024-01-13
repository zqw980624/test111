package com.yami.trading.api.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class GetConstituentStockListModel  {

    @ApiModelProperty("获取etf id")
    private  String symbol;
}
