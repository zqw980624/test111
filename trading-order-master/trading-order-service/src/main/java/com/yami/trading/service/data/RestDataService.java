package com.yami.trading.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yami.trading.bean.data.domain.*;
import com.yami.trading.common.util.ApplicationContextUtils;
import com.yami.trading.common.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RestDataService  implements DataService {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<Realtime> realtime(String symbol) {
        DataService dataService;
        try {
             dataService = ApplicationContextUtils.getBean("dataService", DataService.class);
            if(dataService != null){
                return dataService.realtime(symbol);
            }
        }catch (NoSuchBeanDefinitionException exception){
            log.error("获取 dataService 失败", exception);
        }

        return Lists.newArrayList();
    }

    @Override
    public Depth depth(String symbol) {
        DataService dataService;
        try {
            dataService = ApplicationContextUtils.getBean("dataService", DataService.class);
            if(dataService != null){
                return dataService.depth(symbol);
            }
        }catch (NoSuchBeanDefinitionException exception){
            log.error("获取 dataService 失败", exception);
        }
        return new Depth();
    }

    @Override
    public List<Trend> trend(String symbol) {
        DataService dataService;
        try {
            dataService = ApplicationContextUtils.getBean("dataService", DataService.class);
            if(dataService != null){
                return dataService.trend(symbol);
            }
        }catch (NoSuchBeanDefinitionException exception){
            log.error("获取 dataService 失败", exception);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Kline> kline(String symbol, String line) {
        DataService dataService;
        try {
            dataService = ApplicationContextUtils.getBean("dataService", DataService.class);
            if(dataService != null){
                return dataService.kline(symbol, line);
            }
        }catch (NoSuchBeanDefinitionException exception){
            log.error("获取 dataService 失败", exception);
        }
        return new ArrayList<>();
    }

    @Override
    public Trade trade(String symbol) {
        DataService dataService;
        try {
            dataService = ApplicationContextUtils.getBean("dataService", DataService.class);
            if(dataService != null){
                return dataService.trade(symbol);
            }
        }catch (NoSuchBeanDefinitionException exception){
            log.error("获取 dataService 失败", exception);
        }
        return new Trade();
    }


}
