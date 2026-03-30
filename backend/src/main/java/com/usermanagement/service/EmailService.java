package com.usermanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件发送服务 - 发送各种类型的邮件
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String defaultFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.defaultFrom = "noreply@example.com";
    }

    /**
     * 发送简单邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);

            mailSender.send(message);
            logger.info("邮件发送成功：{}", to);
        } catch (MessagingException e) {
            logger.error("邮件发送失败：{}", to, e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 发送 HTML 邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param htmlContent HTML 内容
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("HTML 邮件发送成功：{}", to);
        } catch (MessagingException e) {
            logger.error("HTML 邮件发送失败：{}", to, e);
            throw new RuntimeException("HTML 邮件发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 发送邮件带抄送
     *
     * @param to      收件人
     * @param cc      抄送列表
     * @param subject 主题
     * @param content 内容
     */
    public void sendEmailWithCc(String to, java.util.List<String> cc, String subject, String content) {
        try {
            MimeMessage message = createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(defaultFrom);
            helper.setTo(to);
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0]));
            }
            helper.setSubject(subject);
            helper.setText(content);

            mailSender.send(message);
            logger.info("邮件发送成功（带抄送）：{}", to);
        } catch (MessagingException e) {
            logger.error("邮件发送失败（带抄送）：{}", to, e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 创建 MimeMessage
     *
     * @return MimeMessage
     */
    public MimeMessage createMimeMessage() {
        return mailSender.createMimeMessage();
    }
}
