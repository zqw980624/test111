package com.yami.trading.common.manager.email;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import freemarker.template.Template;
import freemarker.template.TemplateException;

@Slf4j
@Data
public class EmailManager {
    private SimpleMailMessage mailMessage;
    private FreeMarkerConfigurer freeMarkerConfigurer;
    private JavaMailSenderImpl mailSender;

    public EmailManager(JavaMailSenderImpl javaMailSender) {
        this.mailSender = javaMailSender;
    }

    public EmailManager() {

    }

    /**
     * 发送邮箱
     * @param emailMessage
     */
    public  void send(EmailMessage emailMessage) {
        try {
            MimeMessage mailMsg = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMsg, true, "UTF-8");
            messageHelper.setTo("@gmail.com");// 接收邮箱
            messageHelper.setFrom("@gmail.com");// 发送邮箱
            messageHelper.setSentDate(new Date());// 发送时间
            messageHelper.setSubject(emailMessage.getSubject());// 邮件标题
            if (StrUtil.isEmpty(emailMessage.getFtlname())) {
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
            log.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获取模板并将内容输出到模板
     *
     * @param
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
