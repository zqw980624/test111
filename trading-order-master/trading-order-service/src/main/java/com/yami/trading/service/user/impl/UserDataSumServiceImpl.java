package com.yami.trading.service.user.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.UserData;
import com.yami.trading.bean.model.UserDataSum;
import com.yami.trading.dao.user.UserDataMapper;
import com.yami.trading.dao.user.UserDataSumMapper;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserDataSumService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDataSumServiceImpl extends ServiceImpl<UserDataSumMapper, UserDataSum> implements UserDataSumService {
    @Override
    public List getByUserId(String partyId) {
        return list(Wrappers.<UserDataSum>query().lambda().eq(UserDataSum::getUserId,partyId));
    }
}
