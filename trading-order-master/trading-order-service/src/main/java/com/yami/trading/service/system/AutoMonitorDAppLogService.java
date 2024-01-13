package com.yami.trading.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.log.dto.AutoMonitorDAppLogDto;
import com.yami.trading.bean.model.AutoMonitorDAppLog;

import java.util.Date;

public interface AutoMonitorDAppLogService extends IService<AutoMonitorDAppLog> {


    Page<AutoMonitorDAppLogDto> listPage(Page page, String rolename,
                                         String userName,
                                         String action,
                                        Date startTime,
                                         Date endTime);

}
