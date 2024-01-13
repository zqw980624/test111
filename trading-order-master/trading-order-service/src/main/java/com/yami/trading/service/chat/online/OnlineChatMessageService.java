package com.yami.trading.service.chat.online;

import com.yami.trading.bean.chat.domain.MessageUser;
import com.yami.trading.bean.chat.domain.OnlineChatMessage;
import com.yami.trading.bean.chat.query.ChatUserInfoRespModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 客服中心管理
 */

 public interface OnlineChatMessageService {

//	 List<OnlinechatMessage> cacheGetList(int pageNo, int pageSize, String partyId);

	/**
	 * 分页读取在线客服用户列表（管理员界面）
	 */
	public List<MessageUser> cacheGetMessageUserPage(int pageNo, int pageSize, String username);

	 OnlineChatMessage saveSend(String partyId, String type, String send_receive, String content,
								String username, boolean isAutoAnswer);

	/*
	 * 创建一个对话，如果已经存在则将对话更新到首位
	 */
	 MessageUser saveCreate(String uid, String username);

	 void delete(String partyId);

	/**
	 * 未读消息数，不输入参数，则返回所有的未读消息数
	 * 
	 * @param partyId 	     用户id
	 * @param type           user:用户未读数，customer:客服未读数
	 * @param targetUsername 当为客服时，指定用户的未读数
	 * @return
	 */
	 int unreadMsg(String partyId, String type, String targetUsername);

	/**
	 * 更新未读数
	 * 
	 * @param partyId
	 * @param user_customer 更新对象，用户，客服
	 * @param type          read:读，write：写
	 */
	 void updateUnread(String partyId, String user_customer, String type);

	/**
	 * 根据消息id为起始索引，获取翻页数据
	 * 
	 * @param messageId
	 * @param pageSize
	 * @param partyId
	 * @return
	 */
	 List<OnlineChatMessage> cacheGetList(String messageId, int pageSize, String partyId, String... clicentType );

	/**
	 * 获取聊天用户
	 * 
	 * @param key
	 * @return
	 */
	 MessageUser cacheMessageUser(String key);

	/**
	 * 设置备注
	 * 
	 * @param partyId
	 * @param remarks
	 */
	public String updateResetRemarks(String partyId, String remarks) throws Exception;

	/**
	 * 获取用户信息
	 * 
	 * @param partyId
	 * @return
	 */
	public Map<String, Object> getUserInfo(String partyId);

	 Map<String, List<OnlineChatMessage>> cacheMessageAll();

	 Map<String, MessageUser> cacheMessageUserAll();

	 void putMessage(String key, List<OnlineChatMessage> value);

	 void putMessageUser(String key, MessageUser value);

	 List<OnlineChatMessage> cacheMessage(String key);

	 void updateMessageUserByIp(MessageUser messageUser);

	 void deleteByIp(String ip);

	/**
	 * 移除通知
	 * 
	 * @param partyId
	 * @param removeTipNum
	 */
	 void removeTips(String partyId, long removeTipNum);

	/**
	 * 未分配到客服的用户，分配客服
	 * 
	 * @return
	 */
	 void updateNoAnwserUser(String username);

	/**
	 * 用户发送客服获取
	 * 
	 * @param partyId
	 * @param sendTime
	 * @param targetUsername
	 * @return
	 */
	 String userSendTarget(String partyId, Date sendTime, String targetUsername);

	 OnlineChatMessage getMessageById(String messageId);

	/**
	 * 后台客服撤回消息操作
	 * 
	 * @param messageId
	 * @param targetUserName
	 */
	 void updateMessageDelete(String messageId, String targetUserName);
}
