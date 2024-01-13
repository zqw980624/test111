package com.yami.trading.huobi.data.internal;


import com.yami.trading.bean.data.domain.Realtime;

import java.util.List;

public interface DataDBService {
	/**
	 * 异步保存
	 */
	public void saveAsyn(Realtime entity);

	/**
	 * 数据库最新的实时价格
	 */
	public Realtime get(String symbol);

	/**
	 * 批量保存
	 */
	public void saveBatch(List<Realtime> entities);

	public List<Realtime> findRealtimeOneDay(String symbol);

	/**
	 * 删除数据库里Realtime
	 */
	public void deleteRealtime(int days);

	public void updateOptimize(String table);
	/**
	 * 获取最新60s实时价格数据
	 */
	List<Realtime> listRealTime60s(String symbol);

}
