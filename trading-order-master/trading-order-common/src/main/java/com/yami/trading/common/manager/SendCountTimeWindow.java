package com.yami.trading.common.manager;

import com.yami.trading.common.util.TimeWindow;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class SendCountTimeWindow  {
	private TimeWindow timeWindow = new TimeWindow();

	public SendCountTimeWindow(){
		/**
		 * 10分钟
		 */
		this.timeWindow.setTimeSize(60 * 10);
		this.timeWindow.start();
	}



	public String getIpSend(String key) {
		Object authcode = this.timeWindow.findObject(key);
		if (authcode != null) {
			return String.valueOf(authcode.toString());
		}
		return null;
	}

	public void putIpSend(String key, String ip) {
		this.timeWindow.add(key, ip);
	}

	public void delIpSend(String key) {
		this.timeWindow.remove(key);
	}
}
