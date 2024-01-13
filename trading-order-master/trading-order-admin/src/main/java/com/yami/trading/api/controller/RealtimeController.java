package com.yami.trading.api.controller;

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cn.hutool.json.JSONObject ;
import com.yami.trading.api.util.HttpClientRequest;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.domain.RealtimeDe;
import com.yami.trading.bean.data.domain.TradeDetails;
import com.yami.trading.bean.item.domain.Item;
 import com.yami.trading.bean.item.dto.RealtimeDTO;
 import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.common.util.StringUtils;
 import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.web.bind.annotation.*;

 import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 行情实时价格 http接口
 */
@RestController
@CrossOrigin
@Slf4j
@Api(tags = "实时行情数据")
public class RealtimeController {

    public static final String HOBI = "api/hobi!";
    @Qualifier("dataService")
    @Autowired
    private DataService dataService;
    @Autowired
    private ItemService itemService;
    @Autowired
    RedisTemplate redisTemplate;

    @ApiOperation(value = "行情")//自选
    @GetMapping(HOBI + "getRealtimeDeail.action")
    public Result<List<RealtimeDe>> getRealtimeDeail(@RequestParam String pid) {
        try {
            List<RealtimeDe> data = new ArrayList<>();
            Map<String, Object> param = new HashMap<>();
            param.put("pid", pid);//印度
            String results = HttpClientRequest.doPost("http://api-in.js-stock.top/stock?key=zHPU8uWYMY7eWx78kbC0", param);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<RealtimeDTO>>() {
            }.getType();
            ArrayList<RealtimeDTO> list = gson.fromJson(results, type);
            for (RealtimeDTO jsonData : list) {
                Item bySymbol = itemService.findByPid(jsonData.getPid());
                if (LangUtils.isEnItem()) {
                    bySymbol.transName();
                }
                RealtimeDe d = new RealtimeDe();
                d.setSymbol(bySymbol.getSymbol());
                d.setType(bySymbol.getType());
                d.setName(bySymbol.getName());
                d.setClose(new BigDecimal(jsonData.getLast()));//最新价
                d.setHigh(new BigDecimal(jsonData.getHigh()));//最高
                d.setLow(new BigDecimal(jsonData.getLow()));//最低价格
                d.setChangeRatio(new BigDecimal(jsonData.getPc()));//涨幅
                d.setChg(jsonData.getPcp());//涨幅百分比
                d.setSymbolData(bySymbol.getSymbolData());
                d.setPid(jsonData.getPid());
                data.add(d);
            }
            return Result.ok(data);
        } catch (Exception e) {
            log.error("生成实时数据失败", e);
            throw new YamiShopBindException("生成实时数据失败");
        }
    }

    //印股
    @ApiOperation(value = "行情")
    @GetMapping(HOBI + "getRealtimeYd.action")
    public Result<List<Realtime>> getRealtimeYd(@RequestParam(value = "symbol", required = false) String symbol,
                                                @RequestParam(value = "pid", required = false) String pid,
                                                @RequestParam(value = "type", required = false) String type) {
        try {
            if (StringUtils.isEmpty(symbol) || StringUtils.isEmpty(type) || StringUtils.isEmpty(pid)) {
                Result.failed("null");
            }
            List<Realtime> data = new ArrayList<>();
            Realtime ds = new Realtime();
            //印股
            Object results = redisTemplate.opsForValue().get("ydTask" + pid);
            String ydList = (String) redisTemplate.opsForValue().get("yd" + symbol + pid);//取list
            if (results == null) {
                throw new YamiShopBindException("生成实时数据异常请检查");
            }
            JSONObject msgObject = JSONUtil.parseObj(results);
            com.alibaba.fastjson2.JSONObject jsonObject = null;
            BigDecimal open = new BigDecimal(0.00);
            BigDecimal Volume = new BigDecimal(0.00);
            String fundamentalDividend = null, fundamentalEps = null, fundamentalMarketCap = null,
                    fundamentalRatio = null, fundamentalRevenue = null, prevClose = null,symbolName=null,types=null;
            if (StringUtils.isNotEmpty(ydList)) {
                jsonObject = com.alibaba.fastjson2.JSONObject.parseObject(ydList);
                open = jsonObject.getBigDecimal("Open");
                Volume = jsonObject.getBigDecimal("Volume");//交易量
                fundamentalDividend = jsonObject.getString("FundamentalDividend");//股息
                fundamentalEps = jsonObject.getString("FundamentalEps");//每股收益
                fundamentalMarketCap = jsonObject.getString("FundamentalMarketCap");//市值
                fundamentalRatio = jsonObject.getString("FundamentalRatio");//市盈率
                fundamentalRevenue = jsonObject.getString("FundamentalRevenue");//营收
                prevClose = jsonObject.getString("PrevClose");//昨收
                symbolName=jsonObject.getString("Name");
                types=jsonObject.getString("type");
            }
            ds.setType("YD-stocks");
            ds.setName(symbolName);
            ds.setSymbol(symbol);
            ds.setClose(new BigDecimal(msgObject.getStr("last")));//最新价
            ds.setOpen(open);//今开
            ds.setFundamentalDividend(fundamentalDividend);
            ds.setFundamentalEps(fundamentalEps);
            ds.setFundamentalMarketCap(fundamentalMarketCap);
            ds.setFundamentalRatio(fundamentalRatio);
            ds.setFundamentalRevenue(fundamentalRevenue);
            ds.setPrevClose(prevClose);
            ds.setHigh(new BigDecimal(msgObject.getStr("high")));//最高
            ds.setLow(new BigDecimal(msgObject.getStr("low")));//最低价格
            ds.setChangeRatios(msgObject.getStr("pc"));//涨幅
            ds.setChg(msgObject.getStr("pcp"));//涨幅百分比
            ds.setVolume(new BigDecimal(msgObject.getStr("last")));//成交额用了最新价格
            ds.setAmount(Volume);//成交量
            ds.setSymbolData(types);
            data.add(ds);
            return Result.ok(data);
        } catch (Exception e) {
            log.error("生成实时数据失败", e);
            throw new YamiShopBindException("生成实时数据失败");
        }
    }

    @ApiOperation(value = "行情")
    @GetMapping(HOBI + "getStockTradeList.action")
    public Result<List<TradeDetails>> getTradeDetails(@RequestParam String symbol) {
        return Result.ok(null);
    }

}
