package com.yami.trading.admin.task.robot;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.RobotModel;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.service.data.RobotModelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Slf4j
public class RobotModelTask {
    public static final String ROBOT_MODEL_HASH = "robot-model-hash";
    // 科创板
    String urkKeChuang = "https://stock.xueqiu.com/v5/stock/screener/quote/list.json?size=90&order=desc&order_by=percent&exchange=CN&market=CN&type=kcb&page=";
    // 港股
    String urlHk = "https://stock.xueqiu.com/v5/stock/screener/quote/list.json?size=90&order=desc&orderby=percent&order_by=percent&market=HK&type=hk&is_delay=true&page=";
    String urlUS = "https://stock.xueqiu.com/v5/stock/screener/quote/list.json?size=90&order=desc&orderby=percent&order_by=percent&market=US&type=us&page=";

    @Autowired
    private RobotModelService robotModelService;

    private void saveList(String dateStr, List<RobotModel> list) {
        List<RobotModel> saveList = Lists.newArrayList();
        for (RobotModel robotModel : list) {
            robotModel.setDateStr(dateStr);
            String key = getKey(dateStr, robotModel);
            String s = RedisUtil.hGet(ROBOT_MODEL_HASH, key);
            if (s == null) {
                saveList.add(robotModel);
            }
        }
        robotModelService.saveBatch(saveList);
        for (RobotModel robotModel : saveList) {
            robotModel.setDateStr(dateStr);
            String key = getKey(dateStr, robotModel);
            RedisUtil.hSet(ROBOT_MODEL_HASH, key, "1");

        }
    }


    @NotNull
    private String getKey(String dateStr, RobotModel robotModel) {
        return robotModel.getSymbol() + "-" + dateStr;
    }

    public static  void downloadBiAn(String symbol, String date) throws IOException {
        // https://data.binance.vision/data/spot/daily/klines/AAVEUSDT/3m/AAVEUSDT-3m-2023-05-16.zip
        String url = "https://data.binance.vision/data/spot/daily/klines/{1}/3m/{1}-3m-{2}.zip";
        url = url.replace("{1}", symbol).replace("{2}", date);
        String urlStr = url;  // URL of the ZIP file
        String outputPath = "D:\\project\\bian\\{1}-3m-{2}.zip".replace("{1}", symbol).replace("{2}", date);;  // Path to save the ZIP file
        if(FileUtil.exist(outputPath)){
            System.out.println(outputPath +"已经采集过");

            return;
        }
        URL resource = new URL(url);

        URLConnection connection = resource.openConnection();
        // Set the connection timeout to 10 seconds (10000 milliseconds)
        connection.setConnectTimeout(5000);
        // Set the read timeout to 30 seconds (30000 milliseconds)
        connection.setReadTimeout(5000);

        try (ReadableByteChannel readChannel = Channels.newChannel(connection.getInputStream());
             FileOutputStream fileOS = new FileOutputStream(outputPath)) {
            FileChannel writeChannel = fileOS.getChannel();
            writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
            System.out.println(url +"ZIP file downloaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> thisMonthDates() {
        List<String> dates = new ArrayList<>();
        // 获取当前日期
        LocalDate now = LocalDate.now().minusDays(1);

        // 计算一个月前的日期
        for(int i= 0;i<=30;i++){
            LocalDate oneMonthAgo = now.minusDays(i);
            // 创建日期格式化器
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 格式化日期
            String formattedDate = oneMonthAgo.format(formatter);

            dates.add(formattedDate);
        }


        return dates;
    }

    public void hanlde() {
        String rootPath = "D:\\project\\bian\\";
        List<String> strings = FileUtil.listFileNames(rootPath);
        for(String path : strings){
            parseCsv(rootPath+path);
        }

    }

    private static void downloadZip() throws IOException {
        Document doc = Jsoup.parse(new File("D:\\project\\Collection.html"));
        Elements elementsByTag = doc.getElementById("listing").getElementsByTag("td");
        List<String> symbols = new ArrayList<>();
        for (Element e : elementsByTag) {
            String text = e.text().replace("/", "").trim();
            if (StrUtil.isNotBlank(text) && text.endsWith("USDT")) {
                symbols.add(text);
            }
        }
        AtomicInteger count = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        List<String> dates = thisMonthDates();
        for(String symbol : symbols){
            for(String date : dates){
                executorService.submit(()-> {
                    try {
                        downloadBiAn(symbol, date);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("完成了 :" +count.incrementAndGet());});
            }

        }
    }

    public void parseCsv(String zipFilePath){
        List<Kline> list = new ArrayList<>();
        String dateStr = null;
        String symbol = null;
        try (FileInputStream fis = new FileInputStream(new File(zipFilePath));
             ZipInputStream zis = new ZipInputStream(fis, Charset.forName("UTF-8"))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".csv")) {
                    System.out.println("Reading file " + zipEntry.getName());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis, Charset.forName("UTF-8")));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        List<String> strings = Splitter.on(",").trimResults().splitToList(line);
                        /**
                         * Open time	Open	High	Low	Close	Volume	Close time	Quote asset volume
                         */
                        String ts = strings.get(0);
                        String open = strings.get(1);
                        String high = strings.get(2);
                        String low = strings.get(3);
                        String close = strings.get(4);
                        String volume = strings.get(5);
                        Kline kline = new Kline();
                        kline.setOpen(new BigDecimal(open));
                        String csvName = zipEntry.getName().replace(".csv", "");
                        List<String> strings1 = Splitter.on("-3m-").trimResults().splitToList(csvName);
                        kline.setSymbol(strings1.get(0));
                        kline.setClose(new BigDecimal(close));
                        kline.setHigh(new BigDecimal(high));
                        kline.setLow(new BigDecimal(low));
                        kline.setVolume(new BigDecimal(volume));
                        kline.setTs(Long.parseLong(ts));
                        dateStr = strings1.get(1);
                        symbol = strings1.get(0);
                        list.add(kline);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list.size() < 390) {
            return;
        }
        List<Kline> klines = list.subList(0, 390);
        klines.sort(Kline::compareTo);
        RobotModel robotModel = new RobotModel();
        BigDecimal open = klines.get(0).getOpen();
        BigDecimal close = klines.get(389).getClose();
        List<BigDecimal> prices = new ArrayList<>();
        klines.forEach(k -> {
            prices.add(k.getOpen());
            prices.add(k.getClose());
        });

        BigDecimal chg = null;
        BigDecimal percent = null;
        if (BigDecimal.ZERO.compareTo(open) == 0) {
            chg = BigDecimal.ZERO;
            percent = BigDecimal.ZERO;
        } else {
            chg = close.subtract(open);
            percent = chg.divide(open, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.DOWN);
            ;
        }
        robotModel.setKLineData(JSONObject.toJSONString(klines));
        robotModel.setChg( chg.doubleValue()) ;
        robotModel.setPercent( percent.doubleValue());
        robotModel.setSymbol(symbol);
        robotModel.setDateStr(dateStr);
        Pair<Double, Double> calcualte = calcualte(klines);
//        robotModel.setVar(calcualte.getKey());
//        robotModel.setStdDev(calcualte.getValue());
        robotModelService.save(robotModel);
        System.out.println(zipFilePath +" 完成");

    }

    public static Pair<Double, Double> calcualte(List<Kline> klines){
        //股票价格的样本数据
//        StockPrice[] stockPrices = {
//                new StockPrice(102, 98, 100),
//                new StockPrice(108, 103, 105),
//                new StockPrice(105, 99, 102),
//                // 添加更多的数据...
//        };

        //计算平均价格和收益率
        double[] returns = new double[klines.size() - 1];
        for (int i = 0; i < klines.size() - 1; i++) {
            double avgPriceToday = klines.get(i).getAverage();
            double avgPriceTomorrow = klines.get(i+1).getAverage();
            returns[i] = (avgPriceTomorrow - avgPriceToday) / avgPriceToday;
        }

        //计算方差
        Variance variance = new Variance();
        double var = variance.evaluate(returns);

        //计算标准差
        double stdDev = Math.sqrt(var);
        return Pair.of(var, stdDev);

    }
}
