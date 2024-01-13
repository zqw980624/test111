package com.yami.trading.service;

public interface IdentifyingCodeService {

    /**
     *
     * @param target 邮件或手机号
     * @param ip     会根据发送频率封ip
     */
    public void send(String target, String ip);
}
