/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.syspara.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.syspara.dto.SysparaDTO;
import com.yami.trading.bean.syspara.domain.Syspara;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  SysparaWrapper
 * @author lucas
 * @version 2023-03-17
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface SysparaWrapper extends EntityWrapper<SysparaDTO, Syspara> {

    SysparaWrapper INSTANCE = Mappers.getMapper(SysparaWrapper.class);
}

