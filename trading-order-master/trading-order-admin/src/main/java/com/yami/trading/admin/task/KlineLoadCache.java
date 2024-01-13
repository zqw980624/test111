package com.yami.trading.admin.task;

import cn.hutool.core.collection.CollectionUtil;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.huobi.data.AdjustmentValueCache;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.huobi.data.internal.KlineConstant;
import com.yami.trading.huobi.data.internal.KlineService;
import com.yami.trading.huobi.data.internal.KlineTimeObject;
import com.yami.trading.service.item.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Kline组件初始化加载缓存服务
 * 
 * @author Jerry
 * @date 2023/5/8
 *
 */
@Component
public class KlineLoadCache {

	@Autowired
	private DataDBService dataDBService;
	@Autowired
	private ItemService itemService;
	@Autowired
	private KlineService klineService;

	public void loadCache() {
		List<Item> items = itemService.cacheGetAll();
		
		// 加载调整值到内存
		for(Item item : items) {
			AdjustmentValueCache.getCurrentValue().put(item.getSymbol(), item.getAdjustmentValue());
		}
		
		// 加载最新实时价格数据到内存
		for(Item item : items) {
			String symbol = item.getSymbol();
			Realtime realtime = klineService.findLatestRealtime(symbol);
			if (null != realtime) {
				DataCache.putLatestRealTime(symbol, realtime);
			}
		}
		
		// 加载最新实时价格数据到内存
		for(Item item : items) {
			String symbol = item.getSymbol();
			List<Realtime> list = dataDBService.listRealTime60s(symbol);
			if (null == list || list.size() <= 0) {
				list = new ArrayList<Realtime>();
			}
			DataCache.latestRealTimeMap_60s.put(symbol, list);
			if (CollectionUtil.isNotEmpty(list)) {
				DataCache.putLatestRealTime(symbol, list.get(0));
			}
		}
		
		// 加载K线数据到内存
		for(Item item : items) {
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_1MIN);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_5MIN);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_15MIN);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_30MIN);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_60MIN);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_2HOUR);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_4HOUR);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_1DAY);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_5DAY);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_1WEEK);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_1MON);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_QUARTER);
			bulidInit(item.getSymbol(), KlineConstant.PERIOD_YEAR);
		}
	}
	
	public void bulidInit(String symbol, String line) {
		List<Kline> list = klineService.find(symbol, line, Integer.MAX_VALUE);
		KlineTimeObject model = new KlineTimeObject();
		model.setLastTime(new Date());
		Collections.sort(list);
		model.setKline(list);
		DataCache.putKline(symbol, line, model);
	}

	public void setDataDBService(DataDBService dataDBService) {
		this.dataDBService = dataDBService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public void setKlineService(KlineService klineService) {
		this.klineService = klineService;
	}
	
}
