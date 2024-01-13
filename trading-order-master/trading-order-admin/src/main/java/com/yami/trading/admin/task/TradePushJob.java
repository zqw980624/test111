package com.yami.trading.admin.task;

import com.alibaba.fastjson.JSONObject;

import com.yami.trading.api.websocket.WebSocketServer;
import com.yami.trading.api.websocket.WebSocketSession;
import com.yami.trading.bean.data.domain.Trade;
import com.yami.trading.bean.data.domain.TradeEntry;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.RandomUtil;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.TradeTimeObject;
import com.yami.trading.huobi.data.job.DataQueue;
import com.yami.trading.huobi.data.job.HandleObject;
import com.yami.trading.service.item.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 近期交易记录
 *
 */
@Component
@Slf4j
public class TradePushJob implements Runnable {

	@Autowired
	private ItemService itemService;

	public void start() {
		new Thread(this, "tradePushJob").start();
		if (log.isInfoEnabled())
			log.info("启动tradePushJob！");
	}

	public void run() {

		while (true) {
			try {
				this.realtimeHandle();
			} catch (Exception e) {
				log.error("run fail", e);
			} finally {
				ThreadUtils.sleep(500);
			}
		}

	}

	private void realtimeHandle() {
		try {
			// 数据处理
			ResultObject tradeResult = new ResultObject();
			HashMap<Object, Object> data = new HashMap<>();
			data.put("data", Lists.newArrayList());
			tradeResult.setData(data);
			Map<String, String> tradeResultMap = new HashMap<>();
			
			if (!WebSocketServer.tradeMap.isEmpty()) {

				// 客户端请求的所有币种，去重集合
				Set<String> symbolSet = new HashSet<String>();
				for (String socketKey : WebSocketServer.tradeMap.keySet()) {
					WebSocketSession webSocketSession = WebSocketServer.tradeMap.get(socketKey);
					String symbolKey = webSocketSession.getParam();
					symbolSet.add(symbolKey);
				}
				
				for (String symbol : symbolSet) {
					data.put("symbol", symbol);
					 TradeTimeObject trade = DataCache.getTrade().get(symbol);
					if (null != trade && null != trade.getTrade()) {
						Trade tradeData = trade.getTrade();
						if(itemService.findBySymbol(symbol).getType().equalsIgnoreCase(Item.cryptos)){
							tradeResult.setData(tradeRevise(tradeData, symbol));
						}else{
							Map<String, Object> result =  new HashMap<>();
							result.put("symbol", symbol);
							result.put("ts", tradeData.getTs());
							result.put("data", tradeData.getData());
							tradeResult.setData(result);
						}
					} else {
						Item item = this.itemService.findBySymbol(symbol);
						HandleObject handleObject = new HandleObject();
						handleObject.setType(HandleObject.type_trade);
						handleObject.setItem(item);
						DataQueue.add(handleObject);
					}
					tradeResultMap.put(symbol, JSONObject.toJSONString(tradeResult));
					// System.out.println("交易记录 推送 " + JSONObject.toJSONString(tradeResult));
				}
				
				if (tradeResultMap.isEmpty()) {
					return;
				}
				
				for (String socketKey : WebSocketServer.tradeMap.keySet()) {
//					long timeMillins = System.currentTimeMillis();
					//WebSocketServer server = WebSocketServer.tradeMap.get(socketKey);
//					if (server.getTimeStr() != 0 && timeMillins > server.getTimeStr()) {
//						server.onClose();
//						return;
//					}
					WebSocketSession webSocketSession = WebSocketServer.tradeMap.get(socketKey);

					String type = webSocketSession.getType();
					String symbolKey = webSocketSession.getParam();
					WebSocketServer.sendToMessageById(socketKey, tradeResultMap.get(symbolKey), type);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 近期交易记录解析
	 */
	private Map<String, Object> tradeRevise(Trade data, String symbol) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("symbol", symbol);
		map.put("ts", data.getTs());
		Item item = this.itemService.findBySymbol(data.getSymbol());
		List<Map<String, Object>> tradeEntry_list = new ArrayList<Map<String, Object>>();
		
		for (int i = 0; i < data.getData().size(); i++) {
			TradeEntry tradeEntry = data.getData().get(i);
			Map<String, Object> tradeEntry_map = new HashMap<String, Object>();
			String direction = tradeEntry.getDirection();
			Random random = new Random();
			if (random.nextBoolean()) {
				if ("buy".equals(direction)) {
					direction = "sell";
				}else {
					direction = "buy";
				}
			}
			tradeEntry_map.put("direction", direction);
			tradeEntry_map.put("ts", tradeEntry.getTs());
			tradeEntry_map.put("current_time", tradeEntry.getCurrent_time());
			
			double addPriceValue = getRandomValue((int)tradeEntry.getPrice().doubleValue());
			double addAmountValue = getRandomValue((int)tradeEntry.getAmount().doubleValue());
			
			double price = Arith.add(tradeEntry.getPrice(), addPriceValue);
			double amount = Arith.add(tradeEntry.getAmount(), addAmountValue);

			if (item.getDecimals() == null || item.getDecimals() < 0) {
				tradeEntry_map.put("price", price);
				tradeEntry_map.put("amount", amount);
			} else {
				String format = "";
				if (item.getDecimals() == 0) {
					format = "#";
				} else {
					format = "#.";
					for (int j = 0; j < item.getDecimals(); j++) {
						format = format + "#";
					}
				}

				DecimalFormat df = new DecimalFormat(format);
				df.setRoundingMode(RoundingMode.FLOOR);// 向下取整

				tradeEntry_map.put("price", df.format(price));
				tradeEntry_map.put("amount", df.format(amount));
			}
			tradeEntry_list.add(tradeEntry_map);

		}
		map.put("data", tradeEntry_list);
		return map;
	}
	
	private double getRandomValue(int value) {
		double addValue;
		if (value > 0) {
		    int count = 0;
		    while (value > 0) {
		    	value = value / 10;
		        count++;
		    }
		    // 个
		    if (count == 1) {
		    	addValue = RandomUtil.randomFloat(0.01, 0.1999, 4);
		    	return addValue;
		    } 
		    // 十
		    if (count == 2) {
		    	addValue = RandomUtil.randomFloat(0.1, 0.5999, 4);
		    	return addValue;
		    } 
		    // 百
		    if (count == 3) {
		    	addValue = RandomUtil.randomFloat(0.1, 2.9999, 4);
		    	return addValue;
		    } 
		    // 千
		    if (count == 4) {
		    	addValue = RandomUtil.randomFloat(1, 3.9999, 4);
		    	return addValue;
		    } 
		    // 万
		    if (count == 5) {
		    	addValue = RandomUtil.randomFloat(1, 5.9999, 4);
		    	return addValue;
		    } 
		    // 十万
		    else {
		    	addValue = RandomUtil.randomFloat(1, 5.9999, 4);
		    	return addValue;
		    }
		} else {
			addValue = RandomUtil.randomFloat(0.01, 0.2999, 4);
			return addValue;
		}
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
	
}
