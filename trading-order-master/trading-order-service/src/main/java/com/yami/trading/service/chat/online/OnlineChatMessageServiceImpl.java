package com.yami.trading.service.chat.online;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.bean.Tip;
import com.yami.trading.bean.chat.domain.MessageUser;
import com.yami.trading.bean.chat.domain.OnlineChatMessage;
import com.yami.trading.bean.model.Customer;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserRecom;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.chat.MessageUserService;
import com.yami.trading.service.customer.CustomerService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;


@Service
public class OnlineChatMessageServiceImpl  implements OnlineChatMessageService {




    protected static final Object onlineChatMessage = null;
    private Logger log = LoggerFactory.getLogger(OnlineChatMessageServiceImpl.class);
    private Map<String, List<com.yami.trading.bean.chat.domain.OnlineChatMessage>> cahce_chat = new ConcurrentHashMap<String, List<OnlineChatMessage>>();

    private Map<String, MessageUser> cahceUser = new ConcurrentHashMap<String, MessageUser>();
    @Autowired
    private UserService partyService;
    @Autowired
    private UserRecomService userRecomService;
    @Autowired
    private TipService tipService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private DBOnlineChatMessageServiceImpl dbOnlineChatMessageService;
    @Autowired
    private MessageUserService messageUserService;


    /**
     *
     * @param messageId
     * @param pageSize
     * @param partyId
     * @param clicentType 请求的客户端类型，用户端user，客服端 不传
     * @return
     */
    public List<OnlineChatMessage> cacheGetList(String messageId, int pageSize, String partyId, String... clicentType) {
        List<OnlineChatMessage> cache = cahce_chat.get(partyId);
        if (cache == null) {
            return new LinkedList<OnlineChatMessage>();
        }
        List<OnlineChatMessage> result = new ArrayList<OnlineChatMessage>();
        result.addAll(cache);
        if (clicentType.length != 0 && "user".equals(clicentType[0])) {
            // 过滤掉已撤回的消息
            org.apache.commons.collections.CollectionUtils.filter(result, new Predicate() {
                @Override
                public boolean evaluate(Object arg0) {
                    // TODO Auto-generated method stub
                    OnlineChatMessage msg = (OnlineChatMessage) arg0;
                    return msg.getDeleteStatus() == 0;
                }
            });
        }
        int start = cacheIndex(messageId, result);
//		start = start == 0 ? start : start + 1;// 空消息则表示首页，消息索引的后一条为起始
        int end = start + pageSize;
        if (start >= result.size()) {// 起始数据大于总量，返回空
            return new LinkedList<OnlineChatMessage>();
        }
        if (cache.size() <= end)
            end = result.size();
        List<OnlineChatMessage> list = result.subList(start, end);
        return list;
    }

    /**
     * 获取消息的索引
     *
     * @param messageId
     * @param list
     * @return
     */
    private int cacheIndex(String messageId, List<OnlineChatMessage> list) {
        if (StringUtils.isEmptyString(messageId))
            return 0;
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            OnlineChatMessage message = list.get(i);
            if (messageId.equals(message.getUuid().toString())) {
                index = i;
            }
        }
        if (index == -1) {
            throw new BusinessException("参数异常，消息获取失败");
        }
        return index + 1;
    }

    @Override
    public List<MessageUser> cacheGetMessageUserPage(int pageNo, int pageSize, String username) {
        List<MessageUser> list = new ArrayList<MessageUser>(cahceUser.values());

        List<MessageUser> result = new ArrayList<MessageUser>();
        for (MessageUser user : list) {
            if (user.getDeleteStatus() == -1) {
                // System.out.println("DeleteStatus:" + user.getIp());
                continue;
            }
            // 没有指定客服，客服不匹配
            if (StringUtils.isEmptyString(user.getTargetUsername()) || !username.equals(user.getTargetUsername())) {
                // System.out.println("TargetUsername:" + user.getIp());
                continue;
            }
            result.add(user);
        }
        Collections.sort(result);
        return result;
    }



    @Override
    public OnlineChatMessage saveSend(String partyId, String type, String send_receive, String content,
                                      String username,boolean isAutoAnswer) {
        OnlineChatMessage onlineChatMessage = new OnlineChatMessage();
        onlineChatMessage.setPartyId(partyId);
        onlineChatMessage.setContentType(type);
        onlineChatMessage.setSendReceive(send_receive);
        onlineChatMessage.setContent(content);
        onlineChatMessage.setCreateTime(new Date());
        onlineChatMessage.setUsername(username);

        dbOnlineChatMessageService.save(onlineChatMessage);

        List<OnlineChatMessage> list = cahce_chat.get(partyId);
        if (list == null) {
            list = new LinkedList<OnlineChatMessage>();
        }
        list.add(onlineChatMessage);
        Collections.sort(list);
        Collections.reverse(list);// 添加完后，时间倒叙排序加回
        this.cahce_chat.put(partyId, list);
        if (!cahceUser.containsKey(partyId)) {// 不存在则添加用户
            saveCreateByPartyId(partyId);
        }
        switch (send_receive) {
            case "receive":// 客服发送
                if (!isAutoAnswer) {
                    updateUnread(partyId, "user", "write");
                }
                break;
            case "send":// 用户发送
                updateUnread(partyId, "customer", "write");
//			tipService.saveTip(onlineChatMessage.getUuid().toString(), TipConstants.ONLINECHAT);
                break;
        }
        return onlineChatMessage;
    }

    public String userSendTarget(String partyId, Date sendTime, String targetUsername) {
        if (StringUtils.isNotEmpty(targetUsername)) {
            Customer customer = customerService.cacheByUsername(targetUsername);
            // 表示该用户被有客服权限的系统用户接手
            if (customer == null) {
                return targetUsername;
            }
            // 当前在聊的客服是否在线
            if (customer.getOnlineState() == 1) {
                return customer.getUserName();
            }
        }

        // 不在线则重新分配
        Customer customer = this.customerService.cacheOnlineOne();
        if (null == customer) {
            return null;
        }
        while (true) {
            customer.setLastMessageUser(partyId);
            customer.setLastCustomerTime(sendTime);
            boolean update = customerService.update(customer, true);
            if (update) {// 更新成功，退出
                break;
            } else {// 未成功，说明已下线，重新分配新客服
                customer = this.customerService.cacheOnlineOne();
                if (null == customer) {
                    return null;
                }
            }
        }
        return customer.getUserName();
    }

    /**
     * 更新未读数
     *
     * @param partyId
     * @param user_customer 更新对象，用户，客服
     * @param type          read:读，write：写
     */
    public void updateUnread(final String partyId, String user_customer, String type) {
        MessageUser messageUser = cahceUser.get(partyId);
        if (messageUser == null) {
            saveCreateByPartyId(partyId);
            messageUser = cahceUser.get(partyId);
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

                    final String targetUsername = this.userSendTarget(partyId, new Date(),
                            messageUser.getTargetUsername());
                    if (StringUtils.isNotEmpty(targetUsername)
                            && !targetUsername.equals(messageUser.getTargetUsername())) {
                        final Customer customer = customerService.cacheByUsername(targetUsername);
                        // 客服不存在或者回复内容无效则不回复
                        if (customer != null && customer.getAutoAnswer() != null
                                && !StringUtils.isEmptyString(customer.getAutoAnswer().trim())) {
                            // 客服自动回复一条
                           /* saveSend(partyId, "text", "receive", customer.getAutoAnswer(),
                                    targetUsername + "SYSTEM",true);*/
                            saveSend(partyId, "text", "receive", "Dear user, the reception business is currently busy. Please contact the customer service manager on whatsapp to provide you with timely assistance. I wish you smooth investment and a happy life.",
                                    targetUsername + "SYSTEM",true);
//						Thread t = new Thread(new Runnable() {
//							@Override
//							public void run() {
//								// TODO Auto-generated method stub
//								// 异步，延迟200毫秒发送
//								ThreadUtils.sleep(200);
//								// 客服自动回复一条
//								saveSend(partyId, "text", "receive", customer.getAutoAnswer(),
//										targetUsername + "SYSTEM");
//							}
//						});
//						t.start();
                        }
                    }
                    messageUser.setTargetUsername(targetUsername);
                    if (StringUtils.isNotEmpty(targetUsername)) {// 指定的在线客服存在，则发起通知
                        Tip tip = new Tip();
                        tip.setBusinessId(this.cahce_chat.get(partyId).get(0).getUuid().toString());
                        tip.setModel(TipConstants.ONLINECHAT);
                        tip.setTargetUsername(targetUsername);
                        tipService.saveTip(tip);
                    }
                }
                break;
        }
        updateMessageUser(messageUser);
        if (removeTipNum > 0) {
            removeTips(messageUser.getPartyId(), removeTipNum);
        }
    }

    /**
     * 移除通知
     *
     * @param partyId
     * @param removeTipNum
     */
    public void removeTips(String partyId, int removeTipNum) {
        List<OnlineChatMessage> list = this.cacheGetList(null, removeTipNum, partyId);
        List<String> ids = new ArrayList<String>();
        for (OnlineChatMessage m : list) {
            ids.add(m.getUuid().toString());
        }
        tipService.deleteTip(ids);
    }

    public void updateMessageUser(MessageUser messageUser) {
       messageUserService.saveOrUpdate(messageUser);
        cahceUser.put(messageUser.getPartyId(), messageUser);
    }



    public void saveCreateByPartyId(String partyId) {
        User party = partyService.getById(partyId);
        if (party == null) {
            throw new BusinessException("无效的UID");
        }
        MessageUser messageUser = cahceUser.get(party.getUserId().toString());
        if (messageUser == null) {
            messageUser = new MessageUser();
            messageUser.setUserUnreadmsg(0);
            messageUser.setCustomerUnreadmsg(1);
            messageUser.setPartyId(party.getUserId().toString());
        }
        messageUser.setUpdatetime(new Date());

        messageUserService.saveOrUpdate(messageUser);
        cahceUser.put(party.getUserId().toString(), messageUser);

    }

    @Override
    public MessageUser saveCreate(String uid, String username) {
        User party = partyService.findUserByUserCode(uid);
        if (party == null) {
            party = partyService.findByUserName(uid);
            if (party == null) {
                throw new BusinessException("用户不存在");
            }
        }
        MessageUser messageUser = cahceUser.get(party.getUserId().toString());
        if (messageUser == null) {
            messageUser = new MessageUser();
            messageUser.setUserUnreadmsg(1);
            messageUser.setPartyId(party.getUserId().toString());
        }
        messageUser.setUpdatetime(new Date());
        messageUser.setDeleteStatus(0);
        messageUser.setTargetUsername(username);
        messageUserService.saveOrUpdate(messageUser);
        cahceUser.put(party.getUserId().toString(), messageUser);
        return messageUser;
    }

    @Override
    public void delete(String partyId) {
        MessageUser messageUser = cahceUser.get(partyId);
        if (messageUser != null) {
            messageUser.setDeleteStatus(-1);
            messageUser.setTargetUsername(null);
            this.updateMessageUser(messageUser);
        }

    }

    @Override
    public int unreadMsg(String partyId, String type, String targetUsername) {
        int unreadmsg = 0;
        if (!StringUtils.isNullOrEmpty(partyId)) {
            MessageUser messageUser = cahceUser.get(partyId);
            if (messageUser != null) {
                switch (type) {
                    case "user":
                        unreadmsg = messageUser.getUserUnreadmsg();
                        break;
                    case "customer":
                        unreadmsg = messageUser.getCustomerUnreadmsg();
                        break;
                }
            }
        } else {
            Iterator<Entry<String, MessageUser>> it = cahceUser.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, MessageUser> entry = it.next();
                if (StringUtils.isEmptyString(targetUsername)
                        || !targetUsername.equals(entry.getValue().getTargetUsername())) {
                    continue;
                }
                switch (type) {
                    case "user":
                        unreadmsg += entry.getValue().getUserUnreadmsg();
                        break;
                    case "customer":
                        unreadmsg += entry.getValue().getCustomerUnreadmsg();
                        break;
                }
//				unreadmsg = unreadmsg + entry.getValue().getUnreadmsg();
            }
        }

        return unreadmsg;
    }

    /**
     * 设置备注
     *
     * @param partyId
     * @param remarks
     */
    public String updateResetRemarks(String partyId, String remarks) throws Exception {
        if (StringUtils.isEmptyString(remarks) || StringUtils.isEmptyString(remarks.trim())) {
            return null;
        }
        MessageUser messageUser = this.cacheMessageUser(partyId);
        if (messageUser == null) {
            throw new BusinessException("用户不存在");
        }
        messageUser.setRemarks(URLDecoder.decode(remarks, "utf-8"));
        this.updateMessageUser(messageUser);
        return remarks;
    }

    /**
     * 获取用户信息
     *
     * @param partyId
     * @return
     */
    public Map<String, Object> getUserInfo(String partyId) {
        User party = partyService.getById(partyId);
        if (party == null) {
            throw new BusinessException("用户不存在");
        }
        MessageUser messageUser = this.cacheMessageUser(partyId);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("partyId", partyId);
        result.put("remarks", messageUser.getRemarks());
        result.put("username", party.getUserName());
        result.put("usercode", party.getUserCode());
        result.put("last_login_time", DateUtils.format(party.getUserLasttime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("create_time", DateUtils.format(party.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("role_name", party.getRoleName());
        result.put("loginIp", party.getUserLastip());
//		result.put("online", userService.isOnline(partyId));
        List<UserRecom> parents = userRecomService.getParents(party.getUserId());
        if (!CollectionUtils.isEmpty(parents) && parents.size() >= 1) {
            User parentParty = partyService.getById(parents.get(0).getRecomUserId());
            result.put("recom_parent_name", parentParty == null ? null : parentParty.getUserName());
        } else {
            result.put("recom_parent_name", null);
        }
        return result;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        init();
    }
    public void init() {

        List<MessageUser> listUser = messageUserService.list();

        for (int i = 0; i < listUser.size(); i++) {
            MessageUser item = listUser.get(i);
            if (StringUtils.isEmptyString(item.getPartyId())) {
                this.cahceUser.put(item.getIp(), item);
            } else {
                this.cahceUser.put(item.getPartyId(), item);
            }
        }


        QueryWrapper<OnlineChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<OnlineChatMessage> list_chat =  dbOnlineChatMessageService.list(queryWrapper);

        for (int i = 0; i < list_chat.size(); i++) {

            OnlineChatMessage item = list_chat.get(i);
            List<OnlineChatMessage> list = null;
            if (StringUtils.isEmptyString(item.getPartyId())) {
                list = cahce_chat.get(item.getIp());
            } else {
                list = cahce_chat.get(item.getPartyId());
            }
            if (list == null) {
                list = new LinkedList<OnlineChatMessage>();
            }
            list.add(item);
            if (StringUtils.isEmptyString(item.getPartyId())) {
                this.cahce_chat.put(item.getIp(), list);
            } else {
                this.cahce_chat.put(item.getPartyId(), list);
            }
//			this.cahce_chat.put(item.getPartyId(), list);
        }
    }

    public Map<String, List<OnlineChatMessage>> cacheMessageAll() {
        return cahce_chat;
    }

    public Map<String, MessageUser> cacheMessageUserAll() {
        return cahceUser;
    }

    public MessageUser cacheMessageUser(String key) {
        return cahceUser.get(key);
    }

    public List<OnlineChatMessage> cacheMessage(String key) {
        return cahce_chat.get(key);
    }

    public void putMessage(String key, List<OnlineChatMessage> value) {
        cahce_chat.put(key, value);
    }

    public void putMessageUser(String key, MessageUser value) {
        cahceUser.put(key, value);
    }

    public void updateMessageUserByIp(MessageUser messageUser) {
        messageUserService.saveOrUpdate(messageUser);
        cahceUser.put(messageUser.getIp(), messageUser);
    }

    public void deleteByIp(String ip) {
        MessageUser messageUser = cahceUser.get(ip);
        if (messageUser != null) {
            messageUser.setDeleteStatus(-1);
            messageUser.setTargetUsername(null);
            this.updateMessageUserByIp(messageUser);
        }
    }

    @Override
    public void removeTips(String partyId, long removeTipNum) {

    }

    /**
     * 未分配到客服的用户，分配客服
     *
     * @return
     */
    public void updateNoAnwserUser(String username) {
        List<MessageUser> users = new ArrayList<MessageUser>(this.cacheMessageUserAll().values());
        org.apache.commons.collections.CollectionUtils.filter(users, new Predicate() {

            @Override
            public boolean evaluate(Object arg0) {
                // TODO Auto-generated method stub
                return ((MessageUser) arg0).getCustomerUnreadmsg() > 0
                        && StringUtils.isEmptyString(((MessageUser) arg0).getTargetUsername());
            }
        });
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        for (MessageUser user : users) {
            user.setTargetUsername(username);
            if (StringUtils.isEmptyString(user.getPartyId())) {
                this.updateMessageUserByIp(user);
            } else {
                this.updateMessageUser(user);
            }
        }
    }

    public OnlineChatMessage getMessageById(String messageId) {
        return dbOnlineChatMessageService.getById(messageId);
    }

    public void updateMessageDelete(String messageId, String targetUserName) {
        OnlineChatMessage onlineChatMessage = getMessageById(messageId);
        if (onlineChatMessage.getDeleteStatus() == -1) {
            throw new BusinessException("该消息已撤回");
        }
        //游客或者登录用户
        String userKey = StringUtils.isEmptyString(onlineChatMessage.getPartyId())?onlineChatMessage.getIp():onlineChatMessage.getPartyId();
        MessageUser messageUser = cahceUser.get(userKey);
        if (StringUtils.isEmptyString(messageUser.getTargetUsername())
                || !targetUserName.equals(messageUser.getTargetUsername())) {
            throw new BusinessException("并非当前客服接手的用户，无法撤回");
        }
        if (!"receive".equals(onlineChatMessage.getSendReceive())) {
            throw new BusinessException("只能撤回客服发送消息");
        }

        List<OnlineChatMessage> list = cahce_chat.get(userKey);
        int indexOf = -1;
        for(int i=0;i<list.size();i++){
            if(list.get(i).getUuid().equals(onlineChatMessage.getUuid())){
                indexOf = i;
                break;
            }
        }
        if (indexOf == -1) {
            throw new BusinessException("撤回失败");
        }
        onlineChatMessage.setDeleteStatus(-1);
        dbOnlineChatMessageService.saveOrUpdate(onlineChatMessage);
        list.remove(indexOf);
        list.add(indexOf, onlineChatMessage);
        cahce_chat.put(userKey, list);
    }



}
