package com.yami.trading;


import com.yami.trading.common.manager.email.EmailMessage;
import com.yami.trading.common.manager.email.EmailMessageQueue;
import com.yami.trading.common.util.OffLineEventRejectExecutingHandler;
import com.yami.trading.common.util.RejectExecutionHandlerDelegator;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.InternalEmailSenderService;
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
 * 邮件服务类，负责从短信消息队列取出短信消息并发送
 */
public class EmailServer implements Runnable , InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(EmailServer.class);


    @Autowired
    private InternalEmailSenderService internalEmailSenderService;
    private ThreadPoolTaskExecutor taskExecutor;


    public EmailServer(){
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
     * 服务运行：
     * 1. 从消息队列获取message
     * 2.调用currentProvider发送短信
     */
    public void run() {
        while (true) {

            try {
                EmailMessage item = EmailMessageQueue.poll();

                if (item != null) {
                	System.out.println("邮寄地址：" + item.getTomail());
                    taskExecutor.execute(new HandleRunner(item));
                }
                else {
                	/*
                	 * 限速，最多1秒20个
                	 */
                    ThreadUtils.sleep(50);
                }

            } catch (Throwable e) {
                logger.error("EmailServer taskExecutor.execute() fail", e);

            }
        }
    }

    public class HandleRunner implements Runnable {
        private EmailMessage item;

        public HandleRunner(EmailMessage item) {
            this.item = item;
        }

        public void run() {
            try {
            	internalEmailSenderService.send(item);
            } catch (Throwable t) {
            	 logger.error("EmailServer taskExecutor.execute() fail", t);
            }
           
        }

    }


    public void afterPropertiesSet() throws Exception {

        new Thread(this, "EmailServer").start();
        if (logger.isInfoEnabled()) {
            logger.info("启动邮件发送服务！");
        }

    }
	public void setInternalEmailSenderService(InternalEmailSenderService internalEmailSenderService) {
		this.internalEmailSenderService = internalEmailSenderService;
	}

}
