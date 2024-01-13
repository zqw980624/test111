/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.etf.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.etf.dto.KlineConfigDTO;
import com.yami.trading.bean.etf.domain.KlineConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  KlineConfigWrapper
 * @author lucas
 * @version 2023-05-03
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface KlineConfigWrapper extends EntityWrapper<KlineConfigDTO, KlineConfig> {

    KlineConfigWrapper INSTANCE = Mappers.getMapper(KlineConfigWrapper.class);
}

