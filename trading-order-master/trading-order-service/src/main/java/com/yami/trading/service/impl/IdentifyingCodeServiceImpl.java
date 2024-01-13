package com.yami.trading.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.yami.trading.bean.log.domain.CodeLog;
import com.yami.trading.bean.model.SmsLog;
import com.yami.trading.common.manager.BlacklistIpTimeWindow;
import com.yami.trading.common.manager.SendCountTimeWindow;
import com.yami.trading.service.*;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.CodeLogService;
import com.yami.trading.service.user.SmsLogService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdentifyingCodeServiceImpl implements IdentifyingCodeService {
    private Logger logger = LoggerFactory.getLogger(IdentifyingCodeServiceImpl.class);

    @Autowired
    private SmsSendService smsSendService;
    @Autowired
    private EmailSendService emailSendService;
    @Autowired
    private IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService;
    @Autowired
    private SendCountTimeWindow sendCountTimeWindow;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private BlacklistIpTimeWindow blacklistIpTimeWindow;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private SmsLogService smsLogService;
    @Autowired
    private CodeLogService codeLogService;
    @Autowired
    private EmailService emailService;
    private Map<String, Integer> ipCache = new ConcurrentHashMap<String, Integer>();

    @Override
    public void send(String target, String ip) {
        String code = null;
        if (chcekIp(ip)) {// 被封的ip直接返回 不操作
            return;
        }
        /**
         * 短信发送签名
         */
        // 短信发送文本[TEST]code is ：{0}
        String send_code_text = this.sysparaService.find("send_code_text").getSvalue();
        if (StringUtils.isNullOrEmpty(send_code_text)) {
            logger.error("send_code_text 未配置");
            return;
        }
        /**
         * 是否每次发送的code都不一样
         */
        boolean send_code_always_new = this.sysparaService.find("send_code_always_new").getBoolean();
        Object object = this.identifyingCodeTimeWindowService.getAuthCode(target);
        if (object == null || send_code_always_new) {
            Random random = new Random();
            code = String.valueOf(random.nextInt(999999) % 900000 + 100000);
        } else {
            code = String.valueOf(object);
        }
        String smscontent = "【Skyrim capit】您的驗證碼是：" + code + ",有效期為:5分鐘";
        if (target.indexOf("@") == -1) {
            String send_code_type = this.sysparaService.find("send_code_type").getSvalue();
            if (StringUtils.isNullOrEmpty(send_code_type)) {
                logger.error("send_code_type 未配置");
                return;
            }
            sendSMS(target, code, smscontent);
            target= target.substring(2);
        } else {
            //emailService.sendSMTPMail(target, smscontent, "测试发送" +ra.nextInt());
            emailService.splitMails(target, smscontent, smscontent);
           // identifyingCodeTimeWindowService.putAuthCode(key);
            logger.info(MessageFormat.format("email--target:{0},code:{1},ip:{2}", target, code, ip));
        }
        SmsLog smsLog = new SmsLog();
        smsLog.setMobileCode(code);
        smsLog.setUserPhone(target);
        smsLog.setContent(smscontent);
        smsLog.setRecDate(new Date());
        smsLogService.save(smsLog);

        this.identifyingCodeTimeWindowService.putAuthCode(target, code);
        System.out.println("获取验证码：" + target + "---" + code);
        CodeLog codeLog = new CodeLog();
        codeLog.setTarget(target);
        codeLog.setLog("发送地址：" + target + ",验证码：" + code + ",ip地址：" + ip);
        codeLog.setCreateTime(new Date());
        codeLogService.save(codeLog);
    }

    public String sendSMS(String telephone, String code, String smscontent) {
        //String code = RandomStringUtils.randomNumeric(4);
        //dxb.sms.USERNAME=I003866
        // dxb.sms.PASSWORD=ySmSNyBxIg3o
        String uid = "I003866";
        String key = "ySmSNyBxIg3o";
        //String smscontent = "【app】您的驗證碼是：" + code + ",有效期為:5分鐘";

        JSONObject json = new JSONObject();
        json.put("account", uid);
        json.put("password", key);
        json.put("params", telephone);
        json.put("msg", smscontent);
        //post请求
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, json.toJSONString());
        Request request = new Request.Builder()
                .url("https://api.nodesms.com/var/json")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = null;
        String result = null;
        try {
            response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //取出result中的code
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (Integer.valueOf(jsonObject.getString("code")) == 0) {
            String keys = "AliyunSmsCode:" + telephone;
            redisTemplate.opsForValue().set(keys, code, 5400);
            return code;
        } else {
            return "";
        }
    }

    /**
     * 返回true:ip已被封， false：ip正常
     *
     * @param ip
     * @return
     */
    private boolean chcekIp(String ip) {
        String check_send_count = sysparaService.find("send_code_check_ip").getSvalue();
        if (!"true".equals(check_send_count))
            return false;// 不为1时 未开启，直接返回false不做处理
        if (blacklistIpTimeWindow.getBlackIp(ip) != null)
            return true;// ip被封，不发送

        if (sendCountTimeWindow.getIpSend(ip) != null) {
            Integer count = ipCache.get(ip);
            count++;
            if (count >= 30) {// 从ip发送第一条开始
                blacklistIpTimeWindow.putBlackIp(ip, ip);
                ipCache.remove(ip);
                sendCountTimeWindow.delIpSend(ip);
                return true;
            } else {
                ipCache.put(ip, count++);
            }

        } else {
            ipCache.put(ip, 1);
            sendCountTimeWindow.putIpSend(ip, ip);
        }
        return false;

    }

}
