package com.yami.trading.service.impl;

import com.yami.trading.common.manager.sms.SmsMessage;
import com.yami.trading.common.manager.sms.SmsMessageQueue;
import com.yami.trading.service.SmsSendService;
import org.springframework.stereotype.Service;

@Service
public class SmsSendServiceImpl implements SmsSendService {

    @Override
    public void send(String mobile, String content) {
        SmsMessage smsMessage = new SmsMessage(mobile, content);
        String strh = "";
        strh = mobile.substring(0, 2);
        if ("91".equals(strh)) {
            smsMessage.setInter(false);
        }

        SmsMessageQueue.add(smsMessage);
    }

}
