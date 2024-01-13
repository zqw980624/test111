package com.yami.trading.common.config;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ThreadPoolComponent {
    private ThreadPoolTaskExecutor executor;

    public ThreadPoolComponent() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(32); // 设置核心线程数
        executor.setMaxPoolSize(32); // 设置最大线程数
        executor.setQueueCapacity(10000); // 设置队列容量
        executor.setThreadNamePrefix("ThreadPoolComponent-"); // 设置线程名称前缀
        executor.initialize();
    }

    public ThreadPoolTaskExecutor getExecutor() {
        return executor;
    }
}
