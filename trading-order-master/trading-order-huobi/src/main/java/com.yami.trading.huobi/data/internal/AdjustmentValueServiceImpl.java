package com.yami.trading.huobi.data.internal;


import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.huobi.data.AdjustmentValueCache;
import com.yami.trading.huobi.data.model.AdjustmentValue;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AdjustmentValueServiceImpl implements AdjustmentValueService {

    @Autowired
    @Qualifier("dataService")
    private DataService dataService;
    @Autowired
    private ItemService itemService;

    public void adjust(String symbol, BigDecimal value, double second) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        Realtime realtime = dataService.realtime(symbol).get(0);
        BigDecimal new_price = realtime.getClose();
        BigDecimal plus = value.abs();
        if (plus.divide(new_price, 2, RoundingMode.HALF_UP).compareTo(new BigDecimal("0.1")) > 0) {
            throw new YamiShopBindException("调整偏差过大，超过10%");
        }

        if (second <= 0) {
            /**
             * 即时生效
             */

            BigDecimal currentValue = AdjustmentValueCache.getCurrentValue().get(symbol);

            if (currentValue == null) {
                AdjustmentValueCache.getCurrentValue().put(symbol, value);
            } else {
                AdjustmentValueCache.getCurrentValue().put(symbol, currentValue.add(value));
            }
            // 马上扣除价格，避免因为数据没有拉取导致加不正确
            realtime.setClose(realtime.getClose().add(value));
            /*
             * 持久化缓存
             */
            Item item = this.itemService.findBySymbol(symbol);
            if (item.getAdjustmentValue().compareTo(AdjustmentValueCache.getCurrentValue().get(symbol)) != 0) {
                item.setAdjustmentValue(AdjustmentValueCache.getCurrentValue().get(symbol));
                itemService.saveOrUpdate(item);
            }

        } else {
            AdjustmentValue adjustmentValue = new AdjustmentValue();
            adjustmentValue.setSymbol(symbol);
            adjustmentValue.setValue(value);
            adjustmentValue.setSecond(second);
            AdjustmentValueCache.getDelayValue().put(symbol, adjustmentValue);
        }
    }

    @Override
    public BigDecimal getCurrentValue(String symbol) {
        return AdjustmentValueCache.getCurrentValue().get(symbol);
    }

    @Override
    public AdjustmentValue getDelayValue(String symbol) {
        return AdjustmentValueCache.getDelayValue().get(symbol);
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

}
