package com.yami.trading.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.log.dto.LogDto;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;

public interface  LogService  extends IService<Log> {
    void saveLog(User user, String logText, String logCategoryC2c);

    Page<LogDto> listPage(Page page, String rolename,
                          String userName,
                          String log, String category,
                          String operator);
}
