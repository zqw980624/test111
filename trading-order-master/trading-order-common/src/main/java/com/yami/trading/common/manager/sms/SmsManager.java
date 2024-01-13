package com.yami.trading.common.manager.sms;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class SmsManager {
    /**
     * 发送的短信接口类型 tiantian---天天---smsSendService--->>>>--
     * moduyun---摩杜云---smsSingleSender
     */
    public void send(String sendCodeType, String user, String password, boolean inter,
                     String mobile, String content) {
        if ("tiantian".equals(sendCodeType)) {
            String dest = "";
            String ip = "";
            if (inter) {
                ip = "210.51.190.232";
                int port = 8085;
                HttpClientUtil util = new HttpClientUtil(ip, port, "/mt/MT3.ashx");
                String ServiceID = "SEND";
                // 目的号码
                dest = mobile;
                // 原号码
                String sender = "";
                // 短信内容
                String msg = content;
                // UTF-16BE
                String hex = WebNetEncode.encodeHexStr(0, msg);
                hex = hex.trim() + "&codec=0";
//				util.sendPostMessage(user, pwd, ServiceID, dest, sender, hex);
                log.info("tiantian--" + mobile + ",短信内容：" + content + "--验证码发送返回信息 = "
                        + util.sendPostMessage(user, password, ServiceID, dest, sender, hex));
//			        System.out.println("验证码发送返回信息 = " + util.sendPostMessage( user,  pwd,  ServiceID,  dest,  sender,  hex));
            } else {
                ip = "210.51.190.233";
                int port = 8085;
                HttpClientUtil util = new HttpClientUtil(ip, port, "/mt/MT3.ashx");
                String ServiceID = "SEND";
                // 目的号码
                dest = mobile;
                // 原号码
                String sender = "";
                // 短信内容
                String msg = content;
                // UTF-16BE
                String hex = WebNetEncode.encodeHexStr(8, msg);
                hex = hex.trim() + "&codec=8";
//				util.sendPostMessage(user, pwd, ServiceID, dest, sender, hex);
                log.info("tiantian--" + mobile + ",短信内容：" + content + "--验证码发送返回信息 = "
                        + util.sendPostMessage(user, password, ServiceID, dest, sender, hex));
            }
        } else if ("smsbao".equals(sendCodeType)) {
            String httpUrl = null;
            if (inter) {
                // 国际
                httpUrl = "http://api.smsbao.com/wsms";
                // 国际
//				httpUrl = "http://iauhnbqszxl.site";
            } else {
                httpUrl = "http://api.smsbao.com/sms";
//				httpUrl = "http://xahsdfg.site";
            }
            StringBuffer httpArg = new StringBuffer();
            httpArg.append("u=").append(user).append("&");
            httpArg.append("p=").append(md5(password)).append("&");
            if (inter) {
                // 国际
                httpArg.append("m=").append(encodeUrlString("+", "UTF-8") + mobile).append("&");
            } else {
                httpArg.append("m=").append(mobile.substring(2, mobile.length()))
                        .append("&");
            }
            httpArg.append("c=").append(encodeUrlString(content, "UTF-8"));
            String result = request(httpUrl, httpArg.toString());
            if (!"0".equals(result)) {
                log.info("Smsbao--" + mobile + ",短信内容：" + content + "--验证码发送失败 ");
            } else {
                log.info("Smsbao--" + mobile + ",短信内容：" + content + "--验证码发送成功 ");
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
