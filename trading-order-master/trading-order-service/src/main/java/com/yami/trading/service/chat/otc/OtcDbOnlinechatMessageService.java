package com.yami.trading.service.chat.otc;

import com.yami.trading.bean.chat.domain.OtcOnlineChatMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.dao.chat.OtcOnlinechatMessageMapper;

/**
 * im消息Service
 * @author lucas
 * @version 2023-04-15
 */
@Service
@Transactional
public class OtcDbOnlinechatMessageService extends ServiceImpl<OtcOnlinechatMessageMapper, OtcOnlineChatMessage> {

}
