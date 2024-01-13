package com.yami.trading.api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.yami.trading.bean.chat.domain.OtcMessageUser;
import com.yami.trading.bean.chat.domain.OtcOnlineChatMessage;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.chat.otc.OtcOnlineChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class ApiOtcOnlineChatController {
	@Autowired
	AwsS3OSSFileService awsS3OSSFileService;
	private Logger logger = LoggerFactory.getLogger(ApiOtcOnlineChatController.class);
	
	@Resource
	private OtcOnlineChatMessageService otcOnlineChatMessageService;
	
	public final String action = "api/otcOnlinechat";

	/**
	 * 消息列表
	 */
	@RequestMapping(action + "!list.action")
	public Object list(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();

		String partyId = SecurityUtils.getCurrentUserId();
		try {
			String messageId = request.getParameter("message_id");
			String orderNo = request.getParameter("orderNo");
			// 首页的时候才更新未读数
			if (StringUtils.isNullOrEmpty(messageId)) {
				OtcMessageUser cacheMessageUser = otcOnlineChatMessageService.cacheMessageUser(orderNo);
				if (cacheMessageUser != null && cacheMessageUser.getUserUnreadmsg() > 0) {
					otcOnlineChatMessageService.updateUnread(partyId, "read", orderNo);
				}
			}

			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			List<OtcOnlineChatMessage> list = otcOnlineChatMessageService.cacheGetList(messageId, 10, orderNo, "user");
			for (OtcOnlineChatMessage message : list) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", message.getUuid());
				// 发送管理员为空的时候，认为是发送的
				if (partyId.equals(message.getPartyId()) && StringUtils.isEmptyString(message.getUsername())) {
					map.put("send_receive", "send");
				} else {
					map.put("send_receive", "receive");
				}
				
				String type = message.getContentType();
				map.put("type", type);
				String content = message.getContent();
				String contentType = message.getContentType();
				if ("img".equals(contentType) && !message.getContent().startsWith("http")){
					content=awsS3OSSFileService.getUrl(message.getContent());
				} else {
					content=message.getContent();
				}
				map.put("content", content);
				map.put("createtime", DateUtils.format(message.getCreateTime(), "MM-dd HH:mm"));
				data.add(map);
			}
			resultObject.setData(data);
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
			logger.error("error:", e);
		}
		return resultObject;
	}

	/**
	 * 发送消息
	 */
	@RequestMapping(value = action + "!send.action")
	public Object send(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		try {
			String content = request.getParameter("content");
			String type = request.getParameter("type");
			String orderNo = request.getParameter("orderNo");
			if (StringUtils.isNullOrEmpty(content.trim()) || StringUtils.isNullOrEmpty(type)) {
				return resultObject;
			}
			
			if(StringUtils.isEmptyString(orderNo)) {
				throw new YamiShopBindException("订单号不能未空");
			}

			otcOnlineChatMessageService.saveSend(SecurityUtils.getCurrentUserId(), type, content, null, orderNo);
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		return resultObject;
	}

	/**
	 * 获取未读消息数
	 */
	@RequestMapping(action + "!unread.action")
	public Object unread(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();

		try {
			int unreadMsg = 0;
			String orderNo = request.getParameter("orderNo");
			if(StringUtils.isEmptyString(orderNo)) {
				throw new YamiShopBindException("订单号不能未空");
			}
			unreadMsg = otcOnlineChatMessageService.unreadMsg(orderNo, SecurityUtils.getCurrentUserId());
			resultObject.setData(unreadMsg);

		}catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		return resultObject;
	}

	public void setOtcOnlineChatMessageService(OtcOnlineChatMessageService otcOnlineChatMessageService) {
		this.otcOnlineChatMessageService = otcOnlineChatMessageService;
	}

}
