package com.yami.trading.service;

public interface SessionTokenService {



    public String savePut(String partyId);

    public String cacheGet(String session_token);

    public void del(String session_token);

}
