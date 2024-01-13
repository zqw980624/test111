/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.data.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.data.dto.RealtimeDTO;
import com.yami.trading.bean.data.domain.Realtime;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  RealtimeWrapper
 * @author lucas
 * @version 2023-03-16
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface RealtimeWrapper extends EntityWrapper<RealtimeDTO, Realtime> {

    RealtimeWrapper INSTANCE = Mappers.getMapper(RealtimeWrapper.class);
}

