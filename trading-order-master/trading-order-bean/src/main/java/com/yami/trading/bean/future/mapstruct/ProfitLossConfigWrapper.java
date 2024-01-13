/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.future.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.future.dto.ProfitLossConfigDTO;
import com.yami.trading.bean.future.domain.ProfitLossConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  ProfitLossConfigWrapper
 * @author lucas
 * @version 2023-04-08
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface ProfitLossConfigWrapper extends EntityWrapper<ProfitLossConfigDTO, ProfitLossConfig> {

    ProfitLossConfigWrapper INSTANCE = Mappers.getMapper(ProfitLossConfigWrapper.class);
}

