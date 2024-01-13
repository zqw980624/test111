package com.yami.trading.service.robot;

import com.yami.trading.bean.robot.domain.Robot;
import com.yami.trading.dao.robot.RobotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 下单机器人Service
 * @author lucas
 * @version 2023-05-04
 */
@Service
@Transactional
public class RobotService extends ServiceImpl<RobotMapper, Robot> {

}
