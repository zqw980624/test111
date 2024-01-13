package com.yami.trading.huobi.data.job;


import cn.hutool.core.util.StrUtil;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.domain.StockMarket;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StockGetMarketJob extends AbstractGetDataJob {

    private static Logger logger = LoggerFactory.getLogger(StockGetMarketJob.class);

    public static volatile boolean first = true;

    public static volatile boolean stockFirstFetch = true;

    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private DataDBService dataDBService;
    @Autowired
    private ItemService itemService;


    @Override
    public String getName() {
        return "股票实时数据采集";
    }

    @Override
    public void realtimeHandle(String symbols) {

    }


    @Override
    public void run() {

    }
}
