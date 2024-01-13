package com.yami.trading.huobi.data;


import com.yami.trading.bean.data.domain.*;
import com.yami.trading.huobi.data.internal.DepthTimeObject;
import com.yami.trading.huobi.data.internal.KlineTimeObject;
import com.yami.trading.huobi.data.internal.TradeTimeObject;
import com.yami.trading.huobi.data.internal.TrendTimeObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataCache {
    /**
     * 分时
     */
    private volatile static Map<String, TrendTimeObject> trend = new ConcurrentHashMap<String, TrendTimeObject>();
    /**
     * K线
     */
    private volatile static Map<String, PanKou> panKou = new ConcurrentHashMap<String, PanKou>();
    /**
     * K线
     */
    private volatile static Map<String, KlineTimeObject> kline = new ConcurrentHashMap<String, KlineTimeObject>();


    /**
     * 24小时的历史记录
     */
    private volatile static Map<String, List<Realtime>> realtimeHistory = new ConcurrentHashMap<String, List<Realtime>>();
    /**
     * 最高最低
     */
    private volatile static Map<String, Double> realtimeHigh = new ConcurrentHashMap<String, Double>();
    private volatile static Map<String, Double> realtimeLow = new ConcurrentHashMap<String, Double>();
    /**
     * 向前24小时时间点的开盘价
     */
    private volatile static Map<String, Double> realtime24HBeforeOpen = new ConcurrentHashMap<String, Double>();

    private volatile static Map<String, Kline> kline_hobi = new ConcurrentHashMap<String, Kline>();

    /**
     * 市场深度数据
     */
    private volatile static Map<String, DepthTimeObject> depth = new ConcurrentHashMap<String, DepthTimeObject>();
    /**
     * 市场深度数据
     */
    private volatile static Map<String, List<TradeDetails>> tradeList = new ConcurrentHashMap<String, List<TradeDetails>>();
    /**
     * 近期交易记录
     */
    private volatile static Map<String, TradeTimeObject> trade = new ConcurrentHashMap<String, TradeTimeObject>();
    /**
     * 最近60s实时价格信息列表
     */
    public static Map<String, List<Realtime>> latestRealTimeMap_60s = new ConcurrentHashMap<>();

    public static Realtime getLatestRealTime(String symbol) {
        return latestRealTime.get(symbol);
    }

    /**
     * 最新的实时价格信息缓存 <币种code, RealTime>
     */
    public static Map<String, Realtime> latestRealTime = new ConcurrentHashMap<>();
    //股票时区暂存
    public static Map<String, StockMarket> realMarketMap = new ConcurrentHashMap<>();
    public static void depthToTrade(Depth depth) {
        String symbol = depth.getSymbol();
        TradeTimeObject timeObject = DataCache.getTrade().get(symbol);
        if (timeObject == null) {
            timeObject = new TradeTimeObject();
        }
        timeObject.setLastTime(new Date());

        List<TradeEntry> data = new ArrayList<TradeEntry>();
        List<DepthEntry> asks = depth.getAsks();
        List<DepthEntry> bids = depth.getBids();
        List<TradeEntry> sell = asks.stream().map(a -> {
            TradeEntry tradeEntry = new TradeEntry();
            tradeEntry.setDirection("sell");
            tradeEntry.setAmount(a.getAmount());
            tradeEntry.setPrice(a.getPrice());
            tradeEntry.setTs(depth.getTs());
            return tradeEntry;
        }).collect(Collectors.toList());
        List<TradeEntry> buy = bids.stream().map(a -> {
            TradeEntry tradeEntry = new TradeEntry();
            tradeEntry.setDirection("buy");
            tradeEntry.setAmount(a.getAmount());
            tradeEntry.setPrice(a.getPrice());
            tradeEntry.setTs(depth.getTs());
            return tradeEntry;
        }).collect(Collectors.toList());
        data.addAll(sell);
        data.addAll(buy);
        timeObject.put(symbol, data);
        DataCache.getTrade().put(symbol, timeObject);
    }

    public static KlineTimeObject getKline(String symbol, String line) {
        String key = symbol;
        if (!StringUtils.isBlank(line)) {
            key = key + "_" + line;
        }
        return kline.get(key);
    }

    public static TrendTimeObject getTrend(String symbol) {
        return trend.get(symbol);
    }

    public static void putTrend(String symbol, TrendTimeObject model) {
        trend.put(symbol, model);
    }

    public static void putKline(String symbol, String line, KlineTimeObject model) {
        String key = symbol;
        if (!StringUtils.isBlank(line)) {
            key = key + "_" + line;
        }
        kline.put(key, model);
    }

    public static List<TradeDetails> getStockTradeList(String symbol) {
        return tradeList.get(symbol);
    }

    public static void putStockTradeList(String symbol, List<TradeDetails> model) {
        tradeList.put(symbol, model);
    }

    public static Realtime getRealtime(String symbol) {
        Realtime realtime = latestRealTime.get(symbol);
        if (realtime == null) {
            if (StringUtils.isAllLowerCase(symbol)) {
                symbol = symbol.toUpperCase();
            } else if (StringUtils.isAllUpperCase(symbol)) {
                symbol = symbol.toLowerCase();
            }
            return latestRealTime.get(symbol);

        } else {
            return realtime;
        }
    }

    public static void putRealtime(String symbol, Realtime model) {
        latestRealTime.put(symbol, model);
    }

    public static void putMarket(String symbol,  StockMarket market) {
        realMarketMap.put(symbol, market);
    }
    public static StockMarket getMarket(String symbol) {
        return realMarketMap.get(symbol);
    }
    public static Map<String, List<Realtime>> getRealtimeHistory() {
        return realtimeHistory;
    }

    public static void setRealtimeHistory(Map<String, List<Realtime>> realtimeHistory) {
        DataCache.realtimeHistory = realtimeHistory;
    }

    public static Map<String, DepthTimeObject> getDepth() {
        return depth;
    }

    public static Map<String, TradeTimeObject> getTrade() {
        return trade;
    }

    public static Map<String, Double> getRealtimeHigh() {
        return realtimeHigh;
    }

    public static Map<String, Double> getRealtimeLow() {
        return realtimeLow;
    }

    public static Map<String, Kline> getKline_hobi() {
        return kline_hobi;
    }

    public static Map<String, Double> getRealtime24HBeforeOpen() {
        return realtime24HBeforeOpen;
    }

    public static void putLatestRealTime(String symbol, Realtime model) {
        latestRealTime.put(symbol, model);
    }
}
