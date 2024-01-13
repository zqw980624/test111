/**
 * Copyright © 2021-2025 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.yami.trading.bean.chat.mapstruct;


import com.yami.trading.bean.chat.domain.OtcOnlineChatMessage;
import com.yami.trading.common.mapstruct.EntityWrapper;
import com.yami.trading.bean.chat.dto.TOtcOnlinechatMessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 *  TOtcOnlinechatMessageWrapper
 * @author lucas
 * @version 2023-04-15
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {} )
public interface TOtcOnlinechatMessageWrapper extends EntityWrapper<TOtcOnlinechatMessageDTO, OtcOnlineChatMessage> {

    TOtcOnlinechatMessageWrapper INSTANCE = Mappers.getMapper(TOtcOnlinechatMessageWrapper.class);
}

