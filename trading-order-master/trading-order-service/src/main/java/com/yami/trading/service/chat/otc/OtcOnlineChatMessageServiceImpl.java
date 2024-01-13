package com.yami.trading.service.chat.otc;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.chat.domain.OtcMessageUser;
import com.yami.trading.bean.chat.domain.OtcOnlineChatMessage;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserRecom;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtcOnlineChatMessageServiceImpl implements OtcOnlineChatMessageService {

    @Autowired
    private OtcDbOnlinechatMessageService dbOnlinechatMessageService;

    @Autowired
    private UserService partyService;
    @Autowired
    private UserRecomService userRecomService;
    @Autowired
    private TipService tipService;
    @Autowired
    private C2cOrderService c2cOrderService;
    @Autowired
    private OtcMessageUserService otcMessageUserService;

    private Map<String, List<OtcOnlineChatMessage>> cahce_chat = new ConcurrentHashMap<String, List<OtcOnlineChatMessage>>();
    private Map<String, OtcMessageUser> cahce_user = new ConcurrentHashMap<String, OtcMessageUser>();

    public List<OtcOnlineChatMessage> cacheGetList(String messageId, int pageSize, String orderNo) {
        List<OtcOnlineChatMessage> cache = cahce_chat.get(orderNo);
        if (cache == null) {
            return new LinkedList<OtcOnlineChatMessage>();
        }
        List<OtcOnlineChatMessage> result = new ArrayList<OtcOnlineChatMessage>(cache);
        int start = cacheIndex(messageId, result);
//		start = start == 0 ? start : start + 1;// 空消息则表示首页，消息索引的后一条为起始
        int end = start + pageSize;

        if (start >= result.size()) {// 起始数据大于总量，返回空
            return new LinkedList<OtcOnlineChatMessage>();
        }
        if (result.size() <= end)
            end = cache.size();

//		List<OtcOnlineChatMessage> result = new ArrayList<OtcOnlineChatMessage>();
//		result.addAll(cache);
        List<OtcOnlineChatMessage> list = result.subList(start, end);

        return list;
    }

    public List<OtcOnlineChatMessage> cacheGetList(String messageId, int pageSize, String orderNo, String clicentType) {
        List<OtcOnlineChatMessage> cache = cahce_chat.get(orderNo);
        if (cache == null) {
            return new LinkedList<OtcOnlineChatMessage>();
        }
        List<OtcOnlineChatMessage> result = new ArrayList<OtcOnlineChatMessage>(cache);

        if ("user".equals(clicentType)) {
            // 过滤掉已撤回的消息
            org.apache.commons.collections.CollectionUtils.filter(result, new Predicate() {
                @Override
                public boolean evaluate(Object arg0) {
                    OtcOnlineChatMessage msg = (OtcOnlineChatMessage) arg0;
                    return msg.getDeleteStatus() == null || msg.getDeleteStatus() == 0;
                }
            });
        }

        int start = cacheIndex(messageId, result);
//		start = start == 0 ? start : start + 1;// 空消息则表示首页，消息索引的后一条为起始
        int end = start + pageSize;

        if (start >= result.size()) {// 起始数据大于总量，返回空
            return new LinkedList<OtcOnlineChatMessage>();
        }
        if (result.size() <= end)
            end = result.size();

//		List<OtcOnlineChatMessage> result = new ArrayList<OtcOnlineChatMessage>();
//		result.addAll(cache);
        List<OtcOnlineChatMessage> list = result.subList(start, end);

        return list;
    }

    /**
     * 获取消息的索引
     *
     * @param messageId
     * @param list
     * @return
     */
    private int cacheIndex(String messageId, List<OtcOnlineChatMessage> list) {
        if (StringUtils.isEmptyString(messageId))
            return 0;
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            OtcOnlineChatMessage message = list.get(i);
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
    public List<OtcMessageUser> cacheGetMessageUserPage(int pageNo, int pageSize) {
        List<OtcMessageUser> list = new ArrayList<OtcMessageUser>(cahce_user.values());
        List<OtcMessageUser> result = new ArrayList<OtcMessageUser>();
        for (OtcMessageUser user : list) {
            if (user.getDeleteStatus() == -1)
                continue;
            result.add(user);
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public void saveSend(String partyId, String type, String content, String username, String orderNo) {
        OtcOnlineChatMessage onlineChatMessage = new OtcOnlineChatMessage();
        onlineChatMessage.setPartyId(partyId);
        onlineChatMessage.setContentType(type);
        onlineChatMessage.setSendReceive("");
        onlineChatMessage.setContent(content);
        onlineChatMessage.setCreateTime(new Date());
        // 后台管理员账户，记录谁回复的消息
        onlineChatMessage.setUsername(username);
        onlineChatMessage.setOrderNo(orderNo);

        dbOnlinechatMessageService.save(onlineChatMessage);

        List<OtcOnlineChatMessage> list = cahce_chat.get(orderNo);
        if (list == null) {
            list = new LinkedList<OtcOnlineChatMessage>();
        }
        list.add(onlineChatMessage);
        Collections.sort(list);
        Collections.reverse(list);// 添加完后，时间倒叙排序加回
        this.cahce_chat.put(orderNo, list);
        if (!cahce_user.containsKey(orderNo)) {// 不存在则添加用户
            saveCreateByOrderNo(orderNo);
        }
        // 用户发送
        if (null == username) {
            updateUnread(partyId, "write", orderNo);
            tipService.saveTip(onlineChatMessage.getUuid().toString(), TipConstants.OTCORDER_ONLINECHAT);
        }
        // 后台客服
        else {
            updateUnreadAdmin(partyId, "write", orderNo);
        }

    }

    /**
     * 更新未读数
     *
     * @param type read:读，write：写
     */
    public void updateUnread(String partyId, String type, String orderNo) {
        OtcMessageUser messageUser = cahce_user.get(orderNo);
        if (messageUser == null) {
            saveCreateByOrderNo(orderNo);
            messageUser = cahce_user.get(orderNo);
        }
        C2cOrder c2cOrder = c2cOrderService.get(orderNo);
        // 承兑商ID
        String c2cUserId = c2cOrder.getC2cUserPartyId();
        // 下单用户ID
        String orderUserId = c2cOrder.getPartyId();
        // 用户承兑商
        if (partyId.equals(c2cUserId)) {
            if ("read".equals(type)) {
                messageUser.setCustomerUnreadmsg(0);
            } else if ("write".equals(type)) {
                messageUser.setUserUnreadmsg(messageUser.getCustomerUnreadmsg() + 1);
                messageUser.setDeleteStatus(0);
            }
        }
        // 下单用户
        else if (partyId.equals(orderUserId)) {
            if ("read".equals(type)) {
                messageUser.setUserUnreadmsg(0);
            } else if ("write".equals(type)) {
                messageUser.setCustomerUnreadmsg(messageUser.getUserUnreadmsg() + 1);
                messageUser.setDeleteStatus(0);
            }
        }
        updateMessageUser(messageUser);
    }

    /**
     * 更新未读数
     *
     * @param type read:读，write：写
     */
    public void updateUnreadAdmin(String partyId, String type, String orderNo) {
        OtcMessageUser messageUser = cahce_user.get(orderNo);
        if (messageUser == null) {
            saveCreateByOrderNo(orderNo);
            messageUser = cahce_user.get(orderNo);
        }
        C2cOrder c2cOrder = c2cOrderService.get(orderNo);
        // 承兑商ID
        String c2cUserId = c2cOrder.getC2cUserPartyId();
        // 下单用户ID
        String orderUserId = c2cOrder.getPartyId();
        // 后台承兑商 发送
        if (partyId.equals(c2cUserId)) {
            if ("read".equals(type)) {
                int removeTipNum = 0;
                messageUser.setCustomerUnreadmsg(0);
                if (removeTipNum > 0) {
                    removeTips(messageUser.getOrderNo(), removeTipNum);
                }
            } else if ("write".equals(type)) {
                messageUser.setUserUnreadmsg(messageUser.getUserUnreadmsg() + 1);
                messageUser.setDeleteStatus(0);
            }
        }
        // 下单用户发送
        else if (partyId.equals(orderUserId)) {
            if ("read".equals(type)) {
                messageUser.setUserUnreadmsg(0);
            } else if ("write".equals(type)) {
                messageUser.setCustomerUnreadmsg(messageUser.getUserUnreadmsg() + 1);
                messageUser.setDeleteStatus(0);
            }
        }
        updateMessageUser(messageUser);
    }

    /**
     * 移除通知
     *
     * @param orderNo
     * @param removeTipNum
     */
    public void removeTips(String orderNo, int removeTipNum) {
        List<OtcOnlineChatMessage> list = this.cacheGetList(null, removeTipNum, orderNo);
        List<String> ids = new ArrayList<String>();
        for (OtcOnlineChatMessage m : list) {
            ids.add(m.getUuid().toString());
        }
        tipService.deleteTip(ids);
    }

    public void updateMessageUser(OtcMessageUser messageUser) {
        otcMessageUserService.updateById(messageUser);
        cahce_user.put(messageUser.getOrderNo(), messageUser);
    }

    public OtcMessageUser saveCreateByOrderNo(String orderNo) {
        OtcMessageUser messageUser = cahce_user.get(orderNo);
        if (messageUser == null) {
            messageUser = new OtcMessageUser();
            messageUser.setOrderNo(orderNo);
        }
        messageUser.setUpdatetime(new Date());
        otcMessageUserService.saveOrUpdate(messageUser);
        cahce_user.put(orderNo, messageUser);
        return messageUser;
    }

    @Override
    public void delete(String orderNo) {
        OtcMessageUser messageUser = cahce_user.get(orderNo);
        if (messageUser != null) {
            messageUser.setDeleteStatus(-1);
            this.updateMessageUser(messageUser);
        }

    }

    /**
     * 批量获取未读数
     *
     * @param orderNos
     * @param type
     * @return
     */
    public Map<String, Integer> unreadMsgs(List<String> orderNos) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        if (CollectionUtils.isEmpty(orderNos)) {
            return result;
        }
        for (String orderNo : orderNos) {
            result.put(orderNo, unreadMsgAdmin(orderNo));
        }
        return result;
    }

    /**
     * 管理后台获取未读消息
     */
    public int unreadMsgAdmin(String orderNo) {
        int unreadmsg = 0;
        if (!StringUtils.isNullOrEmpty(orderNo)) {
            OtcMessageUser messageUser = cahce_user.get(orderNo);
            if (messageUser != null) {
                unreadmsg = messageUser.getCustomerUnreadmsg();
            }
        } else {
            Iterator<Map.Entry<String, OtcMessageUser>> it = cahce_user.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, OtcMessageUser> entry = it.next();
                unreadmsg += entry.getValue().getCustomerUnreadmsg();
            }
        }
        return unreadmsg;
    }

    public Map<String, Integer> unreadMsgsApi(List<String> orderNos) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (String orderNo : orderNos) {
            result.put(orderNo, unreadMsgApi(orderNo));
        }
        return result;
    }

    public int unreadMsgApi(String orderNo) {
        int unreadmsg = 0;
        if (!StringUtils.isNullOrEmpty(orderNo)) {
            OtcMessageUser messageUser = cahce_user.get(orderNo);
            if (messageUser != null) {
                unreadmsg = messageUser.getUserUnreadmsg();
            }
        } else {
            Iterator<Map.Entry<String, OtcMessageUser>> it = cahce_user.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, OtcMessageUser> entry = it.next();
                unreadmsg += entry.getValue().getUserUnreadmsg();
            }
        }
        return unreadmsg;
    }

    @Override
    public int unreadMsg(String orderNo, String partyId) {
        C2cOrder c2cOrder = c2cOrderService.get(orderNo);
        // 承兑商ID
        String c2cUserId = c2cOrder.getC2cUserPartyId();
        // 下单用户ID
        String orderUserId = c2cOrder.getPartyId();
        int unreadmsg = 0;
        OtcMessageUser messageUser = cahce_user.get(orderNo);
        if (messageUser != null) {
            // 承兑商
            if (partyId.equals(c2cUserId)) {
                unreadmsg = messageUser.getCustomerUnreadmsg();
            }
            // 下单用户
            else if (partyId.equals(orderUserId)) {
                unreadmsg = messageUser.getUserUnreadmsg();
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
    public String resetRemarks(String orderNo, String remarks) throws Exception {
        if (StringUtils.isEmptyString(remarks) || StringUtils.isEmptyString(remarks.trim())) {
            return null;
        }
        OtcMessageUser messageUser = this.cacheMessageUser(orderNo);
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
    public Map<String, Object> saveGetUserInfo(String orderNo, String partyId) {
        OtcMessageUser messageUser = this.cacheMessageUser(orderNo);
        User party = partyService.getById(partyId);
        if (party == null) {
            throw new BusinessException("用户不存在");
        }
        if (messageUser == null) {
            messageUser = this.saveCreateByOrderNo(orderNo);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("remarks", messageUser.getRemarks());
        result.put("username", party.getUserName());
        result.put("usercode", party.getUserCode());
        result.put("last_login_time", DateUtils.format(party.getUserLasttime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("create_time", DateUtils.format(party.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("role_name", party.getRoleName());
        result.put("login_ip", party.getUserLastip());
        List<UserRecom> parents = userRecomService.getParents(party.getUserId());
        if (!CollectionUtils.isEmpty(parents) && parents.size() >= 2) {
            User parentParty = partyService.getById(parents.get(1).getUserId());
            result.put("recom_parent_name", parentParty == null ? null : parentParty.getUserName());
        } else {
            result.put("recom_parent_name", null);
        }
        return result;
    }

    public OtcOnlineChatMessage getMessageById(String messageId) {
        return dbOnlinechatMessageService.getById(messageId);
    }

    public void updateMessageDelete(String messageId, String targetUserName) {
        OtcOnlineChatMessage otcOnlineChatMessage = getMessageById(messageId);
        if (otcOnlineChatMessage.getDeleteStatus() == -1) {
            throw new BusinessException("该消息已撤回");
        }
        // 游客或者登录用户
        String userKey = StringUtils.isEmptyString(otcOnlineChatMessage.getPartyId()) ? otcOnlineChatMessage.getIp()
                : otcOnlineChatMessage.getPartyId();

        otcOnlineChatMessage.setDeleteStatus(-1);
        dbOnlinechatMessageService.updateById(otcOnlineChatMessage);
        List<OtcOnlineChatMessage> list = cahce_chat.get(otcOnlineChatMessage.getOrderNo());
        int indexOf = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUuid().equals(otcOnlineChatMessage.getUuid())) {
                indexOf = i;
                break;
            }
        }
        list.remove(indexOf);
        list.add(indexOf, otcOnlineChatMessage);
        cahce_chat.put(userKey, list);
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        init();
    }

    public void init() {

        List<OtcMessageUser> list_user = otcMessageUserService.list();

        for (int i = 0; i < list_user.size(); i++) {
            OtcMessageUser item = list_user.get(i);
            this.cahce_user.put(item.getOrderNo(), item);
        }
        QueryWrapper<OtcOnlineChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<OtcOnlineChatMessage> list_chat = dbOnlinechatMessageService.list(queryWrapper);

        for (int i = 0; i < list_chat.size(); i++) {

            OtcOnlineChatMessage item = list_chat.get(i);
            List<OtcOnlineChatMessage> list = null;
            list = cahce_chat.get(item.getOrderNo());
            if (list == null) {
                list = new LinkedList<OtcOnlineChatMessage>();
            }
            list.add(item);
            this.cahce_chat.put(item.getOrderNo(), list);
        }
    }

    public Map<String, List<OtcOnlineChatMessage>> cacheMessageAll() {
        return cahce_chat;
    }

    public Map<String, OtcMessageUser> cacheMessageUserAll() {
        return cahce_user;
    }

    public OtcMessageUser cacheMessageUser(String key) {
        return cahce_user.get(key);
    }

    public List<OtcOnlineChatMessage> cacheMessage(String key) {
        return cahce_chat.get(key);
    }

    public void putMessage(String key, List<OtcOnlineChatMessage> value) {
        cahce_chat.put(key, value);
    }

    public void putMessageUser(String key, OtcMessageUser value) {
        cahce_user.put(key, value);
    }

    public void updateMessageUserByIp(OtcMessageUser messageUser) {
        otcMessageUserService.updateById(messageUser);
        cahce_user.put(messageUser.getIp(), messageUser);
    }

    public void deleteByIp(String ip) {
        OtcMessageUser messageUser = cahce_user.get(ip);
        if (messageUser != null) {
            messageUser.setDeleteStatus(-1);
            this.updateMessageUserByIp(messageUser);
        }
    }


    public void setUserRecomService(UserRecomService userRecomService) {
        this.userRecomService = userRecomService;
    }

    public void setTipService(TipService tipService) {
        this.tipService = tipService;
    }

    public C2cOrderService getC2cOrderService() {
        return c2cOrderService;
    }

    public void setC2cOrderService(C2cOrderService c2cOrderService) {
        this.c2cOrderService = c2cOrderService;
    }

}
