package com.yami.trading.admin.task.future.consumer;

import com.yami.trading.bean.future.domain.FuturesRedisKeys;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.future.FuturesOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class FuturesRecomConsumeServer {

    ExecutorService FAST_THREAD = Executors.newSingleThreadExecutor();
    @Autowired
    private FuturesOrderService futuresOrderService;

    public void start() {
        FAST_THREAD.execute(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    FuturesRecomMessage item = null;
                    try {
                        item = (FuturesRecomMessage) RedisUtil.poll(FuturesRedisKeys.FUTURES_RECOM_QUEUE_UPDATE);

                        if (item != null) {
                            futuresOrderService.saveRecomProfit(item.getPartyId(), item.getVolume());
                            //事务提交
                            ThreadUtils.sleep(3000);
                        }

                    } catch (Throwable e) {
                        log.error("FuturesRecomConsumeServer FAST_THREAD() fail", e);

                    } finally {
                        if (item == null) {//无任务则休息三秒
                            ThreadUtils.sleep(3000);
                        }
                    }
                }
            }
        });

    }


}
