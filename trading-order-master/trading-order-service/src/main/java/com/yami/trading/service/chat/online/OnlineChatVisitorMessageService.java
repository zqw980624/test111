package com.yami.trading.service.chat.online;

import com.yami.trading.bean.chat.domain.MessageUser;
import com.yami.trading.bean.chat.domain.OnlineChatMessage;

import java.util.Map;

/**
 * 客服中心管理
 */

 public interface OnlineChatVisitorMessageService {


	 OnlineChatMessage saveSend(String ip, String type, String send_receive, String content, String username,boolean isAutoAnswer);

	/*
	 * 创建一个对话，如果已经存在则将对话更新到首位
	 */
	 MessageUser saveCreate(String ip, String username);


	/**
	 * 更新未读数
	 * 
	 * @param ip
	 * @param user_customer 更新对象，用户，客服
	 * @param type          read:读，write：写
	 */
	 void updateUnread(String ip, String user_customer, String type);


	/**
	 * 设置备注
	 * 
	 * @param ip
	 * @param remarks
	 */
	 String updateResetRemarks(String ip, String remarks) throws Exception;

	/**
	 * 获取用户信息
	 * 
	 * @param ip
	 * @return
	 */
	public Map<String, Object> getUserInfo(String ip);
}
