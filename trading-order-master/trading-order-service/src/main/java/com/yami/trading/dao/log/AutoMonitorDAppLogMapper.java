package com.yami.trading.dao.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.log.dto.AutoMonitorDAppLogDto;
import com.yami.trading.bean.model.AutoMonitorDAppLog;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface AutoMonitorDAppLogMapper extends BaseMapper<AutoMonitorDAppLog> {


    Page<AutoMonitorDAppLogDto> listPage(Page page, @Param("rolename")  String rolename,
                                         @Param("userName") String userName,
                                        @Param("action") String action,
                                         @Param("startTime") Date startTime,
                                         @Param("endTime") Date endTime);

}
