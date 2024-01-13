package com.yami.trading.common.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.sun.mail.imap.protocol.Item;
import com.yami.trading.common.domain.OpenCloseTime;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MarketOpenChecker {
    /**
     * 美股
     */
    public final static String US_STOCKS = "US-stocks";

    /**
     * 港股
     */
    public final static String HK_STOCKS = "HK-stocks";


    /**
     * 港股
     */
    public final static String A_STOCKS = "A-stocks";

    public final  static  String STOP_DAY_A_STOCK =  "2022-12-31,2023-01-01,2023-01-02,2023-01-21,2023-01-22,2023-01-23,2023-01-24,2023-01-25,2023-01-26,2023-01-27,2023-04-05,2023-04-29,2023-04-30,2023-05-01,2023-05-02,2023-05-03,2023-06-22,2023-06-23,2023-06-24,2023-09-29,2023-09-30,2023-10-01,2023-10-02,2023-10-03,2023-10-04,2023-10-05,2023-10-06";
    public final  static  String STOP_DAY_HK_STOCK="2023-01-02,2023-01-23,2023-01-24,2023-01-25,2023-04-05,2023-04-07,2023-04-10,2023-05-01,2023-05-26,2023-06-22,2023-10-02,2023-10-23,2023-12-25,2023-12-26";
    public final  static  String  STOP_DAY_US_STOCK= "2023-01-02,2023-01-16,2023-02-20,2023-04-07,2023-05-29,2023-07-04,2023-09-04,2023-11-23,2023-12-25";

    private static Set<String> aStockStopSet = new HashSet<>();
    private static Set<String> hkStockStopSet = new HashSet<>();
    private static Set<String> usStockStopSet = new HashSet<>();

    static {
        aStockStopSet.addAll(Splitter.on(",").trimResults().splitToList(STOP_DAY_A_STOCK));
        hkStockStopSet.addAll(Splitter.on(",").trimResults().splitToList(STOP_DAY_HK_STOCK));
        usStockStopSet.addAll(Splitter.on(",").trimResults().splitToList(STOP_DAY_US_STOCK));

    }

    public static void main(String[] args) {
 //       listUsOpenCloseDateTime();
//        System.out.println("A股是否开放：" + isMarketOpen(A_STOCKS));
//        System.out.println("港股是否开放：" + isMarketOpen(HK_STOCKS));
       System.out.println("美股是否开放：" + isMarketOpen(US_STOCKS));
    }

    /**
     * 判断是否开市
     * @param closeType
     * @return
     */
    public static boolean isMarketOpenByItemCloseType(String closeType){

        List<String> stocksType = Lists.newArrayList(A_STOCKS, HK_STOCKS, US_STOCKS);
        if(stocksType.contains(closeType)){
            return isMarketOpen(closeType);
        }else if("forex".equalsIgnoreCase(closeType)){
            return UTCDateUtils.isOpen();
        }else{
            return true;
        }

    }

    public static boolean isMarketOpen(String market) {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        switch (market) {
            case A_STOCKS: {
                ZonedDateTime nowShanghai = nowUtc.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
                String formattedDate = nowShanghai.format(formatter);
                if(aStockStopSet.contains(formattedDate)){
                    return false;
                }
                if (nowShanghai.getDayOfWeek().getValue() < 6 &&
                        ((nowShanghai.getHour() == 9 && nowShanghai.getMinute() >= 30) ||
                        (nowShanghai.getHour() == 10) ||
                        (nowShanghai.getHour() == 11 && nowShanghai.getMinute() <= 30) ||
                        (nowShanghai.getHour() >= 13 && nowShanghai.getHour() < 15))) {
                    return true;
                }
                break;
            }
            case HK_STOCKS: {

                ZonedDateTime nowHongKong = nowUtc.withZoneSameInstant(ZoneId.of("Asia/Hong_Kong"));
                String formattedDate = nowHongKong.format(formatter);
                if(hkStockStopSet.contains(formattedDate)){
                    return false;
                }
                if (nowHongKong.getDayOfWeek().getValue() < 6 &&
                        ((nowHongKong.getHour() == 9 && nowHongKong.getMinute() >= 30) ||
                        (nowHongKong.getHour() > 9 && nowHongKong.getHour() < 12) ||
                        (nowHongKong.getHour() >= 13 && nowHongKong.getHour() < 16))) {
                    return true;
                }
                break;
            }
            case US_STOCKS: {
                ZonedDateTime nowNewYork = nowUtc.withZoneSameInstant(ZoneId.of("America/New_York"));
                String formattedDate = nowNewYork.format(formatter);
                if(usStockStopSet.contains(formattedDate)){
                    return false;
                }
                if (nowNewYork.getDayOfWeek().getValue() < 6 &&
                        ((nowNewYork.getHour() == 9 && nowNewYork.getMinute() >= 30) ||
                        (nowNewYork.getHour() > 9 && nowNewYork.getHour() < 16))) {
                    return true;
                }
                break;
            }
            default:
                throw new IllegalArgumentException("无效的市场名称");
        }
        return false;
    }



    public static List<OpenCloseTime> listUsOpenCloseDateTime(){
        int year = 2023;
        List<OpenCloseTime> times = Lists.newArrayList();
        ZoneId nyseZone = ZoneId.of("America/New_York");  // 纽约时区
        ZoneId beijingZone = ZoneId.of("Asia/Shanghai");  // 北京时区

        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 0, 0);

        LocalDateTime currentDateTime = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (!currentDateTime.isAfter(endDate)) {
            OpenCloseTime openCloseTime = new OpenCloseTime();

            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime nowNewYork = nowUtc.withZoneSameInstant(ZoneId.of("America/New_York"));
            String formattedDate = nowNewYork.format(formatter);
            // 休息日停盘
            if(usStockStopSet.contains(formattedDate)){
                continue;
            }
            if (currentDateTime.getDayOfWeek() != DayOfWeek.SATURDAY && currentDateTime.getDayOfWeek() != DayOfWeek.SUNDAY) {
                ZonedDateTime nyseOpen = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(9)
                        .withMinute(30)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime nyseClose = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(16)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime beijingOpen = nyseOpen.withZoneSameInstant(beijingZone);
                ZonedDateTime beijingClose = nyseClose.withZoneSameInstant(beijingZone);
                Date openDate = Date.from(beijingOpen.toInstant());
                Date closeDate = Date.from(beijingClose.toInstant());
                openCloseTime.setOpenBjDate(openDate);
                openCloseTime.setCloseBjDate(closeDate);
                openCloseTime.setCloseTs(beijingClose.toEpochSecond()*1000);
                openCloseTime.setOpenTs(beijingOpen.toEpochSecond()*1000);
                times.add(openCloseTime);
            }

            currentDateTime = currentDateTime.plusDays(1);
        }
        return times;
    }


    public static List<OpenCloseTime> listHKOpenCloseDateTime(){
        int year = 2023;
        List<OpenCloseTime> times = Lists.newArrayList();
        ZoneId nyseZone = ZoneId.of("Asia/Hong_Kong");  // 纽约时区
        ZoneId beijingZone = ZoneId.of("Asia/Shanghai");  // 北京时区

        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 0, 0);

        LocalDateTime currentDateTime = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (!currentDateTime.isAfter(endDate)) {


            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime nowNewYork = nowUtc.withZoneSameInstant(ZoneId.of("Asia/Hong_Kong"));
            String formattedDate = nowNewYork.format(formatter);
            // 休息日停盘
            if(hkStockStopSet.contains(formattedDate)){
                continue;
            }
            if (currentDateTime.getDayOfWeek() != DayOfWeek.SATURDAY && currentDateTime.getDayOfWeek() != DayOfWeek.SUNDAY) {
                OpenCloseTime openCloseTime = new OpenCloseTime();
                ZonedDateTime nyseOpen = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(9)
                        .withMinute(30)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime nyseClose = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(12)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime beijingOpen = nyseOpen.withZoneSameInstant(beijingZone);
                ZonedDateTime beijingClose = nyseClose.withZoneSameInstant(beijingZone);
                Date openDate = Date.from(beijingOpen.toInstant());
                Date closeDate = Date.from(beijingClose.toInstant());
                openCloseTime.setOpenBjDate(openDate);
                openCloseTime.setCloseBjDate(closeDate);
                openCloseTime.setCloseTs(beijingClose.toEpochSecond()*1000);
                openCloseTime.setOpenTs(beijingOpen.toEpochSecond()*1000);
                times.add(openCloseTime);

                OpenCloseTime  openCloseTime1 = new OpenCloseTime();
                ZonedDateTime nyseOpen1 = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(13)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime nyseClose1 = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(16)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime beijingOpen1 = nyseOpen1.withZoneSameInstant(beijingZone);
                ZonedDateTime beijingClose1 = nyseClose1.withZoneSameInstant(beijingZone);
                Date openDate1 = Date.from(beijingOpen1.toInstant());
                Date closeDate1 = Date.from(beijingClose1.toInstant());
                openCloseTime.setOpenBjDate(openDate1);
                openCloseTime.setCloseBjDate(closeDate1);
                openCloseTime.setCloseTs(beijingClose1.toEpochSecond()*1000);
                openCloseTime.setOpenTs(beijingOpen1.toEpochSecond()*1000);
                times.add(openCloseTime1);
            }

            currentDateTime = currentDateTime.plusDays(1);
        }
        return times;
    }


    public static List<OpenCloseTime> listAOpenCloseDateTime(){
        int year = 2023;
        List<OpenCloseTime> times = Lists.newArrayList();
        ZoneId nyseZone = ZoneId.of("Asia/Shanghai");  // 纽约时区
        ZoneId beijingZone = ZoneId.of("Asia/Shanghai");  // 北京时区

        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 0, 0);

        LocalDateTime currentDateTime = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (!currentDateTime.isAfter(endDate)) {


            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime nowNewYork = nowUtc.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
            String formattedDate = nowNewYork.format(formatter);
            // 休息日停盘
            if(aStockStopSet.contains(formattedDate)){
                continue;
            }
            if (currentDateTime.getDayOfWeek() != DayOfWeek.SATURDAY && currentDateTime.getDayOfWeek() != DayOfWeek.SUNDAY) {
                OpenCloseTime openCloseTime = new OpenCloseTime();
                ZonedDateTime nyseOpen = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(9)
                        .withMinute(30)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime nyseClose = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(11)
                        .withMinute(30)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime beijingOpen = nyseOpen.withZoneSameInstant(beijingZone);
                ZonedDateTime beijingClose = nyseClose.withZoneSameInstant(beijingZone);
                Date openDate = Date.from(beijingOpen.toInstant());
                Date closeDate = Date.from(beijingClose.toInstant());
                openCloseTime.setOpenBjDate(openDate);
                openCloseTime.setCloseBjDate(closeDate);
                openCloseTime.setCloseTs(beijingClose.toEpochSecond()*1000);
                openCloseTime.setOpenTs(beijingOpen.toEpochSecond()*1000);
                times.add(openCloseTime);

                OpenCloseTime  openCloseTime1 = new OpenCloseTime();
                ZonedDateTime nyseOpen1 = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(13)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime nyseClose1 = ZonedDateTime.of(currentDateTime, nyseZone)
                        .withHour(15)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                ZonedDateTime beijingOpen1 = nyseOpen1.withZoneSameInstant(beijingZone);
                ZonedDateTime beijingClose1 = nyseClose1.withZoneSameInstant(beijingZone);
                Date openDate1 = Date.from(beijingOpen1.toInstant());
                Date closeDate1 = Date.from(beijingClose1.toInstant());
                openCloseTime.setOpenBjDate(openDate1);
                openCloseTime.setCloseBjDate(closeDate1);
                openCloseTime.setCloseTs(beijingClose1.toEpochSecond()*1000);
                openCloseTime.setOpenTs(beijingOpen1.toEpochSecond()*1000);
                times.add(openCloseTime1);
            }

            currentDateTime = currentDateTime.plusDays(1);
        }
        return times;
    }
}
