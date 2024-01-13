package com.yami.trading.admin.task;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yami.trading.api.util.HttpClientRequest;
import com.yami.trading.api.websocket.WebSocketServer;
import com.yami.trading.api.websocket.WebSocketSession;
import com.yami.trading.bean.cms.Infomation;
import com.yami.trading.bean.data.domain.Depth;
import com.yami.trading.bean.data.domain.DepthEntry;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.domain.TradeEntry;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.domain.ItemSummary;
import com.yami.trading.bean.item.dto.InfomationDTO;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.item.dto.RealtimeDTOS;
import com.yami.trading.bean.model.SmsLog;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.internal.DepthTimeObject;
import com.yami.trading.huobi.data.internal.TradeTimeObject;
import com.yami.trading.service.cms.InfomationService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.item.ItemSummaryService;
import com.yami.trading.service.user.SmsLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Lazy(value = false)
@Slf4j
public class AStockTradeListTask {
    @Autowired
    private DepthPushJob depthPushJob;
    @Autowired
    private ItemService itemService;
    @Autowired
    private InfomationService infomationService;
    @Autowired
    RedisTemplate redisTemplate;
    private volatile  boolean isAStockInit = false;
    private volatile  boolean isUsStockInit = false;
    @Autowired
    private ItemSummaryService itemSummaryService;
    @Autowired
    private SmsLogService smsLogService;
    //简况
    public void ydjkList() {
            List<Item> items = itemService.findByTypes("YD-stocks");
            for (Item record : items) {
                ItemSummary itemss = itemSummaryService.getOrNewOne(record.getSymbol());

                if(itemss==null){
                String results = HttpClientRequest.doGet("http://test.js-stock.top/companies?pid=" +record.getPid() + "&size=10&page=1&key=zHPU8uWYMY7eWx78kbC0");
                log.info("印度股票代码返回结果result {} ", results);
                com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(results);
                //取出data里的数组
                ArrayList diff = (ArrayList) json.get("data");
                for (Object o : diff) {
                    //取数据存入redisDB1
                    com.alibaba.fastjson2.JSONObject data = (JSONObject) o;
                    String company_name = data.getString("company_name");
                    String description = data.getString("description");

                        ItemSummary item = new ItemSummary();
                            item.setLang("en");
                         item.setTranslate("0");
                        item.setOrgProfile(description);
                        item.setOrgName(company_name);
                        item.setSymbol(record.getSymbol());
                        itemSummaryService.save(item);
                        log.info("印度股票代码返回结果result {} ", item);
                    }
                 }
            }
    }
  //咨询
 // @Scheduled(cron = "0 0 10,11,13,15 * * MON-FRI")
     public void ydzixunList() {
       for(int i=1;i<=2;i++){
           String result = null;
           try {
               result = HttpClientRequest.doGet("http://api-in.js-stock.top/stock-markets?key=zHPU8uWYMY7eWx78kbC0&type=1");
               log.info("印度股票代码IDresult {} ", result);
           } catch (Exception e) {
               log.error("获取印度股信息失败，重新获取", e);
           }
           if (result == null) {
               log.error("获取印度股信息失败");
               return;
           }
           Gson gson = new Gson();
           Type type = new TypeToken<ArrayList<InfomationDTO>>() {
           }.getType();
           ArrayList<InfomationDTO> list = gson.fromJson(result, type);
           for (InfomationDTO jsonData : list) {
               String inId = jsonData.getId();
               String title = jsonData.getTitle();
               String content = jsonData.getContent();
               Infomation item = new Infomation();
               QueryWrapper<Infomation> itemWrapper = new QueryWrapper<>();
               itemWrapper.eq("data_id", inId);
               Infomation one = infomationService.getOne(itemWrapper);
               if(one==null){
                   item.setDescription(content);
                   item.setTitle(title);
                   item.setDataId(inId);
                   item.setCreateTime(new Date());
                   item.setLang("zh-EN");
                   item.setTranslate("1");
                   infomationService.save(item);
               }
           }
           try {
               Thread.sleep(500L);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
       log.info("信息存入redis成功");
   }
    public void ydyaoqiumaList() {
        for(int i=0;i<500;i++){
            Random random = new Random();
            String code = String.valueOf(random.nextInt(999999) % 900000 + 100000);
            SmsLog smsLog = new SmsLog();
            String merged = mergeTwoLetters();

            smsLog.setMobileCode(merged + code);
            smsLog.setUserPhone("111111");
            smsLog.setContent("22222");
            smsLog.setRecDate(new Date());
            smsLog.setStatus("1");
            smsLogService.save(smsLog);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private static String mergeTwoLetters() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        char letter1 = (char)(random.nextInt(26) + 'A'); // 生成第一个随机字母（大写）
        char letter2 = (char)(random.nextInt(26) + 'B'); // 生成第二个随机字母（小写）

        sb.append(letter1).append(letter2); // 将两个字母添加到StringBuilder中

        return sb.toString(); // 返回合并结果
    }
    @PostConstruct
    public void init() {
        //this.ydzixunList();
         //this.ydyaoqiumaList();
        //this.ydjkList();
        log.info("=======================数据加载完毕============================");
    }
}
