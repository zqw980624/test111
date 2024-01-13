/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.trading;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.yami.trading.common.manager.BlacklistIpTimeWindow;
import com.yami.trading.common.manager.SendCountTimeWindow;
import com.yami.trading.common.util.BlacklistIpSerivceTimeWindow;
import com.yami.trading.common.util.LocklistIpSerivceTimeWindow;
import com.yami.trading.service.impl.InternalEmailSenderServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author lgh
 */
@SpringBootApplication
//@ComponentScan("com.yami.trading")
@EnableScheduling
@EnableMethodCache(basePackages = "com.yami.trading")
public class WebApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {

        SpringApplication.run(WebApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {

        return builder.sources(WebApplication.class);
    }

    @Bean
    public InternalEmailSenderServiceImpl internalEmailSenderService(){
        return  new InternalEmailSenderServiceImpl();
    }
    @Bean
    public BlacklistIpSerivceTimeWindow blacklistIpSerivceTimeWindow(){
        return  new BlacklistIpSerivceTimeWindow();
    }


    @Bean
    public LocklistIpSerivceTimeWindow locklistIpSerivceTimeWindow(){
        return  new LocklistIpSerivceTimeWindow();
    }

    @Bean
    public EmailServer emailServer(){
        return  new EmailServer();
    }

    @Bean
    public SmsServer  smsServer(){
        return  new SmsServer();
    }





}
