package com.yami.trading.bean.data.domain;

import org.jetbrains.annotations.NotNull;

/**
 * An depth entry consisting of price and amount.
 */
public class DepthEntry  implements Comparable<DepthEntry> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2177789072570310524L;
	
	private Double price;
	
	private Double amount;

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Override
	public int compareTo(DepthEntry depthEntry) {
		if (this.price > depthEntry.getPrice()) {
			return 1;
		} else if (this.price < depthEntry.getPrice()) {
			return -1;
		}
		return 0;
	}
}
