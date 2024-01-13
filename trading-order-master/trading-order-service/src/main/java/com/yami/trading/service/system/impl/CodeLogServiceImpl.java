package com.yami.trading.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.log.domain.CodeLog;
import com.yami.trading.bean.model.AutoMonitorDAppLog;
import com.yami.trading.dao.log.CodeLogMapper;
import com.yami.trading.service.system.AutoMonitorDAppLogService;
import com.yami.trading.service.system.CodeLogService;
import org.springframework.stereotype.Service;

@Service
public class CodeLogServiceImpl  extends ServiceImpl<CodeLogMapper, CodeLog> implements CodeLogService {
}
