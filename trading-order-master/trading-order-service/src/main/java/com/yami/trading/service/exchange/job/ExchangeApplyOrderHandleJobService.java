package com.yami.trading.service.exchange.job;

import cn.hutool.json.JSONObject;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.item.dto.RealtimeDTO;

public interface ExchangeApplyOrderHandleJobService {

    public void handle(ExchangeApplyOrder applyOrder, JSONObject msgObject);

    public void handles(ExchangeApplyOrder applyOrder, RealtimeDTO realtime);
}
