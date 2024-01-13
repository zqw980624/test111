package com.yami.trading.huobi.data.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 修正值
 *
 */
public class AdjustmentValue implements Serializable {
	private static final long serialVersionUID = 2896031576741063236L;
	private String symbol;
	/**
	 * 修正值
	 */
	private BigDecimal value;
	/**
	 * 延长时间，秒
	 */
	private double second;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public double getSecond() {
		return second;
	}

	public void setSecond(double second) {
		this.second = second;
	}

}
