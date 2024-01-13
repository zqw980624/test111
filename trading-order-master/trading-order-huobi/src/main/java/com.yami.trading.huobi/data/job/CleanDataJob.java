package com.yami.trading.huobi.data.job;

import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.common.util.UTCDateUtils;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.huobi.data.internal.KlineService;
import com.yami.trading.service.data.KlineDBService;
import com.yami.trading.service.item.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
public class CleanDataJob {

    private static Logger logger = LoggerFactory.getLogger(CleanDataJob.class);
    @Autowired
    private DataDBService dataDBService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private KlineService klineService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void taskJob() {
        klineService.clean();
    }

}
