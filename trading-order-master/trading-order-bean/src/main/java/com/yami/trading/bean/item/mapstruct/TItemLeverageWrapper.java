/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.item.mapstruct;


import com.yami.trading.bean.item.domain.ItemLeverage;
import com.yami.trading.bean.item.dto.ItemLeverageDTO;
import com.yami.trading.common.mapstruct.EntityWrapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  TItemLeverageWrapper
 * @author lucas
 * @version 2023-03-10
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface TItemLeverageWrapper extends EntityWrapper<ItemLeverageDTO, ItemLeverage> {

    TItemLeverageWrapper INSTANCE = Mappers.getMapper(TItemLeverageWrapper.class);
    
    ItemLeverage toEntity(ItemLeverageDTO dto);


 
    ItemLeverageDTO toDTO(ItemLeverage entity);
}

