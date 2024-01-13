package com.yami.trading.dao.robot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.robot.domain.Robot;
import com.yami.trading.bean.robot.dto.RobotDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 下单机器人MAPPER接口
 * @author lucas
 * @version 2023-05-04
 */
public interface RobotMapper extends BaseMapper<Robot> {

    /**
     * 根据id获取下单机器人
     * @param id
     * @return
     */
    RobotDTO findById(String id);

    /**
     * 获取下单机器人列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<RobotDTO> findList(Page<RobotDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
