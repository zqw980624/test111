package com.yami.trading.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yami.trading.api.util.HttpClientRequest;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Klines;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.huobi.data.internal.KlineService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * K线图
 */
@RestController
@CrossOrigin
@Api(tags = "K线图实时行情")
public class KlineController {

    public static final String HOBI = "/api/hobi!";
    private static Logger logger = LoggerFactory.getLogger(KlineController.class);
    @Autowired
    private KlineService klineService;
    @Autowired
    private ItemService itemService;
    @Autowired
    @Qualifier("dataService")
    private DataService dataService;

    @ApiOperation(value = "行情")
    @GetMapping(HOBI + "getKline.action")
    public Result<List<Map<String, Object>>> getKline(@RequestParam String symbol, @RequestParam String line) {
        // 1min, 5min, 15min, 30min, 60min, 4hour, 1day, 1mon, 1week
        try {
            if ("1quarter".equalsIgnoreCase(line)) {
                line = Kline.PERIOD_QUARTER;
            }
            if ("1year".equalsIgnoreCase(line)) {
                line = Kline.PERIOD_YEAR;
            }
            // 数据处理
            List<Kline> data = this.dataService.kline(symbol, line);
            if ("1day".equals(line) || "5day".equals(line) || "1mon".equals(line) || "1week".equals(line) || "quarter".equalsIgnoreCase(line)
                    || "year".equalsIgnoreCase(line)) {
                for (Kline datum : data) {
                    datum.setCurrentTime(
                            DateUtils.timeStamp2Date(String.valueOf(datum.getTs()), "yyyy-MM-dd"));
                }
            } else if ("1min".equals(line)) {
                for (Kline datum : data) {
                    datum.setCurrentTime(DateUtils.timeStamp2Date(String.valueOf(datum.getTs()), "HH:mm"));
                }
            } else {
                for (Kline datum : data) {
                    datum.setCurrentTime(
                            DateUtils.timeStamp2Date(String.valueOf(datum.getTs()), "MM-dd HH:mm"));
                }
            }
            return Result.succeed(this.build(data, line, symbol));
        } catch (Exception e) {
            logger.error("getKline error", e);
            throw new YamiShopBindException("k线图获取失败");
        }
    }

    @ApiOperation(value = "行情")
    @GetMapping(HOBI + "getKlineyd.action")
    public Result<List> getKlineyd(@RequestParam String pid, @RequestParam String line) {
        // 1, 5, 15, 30, 45, D, W, M
        try {
            if (line.equals("1")) {
                line = "5";
            }
            String result = HttpClientRequest.doGet("http://api-in.js-stock.top/kline?pid=" + pid + "&interval=" + line + "&key=zHPU8uWYMY7eWx78kbC0");
            if (result == null) {
                throw new YamiShopBindException("k线图获取第三方数据失败失败");
            }
            JSONArray jsonArray = JSON.parseArray(result);
            List<Klines> valueList = new ArrayList<>();
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                Klines kine = new Klines();
                String ts = String.valueOf(jsonObject.get("t"));
                String close = (String) jsonObject.get("c");
                String open = (String) jsonObject.get("o");
                String high = (String) jsonObject.get("h");
                String low = (String) jsonObject.get("l");
                kine.setClose(new BigDecimal(close));
                kine.setHigh(new BigDecimal(high));
                kine.setOpen(new BigDecimal(open));
                kine.setLow(new BigDecimal(low));
                Date date = new Date(Long.valueOf(ts) * 1000L);
                // 创建 SimpleDateFormat 对象并设置时区
                String format = DateUtils.format(date, DateUtils.DF_yyyyMMddHHmmss);
                Date dates = DateUtils.toDates(format, DateUtils.NORMAL_DATE_FORMAT);
                long timestamp = dates.getTime();
                kine.setTimestamp(timestamp);
                valueList.add(kine);
            }
            return Result.succeed(valueList);
        } catch (Exception e) {
            logger.error("getKline error", e);
            throw new YamiShopBindException("k线图获取失败");
        }
    }

    private List<Map<String, Object>> build(List<Kline> data, String line, String symbol) {
        Collections.sort(data);
        int len = data.size();
        for (int i = 1; i < len; i++) {
            data.get(i).setOpen(data.get(i - 1).getClose());
        }
        Set<Long> tsSet = new HashSet<Long>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Item bySymbol = itemService.findBySymbol(symbol);
        for (int i = 0; i < data.size(); i++) {
            Kline kline = data.get(i);
            Long ts = kline.getTs();
            if (tsSet.contains(ts)) {
                continue;
            } else {
                tsSet.add(ts);
            }
            int decimal = bySymbol.getDecimals();
            BigDecimal low = kline.getLow();
            BigDecimal high = kline.getHigh();
            BigDecimal open = kline.getOpen();
            BigDecimal close = kline.getClose();
            if (low == null || low.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (high == null || high.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (close == null || close.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (open == null || open.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("line", line);
            map.put("symbol", kline.getSymbol());
            map.put("timestamp", ts);
            map.put("decimals", decimal);
            map.put("ts", ts);
            map.put("current_time", kline.getCurrentTime());
            map.put("open", open.setScale(decimal, RoundingMode.HALF_UP));
            map.put("close", close.setScale(decimal, RoundingMode.HALF_UP));
            map.put("high", high.setScale(decimal, RoundingMode.HALF_UP));
            map.put("low", low.setScale(decimal, RoundingMode.HALF_UP));
            map.put("volume", kline.getVolume());
            list.add(map);
        }
        return list;
    }

}
