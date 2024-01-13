package com.yami.trading;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.etf.domain.EtfSecKLine;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.huobi.data.internal.KlineService;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.data.RestDataService;
import com.yami.trading.service.etf.EtfSecKLineService;
import com.yami.trading.service.item.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = WebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MyServiceTests {

    @Autowired
    private ItemService itemService;
    @Autowired
    private DataDBService dataDBServiceImpl;
    @Autowired
    private KlineService klineService;

    @Autowired
    private RestDataService restDataService;
    @Autowired
    private EtfSecKLineService etfSecKLineService;
    @Autowired
    private WalletService walletService;

    @Test
    public void testGetMoneyContract(){
        Map<String, Double> data = walletService.getMoneyContract("ff808081863c63c201864a68bc9c001c", Item.US_STOCKS);
        System.out.println(data);
    }
    @Test
    public void testEtf(){
        List<EtfSecKLine> etfSecKLines = Lists.newArrayList();
        for(int i = 0;i<=1000;i++){
            EtfSecKLine e = new EtfSecKLine();
            e.setSymbol("aaa");
            e.setTs(System.currentTimeMillis());
            etfSecKLines.add(e);
        }
        etfSecKLineService.saveBatch(etfSecKLines,1000);
    }

    @Test
    public void findRestDataService() {
        List<Realtime> btcusd = restDataService.realtime("BTCUSD");
        System.out.println(btcusd);
    }
    @Test
    public void saveItems() {
        String ukStocks="AAPL,ATO,OII,TM,HMC,RACE,ETR,VLO,CMI,SHEL,CLMT,ETRN,NI,ALB,ENPH,NEE,WOLF,STEM,AMRC,CWEN,DUK,FE,GIFI,DQ,JKS,NIO,CSIQ,AMZN,GOOGL,MSFT,META";
    //    String aStocks= "SZ300750,SZ300033";
     //   String hkStocks= "00941,00992,00388,00700";
        Item sample = itemService.findBySymbol("EOSUSD");
        Splitter.on(",").trimResults().splitToList(ukStocks).forEach(stock->{
            if(itemService.findBySymbol(stock)==null){
                Item item = new Item();
                BeanUtil.copyProperties(sample, item, "uuid");
                item.setName(stock);
                item.setSymbol(stock);
                item.setSymbolData(stock);
                item.setDecimals(2);
                item.setFake("0");
                item.setType(Item.US_STOCKS);
                item.setCategory(Item.US_STOCKS);
                itemService.save(item);
            }
        });

//        Splitter.on(",").trimResults().splitToList(hkStocks).forEach(stock->{
//            if(itemService.findBySymbol(stock)==null){
//                Item item = new Item();
//                BeanUtil.copyProperties(sample, item, "uuid");
//                item.setName(stock);
//                item.setSymbol(stock);
//                item.setSymbolData(stock);
//                item.setType(Item.HK_STOCKS);
//                item.setCategory(Item.HK_STOCKS);
//                itemService.save(item);
//            }
//        });
//
//
//        Splitter.on(",").trimResults().splitToList(aStocks).forEach(stock->{
//            if(itemService.findBySymbol(stock)==null){
//                Item item = new Item();
//                BeanUtil.copyProperties(sample, item, "uuid");
//                item.setName(stock);
//                item.setSymbol(stock);
//                item.setSymbolData(stock);
//                item.setType(Item.A_STOCKS);
//                item.setCategory(Item.A_STOCKS);
//                itemService.save(item);
//            }
//        });
    }

    @Test
    public void find() {
        klineService.find("1", "1", 1);
    }

    @Test
    public void testDataDBServiceImpl() {
        for (int i = 0; i <= 1000; i++) {
            Realtime audjpy = dataDBServiceImpl.get("AUDJPY");
            System.out.println(audjpy);
        }

    }

    @Test
    public void testMyServiceMethod() {
        // 编写您的测试逻辑
        itemService.list();
        Item dog = itemService.findBySymbol("USDCAD");
        itemService.list();
        assertNotNull(dog);
    }

    @Test
    public void testAddItem() {
        Item item = new Item();
        item.setSymbol("123");
        item.setName("123");
        itemService.save(item);
    }


    @Test
    public void testRedisUtils() {
        Map<String, Map<String, Double>> contractAssetsMap = new ConcurrentHashMap<String, Map<String, Double>>();
        contractAssetsMap.put("123", new HashMap<>());
        RedisUtil.set("money_contract", contractAssetsMap);

        Map<String, Map<String, Double>> data = RedisUtil.get("money_contract");
        System.out.println(data);
    }
}