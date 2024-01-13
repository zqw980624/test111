package com.yami.trading.huobi.data.internal;

import com.yami.trading.huobi.data.model.AdjustmentValue;

import java.math.BigDecimal;

public interface AdjustmentValueService {
	/**
	 * 调整
	 */
	public void adjust(String symbol, BigDecimal value, double second);

	public BigDecimal getCurrentValue(String symbol);

	public AdjustmentValue getDelayValue(String symbol);

}
