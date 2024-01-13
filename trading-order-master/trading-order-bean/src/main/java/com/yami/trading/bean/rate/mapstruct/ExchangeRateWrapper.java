/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.rate.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.rate.dto.ExchangeRateDTO;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  ExchangeRateWrapper
 * @author lucas
 * @version 2023-03-28
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface ExchangeRateWrapper extends EntityWrapper<ExchangeRateDTO, ExchangeRate> {

    ExchangeRateWrapper INSTANCE = Mappers.getMapper(ExchangeRateWrapper.class);
}

