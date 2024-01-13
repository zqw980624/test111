package com.yami.trading.admin.config;

import org.springframework.stereotype.Component;


@Component
public class ThreadPoolTaskExecutor extends org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor {

    private static final long serialVersionUID = 119645098645321188L;

    /**
     * 	<property name="corePoolSize" value="4" />
     * 		<property name="keepAliveSeconds" value="60" />
     * 		<property name="maxPoolSize" value="20" />
     * 		<property name="queueCapacity" value="0" />
     */
    public ThreadPoolTaskExecutor(){
        this.setCorePoolSize(4);
        this.setKeepAliveSeconds(60);
        this.setMaxPoolSize(20);
        this.setQueueCapacity(0);
        this.setRejectedExecutionHandler(new BalanceOffLineEventRejectExecutingHandler());

    }

    @Override
    public void execute(Runnable runnable) {
        super.execute(runnable);
    }
}
