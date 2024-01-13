package com.yami.trading.service.robot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.robot.domain.RobotOrder;
import com.yami.trading.dao.robot.RobotOrderMapper;

/**
 * 机器人下单Service
 * @author lucas
 * @version 2023-05-27
 */
@Service
@Transactional
public class RobotOrderService extends ServiceImpl<RobotOrderMapper, RobotOrder> {

}
