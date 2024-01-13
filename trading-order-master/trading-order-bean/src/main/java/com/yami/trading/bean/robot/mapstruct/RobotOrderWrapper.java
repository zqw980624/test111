/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.robot.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.robot.dto.RobotOrderDTO;
import com.yami.trading.bean.robot.domain.RobotOrder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  RobotOrderWrapper
 * @author lucas
 * @version 2023-05-27
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface RobotOrderWrapper extends EntityWrapper<RobotOrderDTO, RobotOrder> {

    RobotOrderWrapper INSTANCE = Mappers.getMapper(RobotOrderWrapper.class);
}

