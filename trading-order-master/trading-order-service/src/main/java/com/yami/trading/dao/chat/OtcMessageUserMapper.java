package com.yami.trading.dao.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yami.trading.bean.chat.domain.OtcMessageUser;
import com.yami.trading.bean.chat.dto.OtcMessageUserDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;

/**
 * 聊天用户MAPPER接口
 * @author lucas
 * @version 2023-04-15
 */
public interface OtcMessageUserMapper extends BaseMapper<OtcMessageUser> {

    /**
     * 根据id获取聊天用户
     * @param id
     * @return
     */
    OtcMessageUserDTO findById(String id);

    /**
     * 获取聊天用户列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<OtcMessageUserDTO> findList(Page<OtcMessageUserDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
