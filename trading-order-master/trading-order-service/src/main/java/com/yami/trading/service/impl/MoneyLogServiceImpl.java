package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.HighLevelAuthRecord;
import com.yami.trading.bean.model.MoneyLog;
import com.yami.trading.bean.user.dto.SumBSalanceDto;
import com.yami.trading.dao.user.HighLevelAuthRecordMapper;
import com.yami.trading.dao.user.MoneyLogMapper;
import com.yami.trading.service.HighLevelAuthRecordService;
import com.yami.trading.service.MoneyLogService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MoneyLogServiceImpl extends ServiceImpl<MoneyLogMapper, MoneyLog> implements MoneyLogService {
    @Override
    public Page pageMoneyLog(String userCode, Page page, String rolename, Date startTime,
                             Date endTime,
                             String userName,String log,String category) {
        return baseMapper.pageMoneyLog(page, userCode, rolename, startTime, endTime, userName,log,category);
    }

    @Override
    public SumBSalanceDto sumBSalance(String category, String contentType, String partyId) {
        return baseMapper.sumBSalance(category,contentType,partyId);
    }
}


