package com.usermanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * EmailService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    private static final String TEST_TO = "recipient@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_CONTENT = "Test Content";

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
    }

    @Test
    @DisplayName("发送简单邮件成功")
    void shouldSendSimpleEmailSuccessfully() throws Exception {
        // Given
        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage mimeMessage = new MimeMessage(session);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        // When
        emailService.sendSimpleEmail(TEST_TO, TEST_SUBJECT, TEST_CONTENT);

        // Then
        then(mailSender).should().send(any(MimeMessage.class));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        then(mailSender).should().send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString()).isEqualTo(TEST_TO);
        assertThat(sentMessage.getSubject()).isEqualTo(TEST_SUBJECT);
    }

    @Test
    @DisplayName("发送 HTML 邮件成功")
    void shouldSendHtmlEmailSuccessfully() throws Exception {
        // Given
        String htmlContent = "<html><body><h1>Test HTML</h1></body></html>";
        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage mimeMessage = new MimeMessage(session);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        // When
        emailService.sendHtmlEmail(TEST_TO, TEST_SUBJECT, htmlContent);

        // Then
        then(mailSender).should().send(any(MimeMessage.class));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        then(mailSender).should().send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString()).isEqualTo(TEST_TO);
        assertThat(sentMessage.getSubject()).isEqualTo(TEST_SUBJECT);
    }

    @Test
    @DisplayName("发送邮件带抄送成功")
    void shouldSendEmailWithCcSuccessfully() throws Exception {
        // Given
        String ccEmail = "cc@example.com";
        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage mimeMessage = new MimeMessage(session);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        // When
        emailService.sendEmailWithCc(TEST_TO, Collections.singletonList(ccEmail), TEST_SUBJECT, TEST_CONTENT);

        // Then
        then(mailSender).should().send(any(MimeMessage.class));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        then(mailSender).should().send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString()).isEqualTo(TEST_TO);
    }

    @Test
    @DisplayName("创建 MimeMessage 成功")
    void shouldCreateMimeMessageSuccessfully() {
        // Given
        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage mimeMessage = new MimeMessage(session);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        // When
        MimeMessage result = emailService.createMimeMessage();

        // Then
        assertThat(result).isNotNull();
        then(mailSender).should().createMimeMessage();
    }
}
