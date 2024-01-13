package com.yami.trading.huobi.data.internal;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.config.RequestDataHelper;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.common.util.UTCDateUtils;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.service.data.KlineDBService;
import com.yami.trading.service.data.RealtimeService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class KlineServiceImpl implements KlineService {
    private static Logger logger = LoggerFactory.getLogger(KlineServiceImpl.class);
    @Autowired
    private NamedParameterJdbcOperations namedParameterJdbcTemplate;
    @Autowired
    private ItemService itemService;
    @Autowired
    private KlineDBService klineDBService;
    @Autowired
    private RealtimeService realtimeService;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private DataDBService dataDBService;

    @Override
    public void saveInit(String symbol, Map<String, List<Kline>> dailyWeekMonthHistoryMap,
                         Map<String, List<Kline>> hourlyAndMinuteHistoryMap) {
        RequestDataHelper.set("symbol", symbol);

        logger.info("正在初始化k线图: {}", symbol);
        Map<String, Object> parameters = new HashMap();
        parameters.put("symbol", symbol);
        for (int i = 0; i <= Constants.TABLE_PARTITIONS - 1; i++) {
            namedParameterJdbcTemplate.update("DELETE FROM t_kline_" + i + " WHERE SYMBOL = :symbol", parameters);
        }
        for (String line : dailyWeekMonthHistoryMap.keySet()) {
            List<Kline> list = dailyWeekMonthHistoryMap.get(line);
            klineDBService.saveOrUpdateBatch(list);

            KlineTimeObject klineTimeObject = new KlineTimeObject();
            Collections.sort(list);
            klineTimeObject.setKline(list);
            klineTimeObject.setLastTime(new Date());
            DataCache.putKline(symbol, line, klineTimeObject);
        }

        for (String line : hourlyAndMinuteHistoryMap.keySet()) {
            List<Kline> list = hourlyAndMinuteHistoryMap.get(line);
            klineDBService.saveOrUpdateBatch(list);

            KlineTimeObject klineTimeObject = new KlineTimeObject();
            Collections.sort(list);
            klineTimeObject.setKline(list);
            klineTimeObject.setLastTime(new Date());
            DataCache.putKline(symbol, line, klineTimeObject);
        }
        RequestDataHelper.clear();

    }

    /**
     * 查询所有的K线数据
     * 时间升序排列 [1,2,3]
     * 最后一条数据是最新的
     */
    @Override
    public List<Kline> find(String symbol, String line, int pageSize) {
        RequestDataHelper.set("symbol", symbol);
        LambdaQueryWrapper<Kline> queryWrapper = new LambdaQueryWrapper<Kline>()
                .eq(Kline::getSymbol, symbol)
                .eq(Kline::getPeriod, line)
                .orderByAsc(Kline::getTs)
                .last("LIMIT " + pageSize);
        List<Kline> klines = klineDBService.getBaseMapper().selectList(queryWrapper);
        RequestDataHelper.clear();
        return klines;
    }


    @Override
    public Realtime findLatestRealtime(String symbol) {
        RequestDataHelper.set("symbol", symbol);
        LambdaQueryWrapper<Realtime> queryWrapper = new LambdaQueryWrapper<Realtime>().eq(Realtime::getSymbol, symbol).orderByDesc(Realtime::getTs).last("LIMIT 5");

        List<Realtime> list = realtimeService.getBaseMapper().selectList(queryWrapper);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }

        RequestDataHelper.clear();
        return null;

    }

    @Override
    public void delete(String line, int days) {
        Map<String, Object> parameters = new HashMap();
        Long ts = DateUtils.addDate(new Date(), days).getTime();
        parameters.put("line", line);
        parameters.put("ts", ts);
        for (int i = 0; i <= Constants.TABLE_PARTITIONS - 1; i++) {
            this.namedParameterJdbcTemplate.update("DELETE FROM t_kline_" + i + " WHERE TS < :ts  AND PERIOD=:line ", parameters);
        }


    }

    //==================================构建Kline数据入库========================================================

    /**
     * 构建一分钟Kline数据
     */
    @Override
    public void saveKline1Minute(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = build1Min(symbol, line);
        // 取15分钟K线全部数据集合
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);

        // 一分钟计算完后，清除数据。
        DataCache.latestRealTimeMap_60s.get(symbol).clear();
    }

    /**
     * 构建五分钟Kline数据
     */
    @Override
    public void saveKline5Minute(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1MIN, 5);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        // 取15分钟K线全部数据集合
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
    }


    public Kline buildKline(String symbol, String line, String smallLevelLine, int nums) {
        try {
            // 取5分钟K线全部数据集合
            KlineTimeObject timeObject = DataCache.getKline(symbol, line);
            if (timeObject == null) {
                return null;
            }
            List<Kline> klineList = timeObject.getKline();
            Item item = itemService.findBySymbol(symbol);
            // 获取最近一个kline
            Kline latestSameLineKline = null;
            if (null == klineList || klineList.size() <= 0) {
                if (item.getFake().equalsIgnoreCase("0")) {
                    return null;
                }
            } else {
                latestSameLineKline = klineList.get(klineList.size() - 1);
            }
            // 取上个更小维度的k线图进行聚合计算
            List<Kline> klineOne = DataCache.getKline(symbol, smallLevelLine).getKline();
            if (null == klineOne || klineOne.size() <= 0) {
                return null;
            }
            if (nums > klineOne.size()) {
                nums = klineOne.size();
            }
            // 1分钟K线最新的5条数据,上个层级最近的几条数据
            List<Kline> klineOneTop5 = new ArrayList<>(klineOne.subList(klineOne.size() - nums, klineOne.size()));
            Kline realtimeKline = klineOneTop5.get(nums - 1);
            if (realtimeKline == null) {
                return null;
            }
            if (latestSameLineKline != null && latestSameLineKline.getTs() >= realtimeKline.getTs()) {
                return null;
            }
            if (latestSameLineKline != null) {
                long latestSameLineKlineTs = latestSameLineKline.getTs();
                klineOneTop5 = klineOneTop5.stream().filter(r -> r.getTs() > latestSameLineKlineTs).collect(Collectors.toList());
            }


            Double high = null;
            Double low = null;
            for (Kline kline : klineOneTop5) {
                if (high == null || high <= kline.getHigh().doubleValue()) {
                    high = kline.getHigh().doubleValue();
                }
                if (low == null || low >= kline.getLow().doubleValue()) {
                    low = kline.getLow().doubleValue();
                }
            }

            Kline kline = new Kline();
            kline.setSymbol(symbol);
            kline.setTs(realtimeKline.getTs());
            if (latestSameLineKline != null) {
                kline.setOpen(latestSameLineKline.getClose());
            } else {
                kline.setOpen(klineOneTop5.get(0).getOpen());
            }

            kline.setHigh(new BigDecimal(high));
            kline.setLow(new BigDecimal(low));
            kline.setClose(realtimeKline.getClose());
            kline.setPeriod(line);
            BigDecimal sumAmount = klineOneTop5.stream()
                    .map(Kline::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumVolume = klineOneTop5.stream()
                    .map(Kline::getVolume)
                    .filter(volume -> volume != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            kline.setAmount(sumAmount);
            kline.setVolume(sumVolume);
            repairKline(kline);
            if (kline.getOpen().compareTo(BigDecimal.ZERO) == 0 || kline.getClose().compareTo(BigDecimal.ZERO) == 0) {
                return null;
            }
            return kline;
        } catch (Exception e) {
            logger.error("build1Min error: {}, {}", symbol, line, e);
        }
        return null;

    }

    /**
     * 对生成k线图进行修正
     * 1、对最高和最低进行修正
     * 2、如果开盘和停盘价格一样，将停盘价格设置成和开盘和停盘价格不一样的最高或者最低。
     */
    public void repairKline(Kline kline) {
        Integer decimal = itemService.getDecimal(kline.getSymbol());
        applyPrecision(kline, decimal);
        List<BigDecimal> numbers = Arrays.asList(
                kline.getHigh(), kline.getLow(), kline.getClose(), kline.getOpen()
        );
        BigDecimal max = numbers.stream()
                .filter(number -> number != null && number.compareTo(BigDecimal.ZERO) != 0)
                .max(BigDecimal::compareTo)
                .orElse(null);

        BigDecimal min = numbers.stream()
                .filter(number -> number != null && number.compareTo(BigDecimal.ZERO) != 0)
                .min(BigDecimal::compareTo)
                .orElse(null);
        kline.setLow(min);
        kline.setHigh(max);
        if (kline.getOpen().compareTo(kline.getClose()) == 0) {
            if (kline.getHigh()!=null &&  kline.getHigh().compareTo(kline.getOpen()) != 0) {
                kline.setClose(kline.getHigh());
            } else {
                kline.setClose(kline.getLow());
            }
        }
    }

    /**
     * 一定概率，让最高和最低，从当前开盘收盘取
     */
    public void smoothlyKline(Kline kline, double probability) {

        boolean shouldReturnTrue = Math.random() < probability;

        Integer decimal = itemService.getDecimal(kline.getSymbol());
        applyPrecision(kline, decimal);

        BigDecimal max = kline.getOpen().max(kline.getClose());
        BigDecimal min = kline.getOpen().min(kline.getClose());
        if (shouldReturnTrue) {
            kline.setHigh(max);
            kline.setLow(min);
        }
    }

    @Override
    public void clean() {
        try {
            logger.info("clear k line data");
            // todo 适配股票数据
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            f.setTimeZone(TimeZone.getTimeZone(UTCDateUtils.GMT_TIME_ZONE));
            this.dataDBService.deleteRealtime(-12);
            this.dataDBService.updateOptimize("T_REALTIME");

            delete(Kline.PERIOD_1MIN, -21);
            delete(Kline.PERIOD_5MIN, -21);
            delete(Kline.PERIOD_15MIN, -21);
            delete(Kline.PERIOD_30MIN, -21);
            delete(Kline.PERIOD_60MIN, -96);
            delete(Kline.PERIOD_4HOUR, -96);
            delete(Kline.PERIOD_2HOUR, -96);

            this.dataDBService.updateOptimize("T_KLINE");


            ThreadUtils.sleep(1000);

            /**
             * 重置实时数据历史缓存
             */
            List<Item> item_list = itemService.list();
            for (int i = 0; i < item_list.size(); i++) {
                Item item = item_list.get(i);
                if(!MarketOpenChecker.isMarketOpenByItemCloseType(item.getOpenCloseType())){
                    continue;
                }

                List<Realtime> list = this.dataDBService.findRealtimeOneDay(item.getSymbol());
                DataCache.getRealtimeHistory().put(item.getSymbol(), list);

                DataCache.getKline(item.getSymbol(), Kline.PERIOD_1MIN)
                        .setKline(find(item.getSymbol(), Kline.PERIOD_1MIN, Integer.MAX_VALUE));
                DataCache.getKline(item.getSymbol(), Kline.PERIOD_5MIN)
                        .setKline(find(item.getSymbol(), Kline.PERIOD_5MIN, Integer.MAX_VALUE));
                DataCache.getKline(item.getSymbol(), Kline.PERIOD_15MIN)
                        .setKline(find(item.getSymbol(), Kline.PERIOD_15MIN, Integer.MAX_VALUE));
                DataCache.getKline(item.getSymbol(), Kline.PERIOD_30MIN)
                        .setKline(find(item.getSymbol(), Kline.PERIOD_30MIN, Integer.MAX_VALUE));
                DataCache.getKline(item.getSymbol(), Kline.PERIOD_60MIN)
                        .setKline(find(item.getSymbol(), Kline.PERIOD_60MIN, Integer.MAX_VALUE));
                DataCache.getKline(item.getSymbol(), Kline.PERIOD_2HOUR)
                        .setKline(find(item.getSymbol(), Kline.PERIOD_2HOUR, Integer.MAX_VALUE));
                DataCache.getKline(item.getSymbol(), Kline.PERIOD_4HOUR)
                        .setKline(find(item.getSymbol(), Kline.PERIOD_4HOUR, Integer.MAX_VALUE));

            }

        } catch (Exception e) {
            logger.error("clear k line data fail", e);
        }
    }

    public static void applyPrecision(Object obj, int precision) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(BigDecimal.class)) {
                field.setAccessible(true);
                try {
                    BigDecimal value = (BigDecimal) field.get(obj);
                    if (value != null) {
                        BigDecimal newValue = value.setScale(precision, BigDecimal.ROUND_HALF_UP);
                        field.set(obj, newValue);
                    }
                } catch (IllegalAccessException e) {
                    logger.error("对k线图进行精度处理失败", e);
                }
            }
        }
    }

    /**
     * 构建15分钟Kline数据
     */
    @Override
    public void saveKline15Minute(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1MIN, 15);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        // 取15分钟K线全部数据集合
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
    }

    /**
     * 获取最近一次k线图
     *
     * @param symbol
     * @param line
     * @return
     */
    public Kline getLast(String symbol, String line) {
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        List<Kline> kline = timeObject.getKline();
        Item item = itemService.findBySymbol(symbol);
        Kline latestKilne = null;
        if (null == kline || kline.size() <= 0) {
            if (item.getFake().equalsIgnoreCase("0")) {
                return null;
            }
        } else {
            latestKilne = kline.get(kline.size() - 1);
        }
        return latestKilne;
    }

    /**
     * 构建30分钟Kline数据
     */
    @Override
    public void saveKline30Minute(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1MIN, 30);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
    }

    /**
     * 构建60分钟Kline数据
     */
    @Override
    public void saveKline60Minute(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1MIN, 60);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);

    }

    /**
     * 构建4小时Kline数据
     */
    @Override
    public void saveKline4Hour(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_60MIN, 4);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);

    }

    @Override
    public void saveKline2Hour(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_60MIN, 2);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
    }

    /**
     * 构建1天Kline数据
     */
    @Override
    public void saveKline1Day(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_60MIN, 6);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
    }

    @Override
    public void saveKline5Day(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1DAY, 5);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
    }


    /**
     * 构建1周Kline数据
     */
    @Override
    public void saveKline1Week(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1DAY, 7);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
        RequestDataHelper.clear();
    }

    /**
     * 构建1月Kline数据
     */
    @Override
    public void saveKline1Mon(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1DAY, 22);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
        RequestDataHelper.clear();
    }

    @Override
    public void saveKlineQuarter(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_1MON, 3);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
        RequestDataHelper.clear();
    }

    @Override
    public void saveKlineYear(String symbol, String line) {
        RequestDataHelper.set("symbol", symbol);
        Kline kline = buildKline(symbol, line, KlineConstant.PERIOD_QUARTER, 4);
        if (kline != null) {
            klineDBService.save(kline);
            RequestDataHelper.clear();
        } else {
            return;
        }
        KlineTimeObject timeObject = DataCache.getKline(symbol, line);
        // 缓存中新增一条K线
        timeObject.getKline().add(kline);
        timeObject.setLastTime(new Date());
        DataCache.putKline(symbol, line, timeObject);
        RequestDataHelper.clear();
    }

    //==================================构建临时Kline,先这些写,后面再优化===========================================

    @Override
    public Kline bulidKline1Minute(Realtime real, String line) {
        String symbol = real.getSymbol();
        return build1Min(symbol, line);
    }

    @Nullable
    private Kline build1Min(String symbol, String line) {
        try {
            // 60s最新实时价格集合
            List<Realtime> realTimeList = DataCache.latestRealTimeMap_60s.get(symbol);
            int data_interval = sysparaService.find("data_interval").getInteger().intValue();
            int maxSize = 60 * 1000 / data_interval;
            if (realTimeList == null) {
                return null;
            }
            if (realTimeList.size() > maxSize) {
                realTimeList = new ArrayList<>(realTimeList.subList(realTimeList.size() - maxSize, realTimeList.size()));
            }
            if (null == realTimeList || realTimeList.size() <= 0) {
                return null;
            }

            // 取1分钟K线全部数据集合
            KlineTimeObject timeObject = DataCache.getKline(symbol, line);
            if (timeObject == null) {
                return null;
            }
            List<Kline> klineList = timeObject.getKline();
            Item item = itemService.findBySymbol(symbol);
            Kline latestKilne = null;
            if (null == klineList || klineList.size() <= 0) {
                if (item.getFake().equalsIgnoreCase("0")) {
                    return null;
                }
            } else {
                latestKilne = klineList.get(klineList.size() - 1);
            }
            Realtime realtime = realTimeList.get(realTimeList.size() - 1);
            if (latestKilne != null && latestKilne.getTs() >= realtime.getTs()) {
                return null;
            }
            long lastKlineTs = latestKilne.getTs();
            realTimeList = realTimeList.stream().filter(r -> r.getTs() > lastKlineTs).collect(Collectors.toList());
            Double high = null;
            Double low = null;
            for (Realtime realTime : realTimeList) {
                if (high == null || high <= realTime.getClose().doubleValue()) {
                    high = realTime.getClose().doubleValue();
                }
                if (low == null || low >= realTime.getClose().doubleValue()) {
                    low = realTime.getClose().doubleValue();
                }
            }

            // 保存K线到数据库
            Kline kline = new Kline();
            kline.setSymbol(symbol);
            kline.setTs(realtime.getTs());
            if (latestKilne != null) {
                kline.setOpen(latestKilne.getClose());
            } else {
                kline.setOpen(realTimeList.get(0).getOpen());
            }
            kline.setHigh(new BigDecimal(high));
            kline.setLow(new BigDecimal(low));
            kline.setClose(realtime.getClose());
            kline.setPeriod(line);
            BigDecimal sumAmount = realTimeList.stream()
                    .map(Realtime::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumVolume = realTimeList.stream()
                    .map(Realtime::getVolume)
                    .filter(volume -> volume != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            kline.setAmount(sumAmount);
            kline.setVolume(sumVolume);
            repairKline(kline);
            if (kline.getOpen().compareTo(BigDecimal.ZERO) == 0 || kline.getClose().compareTo(BigDecimal.ZERO) == 0) {
                return null;
            }
            return kline;
        } catch (Exception e) {
            logger.error("build1Min error: {}, {}", symbol, line, e);
        }
        return null;

    }

    @Override
    public Kline bulidKline5Minute(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1MIN, 5);
    }

    @Override
    public Kline bulidKline15Minute(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1MIN, 15);

    }

    @Override
    public Kline bulidKline30Minute(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1MIN, 30);
    }

    @Override
    public Kline bulidKline60Minute(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1MIN, 60);

    }

    @Override
    public Kline bulidKline4Hour(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_60MIN, 4);

    }

    @Override
    public Kline bulidKline1Day(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_60MIN, 6);

    }

    @Override
    public Kline bulidKline5Day(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1DAY, 5);

    }

    @Override
    public Kline bulidKline1Week(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1DAY, 7);

    }

    @Override
    public Kline bulidKline1Mon(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1DAY, 22);

    }

    @Override
    public Kline bulidKlineQuarter(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_1MON, 3);

    }

    @Override
    public Kline bulidKlineYear(Realtime realtime, String line) {
        return buildKline(realtime.getSymbol(), line, Kline.PERIOD_QUARTER, 4);

    }

    /**
     * 格式化小数点位
     */
    public void formatPoint(Kline kline) {
        Item item = this.itemService.findBySymbol(kline.getSymbol());
        if (item.getDecimals() != null && item.getDecimals() >= 0) {
            String format = "";
            if (item.getDecimals() == 0) {
                format = "#";
            } else {
                format = "#.";
                for (int j = 0; j < item.getDecimals(); j++) {
                    format = format + "#";
                }
            }
            DecimalFormat df = new DecimalFormat(format);
            // 向下取整
            df.setRoundingMode(RoundingMode.FLOOR);
            kline.setHigh(new BigDecimal(df.format(kline.getHigh())));
            kline.setLow(new BigDecimal(df.format(kline.getLow())));
            // kline.setVolume(Double.parseDouble(df.format(kline.getVolume())));
            kline.setClose(new BigDecimal(df.format(kline.getClose())));
            kline.setOpen(new BigDecimal(df.format(kline.getOpen())));
        }
    }

    public Kline bulidKline(Realtime realtime, Kline lastOne, Kline hobiOne, String line) {
        Kline kline = new Kline();
        kline.setSymbol(realtime.getSymbol());
        kline.setTs(realtime.getTs());
        kline.setOpen(realtime.getOpen());
        kline.setHigh(realtime.getHigh());
        kline.setLow(realtime.getLow());
        kline.setClose(realtime.getClose());
        /**
         * 新传回来的volume是固定的 需要除以Arith.div(realtime.getVolume(), 倍数)
         */
        kline.setVolume(realtime.getVolume());

        if (lastOne != null) {
            kline.setOpen(lastOne.getClose());
        }
        int interval = this.sysparaService.find("data_interval").getInteger().intValue() / 1000;

        HighLow highLow = null;
        switch (line) {
            case "1min":
                highLow = HighLowHandle.get(realtime.getSymbol(), (60) / interval, interval);
                break;

            case "5min":
                highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 5) / interval, interval);
                break;
            case "15min":
                highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 15) / interval, interval);
                break;
            case "30min":
                highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 30) / interval, interval);
                break;

            case "60min":
                highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 60) / interval, interval);
                break;

            case "4hour":
                highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 60 * 4) / interval, interval);
                break;
            case "1day":
                highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 60 * 24) / interval, interval);
                break;

            case Kline.PERIOD_1WEEK:
                highLow = HighLowHandle.getByDay(realtime.getSymbol(), 7);
                break;

            case Kline.PERIOD_1MON:
                highLow = HighLowHandle.getByDay(realtime.getSymbol(), 30);
                break;

        }

        if (highLow != null && highLow.getHigh() != null) {
            kline.setHigh(highLow.getHigh());
        }
        if (highLow != null && highLow.getLow() != null) {
            kline.setLow(highLow.getLow());
        }

        kline.setVolume(hobiOne.getVolume());

        return kline;
    }
    /**
     * 按多少分钟进行partition聚合
     *
     * @param symbol
     * @param seq       按seq分段 聚合出k线图
     * @param klineList
     * @return
     */
    public List<Kline> calculateKline(String symbol, int seq, String period, List<Kline> klineList) {
        int decimal = itemService.getDecimal(symbol);
        List<Kline> result = new ArrayList<>();
        // 抽象成minute分钟图，方便前端显示
        List<List<Kline>> partition = Lists.partition(klineList, seq);
        for (List<Kline> list1Min : partition) {
            if(list1Min.get(0).getHigh() == null){
                continue;
            }
            if(list1Min.get(0).getLow() == null){
                continue;
            }
            Double high = list1Min.get(0).getHigh().doubleValue();
            Double low = list1Min.get(0).getLow().doubleValue();
            for (Kline kline : list1Min) {
                if(kline.getHigh() == null){
                    continue;
                }
                if(kline.getLow() == null){
                    continue;
                }
                if (high <= kline.getHigh().doubleValue()) {
                    high = kline.getHigh().doubleValue();
                }
                if (low >= kline.getLow().doubleValue()) {
                    low = kline.getLow().doubleValue();
                }
            }
            int lastIndex = list1Min.size() - 1;
            Kline kline = new Kline();
            kline.setSymbol(symbol);
            kline.setTs(list1Min.get(lastIndex).getTs());
            BigDecimal open = list1Min.get(0).getOpen();
            if(open == null){
                continue;
            }
            kline.setOpen(open.setScale(decimal, BigDecimal.ROUND_DOWN));
            BigDecimal highD = new BigDecimal(high);
            kline.setHigh(highD.setScale(decimal, BigDecimal.ROUND_DOWN));
            kline.setLow(new BigDecimal(low).setScale(decimal, BigDecimal.ROUND_DOWN));
            BigDecimal close = list1Min.get(lastIndex).getClose();
            if(close == null){
                continue;
            }
            kline.setClose(close.setScale(decimal, BigDecimal.ROUND_DOWN));
            kline.setPeriod(period);
            // 格式化小数点位
            // klineOneTop.formatPoint(kline);
            BigDecimal sumAmount = klineList.stream()
                    .map(Kline::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumVolume = klineList.stream()
                    .map(Kline::getVolume)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            kline.setAmount(sumAmount);
            kline.setVolume(sumVolume);
            result.add(kline);
        }
        return result;
    }
}
