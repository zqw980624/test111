package com.yami.trading.huobi.data.internal;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class TimeObject implements Serializable {
	private static final long serialVersionUID = -7709770878909783696L;

	/**
	 * 最后读取远程数据时间
	 */
	private Date lastTime;

	
}
