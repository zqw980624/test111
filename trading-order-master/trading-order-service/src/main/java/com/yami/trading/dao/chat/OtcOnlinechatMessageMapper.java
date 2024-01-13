package com.yami.trading.dao.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yami.trading.bean.chat.domain.OtcOnlineChatMessage;
import com.yami.trading.bean.chat.dto.TOtcOnlinechatMessageDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;

/**
 * im消息MAPPER接口
 * @author lucas
 * @version 2023-04-15
 */
public interface OtcOnlinechatMessageMapper extends BaseMapper<OtcOnlineChatMessage> {

    /**
     * 根据id获取im消息
     * @param id
     * @return
     */
    TOtcOnlinechatMessageDTO findById(String id);

    /**
     * 获取im消息列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<TOtcOnlinechatMessageDTO> findList(Page<TOtcOnlinechatMessageDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
