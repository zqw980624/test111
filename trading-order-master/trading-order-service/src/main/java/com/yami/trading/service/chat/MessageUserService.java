package com.yami.trading.service.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.chat.domain.MessageUser;
import com.yami.trading.dao.chat.MessageUserMapper;

/**
 * 聊天用户Service
 * @author lucas
 * @version 2023-04-16
 */
@Service
@Transactional
public class MessageUserService extends ServiceImpl<MessageUserMapper, MessageUser> {


    public MessageUser cacheMessageUser(String partyId) {
        QueryWrapper<MessageUser> queryWrapper = new QueryWrapper();
        queryWrapper.and(w->w.eq("party_id", partyId).or().eq("ip", partyId));
        return baseMapper.selectOne(queryWrapper);
    }
}
