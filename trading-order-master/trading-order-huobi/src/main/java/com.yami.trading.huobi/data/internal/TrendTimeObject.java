package com.yami.trading.huobi.data.internal;

import com.yami.trading.bean.data.domain.Trend;

import java.util.List;



public class TrendTimeObject  extends TimeObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4078190280148061255L;

	private List<Trend> trend;

	public List<Trend> getTrend() {
		return trend;
	}

	public void setTrend(List<Trend> trend) {
		this.trend = trend;
	}
}
