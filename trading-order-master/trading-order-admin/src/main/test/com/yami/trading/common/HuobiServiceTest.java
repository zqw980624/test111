package com.yami.trading.common;

import com.yami.trading.WebApplication;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.huobi.hobi.HobiDataService;
import com.yami.trading.huobi.hobi.http.HttpHelper;
import com.yami.trading.huobi.hobi.internal.HobiDataServiceImpl;
import com.yami.trading.huobi.hobi.internal.XueQiuDataServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

//@SpringBootTest(classes = WebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HuobiServiceTest {
    @Autowired
    private HobiDataService hobiDataService ;

    @Test
    public void testRealTime(){
        List<Kline> tsla = hobiDataService.getTimeseriesForOneHourly("XELAP");
        System.out.println(tsla);
    }

    @Test
    public void testRealTime1(){
        XueQiuDataServiceImpl service = new XueQiuDataServiceImpl();
        List<Kline> tsla = service.getTimeseriesThirtyMinute("TSLA");
        System.out.println(tsla.size());
    }


    @Test
    public void testGetTimeseriesForFourHourly(){
        XueQiuDataServiceImpl service = new XueQiuDataServiceImpl();
        List<Kline> tsla = service.getTimeseriesForFourHourly("TSLA");
        System.out.println(tsla.size());
    }

    @Test
    public void testGetTimeseriesForOneDaY(){
        XueQiuDataServiceImpl service = new XueQiuDataServiceImpl();
        List<Kline> tsla = service.buildOneDayPeriod("TSLA");
        System.out.println(tsla.size());
    }
    @Test
    public void testGetTimeseriesForOneWeek(){
        XueQiuDataServiceImpl service = new XueQiuDataServiceImpl();
        List<Kline> tsla = service.buildOneWeekPeriod("TSLA");
        System.out.println(tsla.size());
    }
    @Test
    public void testGetTimeseriesForOneMonth(){
        XueQiuDataServiceImpl service = new XueQiuDataServiceImpl();
        List<Kline> tsla = service.buildOneMonthPeriod("TSLA");
        System.out.println(tsla.size());
    }

    @Test
    public void getCookie(){
        String cookie = HttpHelper.getCookie("https://xueqiu.com/");
        System.out.println(cookie);
    }
}
