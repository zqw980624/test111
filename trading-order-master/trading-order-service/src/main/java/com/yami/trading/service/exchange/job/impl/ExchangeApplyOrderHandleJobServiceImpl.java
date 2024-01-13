package com.yami.trading.service.exchange.job.impl;

import cn.hutool.json.JSONObject;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.service.exchange.job.ExchangeApplyOrderHandleJob;
import com.yami.trading.service.exchange.job.ExchangeApplyOrderHandleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExchangeApplyOrderHandleJobServiceImpl implements ExchangeApplyOrderHandleJobService {

    @Autowired
    private ExchangeApplyOrderHandleJob exchangeApplyOrderHandleJob;

    public void handle(ExchangeApplyOrder applyOrder, JSONObject msgObject) {
        exchangeApplyOrderHandleJob.handle(applyOrder, msgObject);
    }
    public void handles(ExchangeApplyOrder applyOrder, RealtimeDTO realtime) {
        exchangeApplyOrderHandleJob.handles(applyOrder, realtime);
    }

}
