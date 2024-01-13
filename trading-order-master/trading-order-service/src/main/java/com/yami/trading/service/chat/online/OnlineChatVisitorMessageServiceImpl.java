package com.yami.trading.service.chat.online;


import com.yami.trading.bean.Tip;
import com.yami.trading.bean.chat.domain.MessageUser;
import com.yami.trading.bean.chat.domain.OnlineChatMessage;
import com.yami.trading.bean.model.Customer;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.chat.MessageUserService;
import com.yami.trading.service.customer.CustomerService;
import com.yami.trading.service.system.TipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.*;

/**
 * @author lucas
 */
@Service
public class OnlineChatVisitorMessageServiceImpl implements OnlineChatVisitorMessageService {
	@Autowired
	private TipService tipService;

	@Autowired
    OnlineChatMessageService onlinechatMessageService;

	@Autowired
	private MessageUserService messageUserService;

	@Autowired
	CustomerService customerService;
	@Autowired
	private DBOnlineChatVisitorMessageServiceImpl onlineChatVisitorMessageService;


	@Override
	public OnlineChatMessage saveSend(String ip, String type, String send_receive, String content, String username,boolean isAutoAnswer) {
		OnlineChatMessage onlineChatMessage = new OnlineChatMessage();
		onlineChatMessage.setContentType(type);
		onlineChatMessage.setSendReceive(send_receive);
		onlineChatMessage.setContent(content);
		onlineChatMessage.setCreateTime(new Date());
		onlineChatMessage.setUsername(username);
		onlineChatMessage.setIp(ip);

		onlineChatVisitorMessageService.save(onlineChatMessage);
		List<OnlineChatMessage> list = onlinechatMessageService.cacheMessage(ip);
		if (list == null) {
			list = new LinkedList<OnlineChatMessage>();
		}
		list.add(onlineChatMessage);
		Collections.sort(list);
		Collections.reverse(list);// 添加完后，时间倒叙排序加回
		onlinechatMessageService.putMessage(ip, list);
		if (onlinechatMessageService.cacheMessageUser(ip) == null) {// 不存在则添加用户
			saveCreate(ip,username);
		}
		switch (send_receive) {
			case "receive":// 客服发送
				if (!isAutoAnswer) {
					updateUnread(ip, "user", "read");
				}
				break;
			case "send":// 用户发送
				updateUnread(ip, "customer", "write");
//			tipService.saveTip(onlineChatMessage.getId().toString(), TipConstants.ONLINECHAT);
				break;
		}
		return onlineChatMessage;
	}

	/**
	 * 更新未读数
	 *
	 * @param ip
	 * @param user_customer 更新对象，用户，客服
	 * @param type          read:读，write：写
	 */
	public void updateUnread(final String ip, String user_customer, String type) {
		MessageUser messageUser = onlinechatMessageService.cacheMessageUser(ip);
		if (messageUser == null) {
			saveCreate(ip,null);
			messageUser = onlinechatMessageService.cacheMessageUser(ip);
		}
		int removeTipNum = 0;
		switch (user_customer) {
			case "user":
				if ("read".equals(type)) {
					messageUser.setUserUnreadmsg(0);
				} else if ("write".equals(type)) {
					messageUser.setUserUnreadmsg(messageUser.getUserUnreadmsg() + 1);
					messageUser.setDeleteStatus(0);
				}
				break;
			case "customer":
				if ("read".equals(type)) {
					removeTipNum = messageUser.getCustomerUnreadmsg();
					messageUser.setCustomerUnreadmsg(0);
				} else if ("write".equals(type)) {
					messageUser.setCustomerUnreadmsg(messageUser.getCustomerUnreadmsg() + 1);
					messageUser.setDeleteStatus(0);
					final String targetUsername = onlinechatMessageService.userSendTarget(ip, new Date(), messageUser.getTargetUsername());
					if (StringUtils.isNotEmpty(targetUsername)
							&& !targetUsername.equals(messageUser.getTargetUsername())) {
						final Customer customer = customerService.cacheByUsername(targetUsername);
						// 客服不存在或者回复内容无效则不回复
						if (customer != null && customer.getAutoAnswer() != null
								&& !StringUtils.isEmptyString(customer.getAutoAnswer().trim())) {
							// 客服自动回复一条
							//saveSend(ip, "text", "receive", customer.getAutoAnswer(), targetUsername + "SYSTEM",true);
							saveSend(ip, "text", "receive", "Dear user, the reception business is currently busy. Please contact the customer service manager on whatsapp to provide you with timely assistance. I wish you smooth investment and a happy life.", targetUsername + "SYSTEM",true);
//						Thread t = new Thread(new Runnable() {
//							@Override
//							public void run() {
//								// TODO Auto-generated method stub
//								// 异步，延迟200毫秒发送
//								ThreadUtils.sleep(200);
//								saveSend(ip, "text", "receive", customer.getAutoAnswer(), targetUsername + "SYSTEM");// 客服自动回复一条
//							}
//						});
//						t.start();
						}
					}
					messageUser.setTargetUsername(targetUsername);
					if (StringUtils.isNotEmpty(targetUsername)) {// 指定的在线客服存在，则发起通知
						Tip tip = new Tip();
						tip.setBusinessId(onlinechatMessageService.cacheMessage(ip).get(0).getUuid().toString());
						tip.setModel(TipConstants.ONLINECHAT);
						tip.setTargetUsername(targetUsername);
						tipService.saveTip(tip);
					}
				}
				break;
		}
		onlinechatMessageService.updateMessageUserByIp(messageUser);
		if (removeTipNum > 0)
			onlinechatMessageService.removeTips(messageUser.getIp(), removeTipNum);
	}

	@Override
	public MessageUser saveCreate(String ip,String username) {
		MessageUser messageUser = onlinechatMessageService.cacheMessageUser(ip);
		if (messageUser == null) {
			messageUser = new MessageUser();
			messageUser.setIp(ip);
			messageUser.setUserUnreadmsg(0);
			messageUser.setCustomerUnreadmsg(0);
		}
		messageUser.setUpdatetime(new Date());
		messageUser.setDeleteStatus(0);
		messageUser.setTargetUsername(username);
		messageUserService.saveOrUpdate(messageUser);
		onlinechatMessageService.putMessageUser(ip, messageUser);
		return messageUser;
	}

	/**
	 * 设置备注
	 *
	 * @param ip
	 * @param remarks
	 */
	public String updateResetRemarks(String ip, String remarks) throws Exception {
		if (StringUtils.isEmptyString(remarks) || StringUtils.isEmptyString(remarks.trim())) {
			return null;
		}
		MessageUser messageUser = onlinechatMessageService.cacheMessageUser(ip);
		if (messageUser == null) {
			throw new BusinessException("用户不存在");
		}
		messageUser.setRemarks(URLDecoder.decode(remarks, "utf-8"));
		onlinechatMessageService.updateMessageUserByIp(messageUser);
		return remarks;
	}

	/**
	 * 获取用户信息
	 *
	 * @param ip
	 * @return
	 */
	public Map<String, Object> getUserInfo(String ip) {
		MessageUser messageUser = this.onlinechatMessageService.cacheMessageUser(ip);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("partyId", ip);
		result.put("remarks", messageUser.getRemarks());
//		result.put("username", party.getUsername());
//		result.put("usercode", party.getUsercode());
//		result.put("last_login_time", DateUtils.format(party.getLast_loginTime(),DateUtils.DF_yyyyMMddHHmmss));
//		result.put("create_time", DateUtils.format(party.getCreateTime(),DateUtils.DF_yyyyMMddHHmmss));
//		result.put("role_name", party.getRolename());
//		result.put("loginIp", party.getLoginIp());
//		List<UserRecom> parents = userRecomService.getParents(party.getId());
//		if(!CollectionUtils.isEmpty(parents)&&parents.size()>=2) {
//			Party parentParty = partyService.cachePartyBy(parents.get(1).getPartyId(), true);
//			result.put("recom_parent_name", parentParty==null?null:parentParty.getUsername());
//		}else {
//			result.put("recom_parent_name", null);
//		}
		return result;
	}



}
