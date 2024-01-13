/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.robot.mapstruct;


import com.yami.trading.bean.robot.domain.Robot;
import com.yami.trading.bean.robot.dto.RobotDTO;
import com.yami.trading.common.mapstruct.EntityWrapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  RobotWrapper
 * @author lucas
 * @version 2023-05-04
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface RobotWrapper extends EntityWrapper<RobotDTO, Robot> {

    RobotWrapper INSTANCE = Mappers.getMapper(RobotWrapper.class);
}

