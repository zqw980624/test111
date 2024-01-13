/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.sys.dao.SysLogMapper;
import com.yami.trading.sys.model.SysLog;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysLogService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author lgh
 */
@Service("sysLogService")
@AllArgsConstructor
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

	private final SysLogMapper sysLogMapper;

	//@Override
	//public void saveUserAndUserRole(SysUser user) {

	//}
}
