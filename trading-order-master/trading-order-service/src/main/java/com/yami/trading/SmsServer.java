package com.yami.trading;

import com.yami.trading.common.manager.sms.SmsMessage;
import com.yami.trading.common.manager.sms.SmsMessageQueue;
import com.yami.trading.common.util.OffLineEventRejectExecutingHandler;
import com.yami.trading.common.util.RejectExecutionHandlerDelegator;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.InternalSmsSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * 短信服务类，负责从短信消息队列取出短信消息并发送
 */
public class SmsServer implements InitializingBean, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SmsServer.class);

	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private InternalSmsSenderService internalSmsSenderService;

	public SmsServer(){
		taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(2);
		taskExecutor.setKeepAliveSeconds(60);
		taskExecutor.setMaxPoolSize(5);
		taskExecutor.setQueueCapacity(3000);
		taskExecutor.initialize();
		RejectExecutionHandlerDelegator rejectExecutionHandlerDelegator = new RejectExecutionHandlerDelegator();
		Collection<RejectedExecutionHandler> list=new ArrayList<>();
		list.add(new OffLineEventRejectExecutingHandler());
		rejectExecutionHandlerDelegator.setRejectExecutionHandlers(list);
		taskExecutor.setRejectedExecutionHandler(rejectExecutionHandlerDelegator);
	}

	/**
	 * 服务运行： 1. 从消息队列获取message 2.调用currentProvider发送短信
	 */
	public void run() {
		while (true) {

			try {
				SmsMessage  item = SmsMessageQueue.poll();

				if (item != null) {

					taskExecutor.execute(new HandleRunner(item));
				} else {
					/*
					 * 限速，最多1秒2个
					 */
					ThreadUtils.sleep(500);
				}

			} catch (Throwable e) {
				logger.error("SmsServer taskExecutor.execute() fail", e);

			}
		}
	}

	public class HandleRunner implements Runnable {
		private SmsMessage item;

		public HandleRunner(SmsMessage item) {
			this.item = item;
		}

		public void run() {
			try {
				internalSmsSenderService.send(item);
			} catch (Throwable t) {
				logger.error("SmsServer taskExecutor.execute() fail", t);
			}

		}

	}

	public void afterPropertiesSet() throws Exception {

		new Thread(this, "SmsbaoServer").start();
		if (logger.isInfoEnabled()) {
			logger.info("启动短信(Smsbao)服务！");
		}

	}


}
