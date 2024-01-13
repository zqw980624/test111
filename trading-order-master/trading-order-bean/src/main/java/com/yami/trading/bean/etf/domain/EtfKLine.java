package com.yami.trading.bean.etf.domain;

import com.yami.trading.bean.data.domain.Kline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtfKLine {

    String modelId;

    List<Kline> klineList;
}
