package com.yami.trading.service.purchasing.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.purchasing.UserPurchasingRecord;
import com.yami.trading.bean.purchasing.dto.UserPurchasingRecordDto;
import com.yami.trading.dao.purchasing.UserPurchasingRecordMapper;
import com.yami.trading.service.purchasing.UserPurchasingRecordService;
import org.springframework.stereotype.Service;

@Service
public class UserPurchasingRecordServiceImpl extends ServiceImpl<UserPurchasingRecordMapper,UserPurchasingRecord> implements UserPurchasingRecordService {
    @Override
    public Page<UserPurchasingRecordDto> listPage(Page page, String rolename, String userName) {
        return baseMapper.listPage(page,rolename,userName);
    }
}
