package com.yami.trading.service.data;


import com.yami.trading.bean.data.domain.*;

import java.util.List;

public interface DataService {
	/**
	 * 实时价格
	 *
	 * @param symbol 指定产品代码，多个用逗号分割，最大100个
	 *
	 */
	public List<Realtime> realtime(String symbol);

	/**
	 * 市场深度数据
	 */
	public Depth depth(String symbol);

	/**
	 * 分时
	 */
	public List<Trend> trend(String symbol);

	/**
	 * K线
	 *
	 */
	public List<Kline> kline(String symbol, String line);

	/**
	 * 获得近期交易记录
	 */
	public Trade trade(String symbol);

}
