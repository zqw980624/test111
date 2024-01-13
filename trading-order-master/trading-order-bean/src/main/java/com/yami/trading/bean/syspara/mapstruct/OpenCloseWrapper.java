/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.syspara.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.syspara.dto.OpenCloseDTO;
import com.yami.trading.bean.syspara.domain.OpenClose;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  OpenCloseWrapper
 * @author lucas
 * @version 2023-05-20
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface OpenCloseWrapper extends EntityWrapper<OpenCloseDTO, OpenClose> {

    OpenCloseWrapper INSTANCE = Mappers.getMapper(OpenCloseWrapper.class);
}

