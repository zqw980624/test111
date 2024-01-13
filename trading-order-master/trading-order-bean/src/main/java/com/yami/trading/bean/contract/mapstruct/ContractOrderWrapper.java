/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.contract.mapstruct;


import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.contract.dto.ContractOrderDTO;
import com.yami.trading.bean.contract.domain.ContractOrder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  ContractOrderWrapper
 * @author lucas
 * @version 2023-03-29
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface ContractOrderWrapper extends EntityWrapper<ContractOrderDTO, ContractOrder> {

    ContractOrderWrapper INSTANCE = Mappers.getMapper(ContractOrderWrapper.class);
}

