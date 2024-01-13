/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.item.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.item.dto.ItemUserOptionalDTO;
import com.yami.trading.bean.item.domain.ItemUserOptional;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  ItemUserOptionalWrapper
 * @author lucas
 * @version 2023-03-10
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface ItemUserOptionalWrapper extends EntityWrapper<ItemUserOptionalDTO, ItemUserOptional> {

    ItemUserOptionalWrapper INSTANCE = Mappers.getMapper(ItemUserOptionalWrapper.class);
}

