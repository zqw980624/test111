package com.yami.trading.huobi.data.internal;

import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.huobi.data.DataCache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HighLowHandle {

    public static HighLow get(String symbol, int num, int interval) {
        List<Realtime> history = bulidNum(DataCache.getRealtimeHistory().get(symbol), num);
        HighLow highLow = new HighLow();
        if (history.size() == 0) {
            return highLow;

        }
        BigDecimal high = null;

        BigDecimal low = null;

        for (int j = 0; j < history.size(); j++) {
            Realtime realtime = history.get(j);
            /**
             * 停机过久导致的处理
             */
            if (realtime.getTs() < DateUtils.addSecond(new Date(), -num * interval).getTime()) {
                continue;
            }

            if (high == null || high.compareTo(realtime.getClose()) < 0) {
                high = realtime.getClose();
            }

            if (low == null || low.compareTo(realtime.getClose()) > 0) {
                low = realtime.getClose();
            }
        }

        highLow.setHigh(high);
        highLow.setLow(low);

        return highLow;

    }

    public static List<Realtime> bulidNum(List<Realtime> cacheList, int num) {
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

    public static HighLow getByDay(String symbol, int num) {

        KlineTimeObject timeObject = DataCache.getKline(symbol, Kline.PERIOD_1DAY);
        HighLow highLow = new HighLow();
        if (timeObject == null) {
            return highLow;
        }

        List<Kline> list = timeObject.getKline();
        List<Kline> history = new ArrayList<Kline>();
        if (num > list.size()) {
            num = list.size();
        }

        for (int i = list.size() - num; i < list.size(); i++) {
            history.add(list.get(i));
        }

        if (history.size() == 0) {
            return highLow;

        }
        BigDecimal high = null;

        BigDecimal low = null;

        for (int j = 0; j < history.size(); j++) {
            Kline kline = history.get(j);
            /**
             * 停机过久导致的处理
             */
            if (kline.getTs() < DateUtils.addDay(new Date(), -num).getTime()) {
                continue;
            }

            if (high == null || high.compareTo(kline.getClose()) < 0) {
                high = kline.getClose();
            }

            if (low == null || low.compareTo(kline.getClose()) > 0) {
                low = kline.getClose();
            }
        }

        highLow.setHigh(high);
        highLow.setLow(low);

        return highLow;

    }

}
