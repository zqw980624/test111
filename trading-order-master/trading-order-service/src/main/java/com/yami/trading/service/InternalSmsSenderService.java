package com.yami.trading.service;

import com.yami.trading.common.manager.sms.SmsMessage;

public interface InternalSmsSenderService {
    /**
     * 短信发送
     *
     * @param phone   手机号码
     * @param content 短信内容
     */
    public void send(SmsMessage smsMessage);

}
