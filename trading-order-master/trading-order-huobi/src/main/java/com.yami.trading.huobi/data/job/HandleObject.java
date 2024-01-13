package com.yami.trading.huobi.data.job;


import com.yami.trading.bean.item.domain.Item;

public class HandleObject {

	private String type;
	private Item item;

	public static String type_depth = "depth";

	public static String type_trade = "trade";
	/**
	 * K线图的参数line
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

}
