/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.data.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.data.dto.KlineDTO;
import com.yami.trading.bean.data.domain.Kline;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  KlineWrapper
 * @author lucas
 * @version 2023-03-16
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface KlineWrapper extends EntityWrapper<KlineDTO, Kline> {

    KlineWrapper INSTANCE = Mappers.getMapper(KlineWrapper.class);
}

