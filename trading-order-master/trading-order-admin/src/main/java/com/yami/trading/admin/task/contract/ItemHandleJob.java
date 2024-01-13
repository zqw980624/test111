package com.yami.trading.admin.task.contract;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yami.trading.api.util.HttpClientRequest;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.item.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ItemHandleJob implements Runnable{
    @Autowired
    private ItemService itemService;
    @Autowired
    RedisTemplate redisTemplate;
    public void run() {
        /*
         * 系统启动先暂停30秒
         */
        ThreadUtils.sleep(1000 * 20);
        while (true)
            try {
                String ids = (String)redisTemplate.opsForValue().get("idYDstocks");
                Map<String, Object> param = new HashMap<>();
                param.put("pid", ids.substring(0, ids.length() - 1));//印度
                String results = null;
                try {
                    results = HttpClientRequest.doPost("http://api-in.js-stock.top/stock?key=zHPU8uWYMY7eWx78kbC0" , param);
                    log.info("印度股票实施数据");
                    //log.info("印度股票代码返回结果result {} ", results);
                } catch (Exception e) {
                    log.error("获取印度股行情信息失败，重新获取", e);
                }
                if (results == null) {
                    log.error("获取印度股行情信息失败");
                    return;
                }
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<RealtimeDTO>>() {
                }.getType();
                ArrayList<RealtimeDTO> list = gson.fromJson(results, type);
                for (RealtimeDTO jsonData : list) {
                    redisTemplate.opsForValue().set("ydTask" + jsonData.getPid(), jsonData);
                }
            } catch (Exception e) {
                log.error("run fail", e);
            } finally {
               ThreadUtils.sleep(50 * 1);
            }
    }

    public void start(){
        new Thread(this, "itemHandleJob").start();
        if (log.isInfoEnabled())
            log.info("实施行情！");
    }
}
