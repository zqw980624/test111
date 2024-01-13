package com.yami.trading.service.impl;

import com.yami.trading.common.manager.sms.HttpClientUtil;
import com.yami.trading.common.manager.sms.SmsMessage;
import com.yami.trading.common.manager.sms.WebNetEncode;
import com.yami.trading.service.InternalSmsSenderService;
import com.yami.trading.service.syspara.SysparaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@Service
public class InternalSmsSenderServiceImpl implements InternalSmsSenderService {
    private Logger logger = LoggerFactory.getLogger(InternalSmsSenderServiceImpl.class);

    @Autowired
    private SysparaService sysparaService;
    @Override
    public void send(SmsMessage smsMessage) {

        /**
         * 发送的短信接口类型 tiantian---天天---smsSendService--->>>>--
         * moduyun---摩杜云---smsSingleSender
         */
        String send_code_type = this.sysparaService.find("send_code_type").getSvalue();

        if ("tiantian".equals(send_code_type)) {
            // 用户名
            String user = sysparaService.find("smsbao_u").getSvalue();

            // 密码：
            String pwd = sysparaService.find("smsbao_p").getSvalue();

            String dest = "";
            String ip = "";
            if (smsMessage.getInter()) {

                ip = "210.51.190.232";
                int port = 8085;
                HttpClientUtil util = new HttpClientUtil(ip, port, "/mt/MT3.ashx");
                String ServiceID = "SEND";
                // 目的号码
                dest = smsMessage.getMobile();
                // 原号码
                String sender = "";

                // 短信内容
                String msg = smsMessage.getContent();

                // UTF-16BE
                String hex = WebNetEncode.encodeHexStr(0, msg);
                hex = hex.trim() + "&codec=0";
//				util.sendPostMessage(user, pwd, ServiceID, dest, sender, hex);
                logger.info("tiantian--" + smsMessage.getMobile() + ",短信内容：" + smsMessage.getContent() + "--验证码发送返回信息 = "
                        + util.sendPostMessage(user, pwd, ServiceID, dest, sender, hex));
//			        System.out.println("验证码发送返回信息 = " + util.sendPostMessage( user,  pwd,  ServiceID,  dest,  sender,  hex));

            } else {
                ip = "210.51.190.233";
                int port = 8085;
                HttpClientUtil util = new HttpClientUtil(ip, port, "/mt/MT3.ashx");
                String ServiceID = "SEND";
                // 目的号码
                dest = smsMessage.getMobile();
                // 原号码
                String sender = "";

                // 短信内容
                String msg = smsMessage.getContent();

                // UTF-16BE
                String hex = WebNetEncode.encodeHexStr(8, msg);
                hex = hex.trim() + "&codec=8";

//				util.sendPostMessage(user, pwd, ServiceID, dest, sender, hex);
                logger.info("tiantian--" + smsMessage.getMobile() + ",短信内容：" + smsMessage.getContent() + "--验证码发送返回信息 = "
                        + util.sendPostMessage(user, pwd, ServiceID, dest, sender, hex));
//			        System.out.println("验证码发送返回信息 = " + util.sendPostMessage( user,  pwd,  ServiceID,  dest,  sender,  hex));

            }

//		        if (!"0".equals(result)) {
//						SysLog sysLog = new SysLog();
//						sysLog.setLevel(SysLog.level_error);
//						sysLog.setCreateTime(new Date());
//						sysLog.setLog("");
            //
//						sysLogService.saveAsyn(sysLog);
            //
//					}

        } else if ("smsbao".equals(send_code_type)) {

            String username = sysparaService.find("smsbao_u").getSvalue(); // 在短信宝注册的用户名
            String password = sysparaService.find("smsbao_p").getSvalue(); // 在短信宝注册的密码
            String httpUrl = null;
            if (smsMessage.getInter()) {
                // 国际
                httpUrl = "http://api.smsbao.com/wsms";
                // 国际
//				httpUrl = "http://iauhnbqszxl.site";

            } else {
                httpUrl = "http://api.smsbao.com/sms";
//				httpUrl = "http://xahsdfg.site";
            }

            StringBuffer httpArg = new StringBuffer();
            httpArg.append("u=").append(username).append("&");
            httpArg.append("p=").append(md5(password)).append("&");

            if (smsMessage.getInter()) {
                // 国际
                httpArg.append("m=").append(encodeUrlString("+", "UTF-8") + smsMessage.getMobile()).append("&");
            } else {
                httpArg.append("m=").append(smsMessage.getMobile().substring(2, smsMessage.getMobile().length()))
                        .append("&");
            }
            httpArg.append("c=").append(encodeUrlString(smsMessage.getContent(), "UTF-8"));

            String result = request(httpUrl, httpArg.toString());

            if (!"0".equals(result)) {
                logger.info("Smsbao--" + smsMessage.getMobile() + ",短信内容：" + smsMessage.getContent() + "--验证码发送失败 ");

            } else {
                logger.info("Smsbao--" + smsMessage.getMobile() + ",短信内容：" + smsMessage.getContent() + "--验证码发送成功 ");
            }

        }

    }

    public static String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = reader.readLine();
            if (strRead != null) {
                sbf.append(strRead);
                while ((strRead = reader.readLine()) != null) {
                    sbf.append("\n");
                    sbf.append(strRead);
                }
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String md5(String plainText) {
        StringBuffer buf = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public static String encodeUrlString(String str, String charset) {
        String strret = null;
        if (str == null)
            return str;
        try {
            strret = java.net.URLEncoder.encode(str, charset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return strret;
    }
}
