package com.yami.trading.admin.task;

import com.yami.trading.admin.task.contract.ContractApplyOrderHandleJob;
import com.yami.trading.admin.task.contract.ContractOrderCalculationJob;
import com.yami.trading.admin.task.contract.ItemHandleJob;
import com.yami.trading.admin.task.future.FuturesOrderCalculationJob;
import com.yami.trading.admin.task.future.consumer.FuturesRecomConsumeServer;
import com.yami.trading.admin.task.summary.SummaryCrawl;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.BuyAndSellUtils;
import com.yami.trading.huobi.data.AdjustmentValueCache;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.huobi.data.internal.KlineService;
import com.yami.trading.huobi.data.job.*;
import com.yami.trading.service.contract.ContractOrderCalculationService;
import com.yami.trading.service.exchange.job.ExchangeApplyOrderHandleJob;
import com.yami.trading.service.future.FuturesLoadCacheService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.TipService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
@Component
public class InitHandle implements CommandLineRunner {

    @Autowired
    protected ItemService itemService;
    @Autowired
    protected SummaryCrawl summaryCrawl;
    @Autowired
    protected DataDBService dataDBService;
    @Autowired
    protected KlineService klineService;
    @Autowired
    protected HighLowHandleJob highLowHandleJob;
    @Autowired
    protected StockGetDataJob stockGetDataJob;
    @Autowired
    protected CryptosGetDataJob cryptosGetDataJob;
    @Autowired
    protected SaveRealtimeServer saveRealtimeServer;
    @Autowired
    private FuturesLoadCacheService futuresLoadCacheService;
    @Autowired
    private FuturesRecomConsumeServer futuresRecomConsumeServer;
    @Autowired
    protected ContractOrderCalculationService contractOrderCalculationService;
    @Autowired
    protected ContractApplyOrderHandleJob contractApplyOrderHandleJob;
    @Autowired
    ItemHandleJob itemHandleJob;
    @Autowired
    protected ContractOrderCalculationJob contractOrderCalculationJob;
    @Autowired
    protected SysparaService sysparaService;
    @Autowired
    private RealtimePushJob realtimePushJob;
    @Autowired
    private CleanDataJob cleanDataJob;
    @Autowired
    private StockGetMarketJob stockGetMarketJob;
    @Autowired
    private FuturesOrderCalculationJob futuresOrderCalculationJob;
    @Autowired
    protected DepthPushJob depthPushJob;
    @Autowired
    protected TradePushJob tradePushJob;
    @Autowired
    private KlineLoadCache klineLoadCache;
    @Autowired
    ExchangeApplyOrderHandleJob exchangeApplyOrderHandleJob;
    @Autowired
    TipService tipService;

    @Override
    public void run(String... args) throws Exception {
        tipService.init();
        futuresLoadCacheService.loadcache();
        log.info("开始Data初始化........");
       /* boolean am = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:00", "17:00");
        } catch (Exception e) {
            log.error("印度股存= {}", e);
        }
        if (am) {
            //当前毫秒时间戳
            itemHandleJob.start();
        }*/
        List<Item> items = itemService.list();
        for (Item item : items) {
            AdjustmentValueCache.getCurrentValue().put(item.getSymbol(), item.getAdjustmentValue());
        }

        for (Item item : items) {
            Realtime realtime = dataDBService.get(item.getSymbol());
            if (realtime != null) {
                DataCache.putRealtime(item.getSymbol(), realtime);
            }
        }
        for (Item item : items) {
            List<Realtime> list = this.dataDBService.findRealtimeOneDay(item.getSymbol());
            DataCache.getRealtimeHistory().put(item.getSymbol(), list);
        }
        klineLoadCache.loadCache();
        // 高低修正
        highLowHandleJob.start();
        // 获取realtime实时数据
        stockGetDataJob.start();
        cryptosGetDataJob.start();
        // 实时数据批量保存线程
        saveRealtimeServer.start();
        // realtime推送JOB
        realtimePushJob.start();
        // 日志异步存储线程启动
//		saveLogServer.start();

        /**
         * 委托单处理线程启动
         */
        contractApplyOrderHandleJob.start();
        /**
         * 持仓单盈亏计算线程启动
         */
        contractOrderCalculationService.setOrder_close_line(this.sysparaService.find("order_close_line").getBigDecimal());
        contractOrderCalculationService.setOrder_close_line_type(this.sysparaService.find("order_close_line_type").getInteger());
        contractOrderCalculationJob.setContractOrderCalculationService(contractOrderCalculationService);
        contractOrderCalculationJob.start();
        // todo 做模块判断,后续打开
        futuresOrderCalculationJob.start();
        List<Item> item_list = itemService.list().stream().filter(i -> i.getType().equalsIgnoreCase(Item.cryptos)).collect(Collectors.toList());
        for (int i = 0; i < item_list.size(); i++) {
            Item item = item_list.get(i);
            HandleObject depth = new HandleObject();
            depth.setType(HandleObject.type_depth);
            depth.setItem(item);
            DataQueue.add(depth);

            HandleObject trade = new HandleObject();
            trade.setType(HandleObject.type_trade);
            trade.setItem(item);
            DataQueue.add(trade);
        }
        realtimePushJob.start();
        depthPushJob.start();
        tradePushJob.start();
        // 最后启动消费者
        cleanDataJob.taskJob();
        log.info("完成Data初始化。");
        /**
         * 股票委托单处理线程启动
         */
        exchangeApplyOrderHandleJob.start();
    }
}
