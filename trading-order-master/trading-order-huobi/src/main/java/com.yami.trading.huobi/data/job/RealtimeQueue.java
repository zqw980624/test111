package com.yami.trading.huobi.data.job;

import com.yami.trading.bean.data.domain.Realtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RealtimeQueue {
	
	private static Logger logger = LoggerFactory.getLogger(RealtimeQueue.class);

	private static ConcurrentLinkedQueue<Realtime> WORKING_EVENTS = new ConcurrentLinkedQueue<Realtime>();

	public static void add(Realtime item) {
		Assert.notNull(item, "The item must not be null.");
		try {
			WORKING_EVENTS.add(item);
		} catch (Throwable e) {
			logger.error("add() fail : ", e);
		}
	}

	public static int size() {
		return WORKING_EVENTS.size();
	}

	public static Realtime poll() {
		Realtime item = null;
		try {
			item = WORKING_EVENTS.poll();
		} catch (Throwable e) {
			logger.error("poll() fail : ", e);
		}
		return item;
	}
}
