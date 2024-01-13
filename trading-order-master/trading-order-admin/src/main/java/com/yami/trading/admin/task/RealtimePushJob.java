package com.yami.trading.admin.task;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.yami.trading.api.websocket.WebSocketServer;
import com.yami.trading.api.websocket.WebSocketSession;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.domain.StockMarket;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.service.item.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 行情数据推送Job
 */
@Component
@Slf4j
public class RealtimePushJob implements Runnable {
    @Autowired
    private ItemService itemService;

    public void start() {
        new Thread(this, "realtimePushJob").start();
        log.info("启动realtimePushJob！");
    }

    public void run() {

        while (true) {
            try {
                this.realtimeHandle();
            } catch (Exception e) {
                log.error("run fail", e);
            } finally {
                ThreadUtils.sleep(1000);
            }
        }
    }

    private void realtimeHandle() {
        try {
            Map<String, String> realtimeResultMap = new HashMap<>();

            // 行情实时价格
            if (!WebSocketServer.realtimeMap.isEmpty()) {

                // 客户端请求的所有币种，去重集合
                Set<String> symbolSet = new HashSet<String>();
                for (String socketKey : WebSocketServer.realtimeMap.keySet()) {
                    WebSocketSession webSocketSession = WebSocketServer.realtimeMap.get(socketKey);
                    String symbolKey = webSocketSession.getParam();
                    symbolSet.add(symbolKey);
                }

                for (String symbol : symbolSet) {
                    Realtime realtimeData = DataCache.getRealtime(symbol);
                    if (realtimeData == null) {
                        log.error("realtimeHandle 获取{} 数据为空", symbol);
                    }
                    this.realtimeRevise(realtimeResultMap, realtimeData, symbol);
                }

                if (realtimeResultMap.isEmpty()) {
                    return;
                }

                for (String socketKey : WebSocketServer.realtimeMap.keySet()) {
                    WebSocketSession webSocketSession = WebSocketServer.realtimeMap.get(socketKey);
                    String type = webSocketSession.getType();
                    String symbolKey = webSocketSession.getParam();
                    WebSocketServer.sendToMessageById(socketKey, realtimeResultMap.get(symbolKey), type);
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 行情实时价格解析
     */
    private void realtimeRevise(Map<String, String> realtimeResultMap, Realtime realtime, String symbol) {

        ResultObject realtimeResult = new ResultObject();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (realtime == null) {
            return;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Integer decimal = itemService.getDecimal(symbol);

        map.put("symbol", symbol);
        map.put("timestamp", realtime.getTs());
        map.put("current_time", realtime.getCurrentTime());
        map.put("name", realtime.getName());
        map.put("change_ratio", realtime.getChangeRatio());
        map.put("netChange", realtime.getNetChange());
        map.put("open", realtime.getOpen());
        map.put("close", realtime.getClose());
        map.put("high", realtime.getHigh());
        map.put("low", realtime.getLow());
        if( realtime.getVolume() != null){
            map.put("volume", realtime.getVolume().setScale(2, RoundingMode.HALF_UP));
        }else{
            map.put("volume", realtime.getVolume());
        }
        if( realtime.getAmount() != null){
            map.put("amount", realtime.getAmount().setScale(2, RoundingMode.HALF_UP));
        }else{
            map.put("amount", realtime.getAmount());
        }
        map.put("ask", realtime.getAsk());
        map.put("bid", realtime.getBid());
        StockMarket market = DataCache.getMarket(symbol);
        Item bySymbol = itemService.findBySymbol(symbol);
        if("1".equals(bySymbol.getFake())){
            // 假ETF默认取美股
            market = DataCache.getMarket("AAPL");
        }
        if(Item.forex.equalsIgnoreCase(bySymbol.getType())){
             market = new StockMarket();
             market.setTime_zone( "Asia/Shanghai");
             if(MarketOpenChecker.isMarketOpenByItemCloseType(bySymbol.getOpenCloseType())){
                 market.setStatus("交易中");
             }else{
                 market.setStatus("未开盘");
             }
             market.calculate();
             map.put("market", market);
        }
        if (market != null) {
            market.calculate();
            map.put("market", market);
        }
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(realtime);
        for (String key : stringObjectMap.keySet()) {
            if (!map.containsKey(key)) {
                map.put(key, stringObjectMap.get(key));
                BigDecimal open = realtime.getClose();
                map.put("open", open != null ? open.setScale(decimal, RoundingMode.HALF_UP) : null);
                BigDecimal close = realtime.getClose();
                map.put("close", close != null ? close.setScale(decimal, RoundingMode.HALF_UP) : null);
                BigDecimal high = realtime.getHigh();
                map.put("high", high != null ? high.setScale(decimal, RoundingMode.HALF_UP) : null);
                BigDecimal low = realtime.getLow();
                map.put("low", low != null ? low.setScale(decimal, RoundingMode.HALF_UP) : null);
            }
        }

        list.add(map);
        realtimeResult.setData(list);
        realtimeResultMap.put(realtime.getSymbol(), JSONObject.toJSONString(realtimeResult));
    }

}
