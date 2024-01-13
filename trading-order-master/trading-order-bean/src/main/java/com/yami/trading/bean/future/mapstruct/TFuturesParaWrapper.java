/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.future.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.future.dto.FuturesParaDTO;
import com.yami.trading.bean.future.domain.FuturesPara;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  TFuturesParaWrapper
 * @author lucas
 * @version 2023-04-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface TFuturesParaWrapper extends EntityWrapper<FuturesParaDTO, FuturesPara> {

    TFuturesParaWrapper INSTANCE = Mappers.getMapper(TFuturesParaWrapper.class);
}

