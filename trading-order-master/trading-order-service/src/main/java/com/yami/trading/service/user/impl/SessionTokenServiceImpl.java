package com.yami.trading.service.user.impl;

import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.UUIDGenerator;
import com.yami.trading.service.SessionTokenService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionTokenServiceImpl implements SessionTokenService {

    private volatile Map<String, String> cache = new ConcurrentHashMap<String, String>();

    public String savePut(String partyId) {
        String session_token = UUIDGenerator.getUUID();
        cache.put(session_token, partyId);
        return session_token;
    }

    public String cacheGet(String session_token) {
        if (StringUtils.isNullOrEmpty(session_token)) {
            return null;
        }
        return cache.get(session_token);
    }

    @Override
    public void del(String session_token) {
        if (StringUtils.isNullOrEmpty(session_token)) {
            return;
        }
        cache.remove(session_token);
    }

}
