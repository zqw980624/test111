package com.yami.trading.service.chat.otc;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.chat.domain.OtcMessageUser;
import com.yami.trading.dao.chat.OtcMessageUserMapper;

/**
 * 聊天用户Service
 * @author lucas
 * @version 2023-04-15
 */
@Service
@Transactional
public class OtcMessageUserService extends ServiceImpl<OtcMessageUserMapper, OtcMessageUser> {

}
