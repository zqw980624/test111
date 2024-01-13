package com.yami.trading.service.etf;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.yami.trading.bean.data.domain.Depth;
import com.yami.trading.bean.data.domain.DepthEntry;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.etf.domain.EtfMinuteKLine;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.purchasing.ProjectBreedService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ETF行情服务
 */
@Service
@Slf4j
@Getter
public class MarketService {

    public static final String IXIC = ".IXIC";
    @Autowired
    ItemService itemService;

    @Autowired
    KlineConfigService klineConfigService;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DataService dataService;
    @Autowired
    private EtfMinuteKLineService etfMinuteKLineService;
    // 加速行情
    public Map<String, Double> accelerate = new HashMap<>();

    public Map<String, Double> getAccelerate() {
        return accelerate;
    }

    private Map<String, LinkedHashMap<Long, Kline>> cacheKline = new ConcurrentHashMap<String, LinkedHashMap<Long, Kline>>();

    public Realtime queryRealtime(String symbol) {

        Kline kline = querySecKline(symbol);

        if (kline == null) {
            //    log.error("current sec market kline not found");
            Realtime realtime = new Realtime();
            realtime.setSymbol(symbol);
            realtime.setName(symbol);

            realtime.setTs(0L);
            realtime.setOpen(BigDecimal.ZERO);
            realtime.setClose(BigDecimal.ZERO);
            realtime.setHigh(BigDecimal.ZERO);
            realtime.setLow(BigDecimal.ZERO);
            return realtime;
        }


        Random random = new Random();


        Realtime realtime = new Realtime();
        int decimal = itemService.getDecimal(symbol);
        realtime.setSymbol(symbol);
        realtime.setName(symbol);

        realtime.setTs(kline.getTs() / 1000);
        realtime.setOpen(kline.getOpen().setScale(decimal, RoundingMode.HALF_UP));
        realtime.setClose(kline.getClose().setScale(decimal, RoundingMode.HALF_UP));
        realtime.setHigh(kline.getHigh().setScale(decimal, RoundingMode.HALF_UP));
        realtime.setLow(kline.getLow().setScale(decimal, RoundingMode.HALF_UP));
//        realtime.setMarketCapital(realtimeJson.getLong("marketCapital"));
//        realtime.setFloatMarketCapital(realtimeJson.getLong("floatMarketCapital"));
//        realtime.setPeForecast(realtimeJson.getBigDecimal("peForecast"));
//        realtime.setVolumeRatio(realtimeJson.getBigDecimal("volumeRatio"));
//        realtime.setTurnoverRate(realtimeJson.getBigDecimal("turnoverRate"));\


        BigDecimal lastAmount = (BigDecimal) Optional.ofNullable(redisTemplate.opsForHash().get(RedisKeys.SYMBOL_AMOUNT_VOLUME + symbol, "amount")).orElse(BigDecimal.ZERO);
        BigDecimal amount = Optional.of(kline.getAmount()).orElse(BigDecimal.ZERO);

        realtime.setAmount(lastAmount.add(amount).setScale(decimal, RoundingMode.HALF_UP));
        BigDecimal lastVolume = (BigDecimal) Optional.ofNullable(redisTemplate.opsForHash().get(RedisKeys.SYMBOL_AMOUNT_VOLUME + symbol, "volume")).orElse(BigDecimal.ZERO);
        BigDecimal volume = Optional.of(kline.getVolume()).orElse(BigDecimal.ZERO);

        realtime.setVolume(lastVolume.add(volume).setScale(decimal, RoundingMode.HALF_UP));
        realtime.setAsk(BigDecimal.valueOf(KlineConfigService.randomBigDecimal(realtime.getLow(), realtime.getClose(), random)));
        realtime.setBid(BigDecimal.valueOf(KlineConfigService.randomBigDecimal(realtime.getHigh(), realtime.getClose(), random)));

        redisTemplate.opsForHash().put(RedisKeys.SYMBOL_AMOUNT_VOLUME + symbol, "amount", realtime.getAmount());
        redisTemplate.opsForHash().put(RedisKeys.SYMBOL_AMOUNT_VOLUME + symbol, "volume", realtime.getVolume());
        return realtime;
    }

    private Kline querySecKline(String symbol) {
        if (cacheKline.size() == 0 || CollectionUtil.isEmpty(cacheKline.get(symbol))) {
            List<Kline> secKlines = klineConfigService.querySecKlineBySymbol(symbol);

            secKlines.sort(Comparator.comparing(Kline::getTs));

            // secKlines 变成key为时间，value为kline的map
            LinkedHashMap collect = secKlines.stream().collect(Collectors.toMap(k -> k.getTs() / 1000, kline -> kline, (k1, k2) -> k1, LinkedHashMap::new));

            if (collect.size() != 0) {
                cacheKline.put(symbol, collect);
                // 一天的秒级k线是23400，取当天最后一秒的数据保存到redis中
                redisTemplate.opsForValue().set(RedisKeys.SYMBOL_DEPTH + symbol, secKlines.size() == 23400 ? secKlines.get(23399) : secKlines.get(secKlines.size() - 1));
            } else {
                // 如果没有数据，跟随纳斯达克大盘
                Kline kline = (Kline) redisTemplate.opsForValue().get(RedisKeys.SYMBOL_DEPTH + symbol);
                if (kline == null) {
                    QueryWrapper<EtfMinuteKLine> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("symbol", symbol);
                    queryWrapper.lt("ts", System.currentTimeMillis());
                    queryWrapper.orderByDesc("ts");
                    queryWrapper.last("limit 1");
                    List<EtfMinuteKLine> list = etfMinuteKLineService.list(queryWrapper);
                    if (CollectionUtil.isNotEmpty(list)) {
                        kline = BeanUtil.copyProperties(list.get(0), Kline.class);
                        kline.setPeriod(Kline.PERIOD_1MIN);
                        kline.setTs(System.currentTimeMillis());
                    } else {
                        return null;
                    }
                }
                Kline retKline = null;
                try {
                    retKline = (Kline) kline.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    return retKline;
                }
                retKline.setTs(System.currentTimeMillis());
                List<Realtime> realtimes = dataService.realtime(IXIC);
                if (CollectionUtils.isNotEmpty(realtimes)) {
                    Realtime realtime = realtimes.get(0);
                    BigDecimal ratio = realtime.getClose().divide(realtime.getOpen(), 10, RoundingMode.HALF_UP);
                    BigDecimal close = ratio.multiply(kline.getClose()).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal low = ratio.multiply(kline.getLow()).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal high = ratio.multiply(kline.getHigh()).setScale(2, RoundingMode.HALF_UP);
                    retKline.setClose(close);
                    retKline.setLow(low);
                    retKline.setHigh(high);
                    return retKline;
                }


            }

        }

        // 获取到
        long currentSec = System.currentTimeMillis() / 1000;

        if (cacheKline.get(symbol) == null) {
            return null;
        }
        Kline kline = cacheKline.get(symbol).get(currentSec);
        return kline;
    }


    public void clear() {
        cacheKline.clear();
    }


    public List<Kline> build5min(String symbol) {

        // 获取所有的1s的kline
        List<Kline> klines = klineConfigService.querySecKlineBySymbolAllData(symbol);

        return convertTo5MinKLine(klines);
    }

    public static List<Kline> convertTo5MinKLine(List<Kline> sKLineList) {

        List<Kline> fiveMinKLineList = new ArrayList<>();

        int currentIndex = 0;
        int dataSize = sKLineList.size();

        while (currentIndex < dataSize) {
            Kline currentKLine = sKLineList.get(currentIndex);
            long currentTimestamp = currentKLine.getTs();
            BigDecimal open = currentKLine.getOpen();
            BigDecimal high = currentKLine.getHigh();
            BigDecimal low = currentKLine.getLow();
            BigDecimal close = currentKLine.getClose();
            BigDecimal volume = currentKLine.getVolume();
            BigDecimal amount = currentKLine.getAmount();

            long nextTimestamp = currentTimestamp + (5 * 60 * 1000); // 5 minutes in milliseconds

            // Find the index of the next KLine that satisfies the 5-minute condition
            int nextIndex = currentIndex + 1;
            while (nextIndex < dataSize && sKLineList.get(nextIndex).getTs() < nextTimestamp) {
                Kline nextKLine = sKLineList.get(nextIndex);
                high = high.max(nextKLine.getHigh());
                low = low.min(nextKLine.getLow());
                volume = volume.add(nextKLine.getVolume());
                amount = amount.add(nextKLine.getAmount());
                close = nextKLine.getClose();
                nextIndex++;
            }

            // Create the 5-minute KLine and add it to the list
            Kline fiveMinKLine = new Kline();
            fiveMinKLine.setSymbol(currentKLine.getSymbol());
            fiveMinKLine.setTs(currentTimestamp);
            fiveMinKLine.setOpen(open);
            fiveMinKLine.setHigh(high);
            fiveMinKLine.setLow(low);
            fiveMinKLine.setClose(close);
            fiveMinKLine.setVolume(volume);
            fiveMinKLine.setAmount(amount);
            fiveMinKLineList.add(fiveMinKLine);

            currentIndex = nextIndex;
        }

        return fiveMinKLineList;

    }


    public Depth queryDepth(String symbol) {
        Depth depth = new Depth();
        depth.setSymbol(symbol);
        depth.setTs(System.currentTimeMillis());


        Kline kline = querySecKline(symbol);


        Item item = itemService.findBySymbol(symbol);

        if (kline == null || kline.getVolume().doubleValue() == 0) {
            kline = (Kline) redisTemplate.opsForValue().get(RedisKeys.SYMBOL_DEPTH + symbol);
        }

        List<BigDecimal> volumeSplit = KlineConfigService.splitData(kline.getVolume().doubleValue(), 10, 10);

        DepthEntry buy;
        DepthEntry sell;


        // 拿到这一秒的挂单数量，怎么分到5档位上
        Random random = new Random();

        for (int i = 0; i < 5; i++) {

            double enlarge = 1;

            if (accelerate.get(symbol) != null) {
                enlarge = accelerate.get(symbol);
            }

            buy = new DepthEntry();
            // 买
            BigDecimal.valueOf(KlineConfigService.randomBigDecimal(kline.getLow(), kline.getClose(), random)).setScale(item.getDecimals(), RoundingMode.HALF_UP).doubleValue();
            buy.setPrice(BigDecimal.valueOf(KlineConfigService.randomBigDecimal(kline.getLow(), kline.getClose(), random)).setScale(item.getDecimals(), RoundingMode.HALF_UP).doubleValue());
            buy.setAmount(enlarge * volumeSplit.get(i).setScale(item.getDecimals(), RoundingMode.HALF_UP).doubleValue());

            // 卖
            sell = new DepthEntry();
            sell.setPrice(BigDecimal.valueOf(KlineConfigService.randomBigDecimal(kline.getHigh(), kline.getClose(), random)).setScale(item.getDecimals(), RoundingMode.HALF_UP).doubleValue());

            sell.setAmount(enlarge * volumeSplit.get(i + 5).setScale(item.getDecimals(), RoundingMode.HALF_UP).doubleValue());


            depth.getAsks().add(sell);
            depth.getBids().add(buy);
        }

        depth.getBids().sort(Comparator.comparing(DepthEntry::getPrice).reversed());
        depth.getAsks().sort(Comparator.comparing(DepthEntry::getPrice));
        return depth;
    }


}
