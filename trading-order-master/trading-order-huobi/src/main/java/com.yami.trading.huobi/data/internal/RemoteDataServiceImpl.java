package com.yami.trading.huobi.data.internal;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.job.DataQueue;
import com.yami.trading.huobi.data.job.HandleObject;
import com.yami.trading.bean.data.domain.Depth;
import com.yami.trading.bean.data.domain.Trade;
import com.yami.trading.bean.data.domain.Trend;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service("remoteDataService")
public class RemoteDataServiceImpl implements DataService {

    @Autowired
    private ItemService itemService;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private KlineService klineService;

    @Override
    public List<Realtime> realtime(String symbols) {
        List<Realtime> list = new ArrayList<>();
        // 单个币种
        if (!StrUtil.isEmpty(symbols) && !symbols.contains(",")) {
            if(symbols.equalsIgnoreCase("btc")){
                symbols = "btcusdt";
            } if(symbols.equalsIgnoreCase("eth")){
                symbols = "ethusdt";
            }
            Realtime realtime = DataCache.getRealtime(symbols);
            if (realtime != null) {
                list.add(realtime);
            }
        }
        // 如果不传参数 返回全部币种行情
        else if (StrUtil.isEmpty(symbols)) {
            List<Item> items = itemService.list();
            for (Item item : items) {
                Realtime realtime = DataCache.getRealtime(item.getSymbol());
                if (realtime != null) {
                    list.add(realtime);
                }
            }
        }
        // 多个币种
        else if (!StrUtil.isEmpty(symbols) && symbols.contains(",")) {
            String[] symbolList = symbols.split(",");
            for (String symbol : symbolList) {
                Realtime realtime = DataCache.getRealtime(symbol);
                if (realtime != null) {
                    list.add(realtime);
                }
            }
        }
        return list;
    }

    @Override
    public List<Kline> kline(String symbol, String line) {
        Item bySymbol = itemService.findBySymbol(symbol);
        if(Item.cryptos.equals(bySymbol.getType())){
            return klineCryptos(symbol, line);
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        List<Kline> list = new ArrayList<Kline>();
        if (timeObject != null) {
            list = timeObject.getKline();
        }
        List<Kline> list_clone = new ArrayList<Kline>();
        try {
            for (int i = 0; i < list.size(); i++) {
                if(list.get(i) == null){
                    continue;
                }
                Kline kline = (Kline) list.get(i).clone();
                list_clone.add(kline);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Realtime realtime = DataCache.getLatestRealTime(symbol);
        if (realtime != null) {
            Kline kline = null;
            if (KlineConstant.PERIOD_1MIN.equals(line)) {
                kline = klineService.bulidKline1Minute(realtime, KlineConstant.PERIOD_1MIN);
            } else if (KlineConstant.PERIOD_5MIN.equals(line)) {
                kline = klineService.bulidKline5Minute(realtime, KlineConstant.PERIOD_5MIN);
            } else if (KlineConstant.PERIOD_15MIN.equals(line)) {
                kline = klineService.bulidKline15Minute(realtime, KlineConstant.PERIOD_15MIN);
            } else if (KlineConstant.PERIOD_30MIN.equals(line)) {
                kline = klineService.bulidKline30Minute(realtime, KlineConstant.PERIOD_30MIN);
            } else if (KlineConstant.PERIOD_60MIN.equals(line)) {
                kline = klineService.bulidKline60Minute(realtime, KlineConstant.PERIOD_60MIN);
            } else if (KlineConstant.PERIOD_4HOUR.equals(line)) {
                kline = klineService.bulidKline4Hour(realtime, KlineConstant.PERIOD_4HOUR);
            } else if (KlineConstant.PERIOD_1DAY.equals(line)) {
                kline = klineService.bulidKline1Day(realtime, KlineConstant.PERIOD_1DAY);
            } else if (KlineConstant.PERIOD_5DAY.equals(line)) {
                kline = klineService.bulidKline5Day(realtime, KlineConstant.PERIOD_5DAY);
            } else if (KlineConstant.PERIOD_1WEEK.equals(line)) {
                kline = klineService.bulidKline1Week(realtime, KlineConstant.PERIOD_1WEEK);
            } else if (KlineConstant.PERIOD_1MON.equals(line)) {
                kline = klineService.bulidKline1Mon(realtime, KlineConstant.PERIOD_1MON);
            } else if (KlineConstant.PERIOD_QUARTER.equals(line)) {
                kline = klineService.bulidKline1Mon(realtime, KlineConstant.PERIOD_QUARTER);
            } else if (KlineConstant.PERIOD_YEAR.equals(line)) {
                kline = klineService.bulidKline1Mon(realtime, KlineConstant.PERIOD_YEAR);
            }
            if (null != kline) {
                list_clone.add(kline);
            }
        }
        // 按时间升序
        Collections.sort(list_clone);
        return list_clone;

    }

    public List<Kline> klineCryptos(String symbol, String line) {
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        List<Kline> list = new ArrayList<Kline>();
        if (timeObject != null) {
            list = timeObject.getKline();
        }
        List<Kline> list_clone = new ArrayList<Kline>();
        try {
            for (int i = 0; i < list.size(); i++) {
                Kline kline = (Kline) list.get(i).clone();
                list_clone.add(kline);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Realtime realtime = DataCache.getRealtime(symbol);
        Kline hobiOne = DataCache.getKline_hobi().get(symbol + "_" + line);

        Kline lastOne = null;
        if (list != null && list.size() > 0) {
            lastOne = list.get(list.size() - 1);
        }
        if (realtime != null && hobiOne != null && lastOne != null) {
            list_clone.add(this.klineService.bulidKline(realtime, lastOne, hobiOne, line));
        }
        Collections.sort(list_clone); // 按时间升序
        return list_clone;

    }

    @Override
    public List<Trend> trend(String symbol) {
        TrendTimeObject trendTimeObject = DataCache.getTrend(symbol);
        trendTimeObject = this.loadTrend(symbol, trendTimeObject);
        if (trendTimeObject != null) {
            return trendTimeObject.getTrend();
        }
        return new ArrayList<Trend>();
    }

    private TrendTimeObject loadTrend(String symbol, TrendTimeObject trendTimeObject) {
        Item item = itemService.findBySymbol(symbol);
        if (trendTimeObject == null || isRemoteTrend(item, trendTimeObject)) {
            /**
             * 秒
             */
            int interval = this.sysparaService.find("data_interval").getInteger().intValue() / 1000;
            int num = (24 * 60 * 60) / interval;
            List<Trend> list = new ArrayList<Trend>();
            /**
             * 24小时的历史记录
             */
            List<Realtime> history = bulidNum(DataCache.getRealtimeHistory().get(symbol), num);

            history = this.take500(history);

            if (history.size() > 500) {
                Collections.sort(history); // 按时间升序
                List<Realtime> history_500 = new ArrayList<Realtime>();
                for (int i = 0; i < 500; i++) {
                    history_500.add(history.get(i));
                }
                history = history_500;
            }

            for (int i = 0; i < history.size(); i++) {
                Realtime realtime = history.get(i);
                Trend trend = bulidTrend(realtime);
                list.add(trend);
            }
            Realtime realtime_last = DataCache.getRealtime(symbol);
            if (realtime_last != null) {
                list.add(bulidTrend(DataCache.getRealtime(symbol)));
            }

            trendTimeObject = new TrendTimeObject();
            trendTimeObject.setTrend(list);
            trendTimeObject.setLastTime(new Date());
            DataCache.putTrend(symbol, trendTimeObject);
        }

        return trendTimeObject;

    }

    private Trend bulidTrend(Realtime realtime) {
        Trend trend = new Trend();
        trend.setSymbol(realtime.getSymbol());
        trend.setTs(realtime.getTs());
        trend.setTrend(realtime.getClose().doubleValue());
        trend.setVolume(realtime.getVolume().doubleValue());
        trend.setAmount(realtime.getAmount().doubleValue());

        return trend;
    }

    /**
     * 按平均频率取500个数据点
     */
    private List<Realtime> take500(List<Realtime> history) {
        List<Realtime> list = new ArrayList<Realtime>();

        int num = history.size() / 500;

        if (num <= 0) {
            return history;
        }

        int i = 0;
        while (true) {
            if (num >= 1.0D) {
                if (i % num == 0) {
                    list.add(history.get(i));
                }
            } else {
                list.add(history.get(i));
            }

            i++;
            if (i >= history.size()) {
                break;
            }
        }

        return list;
    }

    private boolean isRemoteTrend(Item item, TrendTimeObject timeObject) {

        /**
         * 判断是否远程 读取数据，先完成3秒过期。后期补上非开盘时间不调用。
         */
        Date timestamps = timeObject.getLastTime();
        /**
         * 数据超时时间
         */
        int timeout = 3;
        if (DateUtils.addSecond(timestamps, timeout).before(new Date())) {
            return true;
        }

        return false;
    }

    private List<Realtime> bulidNum(List<Realtime> cacheList, int num) {
        List<Realtime> list = new ArrayList<Realtime>();
        if (cacheList == null) {
            return list;
        }
        if (num > cacheList.size()) {
            num = cacheList.size();
        }

        for (int i = cacheList.size() - num; i < cacheList.size(); i++) {
            list.add(cacheList.get(i));
        }

        return list;
    }


    @Override
    public Depth depth(String symbol) {
        DepthTimeObject timeObject = DataCache.getDepth().get(symbol);
        this.loadDepth(symbol, timeObject);
        if (timeObject != null) {
            return timeObject.getDepth();
        }
        timeObject = new DepthTimeObject();
        Depth depth = new Depth();
        timeObject.setLastTime(new Date());
        timeObject.setDepth(depth);
        DataCache.getDepth().put(symbol, timeObject);
        return depth;

    }

    private void loadDepth(String symbol, DepthTimeObject timeObject) {

        Item item = itemService.findBySymbol(symbol);

        if (timeObject == null) {

            HandleObject handleObject = new HandleObject();
            handleObject.setType(HandleObject.type_depth);
            handleObject.setItem(item);
            DataQueue.add(handleObject);

        } else {
            if (isRemoteDepth(item, timeObject)) {
                HandleObject handleObject = new HandleObject();
                handleObject.setType(HandleObject.type_depth);
                handleObject.setItem(item);
                DataQueue.add(handleObject);
            }
        }
    }

    private boolean isRemoteDepth(Item item, DepthTimeObject timeObject) {
        // 判断是否远程 读取数据，先完成3秒过期。后期补上非开盘时间不调用。
        Date timestamps = timeObject.getLastTime();

        // 数据超时时间
        int timeout = 15;
        if (DateUtils.addSecond(timestamps, timeout).before(new Date())) {
            return true;
        }

        return false;
    }

    /**
     * 近期交易记录
     */
    @Override
    public Trade trade(String symbol) {
        TradeTimeObject timeObject = DataCache.getTrade().get(symbol);
        this.loadTrade(symbol, timeObject);
        if (timeObject != null) {
            return timeObject.getTrade();
        }

        timeObject = new TradeTimeObject();
        timeObject.setLastTime(new Date());
        DataCache.getTrade().put(symbol, timeObject);
        return timeObject.getTrade();
    }

    private void loadTrade(String symbol, TradeTimeObject timeObject) {
        Item item = itemService.findBySymbol(symbol);

        if (timeObject == null) {
            HandleObject handleObject = new HandleObject();
            handleObject.setType(HandleObject.type_trade);
            handleObject.setItem(item);
            DataQueue.add(handleObject);
        } else {
            if (isRemoteTrade(item, timeObject)) {
                HandleObject handleObject = new HandleObject();
                handleObject.setType(HandleObject.type_trade);
                handleObject.setItem(item);
                DataQueue.add(handleObject);
            }
        }
    }

    private boolean isRemoteTrade(Item item, TradeTimeObject timeObject) {
        /**
         * 判断是否远程 读取数据，先完成3秒过期。后期补上非开盘时间不调用。
         */
        Date timestamps = timeObject.getLastTime();

        /**
         * 数据超时时间
         */
        // 15秒
        int timeout = 15;
        if (DateUtils.addSecond(timestamps, timeout).before(new Date())) {
            return true;
        }

        return false;
    }

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    public void setSysparaService(SysparaService sysparaService) {
        this.sysparaService = sysparaService;
    }

    public void setKlineService(KlineService klineService) {
        this.klineService = klineService;
    }


}
