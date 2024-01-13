package com.yami.trading.dao.robot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.robot.domain.RobotOrder;
import com.yami.trading.bean.robot.dto.RobotOrderDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 机器人下单MAPPER接口
 * @author lucas
 * @version 2023-05-27
 */
public interface RobotOrderMapper extends BaseMapper<RobotOrder> {

    /**
     * 根据id获取机器人下单
     * @param id
     * @return
     */
    RobotOrderDTO findById(String id);

    /**
     * 获取机器人下单列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<RobotOrderDTO> findList(Page<RobotOrderDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
