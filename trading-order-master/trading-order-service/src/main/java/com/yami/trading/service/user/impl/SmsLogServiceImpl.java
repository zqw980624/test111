package com.yami.trading.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.SmsLog;
 import com.yami.trading.dao.user.SmsLogMapper;
 import com.yami.trading.service.user.SmsLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsLogServiceImpl extends ServiceImpl<SmsLogMapper, SmsLog> implements SmsLogService {

}
