package com.yami.trading.common.manager.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SmsMessageQueue {
	
	private static final Logger logger = LoggerFactory.getLogger(SmsMessageQueue.class);

	private static ConcurrentLinkedQueue<SmsMessage> WORKING_EVENTS = new ConcurrentLinkedQueue<SmsMessage>();

	public static void add(SmsMessage item) {
		try {

			WORKING_EVENTS.add(item);
		} catch (Throwable e) {
			logger.error("add(SmsMessage item) fail : ", e);
		}
	}

	public static int size() {
		return WORKING_EVENTS.size();
	}

	public static SmsMessage poll() {
		SmsMessage item = null;
		try {
			item = WORKING_EVENTS.poll();
		} catch (Throwable e) {
			logger.error("SmsMessage poll() fail : ", e);
		}
		return item;
	}
}
