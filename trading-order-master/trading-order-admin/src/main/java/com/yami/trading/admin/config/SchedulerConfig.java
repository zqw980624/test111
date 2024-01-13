package com.yami.trading.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    private static final int POOL_SIZE = 40; // 设置线程池大小

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(POOL_SIZE);
        taskScheduler.setThreadNamePrefix("spider-");
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}