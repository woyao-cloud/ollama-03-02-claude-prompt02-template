package com.usermanagement.service;

import com.usermanagement.config.AppProperties;
import com.usermanagement.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.UUID;

/**
 * 邮箱验证服务 - 处理邮箱验证相关逻辑
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    private final JavaMailSender mailSender;
    private final VerificationTokenProvider tokenProvider;
    private final String frontendUrl;

    public EmailVerificationService(
            JavaMailSender mailSender,
            VerificationTokenProvider tokenProvider,
            AppProperties appProperties) {
        this.mailSender = mailSender;
        this.tokenProvider = tokenProvider;
        this.frontendUrl = appProperties.getFrontendUrl();
    }

    /**
     * 生成验证 Token
     *
     * @param userId 用户 ID
     * @return 验证 Token
     */
    public String generateToken(UUID userId) {
        return tokenProvider.generateToken(userId);
    }

    /**
     * 发送验证邮件
     *
     * @param user 用户
     */
    public void sendVerificationEmail(User user) {
        String token = generateToken(user.getId());
        String verificationLink = buildVerificationLink(token);
        String htmlContent = buildVerificationEmailHtml(user.getFirstName(), verificationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@example.com");
            helper.setTo(user.getEmail());
            helper.setSubject("【用户管理系统】邮箱验证");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("验证邮件发送成功：{}", user.getEmail());
        } catch (MessagingException e) {
            logger.error("验证邮件发送失败：{}", user.getEmail(), e);
            throw new RuntimeException("验证邮件发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证 Token
     *
     * @param token 验证 Token
     * @return 用户 ID（如果有效）
     */
    public Optional<UUID> verifyToken(String token) {
        return tokenProvider.verifyToken(token);
    }

    /**
     * 构建验证链接
     */
    private String buildVerificationLink(String token) {
        return String.format("%s/auth/verify?token=%s", frontendUrl, token);
    }

    /**
     * 构建验证邮件 HTML 内容
     */
    private String buildVerificationEmailHtml(String firstName, String verificationLink) {
        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "  <meta charset=\"UTF-8\">" +
            "  <style>" +
            "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "    .button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; }" +
            "    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px; }" +
            "  </style>" +
            "</head>" +
            "<body>" +
            "  <div class=\"container\">" +
            "    <h2>欢迎加入用户管理系统！</h2>" +
            "    <p>尊敬的 %s，</p>" +
            "    <p>感谢您注册我们的系统。请点击下方按钮验证您的邮箱地址：</p>" +
            "    <p style=\"text-align: center; margin: 30px 0;\">" +
            "      <a href=\"%s\" class=\"button\">验证邮箱</a>" +
            "    </p>" +
            "    <p>如果按钮无法点击，请复制以下链接到浏览器：</p>" +
            "    <p style=\"word-break: break-all; color: #007bff;\">%s</p>" +
            "    <p>此验证链接将在 24 小时后失效。</p>" +
            "    <div class=\"footer\">" +
            "      <p>此邮件由系统自动发送，请勿回复。</p>" +
            "      <p>&copy; 2026 用户管理系统。All rights reserved.</p>" +
            "    </div>" +
            "  </div>" +
            "</body>" +
            "</html>",
            firstName,
            verificationLink,
            verificationLink
        );
    }
}
