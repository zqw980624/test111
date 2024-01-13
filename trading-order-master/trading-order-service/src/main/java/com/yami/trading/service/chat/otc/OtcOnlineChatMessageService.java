package com.yami.trading.service.chat.otc;

import com.yami.trading.bean.chat.domain.OtcMessageUser;
import com.yami.trading.bean.chat.domain.OtcOnlineChatMessage;

import java.util.List;
import java.util.Map;

/**
 * 客服中心管理
 */
 public interface OtcOnlineChatMessageService {

	/**
	 * 分页读取在线客服用户列表（管理员界面）
	 */
	public List<OtcMessageUser> cacheGetMessageUserPage(int pageNo, int pageSize);

	public void saveSend(String partyId, String type, String content, String username, String orderNo);

	public void delete(String orderNo);

	/**
	 * @param orderNo
	 * @param type    user:下单用户未读数，customer:承兑商未读数
	 */
	public int unreadMsg(String orderNo, String partyId);

	/**
	 * 管理后台获取未读消息
	 */
	public int unreadMsgAdmin(String orderNo);

	/**
	 * 更新未读数
	 *
	 * @param partyId
	 * @param user_customer 更新对象，下单用户，承兑商
	 * @param type          read:读，write：写
	 */
	public void updateUnread(String partyId, String type, String orderNo);

	/**
	 * 更新未读数
	 *
	 * @param partyId
	 * @param user_customer 更新对象，下单用户，承兑商
	 * @param type          read:读，write：写
	 */
	public void updateUnreadAdmin(String partyId, String type, String orderNo);

	/**
	 * 根据消息id为起始索引，获取翻页数据
	 *
	 * @param messageId
	 * @param pageSize
	 * @param partyId
	 * @return
	 */
	public List<OtcOnlineChatMessage> cacheGetList(String messageId, int pageSize, String orderNo);

	public List<OtcOnlineChatMessage> cacheGetList(String messageId, int pageSize, String orderNo, String clicentType);

	/**
	 * 获取聊天用户
	 *
	 * @param key
	 * @return
	 */
	public OtcMessageUser cacheMessageUser(String key);

	/**
	 * 设置备注
	 *
	 * @param partyId
	 * @param remarks
	 */
	public String resetRemarks(String orderNo, String remarks) throws Exception;

	/**
	 * 获取用户信息
	 *
	 * @param partyId
	 * @return
	 */
	public Map<String, Object> saveGetUserInfo(String orderNo, String partyId);

	public Map<String, List<OtcOnlineChatMessage>> cacheMessageAll();

	public Map<String, OtcMessageUser> cacheMessageUserAll();

	public void putMessage(String key, List<OtcOnlineChatMessage> value);

	public void putMessageUser(String key, OtcMessageUser value);

	public List<OtcOnlineChatMessage> cacheMessage(String key);

	public void updateMessageUserByIp(OtcMessageUser messageUser);

	public void deleteByIp(String ip);

	/**
	 * 移除通知
	 */
	public void removeTips(String partyId, int removeTipNum);

	public OtcMessageUser saveCreateByOrderNo(String orderNo);

	/**
	 * 批量获取未读数
	 */
	public Map<String,Integer> unreadMsgs(List<String> orderNos);

	public Map<String, Integer> unreadMsgsApi(List<String> orderNos);

	public void updateMessageDelete(String messageId, String targetUserName);
}
