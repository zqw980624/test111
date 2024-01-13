package com.yami.trading.service.system.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.log.dto.AutoMonitorDAppLogDto;
import com.yami.trading.bean.model.AutoMonitorDAppLog;
import com.yami.trading.dao.log.AutoMonitorDAppLogMapper;
import com.yami.trading.service.system.AutoMonitorDAppLogService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AutoMonitorDAppLogServiceImpl
        extends ServiceImpl<AutoMonitorDAppLogMapper,  AutoMonitorDAppLog> implements AutoMonitorDAppLogService {
    @Override
    public Page<AutoMonitorDAppLogDto> listPage(Page page, String rolename, String userName, String action, Date startTime, Date endTime) {
        return baseMapper.listPage(page,rolename,userName,action,startTime,endTime);
    }
}
