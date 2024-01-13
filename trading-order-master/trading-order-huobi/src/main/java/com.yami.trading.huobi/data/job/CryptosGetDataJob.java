package com.yami.trading.huobi.data.job;


import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.huobi.data.internal.DataDBService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CryptosGetDataJob extends AbstractGetDataJob {

    public static volatile boolean first = true;
    private static Logger logger = LoggerFactory.getLogger(CryptosGetDataJob.class);

    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private DataDBService dataDBService;
    @Autowired
    private ItemService itemService;

    public void start() {
        new Thread(this, "CryptosGetDataJob").start();
    }

    public void run() {

        if (first) {
            /**
             * data数据保存间隔时长(毫秒)
             */
            this.interval = this.sysparaService.find("data_interval").getInteger().intValue();

            first = false;
        }
        while (true) {
            try {
                this.realtimeHandle("");
            } catch (Exception e) {
                logger.error("run fail", e);
            } finally {
                ThreadUtils.sleep(this.interval);
            }
        }

    }

    @Override
    public String getName() {
        return "虚拟货币数据采集";
    }

    @Override
    public void realtimeHandle(String symbols) {

    }

}
