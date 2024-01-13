package com.yami.trading.service.etf;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.RobotModel;
import com.yami.trading.bean.etf.domain.EtfKLine;
import com.yami.trading.bean.etf.domain.EtfMinuteKLine;
import com.yami.trading.bean.etf.domain.EtfSecKLine;
import com.yami.trading.bean.etf.domain.KlineConfig;
import com.yami.trading.bean.etf.dto.KlineConfigDTO;
import com.yami.trading.bean.etf.mapstruct.EtfMinuteKLineWrapper;
import com.yami.trading.bean.etf.mapstruct.EtfSecKLineWrapper;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.robot.domain.RobotOrder;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.dao.data.RobotModelMapper;
import com.yami.trading.dao.etf.mapper.KlineConfigMapper;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.robot.RobotOrderService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * etfK线图配置表Service
 *
 * @author lucas
 * @version 2023-05-03
 */
@Service
@Transactional
public class KlineConfigService extends ServiceImpl<KlineConfigMapper, KlineConfig> {

    @Autowired
    private RobotModelMapper robotModelMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    EtfSecKLineService etfSecKLineService;

    @Autowired
    EtfSecKLineWrapper etfSecKLineWrapper;


    @Autowired
    RobotOrderService robotOrderService;

    @Autowired
    EtfMinuteKLineService etfMinuteKLineService;

    @Autowired
    EtfMinuteKLineWrapper etfMinuteKLineWrapper;

    @Lazy
    @Autowired
    MarketService marketService;


    @Transactional(rollbackFor = Exception.class)
    public void deleteByRobotModelId(List<String> ids) {
        long currentTimeMillis = System.currentTimeMillis();
        for (String id : ids) {
            KlineConfig config = this.getBaseMapper().selectById(id);
            if (config != null) {
                if (config.getOpenTimeTs() <= currentTimeMillis && config.getCloseTimeTs() >= currentTimeMillis) {
                    throw new YamiShopBindException("当前为开市时间，不能删除");
                }
                removeById(id);

                etfSecKLineService.remove(new QueryWrapper<EtfSecKLine>().eq("symbol", config.getSymbol()).between("ts", config.getOpenTimeTs(), config.getCloseTimeTs()));

                //etfMinuteKLineService 根据config的开市时间，结束时间，symbol去删除
                etfMinuteKLineService.remove(new QueryWrapper<EtfMinuteKLine>().eq("symbol", config.getSymbol()).between("ts", config.getOpenTimeTs(), config.getCloseTimeTs()));

                marketService.getCacheKline().put(config.getSymbol(), new LinkedHashMap<>());
            }
        }
    }

    public EtfKLine queryKLine(KlineConfigDTO klineConfigDTO) {
        BigDecimal divide = klineConfigDTO.getClosePrice().subtract(klineConfigDTO.getOpenPrice()).divide(klineConfigDTO.getOpenPrice(), 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));

        List<RobotModel> robotModels = new LambdaQueryChainWrapper<>(robotModelMapper).between(RobotModel::getPercent, divide.subtract(new BigDecimal(0.5)), divide.add(new BigDecimal(0.5))).list();

        if (CollectionUtil.isEmpty(robotModels)) {
            throw new YamiShopBindException("没有满足条件的趋势图");
        }


        //  解析数据库满足条件涨幅的k线图数据
        // 随机获取一个，获取完整一天的分钟k线图
        RobotModel robotModel = robotModels.get(new Random().nextInt(robotModels.size()));
        List<Kline> randomKLine = parseHistoryKLine(robotModel);

        // 获取完整一天的k线图
        randomKLine.sort(Comparator.comparing(Kline::getTs));

        // 生成分钟图返回
        List<Kline> klines = accrodingSecOrign(klineConfigDTO, robotModel, randomKLine, 60);

//        printResult(klines);

        return new EtfKLine(robotModel.getUuid(),klines);

    }

    public List<Kline> generateSecKLine(KlineConfigDTO klineConfigDTO) {


        RobotModel robotModel = robotModelMapper.selectById(klineConfigDTO.getRobot_model_uuid());

        // 假图的分钟图
        List<Kline> klines = parseHistoryKLine(robotModel);

        // 生成分钟图
        List<Kline> minute = accrodingSecOrign(klineConfigDTO, robotModel, klines, 60);

        etfMinuteKLineService.saveBatch(etfMinuteKLineWrapper.toDTO(minute),500);

        saveRobotOrder(klineConfigDTO, minute);

        // 生成的秒图
        return getRecentWholwSecKline(klineConfigDTO.getSymbol(), minute);
    }

    private void saveRobotOrder(KlineConfigDTO klineConfigDTO, List<Kline> minute) {
        Random random = new Random();
        Item item = itemService.findBySymbol(klineConfigDTO.getSymbol());

        List<RobotOrder> robotOrders = new ArrayList<>(minute.size());
        for (Kline kline : minute) {
            RobotOrder robotOrder = new RobotOrder();
            robotOrder.setSymbol(klineConfigDTO.getSymbol());
            robotOrder.setPrice(NumberUtil.roundDown(randomBigDecimal(kline.getHigh(), kline.getLow(),random),item.getDecimals()).doubleValue());
            // 设置订单状态挂单中
            robotOrder.setStatus(1);
            robotOrder.setOrderType(random.nextInt(2) + 1);
            robotOrder.setDirection(random.nextInt(2) + 1);
            robotOrder.setOrderQuantity(NumberUtil.roundDown(randomBigDecimal(kline.getVolume(), kline.getVolume().multiply(new BigDecimal(0.8)), random),item.getDecimals()).doubleValue());
            robotOrder.setTurnover(0d);
            robotOrder.setTs(kline.getTs());
            robotOrder.setUuid(UUID.randomUUID().toString().replaceAll("-", ""));
            robotOrder.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
            robotOrders.add(robotOrder);
        }
        robotOrderService.saveBatch(robotOrders);
    }

    // 生成秒级的假图
    private List<Kline> accrodingSecOrign(KlineConfigDTO klineConfigDTO,RobotModel robotModel, List<Kline> originalSecKline, int sec) {

        Item item = itemService.findBySymbol(klineConfigDTO.getSymbol());

        BigDecimal todayVolume = BigDecimal.valueOf(randomBigDecimal(klineConfigDTO.getTurnoverHigh(), klineConfigDTO.getTurnoverLow(), new Random()));
        BigDecimal todayAmount = todayVolume.divide(klineConfigDTO.getOpenPrice().add(klineConfigDTO.getClosePrice()).divide(BigDecimal.valueOf(2)), item.getDecimals(), BigDecimal.ROUND_HALF_UP);



        double totalVolume = originalSecKline.stream()
                .mapToDouble(klien -> klien.getVolume().doubleValue())
                .sum();


        Tuple startAndEnd = getStartAndEnd(klineConfigDTO.getOpenTimeTs());

        klineConfigDTO.setOpenTimeTs(startAndEnd.get(0));
        klineConfigDTO.setCloseTimeTs(startAndEnd.get(1));

        Kline finalSecondKline;
        BigDecimal open = klineConfigDTO.getOpenPrice();
        BigDecimal secHigh;
        BigDecimal secLow;
        BigDecimal secVolume;
        BigDecimal secAmount;
        boolean isOver = false;
        List<Kline> finalResult = new ArrayList<>(originalSecKline.size());
        Random random = new Random();
        for (int i = 0; i < originalSecKline.size(); i++) {
            Kline kline = originalSecKline.get(i);
            finalSecondKline = new Kline();
            finalSecondKline.setTs(klineConfigDTO.getOpenTimeTs() + i * sec * 1000);
            finalSecondKline.setSymbol(item.getSymbol());

            // 设置开仓价
            finalSecondKline.setOpen(open);

            // 分钟级最高价  model.high / model.open * 分钟的open
            secHigh = kline.getHigh().divide(kline.getOpen(), 10, BigDecimal.ROUND_HALF_UP).multiply(finalSecondKline.getOpen()).setScale(item.getDecimals(), BigDecimal.ROUND_HALF_UP);
            finalSecondKline.setHigh(secHigh);

            // 分钟级最低价
            secLow = kline.getLow().divide(kline.getOpen(), 10, BigDecimal.ROUND_HALF_UP).multiply(finalSecondKline.getOpen()).setScale(item.getDecimals(), BigDecimal.ROUND_HALF_UP);
            finalSecondKline.setLow(secLow);

            // 分钟级成交量
            if (kline.getVolume() == null ||kline.getVolume().longValue() == 0) {
                secVolume = BigDecimal.ZERO;
            } else {
                secVolume = kline.getVolume().divide(BigDecimal.valueOf(totalVolume),10, BigDecimal.ROUND_HALF_UP).multiply(todayVolume).setScale(item.getDecimals(), BigDecimal.ROUND_HALF_UP);
            }
            finalSecondKline.setVolume(secVolume);

            if (kline.getAmount() == null || kline.getAmount().longValue() == 0) {
                secAmount = BigDecimal.ZERO;
            } else {
                // 分钟级成交手数
                secAmount = kline.getAmount().divide(BigDecimal.valueOf(robotModel.getAmount()),10, BigDecimal.ROUND_HALF_UP).multiply(todayAmount).setScale(item.getDecimals(), BigDecimal.ROUND_HALF_UP);
            }

            finalSecondKline.setAmount(secAmount);


            // 收盘价
            if (finalSecondKline.getTs() > klineConfigDTO.getCloseTimeTs() || i == originalSecKline.size() - 1) {
                finalSecondKline.setClose(klineConfigDTO.getClosePrice().setScale(item.getDecimals(), BigDecimal.ROUND_HALF_UP));
                isOver = true;
            } else {
                double close = randomBigDecimal(secHigh, secLow, random);
                finalSecondKline.setClose(BigDecimal.valueOf(close).setScale(item.getDecimals(), BigDecimal.ROUND_HALF_UP));
            }


            open = finalSecondKline.getClose();
            finalResult.add(finalSecondKline);
            if (isOver) {
                break;
            }
        }
        return finalResult;
    }

    @NotNull
    private List<Kline> getRecentWholwSecKline(String symbol, List<Kline> collect) {
        Item bySymbol = itemService.findBySymbol(symbol);

        List<Kline> originalSecKline = new ArrayList<>(collect.size());
        Kline originalSecondKline = null;
        Random random = new Random();
        for (Kline kline : collect) {

            // 将这一分钟成交量拆分成60份
            List<BigDecimal> volumes = splitData(kline.getVolume().doubleValue(), 60, 30);
            List<BigDecimal> amounts = splitData(kline.getAmount().doubleValue(), 60, 30);

            BigDecimal open = originalSecondKline == null ? kline.getOpen() : originalSecondKline.getClose();

            for (int i = 0; i < 60; i++) {
                originalSecondKline = new Kline();
                originalSecondKline.setSymbol(symbol);
                originalSecondKline.setTs(kline.getTs() + i * 1000);

                originalSecondKline.setOpen(open.setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));
                double close = 0;
                if (i == 59) {
                    originalSecondKline.setClose(kline.getClose().setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));
                } else {
                    close = randomBigDecimal(kline.getOpen(), kline.getClose(), random);
                    originalSecondKline.setClose(BigDecimal.valueOf(close).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));
                }
                double high = randomBigDecimal(kline.getLow(), kline.getHigh(), random);
                originalSecondKline.setHigh(BigDecimal.valueOf(high).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));
                double low = randomBigDecimal(kline.getLow(), originalSecondKline.getClose(), random);
                originalSecondKline.setLow(BigDecimal.valueOf(low).min(originalSecondKline.getOpen()).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));
                originalSecondKline.setVolume(volumes.get(i).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));
                originalSecondKline.setAmount(amounts.get(i).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));


                // 重新调整K线图数据
                adjustKline(originalSecondKline);


                // 重新打散
                while (originalSecondKline.getHigh().doubleValue() == originalSecondKline.getLow().doubleValue()) {
                    // 收盘价在最高和最低之间 当最高价最低价相同时, close = random ( high * random(1,1.01) ,  low * random(1,1.01) )

                    // 随机最高价 为当前分钟最高价的1.005倍
                    BigDecimal tempHigh = kline.getHigh().multiply(getRandomBigDecimal(1));
                    // 随机最低价 为当前分钟最低价的0.995倍
                    BigDecimal tempLow = kline.getLow().multiply(getRandomBigDecimal(-1));
                    
                    originalSecondKline.setClose(BigDecimal.valueOf(randomBigDecimal(tempHigh,tempLow, random)).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));

                    // 最高价在收盘价和这一分钟的最高价之间
                    originalSecondKline.setHigh(BigDecimal.valueOf(randomBigDecimal(originalSecondKline.getClose(), kline.getHigh(), random)).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));

                    // 最低价在收盘价和这一分钟的最低价之间
                    originalSecondKline.setLow(BigDecimal.valueOf(randomBigDecimal(kline.getLow(), originalSecondKline.getClose(), random)).setScale(bySymbol.getDecimals(), BigDecimal.ROUND_HALF_UP));

                    adjustKline(originalSecondKline);
                }

                open = originalSecondKline.getClose();
                originalSecKline.add(originalSecondKline);
            }
        }
        return originalSecKline;
    }


    public static Kline adjustKline(Kline kline) {
        BigDecimal[] values = {kline.getHigh(), kline.getOpen(), kline.getClose(), kline.getLow()};
        Arrays.sort(values);
        kline.setHigh(values[3]);
        kline.setLow(values[0]);
        return kline;
    }


    public static BigDecimal getRandomBigDecimal(int input) {
        BigDecimal lowerBound;
        BigDecimal upperBound;

        if (input == 1) {
            lowerBound = BigDecimal.ONE;
            upperBound = new BigDecimal("1.005");
        } else if (input == -1) {
            lowerBound = new BigDecimal("0.995");
            upperBound = BigDecimal.ONE;
        } else {
            throw new IllegalArgumentException("Invalid input value. Expected 1 or -1.");
        }

        BigDecimal range = upperBound.subtract(lowerBound);
        BigDecimal randomValue = range.multiply(BigDecimal.valueOf(Math.random())).add(lowerBound);
        return randomValue.setScale(3, RoundingMode.HALF_UP);
    }

    private void printResult(List<Kline> originalSecKline) {
        // 输出到echarts
        List<List<Object>> result = new ArrayList<>();
        List<String> time = new ArrayList<>();
        // 格式化时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        for (Kline kline : originalSecKline) {
            List<Object> tmp = new ArrayList<>();
            time.add(simpleDateFormat.format(new Date(kline.getTs())));
            tmp.add(kline.getOpen().setScale(5, BigDecimal.ROUND_HALF_UP));
            tmp.add(kline.getClose().setScale(5, BigDecimal.ROUND_HALF_UP));
            tmp.add(kline.getLow().setScale(5, BigDecimal.ROUND_HALF_UP));
            tmp.add(kline.getHigh().setScale(5, BigDecimal.ROUND_HALF_UP));
//            tmp.add(kline.getVolume().setScale(5, BigDecimal.ROUND_HALF_UP));
            result.add(tmp);
        }


        System.err.println(JSON.toJSONString(time));
        System.err.println(JSON.toJSONString(result));
    }

    /**
     * 传入时间字符串获取当天第一个时间戳
     */
    private Long getTodayFirstTimestamp(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 传入时间字符串获取当天最后一个时间戳
     */
    private Long getTodayLastTimestamp(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    @NotNull
    private List<Kline> parseHistoryKLine(RobotModel robotModel) {
        JSONArray jsonArray = JSON.parseArray(robotModel.getKLineData());
        List<Kline> klines = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            Kline kline = new Kline();
            kline.setTs(item.getLong("ts"));
            kline.setVolume(item.getBigDecimal("volume"));
            kline.setAmount(item.getBigDecimal("amount"));
            kline.setOpen(item.getBigDecimal("open"));
            kline.setHigh(item.getBigDecimal("high"));
            kline.setLow(item.getBigDecimal("low"));
            kline.setClose(item.getBigDecimal("close"));
            klines.add(kline);
        }
        return klines;
    }

    public static double randomBigDecimal(BigDecimal a, BigDecimal b, Random random) {
        double diff = b.doubleValue() - a.doubleValue();
        double randNum = diff * random.nextDouble();
        return a.doubleValue() + randNum;
    }


//    public static List<BigDecimal> splitBigDecimal(BigDecimal value, int numOfParts, double maxDifferencePercentage) {
//        List<BigDecimal> parts = new ArrayList<>(numOfParts);
//        Random random = new Random();
//
//        BigDecimal remaining = value;
//        BigDecimal minAllowedValue = value.multiply(BigDecimal.valueOf(1 - maxDifferencePercentage / 100)).divide(BigDecimal.valueOf(numOfParts), value.scale(), BigDecimal.ROUND_HALF_UP);
//        BigDecimal maxAllowedValue = value.multiply(BigDecimal.valueOf(1 + maxDifferencePercentage / 100)).divide(BigDecimal.valueOf(numOfParts), value.scale(), BigDecimal.ROUND_HALF_UP);
//
//        for (int i = 0; i < numOfParts - 1; i++) {
//            BigDecimal part = getRandomBigDecimal(minAllowedValue, maxAllowedValue, random);
//            while (remaining.subtract(part).compareTo(minAllowedValue.multiply(BigDecimal.valueOf(numOfParts - i - 1))) < 0) {
//                part = getRandomBigDecimal(minAllowedValue, maxAllowedValue, random);
//            }
//            parts.add(part);
//            remaining = remaining.subtract(part);
//        }
//
//        parts.add(remaining);
//        return parts;
//    }

    public static List<BigDecimal> splitData(double data, int n, double maxDeviationPercent) {
        // 计算每份的理论平均值
        double avg = data / n;
        // 计算允许的最大偏差
        double maxDeviation = avg * maxDeviationPercent / 100;

        // 创建一个存储每份数据的列表
        List<Double> splits = new ArrayList<>();

        Random random = new Random();
        double remainingData = data;

        // 按照N-1份进行随机拆分
        for (int i = 0; i < n - 1; i++) {
            // 生成一个随机数，表示当前份数据的比例（0到1之间）
            double ratio = random.nextDouble();
            // 计算当前份数据的大小
            double currentSplit = avg + (ratio - 0.5) * 2 * maxDeviation;
            // 将当前份数据加入列表
            splits.add(currentSplit);
            // 更新剩余数据量
            remainingData -= currentSplit;
        }

        // 最后一份数据即为剩余数据量
        splits.add(remainingData);

        // 打乱列表中数据的顺序
        Collections.shuffle(splits);



        return splits.stream().map(BigDecimal::valueOf).collect(Collectors.toList());
    }

    private static BigDecimal getRandomBigDecimal(BigDecimal min, BigDecimal max, Random random) {
        double range = max.doubleValue() - (min.doubleValue());
        double randomValue = range * random.nextDouble();
        return BigDecimal.valueOf(min.doubleValue() + randomValue).setScale(min.scale(), BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 查询秒级K线图
     *
     * @param symbol
     * @return
     */
    public List<Kline> querySecKlineBySymbol(String symbol) {

        Tuple startAndEnd = getStartAndEnd();

        List<EtfSecKLine> list = etfSecKLineService.list(new QueryWrapper<EtfSecKLine>().eq("symbol", symbol).between("ts", startAndEnd.get(0), startAndEnd.get(1)).orderByAsc("ts"));

        return etfSecKLineWrapper.toEntity(list);
    }

    public Tuple getStartAndEnd() {
        // 获取当前时间的时间戳（毫秒级）
        long currentTimestamp = System.currentTimeMillis();

        // 转换为ZonedDateTime对象，使用系统默认时区
        ZonedDateTime currentDateTime = Instant.ofEpochMilli(currentTimestamp)
                .atZone(ZoneId.systemDefault());

        // 获取当天的21:30时间点
        ZonedDateTime targetDateTime;
        if (currentDateTime.getHour() >= 4 && currentDateTime.getHour() <= 23) {
            // 当前时间在4点到21点之间，获取当天的21:30时间点
            targetDateTime = currentDateTime.withHour(23).withMinute(59).withSecond(59).withNano(0);
        } else {
            // 当前时间在0点到4点之间，获取前一天的21:30时间点
            targetDateTime = currentDateTime.minusDays(1).withHour(21).withMinute(30).withSecond(0).withNano(0);
        }


        // 获取第二天凌晨4点时间点
        ZonedDateTime nextDayDateTime = targetDateTime.plusDays(1).withHour(4).withMinute(0);
        // 转换为毫秒时间戳
        long start = targetDateTime.toInstant().toEpochMilli();
        long end = nextDayDateTime.toInstant().toEpochMilli();
        return new Tuple(start, end);
    }


    public Tuple getStartAndEnd(long ts) {
        // 转换为ZonedDateTime对象，使用系统默认时区
        ZonedDateTime inputDateTime = Instant.ofEpochMilli(ts)
                .atZone(ZoneId.systemDefault());

        // 获取当天的21:30时间点
        ZonedDateTime targetDateTime = inputDateTime.withHour(21).withMinute(30).withSecond(0).withNano(0);

        // 获取第二天的4:00时间点
        ZonedDateTime nextDayDateTime = targetDateTime.plusDays(1).withHour(4).withMinute(0);

        // 转换为毫秒时间戳
        long start = targetDateTime.toInstant().toEpochMilli();
        long end = nextDayDateTime.toInstant().toEpochMilli();

        return new Tuple(start, end);
    }



    /**
     * 查询所有秒级K线图
     *
     * @param symbol
     * @return
     */
    public List<Kline> querySecKlineBySymbolAllData(String symbol) {
        long currentTime = System.currentTimeMillis();

//        QueryWrapper<KlineConfig> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("symbol", symbol).le("open_time_ts", currentTime).ge("close_time_ts", currentTime);
//        List<KlineConfig> klineConfigs = this.baseMapper.selectList(queryWrapper);
//
//        if (CollectionUtils.isEmpty(klineConfigs)) {
//            throw new YamiShopBindException("未查询到当天配置的秒级K线图");
//        }
//
//        if (klineConfigs.size() > 1) {
//            throw new YamiShopBindException("查询到多个当天配置的秒级K线图");
//        }
//
//        KlineConfig klineConfig = klineConfigs.get(0);

        List<EtfSecKLine> list = etfSecKLineService.list(new QueryWrapper<EtfSecKLine>().eq("symbol", symbol).orderBy(true, true, "ts"));

        return etfSecKLineWrapper.toEntity(list);
    }
}
