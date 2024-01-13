package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.HighLevelAuthRecord;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.user.dto.HighLevelAuthRecordDto;
import com.yami.trading.dao.user.HighLevelAuthRecordMapper;
import com.yami.trading.service.HighLevelAuthRecordService;
import org.springframework.stereotype.Service;

@Service
public class HighLevelAuthRecordServiceImpl  extends ServiceImpl<HighLevelAuthRecordMapper, HighLevelAuthRecord> implements HighLevelAuthRecordService {
    @Override
    public HighLevelAuthRecord findByUserId(String userId) {
        return getOne(Wrappers.<HighLevelAuthRecord>query().lambda().eq(HighLevelAuthRecord::getUserId, userId));
    }

    @Override
    public Page<HighLevelAuthRecordDto> pageRecord(Page page,String rolename, String status, String userName) {

        return baseMapper.listRecord(page,rolename,status,userName);
    }

    @Override
    public long waitCount() {
        return  count(Wrappers.<HighLevelAuthRecord>query().lambda().eq(HighLevelAuthRecord::getStatus,1));
    }


}
