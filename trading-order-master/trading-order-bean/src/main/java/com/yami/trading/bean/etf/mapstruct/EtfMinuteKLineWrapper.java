/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.etf.mapstruct;


import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.etf.domain.EtfMinuteKLine;
import com.yami.trading.bean.etf.domain.EtfSecKLine;
import com.yami.trading.common.mapstruct.EntityWrapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  KlineConfigWrapper
 * @author lucas
 * @version 2023-05-03
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface EtfMinuteKLineWrapper extends EntityWrapper<EtfMinuteKLine,Kline> {

    EtfMinuteKLineWrapper INSTANCE = Mappers.getMapper(EtfMinuteKLineWrapper.class);
}

