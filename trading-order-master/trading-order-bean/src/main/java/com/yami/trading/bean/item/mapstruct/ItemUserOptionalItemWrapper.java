/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.item.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.item.dto.ItemUserOptionalItemDTO;
import com.yami.trading.bean.item.domain.ItemUserOptionalItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  ItemUserOptionalItemWrapper
 * @author lucas
 * @version 2023-03-10
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface ItemUserOptionalItemWrapper extends EntityWrapper<ItemUserOptionalItemDTO, ItemUserOptionalItem> {

    ItemUserOptionalItemWrapper INSTANCE = Mappers.getMapper(ItemUserOptionalItemWrapper.class);
}

