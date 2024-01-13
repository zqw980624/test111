package com.yami.trading.huobi.data.job;


import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.Arith;
import com.yami.trading.huobi.data.AdjustmentValueCache;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.huobi.data.model.AdjustmentValue;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


public abstract class AbstractGetDataJob implements Runnable {
    public static volatile boolean first = true;
    protected static Logger logger = LoggerFactory.getLogger(StockGetDataJob.class);
    /**
     * 数据接口调用间隔时长(毫秒)
     */
    protected int interval;
    @Autowired
    protected SysparaService sysparaService;
    @Autowired
    protected DataDBService dataDBService;
    @Autowired
    protected ItemService itemService;

    public abstract String getName();


    public abstract void realtimeHandle(String symbols);

    public void handleRealTimeList(List<Realtime> realtimeList) {
        for (Realtime realtime : realtimeList) {

            try {
                String symbol = realtime.getSymbol();
                Integer decimal = itemService.getDecimal(symbol);
                Item item = this.itemService.findBySymbol(symbol);
                BigDecimal currentValue = AdjustmentValueCache.getCurrentValue().get(symbol);
                AdjustmentValue delayValue = AdjustmentValueCache.getDelayValue().get(symbol);

                if (delayValue != null) {
                    // 延时几次
                    int frequency = (int) Arith.div(Arith.mul(delayValue.getSecond(), 1000.0D), this.interval);

                    if (frequency <= 1) {
                        if (currentValue == null) {
                            AdjustmentValueCache.getCurrentValue().put(symbol, delayValue.getValue());
                        } else {
                            AdjustmentValueCache.getCurrentValue().put(symbol,
                                    delayValue.getValue().add(currentValue));
                        }

                        if (!item.getAdjustmentValue().equals(AdjustmentValueCache.getCurrentValue().get(symbol))) {
                            item.setAdjustmentValue(AdjustmentValueCache.getCurrentValue().get(symbol));
                            itemService.saveOrUpdate(item);
                        }
                        AdjustmentValueCache.getDelayValue().remove(symbol);
                    } else {
                        // 本次调整值
                        BigDecimal currentValue_frequency = delayValue.getValue().divide(new BigDecimal(frequency), decimal, RoundingMode.HALF_UP);

                        if (currentValue == null) {
                            AdjustmentValueCache.getCurrentValue().put(symbol, currentValue_frequency);
                        } else {
                            AdjustmentValueCache.getCurrentValue().put(symbol,
                                    currentValue.add(currentValue_frequency));
                        }

                        delayValue.setValue(delayValue.getValue().subtract(currentValue_frequency));
                        delayValue.setSecond(Arith.sub(delayValue.getSecond(), Arith.div(this.interval, 1000.0D)));
                        AdjustmentValueCache.getDelayValue().put(symbol, delayValue);

                        if (!item.getAdjustmentValue().equals(AdjustmentValueCache.getCurrentValue().get(symbol))) {
                            item.setAdjustmentValue(AdjustmentValueCache.getCurrentValue().get(symbol));
                            itemService.saveOrUpdate(item);
                        }
                    }
                }

                currentValue = AdjustmentValueCache.getCurrentValue().get(realtime.getSymbol());

                if (currentValue != null && currentValue.compareTo(BigDecimal.ZERO) != 0) {
                    realtime.setClose(realtime.getClose().add(currentValue).setScale(decimal, RoundingMode.HALF_UP));
                    BigDecimal ask = realtime.getAsk();
                    if(ask!=null){
                        realtime.setAsk(ask.add(currentValue).setScale(decimal, RoundingMode.HALF_UP));
                    }
                    BigDecimal bid = realtime.getBid();
                    if(bid!=null){
                        realtime.setBid(bid.add(currentValue).setScale(decimal, RoundingMode.HALF_UP));
                    }
                    // realtime.setVolume(Arith.add(realtime.getVolume(), Arith.mul(Arith.div(currentValue, realtime.getClose()), realtime.getVolume())));
                    // realtime.setAmount(Arith.add(realtime.getAmount(), Arith.mul(Arith.div(currentValue, realtime.getClose()), realtime.getAmount())));
                }

                // 缓存中最新一条Realtime数据
                Realtime realtimeLast = DataCache.getRealtime(symbol);
                // 临时处理：正常10秒超过25%也不合理,丢弃.只有虚拟货币才这样执行
                boolean checkRate = getName().contains("虚拟货币");
                double rate = 0;
                if (!checkRate) {
                    saveData(realtime, symbol, item);
                }else{
                    if (realtimeLast != null) {
                        rate = Math.abs(Arith.sub(realtime.getClose().doubleValue(), realtimeLast.getClose().doubleValue()));
                    }
                    if (null == realtimeLast || Arith.div(rate, realtimeLast.getClose().doubleValue()) < 0.25D) {
                        saveData(realtime, symbol, item);
                    } else {
                        logger.error("当前{}价格{},上一次价格为{}过25%也不合理,丢弃Realtime,不入库", realtime.getSymbol(),realtimeLast.getClose(), realtime.getClose());
                    }
                }


            } catch (Exception e) {
                logger.error("数据采集失败", e);

            }
        }
    }

    private void saveData(Realtime realtime, String symbol, Item item) {
        Double high = DataCache.getRealtimeHigh().get(symbol);
        Double low = DataCache.getRealtimeLow().get(symbol);
        if (realtime.getTs().toString().length() <= 10) {
            realtime.setTs(Long.valueOf(realtime.getTs() + "000"));
        }
        realtime.setName(item.getName());
        if (high == null || realtime.getHigh().doubleValue() > high) {
            DataCache.getRealtimeHigh().put(symbol, realtime.getHigh().doubleValue());
        }
        if ((low == null || realtime.getLow().doubleValue() < low) && realtime.getLow().doubleValue() > 0) {
            DataCache.getRealtimeLow().put(symbol, realtime.getLow().doubleValue());
        }
        this.dataDBService.saveAsyn(realtime);
    }

}
