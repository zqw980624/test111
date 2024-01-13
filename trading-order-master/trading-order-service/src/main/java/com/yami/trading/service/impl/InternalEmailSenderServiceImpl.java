package com.yami.trading.service.impl;

import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.manager.email.EmailMessage;
import com.yami.trading.common.util.PropertiesUtil;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.InternalEmailSenderService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;


public class InternalEmailSenderServiceImpl implements InternalEmailSenderService, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(InternalEmailSenderServiceImpl.class);
    private JavaMailSenderImpl mailSender;
    private SimpleMailMessage mailMessage;
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Override
    public void afterPropertiesSet() {
        mailSender = new JavaMailSenderImpl();
        mailSender.setUsername(PropertiesUtil.getProperty("email.username"));
        mailSender.setPassword(PropertiesUtil.getProperty("email.password"));
        mailSender.setHost(PropertiesUtil.getProperty("email.host"));
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.port", "465");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mmail.debug", "true");
        javaMailProperties.setProperty("mail.smtp.host", "smtp.gmail.com");
        javaMailProperties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        javaMailProperties.setProperty("mail.smtp.socketFactory.port", "465");
        mailSender.setJavaMailProperties(javaMailProperties);
        mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(PropertiesUtil.getProperty("email.from"));
        freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("classpath:email/ftl");
        Properties settings = new Properties();
        settings.setProperty("template_update_delay", "1800");
        settings.setProperty("default_encoding", "UTF-8");
        settings.setProperty("locale", "zh_CN");
        freeMarkerConfigurer.setFreemarkerSettings(settings);
    }

    @Override
    public void send(EmailMessage emailMessage) {
        try {
            MimeMessage mailMsg = this.mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMsg, true, "UTF-8");
            messageHelper.setTo(emailMessage.getTomail());// 接收邮箱
            messageHelper.setFrom(this.mailMessage.getFrom());// 发送邮箱
            messageHelper.setSentDate(new Date());// 发送时间
            messageHelper.setSubject(emailMessage.getSubject());// 邮件标题
            if (StringUtils.isNullOrEmpty(emailMessage.getFtlname())) {
                messageHelper.setText(emailMessage.getContent());// 邮件内容
            } else {
                messageHelper.setText(this.getMailText(emailMessage.getFtlname(), emailMessage.getMap()), true);// 邮件内容
            }
            // true 表示启动HTML格式的邮件
            if (emailMessage.getFile() != null) {
                // 添加邮件附件
                FileSystemResource rarfile = new FileSystemResource(emailMessage.getFile());
                // addAttachment addInline 两种附件添加方式
                // 以附件的形式添加到邮件
                // 使用MimeUtility.encodeWord 解决附件名中文乱码的问题
                messageHelper.addAttachment(MimeUtility.encodeWord(emailMessage.getFilename()), rarfile);
            }
            this.mailSender.send(mailMsg);// 发送
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取模板并将内容输出到模板
     *
     * @return
     */
    private String getMailText(String ftlname, Map<String, Object> map) {
        String html = "";
        try {
            // 装载模板
            Template tpl = this.freeMarkerConfigurer.getConfiguration().getTemplate(ftlname);
            // 加入map到模板中 输出对应变量
            html = FreeMarkerTemplateUtils.processTemplateIntoString(tpl, map);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return html;
    }
}
