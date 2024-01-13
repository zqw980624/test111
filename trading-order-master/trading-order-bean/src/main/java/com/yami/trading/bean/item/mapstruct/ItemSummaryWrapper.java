/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.item.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.item.dto.ItemSummaryDTO;
import com.yami.trading.bean.item.domain.ItemSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  ItemSummaryWrapper
 * @author lucas
 * @version 2023-05-01
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface ItemSummaryWrapper extends EntityWrapper<ItemSummaryDTO, ItemSummary> {

    ItemSummaryWrapper INSTANCE = Mappers.getMapper(ItemSummaryWrapper.class);
    
    ItemSummary toEntity(ItemSummaryDTO dto);


 
    ItemSummaryDTO toDTO(ItemSummary entity);
}

