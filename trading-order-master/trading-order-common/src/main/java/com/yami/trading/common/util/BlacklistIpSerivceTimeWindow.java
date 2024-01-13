package com.yami.trading.common.util;

import org.springframework.beans.factory.InitializingBean;

import java.util.Date;

public class BlacklistIpSerivceTimeWindow implements InitializingBean {
	
	private TimeWindow timeWindow = new TimeWindow();

	public void afterPropertiesSet() throws Exception {
		/**
		 * 24小时
		 */
		this.timeWindow.setTimeSize(60 * 60 * 24);
		this.timeWindow.start();
	}

	public String getBlackIp(String key) {
		Object authcode = this.timeWindow.findObject(key);
		if (authcode != null) {
			return String.valueOf(authcode.toString());
		}
		return null;
	}

	public void putBlackIp(String key, String ip) {
		this.timeWindow.add(key, ip);
	}

	public void putBlackIp(String key, String ip, Date date) {
		this.timeWindow.add(key, ip, date);
	}

	public void delBlackIp(String key) {
		this.timeWindow.remove(key);
	}

}
