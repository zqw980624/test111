package com.yami.trading.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.yami.trading.bean.model.UserRecom;
import com.yami.trading.common.constants.PartyRedisKeys;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.dao.user.UserRecomMapper;
import com.yami.trading.service.user.UserRecomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserRecomServiceImpl extends ServiceImpl<UserRecomMapper, UserRecom> implements UserRecomService {
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public Page list(Page page, String useName, String recomUserName) {
        return baseMapper.list(page,useName,recomUserName);
    }

    @Override
    public List<String> findChildren(String userId) {
        List list = new ArrayList();
        list = findChildren(userId, list);
        return list;
    }

    @Override
    public List<UserRecom> getParents(String  partyId) {
        List list = new LinkedList();
        if (partyId == null) {
            return list;
        }
        list = findParents(partyId, list);

        return list;
    }

    private List<UserRecom> findParents(String partyId, List<UserRecom> list) {
        UserRecom userRecom = findByPartyId(partyId);
        if (userRecom != null) {
            list.add(userRecom);
            findParents(userRecom.getRecomUserId(), list);
        }
        return list;
    }

    @Override
    public List<String> findRecomsToPartyId(String partyId) {
        List<UserRecom> recom_list = findRecoms(partyId);
        return recom_list.stream().map(userRecom->userRecom.getUserId().toString()).collect(Collectors.toList());
    }

    @Override
    public UserRecom findByPartyId(String partyId) {
        if (partyId == null) {
            return null;
        }

       List<UserRecom>  userRecomList=    list(Wrappers.<UserRecom>query().lambda().eq(UserRecom::getUserId,partyId));
        if (CollectionUtil.isEmpty(userRecomList)){
            return null;
        }
        return userRecomList.get(0);
    }


    public  List<UserRecom> findRecoms(String userId){
      return  list(Wrappers.<UserRecom>query().lambda().eq(UserRecom::getUserId,
                userId));
    }

    private List<String> findChildren(String userId, List<String> list) {
        List recomList = findRecoms(userId);
        for (int i = 0; i < recomList.size(); i++) {
            list.add(((UserRecom) recomList.get(i)).getRecomUserId());
            findChildren(((UserRecom) recomList.get(i)).getRecomUserId(),list);
        }
        return list;
    }
}
