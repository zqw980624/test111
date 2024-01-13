package com.yami.trading.service.impl;

import com.yami.trading.common.util.TimeWindow;
import com.yami.trading.service.IdentifyingCodeTimeWindowService;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class IdentifyingCodeTimeWindowServiceImpl  implements  IdentifyingCodeTimeWindowService,InitializingBean {
    private TimeWindow timeWindow = new TimeWindow();

    public void afterPropertiesSet() throws Exception {
        /**
         * 30分钟
         */
        this.timeWindow.setTimeSize(60 * 30);
        this.timeWindow.start();
    }

    public String getAuthCode(String key) {
        Object authcode = this.timeWindow.findObject(key);
        if (authcode != null) {
            return String.valueOf(authcode);
        }
        return null;
    }

    public void putAuthCode(String key, String authcode) {
        this.timeWindow.add(key, authcode);
    }

    public void delAuthCode(String key) {
        this.timeWindow.remove(key);
    }
}
