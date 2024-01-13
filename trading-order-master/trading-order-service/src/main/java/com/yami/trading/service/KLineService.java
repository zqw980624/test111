package com.yami.trading.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.yami.trading.bean.entity.KLine;
import com.yami.trading.bean.entity.KLineMeta;
import com.yami.trading.bean.entity.KLineValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KLineService {
    @Autowired
    RedisTemplate redisTemplate;
    String formatKey(String symbol, String interval) {
        return StrUtil.format("{}_{}", symbol, interval);
    }

    public void writeRow(KLine kline) {
        if (CollUtil.isEmpty(kline.getValues())) {
            return;
        }
        KLineMeta meta = kline.getMeta();
        if (StrUtil.isBlank(meta.getSymbol())) {
            return;
        }
        String key = this.formatKey(meta.getSymbol(), meta.getInterval());
        for (KLineValue value : kline.getValues()) {
            redisTemplate.opsForHash()
                    .put(key, value.getDatetime(), value);
        }
    }

    public KLineValue readRow(String symbol, String interval, String time) {
        String key = this.formatKey(symbol, interval);
        Object result = redisTemplate.opsForHash().get(key, time);
        if (!(result instanceof KLineValue)) {
            return null;
        }
        return (KLineValue) result;
    }

    public void writeMeta(KLine kline) {
        KLineMeta meta = kline.getMeta();
        if (StrUtil.isBlank(meta.getSymbol())) {
            return;
        }
        String key = this.formatKey(meta.getSymbol(), meta.getInterval()) + "_meta";

        redisTemplate.opsForValue().set(key, meta);
    }

    private KLineMeta readMeta(String symbol, String interval) {
        if (StrUtil.isBlank(symbol)) {
            return null;
        }
        String key = this.formatKey(symbol, interval) + "_meta";
        Object result = redisTemplate.opsForValue().get(key);
        if (!(result instanceof KLineMeta)) {
            return null;
        }
        return (KLineMeta) result;
    }

    String url = "http://hk.yufenghy.cn/hq/api/k/time_series/{}?interval={}&symbol={}&outputsize=5000";
    String key = "YUFENGSHUJU23231211";
    public List readKLine(String interval, String ticket) {
        KLine kline = new KLine();
        KLineMeta meta = this.readMeta(ticket, interval);
        String key = this.formatKey(ticket, interval);
        if (meta == null) {
            String fullUrl = StrUtil.format(url, this.key, interval, ticket);
            String response = HttpUtil.createGet(fullUrl)
                    .execute()
                    .body();
            KLine res = JSONUtil.toBean(response, KLine.class);

            kline.setMeta(res.getMeta());
            this.writeMeta(res);
            for (KLineValue value : res.getValues()) {
                if (redisTemplate.opsForHash().hasKey(key, value.getDatetime())) {
                    continue;
                }
                redisTemplate.opsForHash().put(key, value.getDatetime(), value);
            }
       }
        Map<String, KLineValue> values = redisTemplate.opsForHash().entries(key);

        kline.setMeta(meta);
        List<KLineValue> valueList = new ArrayList<>();
        for (String k : values.keySet()) {
            valueList.add(values.get(k));
        }
        kline.setValues(valueList);

        String str = JSONUtil.toJsonStr(kline);
        JSONObject respJson=JSONObject.parseObject(str);
        JSONArray value = JSON.parseArray(respJson.getString("values"));

        String s = JsonOrderByDate(value.toString());
        s = s.replace("datetime", "timestamp");

        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(s, JsonArray.class);
        List<Object> list = gson.fromJson(jsonArray, new TypeToken<List<Object>>(){}.getType());
        return list;
    }

    public static String JsonOrderByDate(String jsonArrStr) {
        JSONArray jsonArr = JSON.parseArray(jsonArrStr);
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        Collections.sort(jsonValues, new Comparator() {
            public int compare(Object a, Object b) {
                JSONObject jsonObject1 = (JSONObject) a;
                JSONObject jsonObject2 = (JSONObject) b;
                String time1 = jsonObject1.getString("datetime");
                String time2 = jsonObject2.getString("datetime");
                return time1.compareTo(time2);
            }
        });
        for (int i = 0; i < jsonArr.size(); i++) {
            sortedJsonArray.add(jsonValues.get(i));
        }
        return sortedJsonArray.toString();
    }


    public void writeKLine(String symbol, String interval, String time, Double price, Double volume) {
        try {
            String key = this.formatKey(symbol, interval);
            KLineValue value = this.readRow(symbol, interval, time);
            if (value == null) {
                value = new KLineValue();
            }
            value.setDatetime(time);
            value.setClose(price);
            if (value.getOpen() == null) {
                value.setOpen(price);
            }
            if (value.getHigh() == null || value.getHigh() < price) {
                value.setHigh(price);
            }
            if (value.getLow() == null || value.getLow() > price) {
                value.setLow(price);
            }
            value.setVolume(volume);
            redisTemplate.opsForHash().put(key, time, value);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public List readKLines(String interval, String ticket) {
        KLine kline = new KLine();
        KLineMeta meta = this.readMeta(ticket, interval);
        String key = this.formatKey(ticket, interval);
        if (meta == null) {
            String fullUrl = StrUtil.format(url, this.key, interval, ticket);
            String response = HttpUtil.createGet(fullUrl)
                    .execute()
                    .body();
            KLine res = JSONUtil.toBean(response, KLine.class);
            kline.setMeta(res.getMeta());
            this.writeMeta(res);
            for (KLineValue value : res.getValues()) {
                if (redisTemplate.opsForHash().hasKey(key, value.getDatetime())) {
                    continue;
                }
                redisTemplate.opsForHash().put(key, value.getDatetime(), value);
            }
        }
        Map<String, KLineValue> values = redisTemplate.opsForHash().entries(key);
        kline.setMeta(meta);
        List<KLineValue> valueList = new ArrayList<>();
        for (String k : values.keySet()) {
            valueList.add(values.get(k));
        }
        kline.setValues(valueList);
        String str = JSONUtil.toJsonStr(kline);
        JSONObject respJson=JSONObject.parseObject(str);
        JSONArray value = JSON.parseArray(respJson.getString("values"));
        String s = JsonOrderByDate(value.toString());
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(s, JsonArray.class);
        List<Object> list = gson.fromJson(jsonArray, new TypeToken<List<Object>>(){}.getType());
        return list;
    }
}
