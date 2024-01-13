/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.data.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.data.dto.SymbolsDTO;
import com.yami.trading.bean.data.domain.Symbols;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  SymbolsWrapper
 * @author lucas
 * @version 2023-03-17
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface SymbolsWrapper extends EntityWrapper<SymbolsDTO, Symbols> {

    SymbolsWrapper INSTANCE = Mappers.getMapper(SymbolsWrapper.class);
}

