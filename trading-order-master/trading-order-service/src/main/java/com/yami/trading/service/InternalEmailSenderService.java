package com.yami.trading.service;

import com.yami.trading.common.manager.email.EmailMessage;

public interface InternalEmailSenderService {
    /**
     *  邮件发送
     */
    public void send(EmailMessage emailMessage);
}
