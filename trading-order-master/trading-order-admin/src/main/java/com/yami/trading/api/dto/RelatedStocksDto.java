package com.yami.trading.api.dto;


import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.dto.StocksDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/***
 * 指数成分股列表
 */
@Data
@AllArgsConstructor
public class RelatedStocksDto {
    private String symbol;
    List<StocksDto> stocks;

}
