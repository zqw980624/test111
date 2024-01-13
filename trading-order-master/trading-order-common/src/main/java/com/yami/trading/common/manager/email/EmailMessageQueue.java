package com.yami.trading.common.manager.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EmailMessageQueue {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailMessageQueue.class);

	private static ConcurrentLinkedQueue<EmailMessage> WORKING_EVENTS = new ConcurrentLinkedQueue<EmailMessage>();

	public static void add(EmailMessage item) {
		try {
			WORKING_EVENTS.add(item);
		} catch (Throwable e) {
			logger.error("add(SmsMessage item) fail : ", e);
		}
	}

	public static int size() {
		return WORKING_EVENTS.size();
	}

	public static EmailMessage poll() {
		EmailMessage item = null;
		try {
			item = WORKING_EVENTS.poll();
		} catch (Throwable e) {
			logger.error("SmsMessage poll() fail : ", e);
		}
		return item;
	}
}
