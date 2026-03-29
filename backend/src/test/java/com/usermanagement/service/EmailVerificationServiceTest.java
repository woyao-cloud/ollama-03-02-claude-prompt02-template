package com.usermanagement.service;

import com.usermanagement.config.AppProperties;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * EmailVerificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private VerificationTokenProvider tokenProvider;

    @Mock
    private AppProperties appProperties;

    private EmailVerificationService emailVerificationService;

    private static final String TEST_EMAIL = "user@example.com";
    private static final String TEST_TOKEN = "test-verification-token";
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String FRONTEND_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        given(appProperties.getFrontendUrl()).willReturn(FRONTEND_URL);
        emailVerificationService = new EmailVerificationService(mailSender, tokenProvider, appProperties);
    }

    @Test
    @DisplayName("生成验证 Token 成功")
    void shouldGenerateTokenSuccessfully() {
        // Given
        given(tokenProvider.generateToken(TEST_USER_ID)).willReturn(TEST_TOKEN);

        // When
        String token = emailVerificationService.generateToken(TEST_USER_ID);

        // Then
        assertThat(token).isEqualTo(TEST_TOKEN);
        then(tokenProvider).should().generateToken(TEST_USER_ID);
    }

    @Test
    @DisplayName("发送验证邮件成功")
    void shouldSendVerificationEmailSuccessfully() throws Exception {
        // Given
        User user = createUser(TEST_USER_ID, TEST_EMAIL, UserStatus.PENDING);
        given(tokenProvider.generateToken(TEST_USER_ID)).willReturn(TEST_TOKEN);

        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage mimeMessage = new MimeMessage(session);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        // When
        emailVerificationService.sendVerificationEmail(user);

        // Then
        then(mailSender).should(times(1)).send(any(MimeMessage.class));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        then(mailSender).should().send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString()).isEqualTo(TEST_EMAIL);
        assertThat(sentMessage.getSubject()).contains("邮箱验证");
    }

    @Test
    @DisplayName("验证 Token 成功 - 返回用户 ID")
    void shouldVerifyTokenSuccessfully() {
        // Given
        given(tokenProvider.verifyToken(TEST_TOKEN)).willReturn(Optional.of(TEST_USER_ID));

        // When
        Optional<UUID> result = emailVerificationService.verifyToken(TEST_TOKEN);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TEST_USER_ID);
        then(tokenProvider).should().verifyToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("验证 Token 失败 - Token 过期")
    void shouldReturnEmpty_whenTokenExpired() {
        // Given
        given(tokenProvider.verifyToken(TEST_TOKEN)).willReturn(Optional.empty());

        // When
        Optional<UUID> result = emailVerificationService.verifyToken(TEST_TOKEN);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("验证邮件包含正确的验证链接")
    void shouldSendEmailWithCorrectVerificationLink() throws Exception {
        // Given
        User user = createUser(TEST_USER_ID, TEST_EMAIL, UserStatus.PENDING);
        given(tokenProvider.generateToken(TEST_USER_ID)).willReturn(TEST_TOKEN);

        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage mimeMessage = new MimeMessage(session);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        // When
        emailVerificationService.sendVerificationEmail(user);

        // Then
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        then(mailSender).should().send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        String content = (String) sentMessage.getContent();
        assertThat(content).contains("http://localhost:3000/auth/verify?token=" + TEST_TOKEN);
    }

    private User createUser(UUID id, String email, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setStatus(status);
        user.setEmailVerified(false);
        return user;
    }
}
