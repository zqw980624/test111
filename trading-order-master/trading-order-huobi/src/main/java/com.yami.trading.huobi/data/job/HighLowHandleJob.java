package com.yami.trading.huobi.data.job;

import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 最高最低修正
 *
 */
@Component
public class HighLowHandleJob implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(HighLowHandleJob.class);
	
	/**
	 * 数据接口调用间隔时长(毫秒)
	 */
	private int interval;
	public static boolean first = true;
	@Autowired
	private SysparaService sysparaService;
	@Autowired
	private ItemService itemService;
	
	public void start() {
		new Thread(this, "HighLowHandleJob").start();
	}

	@Override
	public void run() {
		ThreadUtils.sleep(1000 * 60 * 3);
		while (true) {
			bulidHighLow();
			ThreadUtils.sleep(1000 * 60 * 3);
		}
	}

	public void bulidHighLow() {
		try {
			if (first) {
				// data数据保存间隔时长(毫秒)
				this.interval = this.sysparaService.find("data_interval").getInteger().intValue() / 1000;
				first = false;
			}
			// 秒
			int num = (24 * 60 * 60) / this.interval;
			List<Item> item_list = itemService.findByType(Item.cryptos);
			for (int i = 0; i < item_list.size(); i++) {
				Item item = item_list.get(i);
				try {

					List<Realtime> history = bulidNum(DataCache.getRealtimeHistory().get(item.getSymbol()), num);
					if (history == null || history.size() == 0) {
						continue;
					}
					Double high = null;
					Double low = null;

					for (int j = 0; j < history.size(); j++) {
						Realtime realtime = history.get(j);

						if (high == null || high < realtime.getClose().doubleValue()) {
							high = realtime.getClose().doubleValue();
						}

						if ((low == null || low > realtime.getClose().doubleValue()) && realtime.getClose() .doubleValue()> 0) {
							low = realtime.getClose().doubleValue();
						}
					}
					if (item == null || item.getSymbol() == null) {
						logger.error("run fail");
					}
					if (high != null) {
						DataCache.getRealtimeHigh().put(item.getSymbol(), high);
					}
					if (low != null && low > 0) {
						DataCache.getRealtimeLow().put(item.getSymbol(), low);
					}

					Collections.sort(history);
					DataCache.getRealtime24HBeforeOpen().put(item.getSymbol(), history.get(0).getClose().doubleValue());

				} catch (Exception e) {
					logger.error("run fail", e);
				}
			}

		} catch (Exception e) {
			logger.error("run fail", e);
		}
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

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

}
