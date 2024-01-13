package com.yami.trading.huobi.data.job;


import cn.hutool.core.util.StrUtil;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.domain.StockMarket;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.*;
import com.yami.trading.huobi.data.AdjustmentValueCache;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.huobi.data.model.AdjustmentValue;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StockGetDataJob extends AbstractGetDataJob {

    private static Logger logger = LoggerFactory.getLogger(StockGetDataJob.class);

    public static volatile boolean first = true;

    public static volatile boolean stockFirstFetch = true;

    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private DataDBService dataDBService;
    @Autowired
    private ItemService itemService;

    public void start() {
        new Thread(this, "StockGetDataJob").start();
    }

    public void run() {

        if (first) {
            // data数据保存间隔时长(毫秒)
            this.interval = this.sysparaService.find("data_interval").getInteger().intValue();
            first = false;
        }
        while (true) {
            try {
                List<Item> list = itemService.list().stream().filter(i->"0".equalsIgnoreCase(i.getFake())).collect(Collectors.toList());
                // etf 和A股开盘时间是一样的
                String aStocSymbols = list.stream().filter(item ->item.getOpenCloseType() != null &&  item.getOpenCloseType().equalsIgnoreCase(Item.A_STOCKS))
                        .map(Item::getSymbol).collect(Collectors.joining(","));
                String hkStocSymbols = list.stream().filter(item -> item.getOpenCloseType() != null &&item.getOpenCloseType().equalsIgnoreCase(Item.HK_STOCKS)).map(Item::getSymbol).collect(Collectors.joining(","));
                String usStocSymbols = list.stream().filter(item -> item.getOpenCloseType() != null &&item.getOpenCloseType().equalsIgnoreCase(Item.US_STOCKS)).map(Item::getSymbol).collect(Collectors.joining(","));
                if(stockFirstFetch){
                    this.realtimeHandle(aStocSymbols);
                    this.realtimeHandle(hkStocSymbols);
                    this.realtimeHandle(usStocSymbols);
                    stockFirstFetch = false;
                }
                if(MarketOpenChecker.isMarketOpen(Item.A_STOCKS)){
                    this.realtimeHandle(aStocSymbols);
                }
                if(MarketOpenChecker.isMarketOpen(Item.HK_STOCKS)){
                    this.realtimeHandle(hkStocSymbols);
                }
                if(MarketOpenChecker.isMarketOpen(Item.US_STOCKS)){
                    this.realtimeHandle(usStocSymbols);
                }

//                String emptyRealTimeSymbols = list.stream().filter(i -> DataCache.getRealtime(i.getSymbol()) == null).map(Item::getSymbol).collect(Collectors.joining(","));
//                if(StringUtils.isNotEmpty(emptyRealTimeSymbols)){
//                    realtimeHandle(emptyRealTimeSymbols);
//                }
            } catch (Exception e) {
                logger.error("run fail", e);
            } finally {
                ThreadUtils.sleep(this.interval);
            }
        }

    }

    @Override
    public String getName() {
        return "股票实时数据采集";
    }

    @Override
    public void realtimeHandle(String symbols) {

    }


}
