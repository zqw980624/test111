package com.yami.trading.admin.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yami.trading.api.util.HttpClientRequest;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTOS;
import com.yami.trading.common.util.BuyAndSellUtils;
import com.yami.trading.service.item.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@Lazy(value = false)
@Slf4j
public class AStockPanKouTask {
    @Autowired
    private DepthPushJob depthPushJob;
    @Autowired
    private ItemService itemService;
    @Autowired
    RedisTemplate redisTemplate;

   /* public void ydTask() {//加载印度股票实时行情
            String varCode = "";
            List<Item> items = itemService.findByItem("YD-stocks");
            for (Item record : items) {
                varCode = varCode + record.getPid() + ",";
            }
            redisTemplate.opsForValue().set("idYDstocks", varCode);
    }*/

    //印度股票 列表
    @Scheduled(cron = "0 15 9,10 * * MON-FRI")
    public void ydTaskList() {
        for(int i=0;i<=11;i++){
            String result = null;
            try {
                result = HttpClientRequest.doGet("http://api-in.js-stock.top/list?country_id=14&size=600&page="+ i + "&key=zHPU8uWYMY7eWx78kbC0&bse_code_en=1");
                log.info("印度股票代码IDresult {} ", result);
            } catch (Exception e) {
                log.error("获取印度股信息失败，重新获取", e);
            }
            if (result == null) {
                log.error("获取印度股信息失败");
                return;
            }
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(result);
            //取出data里的数组
            ArrayList diff = (ArrayList) json.get("data");
            for (Object o : diff) {
                //取数据存入redisDB1
                com.alibaba.fastjson2.JSONObject data = (JSONObject) o;
                String stockCode = data.getString("Symbol");
                String ids = data.getString("Id");
                //存入redis
               redisTemplate.opsForValue().set("yd" + data.getString("type").toUpperCase() + "_" + stockCode+ids, String.valueOf(data));
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("印股信息存入redis成功");
    }

    @PostConstruct
    public void init() {
         //this.ydTask();
        //this.ydTaskList();//印股list 列表数据 每天加载一次
        log.info("=======================数据加载完毕============================");
    }
}
