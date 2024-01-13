import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.stream.CollectorUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yami.trading.bean.data.domain.Kline;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class Test {

    public static void main(String[] args) throws FileNotFoundException {



        File file = new File("/Users/sentry/Desktop/work/workspace/trading-order/trading-order-admin/src/main/test/source.txt");
        RandomAccessFile accessFile = new RandomAccessFile(file, "r");
        String jsonStr = FileUtil.readLine(accessFile, Charset.defaultCharset());

        JSONObject jsonObject = (JSONObject) JSON.parse(jsonStr);


        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("item");


        List<Kline> klines = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray item = (JSONArray) jsonArray.get(i);
            Kline kline = new Kline();
            Long timestamp = Long.valueOf(item.get(0).toString());
            Double volume = Double.valueOf(item.get(1).toString());
            Double open = Double.valueOf(item.get(2).toString());
            Double high = Double.valueOf(item.get(3).toString());
            Double low = Double.valueOf(item.get(4).toString());
            Double close = Double.valueOf(item.get(5).toString());
            kline.setTs(timestamp);
            kline.setVolume(BigDecimal.valueOf(volume));
            kline.setOpen(BigDecimal.valueOf(open));
            kline.setHigh(BigDecimal.valueOf(high));
            kline.setLow(BigDecimal.valueOf(low));
            kline.setClose(BigDecimal.valueOf(close));
            klines.add(kline);
        }


        List<Kline> collect = null;
        int day = 0;
        do {
            long zore = zore(day);
            collect = klines.stream().filter(k -> k.getTs() > zore).collect(Collectors.toList());
            day ++;
        } while (CollectionUtil.isEmpty(collect));

        collect.sort(Kline::compareTo);


        List<Kline> newklines = new ArrayList<>(collect.size());
        Kline secondKline = null;
        for (Kline kline : collect) {
            List<BigDecimal> bigDecimals = splitBigDecimal(kline.getVolume(), 60,30);
            BigDecimal open = secondKline == null ? kline.getOpen() : secondKline.getClose();
            for (int i = 0; i < 60; i++) {
                secondKline = new Kline();
                secondKline.setTs(kline.getTs() + i * 1000);
                secondKline.setOpen(open);
                if (i == 59) {
                    secondKline.setClose(kline.getClose());
                } else {
                    secondKline.setClose(randomBigDecimal(kline.getOpen(),kline.getClose()));
                }
                secondKline.setHigh(randomBigDecimal(secondKline.getClose(),kline.getHigh()));
                secondKline.setLow(randomBigDecimal(kline.getLow(),secondKline.getClose()).min(secondKline.getOpen()));
                secondKline.setVolume(bigDecimals.get(i));
                open = secondKline.getClose();
                newklines.add(secondKline);
            }
        }


        List<List<Object>> result = new ArrayList<>();
        List<String> time = new ArrayList<>();


// 格式化时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Kline kline : newklines) {
            List<Object> tmp = new ArrayList<>();
            tmp.add(simpleDateFormat.format(new Date(kline.getTs())));
            tmp.add(kline.getOpen().setScale(2, BigDecimal.ROUND_HALF_UP));
            tmp.add(kline.getHigh().setScale(2, BigDecimal.ROUND_HALF_UP));
            tmp.add(kline.getLow().setScale(2, BigDecimal.ROUND_HALF_UP));
            tmp.add(kline.getClose().setScale(2, BigDecimal.ROUND_HALF_UP));
            tmp.add(kline.getVolume().setScale(2, BigDecimal.ROUND_HALF_UP));
            result.add(tmp);
        }

//        System.out.println(JSON.toJSONString(time));

        System.out.println(JSON.toJSONString(result));
    }

    public static long zore(int day) {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        LocalDate localDate = currentDate.minusDays(day);

        // 获取当天0点的时间
        LocalDateTime zeroTime = LocalDateTime.of(localDate, LocalTime.MIDNIGHT);

        // 转换成时间戳
        long zeroTimestamp = zeroTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;

        return zeroTimestamp;
    }

    public static BigDecimal randomBigDecimal(BigDecimal a, BigDecimal b) {
        BigDecimal diff = b.subtract(a);
        Random random = new Random();
        BigDecimal randNum = diff.multiply(new BigDecimal(random.nextDouble()));
        BigDecimal result = a.add(randNum);
        return result;
    }


    public static List<BigDecimal> splitBigDecimal(BigDecimal value, int numOfParts, double maxDifferencePercentage) {
        List<BigDecimal> parts = new ArrayList<>(numOfParts);
        Random random = new Random();

        BigDecimal remaining = value;
        BigDecimal minAllowedValue = value.multiply(BigDecimal.valueOf(1 - maxDifferencePercentage / 100)).divide(BigDecimal.valueOf(numOfParts), value.scale(), BigDecimal.ROUND_HALF_UP);
        BigDecimal maxAllowedValue = value.multiply(BigDecimal.valueOf(1 + maxDifferencePercentage / 100)).divide(BigDecimal.valueOf(numOfParts), value.scale(), BigDecimal.ROUND_HALF_UP);

        for (int i = 0; i < numOfParts - 1; i++) {
            BigDecimal part = getRandomBigDecimal(minAllowedValue, maxAllowedValue, random);
            while (remaining.subtract(part).compareTo(minAllowedValue.multiply(BigDecimal.valueOf(numOfParts - i - 1))) < 0) {
                part = getRandomBigDecimal(minAllowedValue, maxAllowedValue, random);
            }
            parts.add(part);
            remaining = remaining.subtract(part);
        }

        parts.add(remaining);
        return parts;
    }

    private static BigDecimal getRandomBigDecimal(BigDecimal min, BigDecimal max, Random random) {
        BigDecimal range = max.subtract(min);
        BigDecimal randomValue = range.multiply(new BigDecimal(random.nextDouble())).setScale(min.scale(), BigDecimal.ROUND_HALF_UP);
        return min.add(randomValue);
    }
}

