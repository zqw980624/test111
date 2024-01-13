package com.yami.trading.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.HighLevelAuthRecord;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.user.dto.HighLevelAuthRecordDto;
import com.yami.trading.bean.user.dto.RealNameAuthDto;

public interface HighLevelAuthRecordService  extends IService<HighLevelAuthRecord> {
    HighLevelAuthRecord findByUserId(String userId);

    Page<HighLevelAuthRecordDto> pageRecord(Page page, String rolename, String status, String userName);

    long waitCount();

}
