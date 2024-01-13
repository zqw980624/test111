package com.yami.trading.api.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.trading.bean.chat.domain.MessageUser;
import com.yami.trading.bean.chat.domain.OnlineChatMessage;
import com.yami.trading.bean.chat.query.ChatRecordReqModel;
import com.yami.trading.bean.chat.query.ChatRecordRespModel;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.chat.online.OnlineChatMessageService;
import com.yami.trading.service.chat.online.OnlineChatVisitorMessageService;

import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Api(tags = "用户客服管理")
@Slf4j
@RestController
public class ApiNewOnlineChatController {
    public final String action = "/api/newOnlinechat";

    @Autowired
    private OnlineChatMessageService onlineChatMessageService;
    @Lazy
    @Autowired
    private OnlineChatVisitorMessageService onlineChatVisitorMessageService;

    @Autowired
    UserService userService;

    @Autowired
    SysparaService sysparaService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @ApiOperation(value = "聊天记录列表")
    @GetMapping(action + "!list.action")
    public Result list(ChatRecordReqModel recordReqModel) {
        String messageId = recordReqModel.getMessage_id();
        String partyId = getPartyIdOrDefaultIp();
        List<OnlineChatMessage> onlinechatMessages = onlineChatMessageService.cacheGetList(messageId, 10, partyId);

        // 首页的时候才更新未读数
        if (StringUtils.isNullOrEmpty(messageId)) {
            MessageUser cacheMessageUser = onlineChatMessageService.cacheMessageUser(partyId);
            // ip，表示游客
            if (cacheMessageUser != null && cacheMessageUser.getUserUnreadmsg() > 0) {
                if (partyId.indexOf(".") != -1 || partyId.indexOf(":") != -1) {
                    onlineChatVisitorMessageService.updateUnread(partyId, "user", "read");
                } else {
                    onlineChatMessageService.updateUnread(partyId, "user", "read");
                }
            }
        }
        List<ChatRecordRespModel> result = new ArrayList<>(onlinechatMessages.size());
        for (OnlineChatMessage onlinechatMessage : onlinechatMessages) {
            ChatRecordRespModel chatRecordRespModel = new ChatRecordRespModel();
            chatRecordRespModel.setId(onlinechatMessage.getUuid());
            chatRecordRespModel.setSendReceive(onlinechatMessage.getSendReceive());
            chatRecordRespModel.setContentType(onlinechatMessage.getContentType());
            String content =null;
            String contentType = onlinechatMessage.getContentType();
            if(onlinechatMessage.getContent().equals("undefined")){
                continue;
            }
            if ("img".equals(contentType) && !onlinechatMessage.getContent().startsWith("http")){
                content=awsS3OSSFileService.getUrl(onlinechatMessage.getContent());
            } else {
                content=onlinechatMessage.getContent();
            }
            chatRecordRespModel.setContent(content);
            chatRecordRespModel.setCreatetime(DateUtils.format(onlinechatMessage.getCreateTime(), "MM-dd HH:mm"));
            chatRecordRespModel.setCreatetimeTs(onlinechatMessage.getCreateTimeTs());
            chatRecordRespModel.setDeleteStatus(onlinechatMessage.getDeleteStatus());
            result.add(chatRecordRespModel);
        }
        return Result.succeed(result);
    }

    private String getPartyIdOrDefaultIp() {
        String  partyId = SecurityUtils.getCurrentUserId();
        if(StrUtil.isEmpty(partyId)) {
            partyId = getIp();
        }
        return partyId;
    }

    @RequestMapping(value = action + "!send.action")
    public Result send(HttpServletRequest request) {
        try {
            String content = request.getParameter("content");
            String type = request.getParameter("type");
            //String type ="img";
            if (StringUtils.isNullOrEmpty(content.trim()) || StringUtils.isNullOrEmpty(type)) {
                return Result.failed("发送消息为空");
            }
            log.info("==========sendsendsendsend=======");
            if(-1>=content.indexOf("%")) content = URLDecoder.decode(content, "utf-8");
            String loginPartyId = SecurityUtils.getCurrentUserId();
            if (StringUtils.isNullOrEmpty(loginPartyId)) {
                if (checkVisitorIp()) {
                    return Result.succeed();
                }
                onlineChatVisitorMessageService.saveSend(this.getIp(), type, "send", content, null,false);
            } else {
                if (checkUserBlack(loginPartyId)) {
                    return Result.succeed();
                }
                onlineChatMessageService.saveSend(loginPartyId, type, "send", content, null,false);
            }
        } catch (Exception e) {
            log.error("error:", e);
            return Result.failed(e.getMessage());
        }
        return Result.succeed();

    }

    @ApiOperation(value = "查询未读消息")
    @GetMapping(action + "!unread.action")
    public ResponseEntity unread() {
        String partyId = getPartyIdOrDefaultIp();
        long unreadMsg = onlineChatMessageService.unreadMsg(partyId, "user", null);
        return ResponseEntity.ok(unreadMsg);
    }

    public String getIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 检验游客ip是否是黑名单，true：是，false：否
     */
    private boolean checkVisitorIp() {
        String blackMenu = sysparaService.find("online_visitor_black_ip_menu").getSvalue();
        List<String> list = new ArrayList<String>(Arrays.asList(blackMenu.split(",")));
        return list.contains(this.getIp());
    }

    private boolean checkUserBlack(String loginPartyId) {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserId, loginPartyId));
        if (user == null) {
            log.warn("用户不存在，userId={}", loginPartyId);
            return false;
        }
        String username = user.getUserName();
        String blackMenu = sysparaService.find("online_username_black_menu").getSvalue();
        List<String> list = new ArrayList<String>(Arrays.asList(blackMenu.split(",")));
        return list.contains(username);
    }


}
