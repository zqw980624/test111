package com.yami.trading.huobi.data.internal;


import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;

import java.util.List;
import java.util.Map;

public interface KlineService {
    public void repairKline(Kline kline) ;

    public void formatPoint(Kline kline) ;

	public void delete(String line, int days);

	public Kline bulidKline(Realtime realtime, Kline lastOne, Kline hobiOne, String line) ;

	/**
	 * 管理后台初始化K线
	 */
	void saveInit(String symbol, Map<String, List<Kline>> dailyWeekMonthHistoryMap, Map<String, List<Kline>> hourlyAndMinuteHistoryMap);

	/**
	 * 查询所有K线
	 */
	List<Kline> find(String symbol, String line, int pageSie);



		Realtime findLatestRealtime(String symbol);

	/**
	 * 构建1分钟Kline数据
	 */
	void saveKline1Minute(String symbol, String line);

	/**
	 * 构建5分钟Kline数据
	 */
	void saveKline5Minute(String symbol, String line);

	/**
	 * 构建15分钟Kline数据
	 */
	void saveKline15Minute(String symbol, String line);

	/**
	 * 构建30分钟Kline数据
	 */
	void saveKline30Minute(String symbol, String line);

	/**
	 * 构建60分钟Kline数据
	 */
	void saveKline60Minute(String symbol, String line);

	/**
	 * 构建4小时Kline数据
	 */
	void saveKline4Hour(String symbol, String line);
	/**
	 * 构建2小时Kline数据
	 */
	void saveKline2Hour(String symbol, String line);
	/**
	 * 构建1天Kline数据
	 */
	void saveKline1Day(String symbol, String line);
    /**
     * 构建5天Kline数据
     */
    void saveKline5Day(String symbol, String line);
	/**
	 * 构建1周Kline数据
	 */
	void saveKline1Week(String symbol, String line);

	/**
	 * 构建1月Kline数据
	 */
	void saveKline1Mon(String symbol, String line);
	/**
	 * 构建1月Kline数据
	 */
	void saveKlineQuarter(String symbol, String line);
	void saveKlineYear(String symbol, String line);

	//=================================================================================

	Kline bulidKline1Minute(Realtime realtime, String line);

	Kline bulidKline5Minute(Realtime realtime, String line);

	Kline bulidKline15Minute(Realtime realtime, String line);

	Kline bulidKline30Minute(Realtime realtime, String line);

	Kline bulidKline60Minute(Realtime realtime, String line);

	Kline bulidKline4Hour(Realtime realtime, String line);

	Kline bulidKline1Day(Realtime realtime, String line);

    Kline bulidKline5Day(Realtime realtime, String line);
	Kline bulidKline1Week(Realtime realtime, String line);

	Kline bulidKline1Mon(Realtime realtime, String line);
	Kline bulidKlineQuarter(Realtime realtime, String line);
	Kline bulidKlineYear(Realtime realtime, String line);
	public void smoothlyKline(Kline kline,  double probability);

	void clean();

	public List<Kline> calculateKline(String symbol, int seq, String period, List<Kline> klineList) ;

	}
