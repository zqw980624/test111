package com.yami.trading.admin.controller.chat;

import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.chat.domain.OtcMessageUser;
import com.yami.trading.bean.chat.domain.OtcOnlineChatMessage;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.chat.otc.OtcOnlineChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@RestController
public class OtcOnlineChatController {
    private final String action = "normal/adminOtcOnlineChatAction!";

    @Autowired
    private OtcOnlineChatMessageService otcOnlineChatMessageService;
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;
    @Autowired
    private C2cOrderService c2cOrderService;

    @RequestMapping(action + "list.action")
    public Result<List<Map<String, Object>>> list(HttpServletRequest request) {

        String messageId = request.getParameter("message_id");
        String orderNo = request.getParameter("order_no");
        String partyId = request.getParameter("partyid");
        // 首页的时候才更新未读数
        if (StringUtils.isNullOrEmpty(messageId) && !StringUtils.isNullOrEmpty(partyId)) {
            OtcMessageUser cacheOtcMessageUser = otcOnlineChatMessageService.cacheMessageUser(orderNo);
            if (cacheOtcMessageUser != null && cacheOtcMessageUser.getCustomerUnreadmsg() > 0) {
                otcOnlineChatMessageService.updateUnreadAdmin(partyId, "read", orderNo);
            }
        }
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        List<OtcOnlineChatMessage> list = otcOnlineChatMessageService.cacheGetList(messageId, 30, orderNo);
        C2cOrder c2cOrder = c2cOrderService.get(orderNo);
        // 承兑商ID
        String c2cUserId = c2cOrder.getC2cUserPartyId();
        for (OtcOnlineChatMessage message : list) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", message.getUuid());
            if (!StringUtils.isNullOrEmpty(c2cUserId) && c2cUserId.equals(message.getPartyId())) {
                map.put("send_receive", "receive");
            } else {
                // 当前登陆用户，记录访问发给谁的。如果当前是这个人登陆，消息全是他的
                if(SecurityUtils.getSysUser().getUsername().equals(message.getUsername())){
                    map.put("send_receive", "receive");
                }else{
                    map.put("send_receive", "send");
                }
            }
            String type = message.getContentType();
            map.put("type", type);
            String content = message.getContent();
            if ("img".equals(type)) {
                content = awsS3OSSFileService.getUrl(message.getContent());
            }
            map.put("content", content);
            if(!content.startsWith("http")){
                map.put("url", awsS3OSSFileService.getUrl(content));
            }else{
                map.put("url", content);
            }
            map.put("createtime", DateUtils.format(message.getCreateTime(), "MM-dd HH:mm"));
            map.put("delete_status", message.getDeleteStatus());
            data.add(map);
        }
        return Result.succeed(data);
    }

    @RequestMapping(value = action + "send.action", produces = "text/html;charset=UTF-8")
    public Result<String> send(HttpServletRequest request) throws UnsupportedEncodingException {
        // 后台配置的承兑商用户ID
        String partyId = request.getParameter("partyid");
        String type = request.getParameter("type");
        String content = request.getParameter("content");
        String orderNo = request.getParameter("order_no");
        if (StringUtils.isNullOrEmpty(partyId)) {
            throw new YamiShopBindException("暂无用户");
        }
        if (StringUtils.isNullOrEmpty(content.trim()) || StringUtils.isNullOrEmpty(type)) {
            throw new YamiShopBindException("请输入内容");
        }
        if (StringUtils.isEmptyString(orderNo)) {
            throw new YamiShopBindException("订单号不能未空");
        }
        // 文字内容乱码处理
        content = URLDecoder.decode(content.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8");
        otcOnlineChatMessageService.saveSend(partyId, type, content, SecurityUtils.getSysUser().getUsername(), orderNo);
        return Result.succeed("ok");
    }

    @RequestMapping(value = action + "create.action", produces = "text/html;charset=UTF-8")
    public Result<String> create(HttpServletRequest request) {
        String orderNo = request.getParameter("order_no");
        otcOnlineChatMessageService.saveCreateByOrderNo(orderNo);
        return Result.succeed("ok");
    }

    @RequestMapping(value = action + "del.action")
    public Result<String> del(HttpServletRequest request) {

        String partyId = request.getParameter("partyid");
        // ip，表示游客
        if (partyId.indexOf(".") != -1) {
            otcOnlineChatMessageService.deleteByIp(partyId);
        } else {
            otcOnlineChatMessageService.delete(partyId);
        }
        return Result.succeed("ok");
    }

    @RequestMapping(value = action + "unread.action", produces = "text/html;charset=UTF-8")
    public Result<Integer> unread() {

        int unreadMsg = otcOnlineChatMessageService.unreadMsgAdmin(null);
        return Result.succeed(unreadMsg);
    }

    @RequestMapping(value = action + "unreads.action", produces = "text/html;charset=UTF-8")
    public Result<List<Map<String, Object>>> unreads(HttpServletRequest request) {

        String orderNos = request.getParameter("order_nos");
        if (StringUtils.isNullOrEmpty(orderNos)) {
            return Result.succeed(new ArrayList<>());
        } else {
            List<String> nos = new ArrayList<String>(Arrays.asList(orderNos.split(",")));
            List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
            Map<String, Integer> unreadMsgs = otcOnlineChatMessageService.unreadMsgs(nos);
            for (Map.Entry<String, Integer> entry : unreadMsgs.entrySet()) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("order_no", entry.getKey());
                data.put("unread_msg", entry.getValue());
                datas.add(data);
            }
            return Result.succeed(datas);
        }
    }

    @RequestMapping(value = action + "resetRemarks.action")
    public Result<String> resetRemarks(HttpServletRequest request) throws Exception {

        String remarks = request.getParameter("remarks");
        String orderNo = request.getParameter("order_no");
        return Result.succeed(otcOnlineChatMessageService.resetRemarks(orderNo, remarks));
    }

    @RequestMapping(value = action + "saveGetUserInfo.action", produces = "text/html;charset=UTF-8")
    public Result<Map<String, Object>> saveGetUserInfo(HttpServletRequest request) {

        String partyId = request.getParameter("partyid");
        String orderNo = request.getParameter("order_no");
        return Result.succeed(otcOnlineChatMessageService.saveGetUserInfo(orderNo, partyId));
    }

    @RequestMapping(value = action + "deleteOnlineChatMessage.action")
    public Result<String> deleteOnlineChatMessage(HttpServletRequest request) {
        String messageId = request.getParameter("message_id");
        otcOnlineChatMessageService.updateMessageDelete(messageId, SecurityUtils.getSysUser().getUsername());
        return Result.succeed("ok");
    }

}
