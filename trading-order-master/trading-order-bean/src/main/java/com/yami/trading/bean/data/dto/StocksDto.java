package com.yami.trading.bean.data.dto;

import com.yami.trading.bean.data.domain.Realtime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StocksDto extends Realtime {
    private String name;
}
