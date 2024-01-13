package com.yami.trading.service.future;

import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.future.domain.FuturesPara;
import com.yami.trading.bean.future.domain.FuturesRedisKeys;
import com.yami.trading.bean.future.domain.ProfitLossConfig;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class FuturesLoadCacheService  {
	@Autowired
	private FuturesOrderService futuresOrderService;


	@Autowired
	private FuturesParaService futuresParaService;
	@Autowired
	private ProfitLossConfigService profitLossConfigService;

	@Autowired
	private WalletService assetService;

	public void loadcache() {
		load();
		log.info("完成FuturesPara数据加载redis");

		loadFuturesOrder();
		log.info("完成FuturesOrder数据加载redis");

		loadProfitAndLossConfig();
		log.info("完成ProfitAndLossConfig数据加载redis");
	}

	public void load() {
		List<FuturesPara> list = futuresParaService.list();

		Map<String, Map<String, FuturesPara>> cacheMap = new ConcurrentHashMap<String, Map<String, FuturesPara>>();

		for (FuturesPara para : list) {
			if (cacheMap.containsKey(para.getSymbol())) {
				Map<String, FuturesPara> map = cacheMap.get(para.getSymbol());
				map.put(para.getUuid().toString(), para);
				cacheMap.put(para.getSymbol(), map);
			} else {
				Map<String, FuturesPara> map = new ConcurrentHashMap<String, FuturesPara>();
				map.put(para.getUuid().toString(), para);
				cacheMap.put(para.getSymbol(), map);
			}
			RedisUtil.set(FuturesRedisKeys.FUTURES_PARA_ID + para.getUuid(), para);
		}
		for (Entry<String, Map<String, FuturesPara>> entry : cacheMap.entrySet()) {
			RedisUtil.set(FuturesRedisKeys.FUTURES_PARA_SYMBOL + entry.getKey(), entry.getValue());
		}
	}

	public void loadFuturesOrder() {
		List<FuturesOrder> list = futuresOrderService.findSubmitted(null);
		// 交割合约：总资产、总未实现盈利
		Map<String, Map<String, Double>> futuresAssetsMap = new ConcurrentHashMap<String, Map<String, Double>>();

		for (FuturesOrder order : list) {
			RedisUtil.set(FuturesRedisKeys.FUTURES_SUBMITTED_ORDERNO + order.getOrderNo(), order);
//			map.put(order.getOrder_no(), order);

			// 获取 单个订单 交割合约总资产、总未实现盈利
			Map<String, Double> futuresAssetsOrder = this.assetService.getMoneyFuturesByOrder(order);

			if (futuresAssetsMap.containsKey(order.getPartyId())) {
				Map<String, Double> futuresAssetsOld = futuresAssetsMap.get(order.getPartyId().toString());
				if (null == futuresAssetsOld) {
					futuresAssetsOld = new HashMap<String, Double>();
					futuresAssetsOld.put("money_futures", 0.000D);
					futuresAssetsOld.put("money_futures_profit", 0.000D);
				}
				futuresAssetsOld.put("money_futures", Arith.add(futuresAssetsOld.get("money_futures"), futuresAssetsOrder.get("money_futures")));
				futuresAssetsOld.put("money_futures_profit", Arith.add(futuresAssetsOld.get("money_futures_profit"), futuresAssetsOrder.get("money_futures_profit")));
				futuresAssetsMap.put(order.getPartyId().toString(), futuresAssetsOld);
			} else {
				futuresAssetsMap.put(order.getPartyId().toString(), futuresAssetsOrder);
			}
		}

//		redisHandler.setSync(FuturesRedisKeys.FUTURES_SUBMITTED_MAP, map);

		for (Entry<String, Map<String, Double>> entry : futuresAssetsMap.entrySet()) {
			RedisUtil.set(FuturesRedisKeys.FUTURES_ASSETS_PARTY_ID + entry.getKey(), entry.getValue().get("money_futures"));
			RedisUtil.set(FuturesRedisKeys.FUTURES_ASSETS_PROFIT_PARTY_ID + entry.getKey(), entry.getValue().get("money_futures_profit"));
		}
	}

	public void loadProfitAndLossConfig() {
		List<ProfitLossConfig> list = profitLossConfigService.list();
		for (ProfitLossConfig config : list) {
			RedisUtil.set(FuturesRedisKeys.FUTURES_PROFIT_LOSS_PARTY_ID + config.getPartyId().toString(),
					config);
		}
	}

}
