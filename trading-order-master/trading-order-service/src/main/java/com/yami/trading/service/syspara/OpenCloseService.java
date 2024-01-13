package com.yami.trading.service.syspara;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.syspara.domain.OpenClose;
import com.yami.trading.dao.syspara.OpenCloseMapper;

/**
 * 开盘停盘时间设置Service
 * @author lucas
 * @version 2023-05-20
 */
@Service
@Transactional
public class OpenCloseService extends ServiceImpl<OpenCloseMapper, OpenClose> {

}
