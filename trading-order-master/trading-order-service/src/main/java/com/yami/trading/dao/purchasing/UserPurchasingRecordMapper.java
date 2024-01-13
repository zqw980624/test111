package com.yami.trading.dao.purchasing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.log.dto.AutoMonitorDAppLogDto;
import com.yami.trading.bean.purchasing.UserPurchasingRecord;
import com.yami.trading.bean.purchasing.dto.UserPurchasingRecordDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface  UserPurchasingRecordMapper extends BaseMapper<UserPurchasingRecord> {

    Page<UserPurchasingRecordDto> listPage(Page page, @Param("rolename")  String rolename,
                                           @Param("userName") String userName);
}

