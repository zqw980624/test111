package com.yami.trading.service;

import com.yami.trading.common.constants.UserRedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    @Autowired
    private RedisTemplate  redisTemplate;

    public Date get(String partyId) {
        return (Date) redisTemplate.opsForValue().get(UserRedisKeys.ONLINEUSER_PARTYID + partyId);
    }

    public List<String> getAll() {
        Map<String, Date> map = (Map<String, Date>) redisTemplate.opsForValue().get(UserRedisKeys.ONLINEUSER);
        if (map != null && !map.isEmpty()){
            return new ArrayList<String>(map.keySet());
        }
        return new ArrayList<String>();

    }

    public void put(String partyId, Date date) {
        redisTemplate.opsForValue().set(UserRedisKeys.ONLINEUSER_PARTYID + partyId, date);

        Map<String, Date> map = (Map<String, Date>) redisTemplate.opsForValue().get(UserRedisKeys.ONLINEUSER);
        if (map == null) {
            map = new ConcurrentHashMap<String, Date>();
        }
        map.put(partyId, date);
        redisTemplate.opsForValue().set(UserRedisKeys.ONLINEUSER, map);
    }

    public void del(String partyId) {
        redisTemplate.delete(UserRedisKeys.ONLINEUSER_PARTYID + partyId);
        Map<String, Date> map = (Map<String, Date>) redisTemplate.opsForValue().get(UserRedisKeys.ONLINEUSER);
        if (map != null && !map.isEmpty()) {
            map.remove(partyId);
            redisTemplate.opsForValue().set(UserRedisKeys.ONLINEUSER, map);
        }
    }
}
