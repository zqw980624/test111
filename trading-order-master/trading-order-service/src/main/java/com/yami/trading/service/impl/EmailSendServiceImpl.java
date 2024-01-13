package com.yami.trading.service.impl;

import com.yami.trading.common.manager.email.EmailMessage;
import com.yami.trading.common.manager.email.EmailMessageQueue;
import com.yami.trading.service.EmailSendService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class EmailSendServiceImpl implements EmailSendService {


    @Override
    public void sendEmail(String tomail, String subject, String content) {
        EmailMessageQueue.add( new EmailMessage(tomail,  subject, content, null, null, null,null));

    }

    @Override
    public void sendEmail(String tomail, String subject, String ftlname, Map<String, Object> map) {
        EmailMessageQueue.add( new EmailMessage( tomail,  subject, null, ftlname, map, null,null));

    }

    @Override
    public void sendEmail(String tomail, String subject, String content, String ftlname, Map<String, Object> map,
                          File file, String filename) {
        EmailMessageQueue.add( new EmailMessage( tomail,  subject, content, ftlname, map, file,filename));

    }

}
