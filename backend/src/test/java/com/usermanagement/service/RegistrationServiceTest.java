package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.security.PasswordValidator;
import com.usermanagement.web.dto.AuthResponse;
import com.usermanagement.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * RegistrationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private EmailVerificationService emailVerificationService;

    private RegistrationService registrationService;

    private static final String TEST_EMAIL = "newuser@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_HASHED_PASSWORD = "hashedPassword123";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_ACCESS_TOKEN = "test-access-token";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";
    private static final String TEST_VERIFICATION_TOKEN = "verification-token-123";

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
            userRepository,
            passwordEncoder,
            jwtTokenProvider,
            passwordValidator,
            emailVerificationService
        );
    }

    @Test
    @DisplayName("注册成功 - 用户状态为 PENDING，发送验证邮件")
    void shouldRegisterSuccessfully_andSendVerificationEmail() {
        // Given
        UUID userId = UUID.randomUUID();
        User savedUser = createUser(userId, UserStatus.PENDING, false);

        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(TEST_HASHED_PASSWORD);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtTokenProvider.generateAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);
        given(emailVerificationService.generateToken(userId)).willReturn(TEST_VERIFICATION_TOKEN);

        RegisterRequest request = RegisterRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        // When
        AuthResponse response = registrationService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.getUserId()).isEqualTo(userId.toString());

        verify(passwordValidator).validate(TEST_PASSWORD);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertThat(createdUser.getPasswordHash()).isEqualTo(TEST_HASHED_PASSWORD);
        assertThat(createdUser.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(createdUser.getEmailVerified()).isFalse();

        verify(emailVerificationService).sendVerificationEmail(savedUser);
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void shouldThrowException_whenEmailExists() {
        // Given
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        RegisterRequest request = RegisterRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        // When & Then
        assertThatThrownBy(() -> registrationService.register(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("邮箱已被注册");

        verify(passwordValidator, never()).validate(any());
        verify(userRepository, never()).save(any());
        verify(emailVerificationService, never()).sendVerificationEmail(any());
    }

    @Test
    @DisplayName("注册失败 - 密码强度不足")
    void shouldThrowException_whenPasswordTooWeak() {
        // Given
        String weakPassword = "weak";
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordValidator).willThrow(new IllegalArgumentException("密码强度不足"));

        RegisterRequest request = RegisterRequest.builder()
            .email(TEST_EMAIL)
            .password(weakPassword)
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        // When & Then
        assertThatThrownBy(() -> registrationService.register(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("密码强度不足");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(emailVerificationService, never()).sendVerificationEmail(any());
    }

    @Test
    @DisplayName("验证邮箱成功 - 用户状态变为 ACTIVE")
    void shouldActivateUser_whenValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, UserStatus.PENDING, false);

        given(emailVerificationService.verifyToken(TEST_VERIFICATION_TOKEN)).willReturn(Optional.of(userId));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When
        registrationService.verifyEmail(TEST_VERIFICATION_TOKEN);

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerified()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("验证邮箱失败 - Token 无效")
    void shouldThrowException_whenInvalidToken() {
        // Given
        given(emailVerificationService.verifyToken(TEST_VERIFICATION_TOKEN)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> registrationService.verifyEmail(TEST_VERIFICATION_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("无效的验证 Token");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("验证邮箱失败 - 用户不存在")
    void shouldThrowException_whenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        given(emailVerificationService.verifyToken(TEST_VERIFICATION_TOKEN)).willReturn(Optional.of(userId));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> registrationService.verifyEmail(TEST_VERIFICATION_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户不存在");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("验证邮箱失败 - 用户已激活")
    void shouldThrowException_whenUserAlreadyActive() {
        // Given
        UUID userId = UUID.randomUUID();
        User activeUser = createUser(userId, UserStatus.ACTIVE, true);

        given(emailVerificationService.verifyToken(TEST_VERIFICATION_TOKEN)).willReturn(Optional.of(userId));
        given(userRepository.findById(userId)).willReturn(Optional.of(activeUser));

        // When & Then
        assertThatThrownBy(() -> registrationService.verifyEmail(TEST_VERIFICATION_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户已激活");

        verify(userRepository, never()).save(any());
    }

    private User createUser(UUID id, UserStatus status, Boolean emailVerified) {
        User user = new User();
        user.setId(id);
        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(TEST_HASHED_PASSWORD);
        user.setFirstName(TEST_FIRST_NAME);
        user.setLastName(TEST_LAST_NAME);
        user.setStatus(status);
        user.setEmailVerified(emailVerified);
        return user;
    }
}
