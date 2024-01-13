package com.yami.trading.common.manager;

import com.yami.trading.common.util.TimeWindow;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;


@Component
public class BlacklistIpTimeWindow{
	private TimeWindow timeWindow = new TimeWindow();

	 public  BlacklistIpTimeWindow(){
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

	public void delBlackIp(String key) {
		this.timeWindow.remove(key);
	}
}
