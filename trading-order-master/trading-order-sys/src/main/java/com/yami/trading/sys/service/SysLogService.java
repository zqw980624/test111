/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.sys.model.SysLog;
import com.yami.trading.sys.model.SysUser;

/**
 * 系统日志
 * @author lgh
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 保存用户与用户角色关系
     * @param user
     */
    //void saveUserAndUserRole(SysUser user);
}
