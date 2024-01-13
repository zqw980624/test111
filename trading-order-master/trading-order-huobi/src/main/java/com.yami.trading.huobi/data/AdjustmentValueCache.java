package com.yami.trading.huobi.data;

import com.yami.trading.huobi.data.model.AdjustmentValue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdjustmentValueCache {
	/**
	 * 当前值
	 */
	private static volatile Map<String, BigDecimal> currentValue = new ConcurrentHashMap();
	/**
	 * 延时值
	 */
	private static volatile Map<String, AdjustmentValue> delayValue = new ConcurrentHashMap();

	public static Map<String, BigDecimal> getCurrentValue() {
		return currentValue;
	}

	public static Map<String, AdjustmentValue> getDelayValue() {
		return delayValue;
	}

}
