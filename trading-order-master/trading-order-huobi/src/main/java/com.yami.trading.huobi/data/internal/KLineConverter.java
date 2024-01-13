package com.yami.trading.huobi.data.internal;

import com.yami.trading.bean.data.domain.Kline;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class KLineConverter {


    /**
     * 假设只取了一个区间的数据，直接对数据进行转
     * @param sKlineList
     * @return
     */
    public static List<Kline> convertTo5MinKline(List<Kline> sKlineList) {
        List<Kline> fiveMinKlineList = new ArrayList<>();

        int currentIndex = 0;
        int dataSize = sKlineList.size();

        while (currentIndex < dataSize) {
            Kline currentKline = sKlineList.get(currentIndex);
            long currentTimestamp = currentKline.getTs();
            BigDecimal open = currentKline.getOpen();
            BigDecimal high = currentKline.getHigh();
            BigDecimal low = currentKline.getLow();
            BigDecimal close = currentKline.getClose();
            BigDecimal volume = currentKline.getVolume();
            BigDecimal amount = currentKline.getAmount();

            long nextTimestamp = currentTimestamp + (5 * 60 * 1000); // 5 minutes in milliseconds

            // Find the index of the next Kline that satisfies the 5-minute condition
            int nextIndex = currentIndex + 1;
            while (nextIndex < dataSize && sKlineList.get(nextIndex).getTs() < nextTimestamp) {
                Kline nextKline = sKlineList.get(nextIndex);
                high = high.max(nextKline.getHigh());
                low = low.min(nextKline.getLow());
                volume = volume.add(nextKline.getVolume());
                amount = amount.add(nextKline.getAmount());
                close = nextKline.getClose();
                nextIndex++;
            }

            // Create the 5-minute Kline and add it to the list
            Kline fiveMinKline = new Kline();
            fiveMinKline.setSymbol(currentKline.getSymbol());
            fiveMinKline.setTs(currentTimestamp);
            fiveMinKline.setOpen(open);
            fiveMinKline.setHigh(high);
            fiveMinKline.setLow(low);
            fiveMinKline.setClose(close);
            fiveMinKline.setVolume(volume);
            fiveMinKline.setAmount(amount);
            fiveMinKlineList.add(fiveMinKline);

            currentIndex = nextIndex;
        }

        return fiveMinKlineList;
    }

}