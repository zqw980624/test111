package com.yami.trading.huobi.data.internal;



import com.yami.trading.bean.data.domain.*;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.syspara.SysparaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("dataService")
public class DataServiceImpl  implements DataService {
	private volatile static Map<String, TimeObject> cache = new ConcurrentHashMap<String, TimeObject>();
	@Autowired
	@Qualifier("remoteDataService")
	private DataService remoteDataService;
	@Autowired
	private SysparaService sysparaService;

	/**
	 * 行情实时价格
	 */
	@Override
	public List<Realtime> realtime(String symbols) {
		List<Realtime> list = remoteDataService.realtime(symbols);
		return list;
	}

	/**
	 * Kline
	 */
	@Override
	public List<Kline> kline(String symbol, String line) {
		String key = "kline_" + symbol + "_" + line;
		List<Kline> list = new ArrayList<Kline>();
		TimeObject timeObject = cache.get(key);
		if (isRemote(timeObject)) {
			list = remoteDataService.kline(symbol, line);
			Collections.sort(list); // 按时间升序
			KlineTimeObject klineTimeObject = new KlineTimeObject();
			klineTimeObject.setLastTime(new Date());
			klineTimeObject.setKline(list);
			cache.put(key, klineTimeObject);
		} else {
			list = ((KlineTimeObject) timeObject).getKline();
		}

		return list;

	}

	private boolean isRemote(TimeObject timeObject) {
		if (timeObject == null) {
			return true;
		}

		/**
		 * 判断是否远程 读取数据，先完成3秒过期。后期补上非开盘时间不调用。
		 */
		Date timestamps = timeObject.getLastTime();

		/**
		 * 数据超时时间
		 */
		// 15秒
		//默认3秒
		double huobi_data_timeout = Double.valueOf(sysparaService.find("symbol_data_timeout").getSvalue());

		//int timeout = 3;
		
		int timeout = (int) huobi_data_timeout;
		if (DateUtils.addSecond(timestamps, timeout).before(new Date())) {
			return true;
		}

		return false;
	}

	public void setRemoteDataService(DataService remoteDataService) {
		this.remoteDataService = remoteDataService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	/**
	 * 分时图
	 */
	@Override
	public List<Trend> trend(String symbol) {
		String key = "trend_" + symbol;
		List<Trend> list = new ArrayList<Trend>();
		TimeObject timeObject = cache.get(key);
		if (isRemote(timeObject)) {
			list = remoteDataService.trend(symbol);
			TrendTimeObject trendTimeObject = new TrendTimeObject();
			trendTimeObject.setLastTime(new Date());
			trendTimeObject.setTrend(list);
			cache.put(key, trendTimeObject);
		} else {
			list = ((TrendTimeObject) timeObject).getTrend();
		}

		return list;
	}
	/**
	 * 深度数据
	 */
	@Override
	public Depth depth(String symbol) {
		String key = "depth_" + symbol;
		Depth depth = new Depth();

		TimeObject timeObject = cache.get(key);
		if (isRemote(timeObject)) {
			depth = remoteDataService.depth(symbol);
			DepthTimeObject depthTimeObject = new DepthTimeObject();
			depthTimeObject.setLastTime(new Date());
			depthTimeObject.setDepth(depth);
			cache.put(key, depthTimeObject);
		} else {
			depth = ((DepthTimeObject) timeObject).getDepth();
		}
		depth.setSymbol(symbol);
		return depth;

	}

	/**
	 * 近期交易记录
	 */
	@Override
	public Trade trade(String symbol) {

		String key = "trade_" + symbol;
		Trade trade = new Trade();
		TimeObject timeObject = cache.get(key);
		if (isRemote(timeObject)) {
			trade = remoteDataService.trade(symbol);
			if(trade!=null) {
				TradeTimeObject tradeTimeObject = new TradeTimeObject();
				tradeTimeObject.setLastTime(new Date());
				tradeTimeObject.put(symbol, trade.getData());
				cache.put(key, tradeTimeObject);
			}

		} else {
			trade = ((TradeTimeObject) timeObject).getTrade();
		}

		return trade;
	}
}
