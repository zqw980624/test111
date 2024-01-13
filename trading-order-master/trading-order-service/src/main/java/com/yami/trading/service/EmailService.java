package com.yami.trading.service;

import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {


    public void splitMails(String to,String title,String Content) {

// 设置SMTP服务器信息
        String host = "85.121.237.76";
        int port = 2525;
        String username = "skyrimc@fengemail.com";
        String password = "jsse231228Jkso87uit9";

        // 创建Properties对象，设置SMTP服务器信息
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // 创建Session对象
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            // 创建MimeMessage对象
            Message message = new MimeMessage(session);
            // 设置发件人
            message.setFrom(new InternetAddress("noreply@skyrimc.com"));
            // 设置收件人
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            // 设置邮件主题
            message.setSubject(title);
            // 设置邮件内容
            message.setText(Content);
            // 发送邮件
            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
    public boolean sendSMTPMail(String to, String text, String title) {
        String host = "smtp.office365.com";
        String mailStoreType = "smtp";
        String popPort = "587";
        final Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.store.protocol", mailStoreType);
        props.put( "mail.smtp.port", popPort );
        //开启SSL
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.socketFactory.port",popPort);
        props.put("mail.smtp.socketFactory.fallback","false");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        try {
            Session session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("gxvqmsusqra@outlook.com", "dk136806");//账号密码
                }
            });
            session.setDebug(true);
            // 创建邮件消息
            MimeMessage message = new MimeMessage(session);
            // 设置发件人
            InternetAddress form = new InternetAddress("gxvqmsusqra@outlook.com");
            message.setFrom(form);
            // 设置收件人
            InternetAddress toAddress = new InternetAddress(to);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            // 设置邮件标题
            message.setSubject(title);
            // 设置邮件的内容体
            message.setContent(text, "text/html;charset=UTF-8");
            // 发送邮件
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void splitMail(String to,String title,String Content) throws Exception {

        // 给用户发送邮件的邮箱
        final String from = "@qq.com";
        // 邮箱的用户名
        final String username = "@qq.com";
        // 邮箱授权码
        final String password = "";
        // 发送邮件的服务器地址，QQ服务器
        final String host = "smtp.qq.com";

        // 使用QQ邮箱时配置
        Properties prop = new Properties();
        prop.setProperty("mail.host", "smtp.qq.com");    // 设置QQ邮件服务器
        prop.setProperty("mail.transport.protocol", "smtp");      // 邮件发送协议
        prop.setProperty("mail.smtp.auth", "true");      // 需要验证用户名和密码

        // 关于QQ邮箱，还要设置SSL加密，其他邮箱不需要
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);

        // 创建定义整个邮件程序所需的环境信息的 Session 对象，QQ才有，其他邮箱就不用了
        Session session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 发件人邮箱用户名，授权码
                return new PasswordAuthentication(username, password);
            }
        });

        // 开启 Session 的 debug 模式，这样就可以查看程序发送 Email 的运行状态
        session.setDebug(true);

        // 通过 session 得到 transport 对象
        Transport ts = session.getTransport();

        // 使用邮箱的用户名和授权码连上邮箱服务器
        ts.connect(host, username, password);

        // 创建邮件，写邮件
        // 需要传递 session
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from)); // 指明邮件的发件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));   // 指明邮件的收件人
        message.setSubject(title);     // 邮件主题
        message.setContent(Content, "text/html;charset=utf-8");

        // 文本
       // MimeBodyPart body2 = new MimeBodyPart();
       // body2.setContent("我不是广告111，<img src='cid:1.png'>", "text/html;charset=utf-8");
        // body2.setContent("我不是广告，<img src='cid:1.png'>", "text/html;charset=utf-8");

        // 保存修改

        // 发送邮件
        ts.sendMessage(message, message.getAllRecipients());

        // 释放资源
        ts.close();
    }
}
