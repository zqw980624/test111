package com.yami.trading.admin.controller.chat;

import com.yami.trading.bean.chat.domain.MessageUser;
import com.yami.trading.bean.chat.domain.OnlineChatMessage;
import com.yami.trading.bean.model.Customer;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.chat.online.OnlineChatMessageService;
import com.yami.trading.service.chat.online.OnlineChatVisitorMessageService;
import com.yami.trading.service.customer.CustomerService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.*;

@RestController
@Slf4j
@Api(tags = "客服接口")
public class NewAdminOnlineChatController {

    private final String action = "public/newAdminOnlineChatAction!";
    @Autowired
    OnlineChatMessageService onlineChatMessageService;
    @Autowired
    OnlineChatVisitorMessageService onlineChatVisitorMessageService;
    @Autowired
    UserService partyService;
    @Autowired
    CustomerService customerService;
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    /**
     * 在线聊天-人员列表
     */
    @ApiOperation("在线聊天-人员列表")
    @RequestMapping(value = action + "userlist.action")
    public ResultObject userlist(HttpServletRequest request) {
        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }
        try {

            int pageno = 1;
            if (null != request.getParameter("page_no")) {
                pageno = Integer.valueOf(request.getParameter("page_no"));
            }

            String token = request.getParameter("token");

            int pageSize = 50;
            List<MessageUser> list = onlineChatMessageService
                    .cacheGetMessageUserPage(pageno, pageSize, SecurityUtils.getSysUser().getUsername());
            List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < list.size(); i++) {

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", list.get(i).getUuid());
                User party = this.partyService.getById(list.get(i).getPartyId());
                if (party != null) {
                    map.put("username", party.getUserName());
                    map.put("uid", party.getUserCode());
                    map.put("partyid", party.getUserId().toString());
                    map.put("last_login_time", DateUtils.format(party.getUserLasttime(), DateUtils.DF_yyyyMMddHHmmss));
                    map.put("role_name", party.getRoleName());
                } else {
                    map.put("username", list.get(i).getIp());
                    map.put("partyid", list.get(i).getIp());
                    map.put("last_login_time", DateUtils.format(list.get(i).getUpdatetime(), DateUtils.DF_yyyyMMddHHmmss) );
                }

                map.put("remarks", list.get(i).getRemarks());
                map.put("unreadmsg", list.get(i).getCustomerUnreadmsg());
                List<OnlineChatMessage> chats = onlineChatMessageService.cacheGetList(null, 1,
                        StringUtils.isNullOrEmpty(list.get(i).getPartyId()) ? list.get(i).getIp() : list.get(i).getPartyId());
                String content = "";
                Date chatDate = null;
                if (chats.size() > 0) {
                    chatDate = chats.get(0).getCreateTime();
                    if ("img".equals(chats.get(0).getContentType())) {
                        content = "[picture]";
                    } else {
                        content = chats.get(0).getContent();
                    }
                }
                map.put("content", content);
                map.put("updatetime",
                        DateUtils.format(chatDate != null ? chatDate : list.get(i).getUpdatetime(), "MM-dd HH:mm"));

                map.put("order_updatetime", chatDate != null && chatDate.after(list.get(i).getUpdatetime()) ? chatDate : list.get(i).getUpdatetime());// 用作排序
                data.add(map);
            }
            Collections.sort(data, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> paramT1, Map<String, Object> paramT2) {
                    Date date1 = (Date) paramT1.get("order_updatetime");
                    Date date2 = (Date) paramT2.get("order_updatetime");
                    return -date1.compareTo(date2);
                }
            });
            int start = 0;
            int end = start + pageSize;

            if (data.size() <= end) {
                end = data.size();
            }

            List<Map<String, Object>> resultData = new ArrayList<Map<String, Object>>();
            resultData.addAll(data);
            List<Map<String, Object>> subList = resultData.subList(start, end);
            resultObject.setData(subList);
        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }
        return resultObject;
    }

    /**
     * 聊天记录列表
     */
    @ApiOperation("聊天记录列表")
    @RequestMapping(value = action + "list.action")
    public ResultObject list(HttpServletRequest request) {
        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }
        try {
            String message_id = request.getParameter("message_id");
            String partyid = request.getParameter("partyid");

            List<OnlineChatMessage> list = onlineChatMessageService.cacheGetList(message_id, 30, partyid);
            // 首页的时候才更新未读数
            if (StringUtils.isNullOrEmpty(message_id) && !StringUtils.isNullOrEmpty(partyid)) {
                MessageUser cacheMessageUser = onlineChatMessageService.cacheMessageUser(partyid);
                // ip，表示游客
                if (partyid.indexOf(".") != -1 || partyid.indexOf(":") != -1) {
                    if (cacheMessageUser != null && cacheMessageUser.getCustomerUnreadmsg() > 0) {
                        onlineChatVisitorMessageService.updateUnread(partyid, "customer", "read");
                    }
                } else {
                    if (cacheMessageUser != null && cacheMessageUser.getCustomerUnreadmsg() > 0) {
                        onlineChatMessageService.updateUnread(partyid, "customer", "read");
                    }
                }
            }
            List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", list.get(i).getUuid().toString());
                map.put("send_receive", list.get(i).getSendReceive());
                String type = list.get(i).getContentType();
                map.put("type", type);
                String content = list.get(i).getContent();
                if(content.equals("undefined")){
                continue;
                }
               /* if(!content.startsWith("http")){
                    //map.put("url", awsS3OSSFileService.getUrl(content));
                    map.put("url",content;
                }else{*/
                    map.put("url", content);
              //  }

                map.put("content", content);
                map.put("createtime", DateUtils.format(list.get(i).getCreateTime(), "MM-dd HH:mm"));
                map.put("delete_status", list.get(i).getDeleteStatus());
                data.add(map);
            }
            resultObject.setData(data);
        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }
        return resultObject;
    }

    /**
     * 发送消息
     */
    @ApiOperation("发送消息")
    @RequestMapping(value = action + "send.action")
    public ResultObject send(@RequestParam String partyid, @RequestParam String type, @RequestParam String content,@RequestParam String send_time_stmp) {
        ResultObject resultObject = new ResultObject();

        if (StringUtils.isNullOrEmpty(partyid)) {
            throw new BusinessException("暂无用户");
        }

        if (StringUtils.isNullOrEmpty(content.trim()) || StringUtils.isNullOrEmpty(type)) {
            throw new BusinessException("请输入内容");
        }

        Customer customer = customerService.cacheByUsername(SecurityUtils.getSysUser().getUsername());
        if (customer != null && customer.getOnlineState() != 1) {
            throw new BusinessException("您已下线无法发送消息");
        }
        try {
            // 文字内容乱码处理
            if (-1 >= content.indexOf("%")) content = URLDecoder.decode(content, "utf-8");

            String loginUsername = SecurityUtils.getSysUser().getUsername();
            MessageUser messageUser = onlineChatMessageService.cacheMessageUser(partyid);
            if (!loginUsername.equals(messageUser.getTargetUsername())) {
                throw new BusinessException("该用户已移交");
            }
            OnlineChatMessage onlineChatMessage = null;
            // ip，表示游客
            if (partyid.indexOf(".") != -1 || partyid.indexOf(":") != -1) {
                onlineChatMessage = onlineChatVisitorMessageService.saveSend(partyid, type, "receive", content,
                        loginUsername,false);
            } else {
                onlineChatMessage = onlineChatMessageService.saveSend(partyid, type, "receive", content, loginUsername,false);
            }
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("send_time_stmp", send_time_stmp);
            data.put("chat_id", onlineChatMessage != null ? onlineChatMessage.getUuid() : null);
            data.put("updatetime",
                    onlineChatMessage != null ? DateUtils.format(onlineChatMessage.getCreateTime(), "MM-dd HH:mm")
                            : null);
            resultObject.setData(data);
        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }

        return resultObject;
    }

    /**
     * 创建新用户消息列表
     */
    @ApiOperation("创建新用户消息列表,发起和partyId的聊天")
    @RequestMapping(value = action + "create.action")
    public ResultObject create(@RequestParam String partyId) {
        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }
        try {
            Customer customer = customerService.cacheByUsername(SecurityUtils.getSysUser().getUsername());
            if (customer != null && customer.getOnlineState() != 1) {
                throw new BusinessException("您已下线无法接收用户");
            }
            String loginUsername = SecurityUtils.getSysUser().getUsername();
            if (partyId.indexOf(".") != -1 || partyId.indexOf(":") != -1) {// ip，表示游客
                MessageUser messageUser = onlineChatMessageService.cacheMessageUser(partyId);
                if (messageUser == null) {// 该ip没有发起聊天
                    throw new BusinessException("用户不存在");
                }
                MessageUser user = onlineChatVisitorMessageService.saveCreate(partyId, loginUsername);
                resultObject.setData(user.getIp());
            } else {
                MessageUser user = onlineChatMessageService.saveCreate(partyId, loginUsername);
                resultObject.setData(user.getPartyId());
            }
        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }
        return resultObject;
    }

    /**
     * 删除聊天
     */
    @ApiOperation("删除聊天会话")
    @RequestMapping(value = action + "del.action")
    public ResultObject del(@RequestParam String partyId) {


        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }

        try {
            if (partyId.indexOf(".") != -1 || partyId.indexOf(":") != -1) {// ip，表示游客
                onlineChatMessageService.deleteByIp(partyId);
            } else {
                onlineChatMessageService.delete(partyId);
            }

        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }
        return resultObject;
    }

    /**
     * 查询未读消息
     */
    @ApiOperation("查询未读消息")
    @RequestMapping(value = action + "unread.action")
    public ResultObject unread(HttpServletRequest request) {
        String token = request.getParameter("token");
        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }
        try {
            // 只有admin才有客服
            int unreadMsg = onlineChatMessageService.unreadMsg(null, "customer", SecurityUtils.getSysUser().getUsername());
            resultObject.setData(unreadMsg);

        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }
        return resultObject;
    }

    /**
     * 设置用户备注
     */
    @ApiOperation("设置用户备注")
    @RequestMapping(value = action + "resetRemarks.action")
    public ResultObject resetRemarks(HttpServletRequest request) {

        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }
        try {
            String partyid = request.getParameter("partyid");
            String remarks = request.getParameter("remarks");

            if (partyid.indexOf(".") != -1 || partyid.indexOf(":") != -1) {// ip，表示游客
                resultObject.setData(onlineChatVisitorMessageService.updateResetRemarks(partyid, remarks));
            } else {
                resultObject.setData(onlineChatMessageService.updateResetRemarks(partyid, remarks));
            }
        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }
        return resultObject;
    }

    @ApiOperation("获取用户信息")
    @RequestMapping(value = action + "getUserInfo.action")
    public ResultObject getUserInfo(@RequestParam String partyId) {

        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }

        try {
            // ip，表示游客
            if (partyId.indexOf(".") != -1 || partyId.indexOf(":") != -1) {
                resultObject.setData(onlineChatVisitorMessageService.getUserInfo(partyId));
            } else {
                resultObject.setData(onlineChatMessageService.getUserInfo(partyId));
            }
        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }

        return resultObject;
    }


    @ApiOperation("后台客服撤回消息操作")
    @RequestMapping(value = action + "deleteOnlineChatMessage.action")
    public ResultObject deleteOnlineChatMessage(@RequestParam String messageId) {


        ResultObject resultObject = new ResultObject();
        resultObject = this.readSecurityContextFromSession(resultObject);
        if (!"0".equals(resultObject.getCode())) {
            return resultObject;
        }
        try {
            onlineChatMessageService.updateMessageDelete(messageId, SecurityUtils.getSysUser().getUsername());

        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Exception e) {
            resultObject.setCode("1");
            resultObject.setMsg("Program error!");
            log.error("error:", e);
        }

        return resultObject;
    }

    public ResultObject readSecurityContextFromSession(ResultObject resultObject) {
        String partyId = SecurityUtils.getCurrentSysUserId();
        if (StringUtils.isNullOrEmpty(partyId)) {
            resultObject.setCode("403");
            resultObject.setMsg("请重新登录");
            return resultObject;
        }
        return resultObject;
    }
}
