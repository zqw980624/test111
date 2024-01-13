package com.yami.trading.service.system.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.log.dto.LogDto;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.dao.log.LogMapper;
import com.yami.trading.service.system.LogService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LogServiceImpl  extends ServiceImpl<LogMapper, Log> implements LogService {
    @Override
    public void saveLog(User user, String logText, String category) {
        Log log = new Log();
        log.setCategory(category);
        log.setOperator(user.getUserName());
        log.setUsername(user.getUserName());
        log.setUserId(user.getUserId());
        log.setLog(logText);
        log.setCreateTime(new Date());
        save(log);
    }

    @Override
    public Page<LogDto> listPage(Page page, String rolename,
                                 String userName,
                                 String log, String category,
                                 String operator) {
        return baseMapper.listPage(page,rolename,userName,log,category,operator);
    }

}
