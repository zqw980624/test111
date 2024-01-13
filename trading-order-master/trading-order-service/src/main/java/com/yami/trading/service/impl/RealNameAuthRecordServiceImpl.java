package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.dao.user.RealNameAuthRecordMapper;
import com.yami.trading.service.RealNameAuthRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RealNameAuthRecordServiceImpl extends ServiceImpl<RealNameAuthRecordMapper, RealNameAuthRecord> implements RealNameAuthRecordService {
    @Override
    public RealNameAuthRecord getByUserId(String userId) {
        return getOne(Wrappers.<RealNameAuthRecord>query().lambda().eq(RealNameAuthRecord::getUserId, userId));
    }

    @Override
    public Page pageRecord(Page page, String rolename, String idNumber, String status,String userCode) {
        return baseMapper.pageRecord(page,rolename,idNumber,status,userCode);
    }

    @Override
    public Page pageRecords(Page page, List<String> roleNames, String idNumber, String status, String userCode, List<String> checkedList) {
        return baseMapper.pageRecords(page,roleNames,idNumber,status,userCode,checkedList);
    }

    @Override
    public boolean isPass(String userId) {
        RealNameAuthRecord kyc = getByUserId(userId);
        if (null == kyc)
            return Boolean.FALSE;
        return kyc.getStatus() == 2;
    }

    @Override
    public long waitCount() {
        return  count(Wrappers.<RealNameAuthRecord>query().lambda().eq(RealNameAuthRecord::getStatus,1));
    }

}
