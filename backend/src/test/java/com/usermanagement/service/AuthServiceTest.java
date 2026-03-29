package com.usermanagement.service;

import com.usermanagement.config.AppProperties;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.security.PasswordValidator;
import com.usermanagement.web.dto.AuthResponse;
import com.usermanagement.web.dto.LoginRequest;
import com.usermanagement.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
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
 * AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AccountLockoutService accountLockoutService;

    @Mock
    private PasswordValidator passwordValidator;

    private AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_HASHED_PASSWORD = "hashedPassword123";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_ACCESS_TOKEN = "test-access-token";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepository,
            passwordEncoder,
            null, // authenticationManager 不再使用
            jwtTokenProvider,
            accountLockoutService,
            passwordValidator
        );
    }

    @Test
    @DisplayName("登录成功 - 返回 Token")
    void shouldLoginSuccessfully_whenValidCredentials() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, UserStatus.ACTIVE, 0, null);

        given(accountLockoutService.isAccountLocked(TEST_EMAIL)).willReturn(false);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        LoginRequest request = LoginRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
        assertThat(response.getUserId()).isEqualTo(userId.toString());
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        verify(accountLockoutService).resetFailedAttempts(TEST_EMAIL);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("登录失败 - 账户被锁定")
    void shouldThrowException_whenAccountLocked() {
        // Given
        given(accountLockoutService.isAccountLocked(TEST_EMAIL)).willReturn(true);

        LoginRequest request = LoginRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(LockedException.class)
            .hasMessage("账户已被锁定，请稍后再试");

        verify(userRepository, never()).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void shouldThrowException_whenUserNotFound() {
        // Given
        given(accountLockoutService.isAccountLocked(TEST_EMAIL)).willReturn(false);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("邮箱或密码错误");
    }

    @Test
    @DisplayName("登录失败 - 账户被禁用")
    void shouldThrowException_whenAccountDisabled() {
        // Given
        User disabledUser = createUser(UUID.randomUUID(), UserStatus.INACTIVE, 0, null);

        given(accountLockoutService.isAccountLocked(TEST_EMAIL)).willReturn(false);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(disabledUser));

        LoginRequest request = LoginRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("账户已被禁用");
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void shouldThrowException_whenInvalidPassword() {
        // Given
        User user = createUser(UUID.randomUUID(), UserStatus.ACTIVE, 0, null);

        given(accountLockoutService.isAccountLocked(TEST_EMAIL)).willReturn(false);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).willReturn(false);

        LoginRequest request = LoginRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("邮箱或密码错误");

        // 验证记录登录失败
        verify(accountLockoutService).recordLoginFailure(TEST_EMAIL);
    }

    @Test
    @DisplayName("登录成功后重置失败次数")
    void shouldResetFailedAttempts_afterSuccessfulLogin() {
        // Given
        User user = createUser(UUID.randomUUID(), UserStatus.ACTIVE, 3, null);

        given(accountLockoutService.isAccountLocked(TEST_EMAIL)).willReturn(false);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        LoginRequest request = LoginRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

        // When
        authService.login(request);

        // Then
        verify(accountLockoutService).resetFailedAttempts(TEST_EMAIL);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("注册成功 - 返回 Token")
    void shouldRegisterSuccessfully_whenValidRequest() {
        // Given
        UUID userId = UUID.randomUUID();
        User savedUser = createUser(userId, UserStatus.PENDING, 0, null);

        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(TEST_HASHED_PASSWORD);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtTokenProvider.generateAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        RegisterRequest request = RegisterRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.getUserId()).isEqualTo(userId.toString());

        // 验证密码策略检查
        verify(passwordValidator).validate(TEST_PASSWORD);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertThat(createdUser.getPasswordHash()).isEqualTo(TEST_HASHED_PASSWORD);
        assertThat(createdUser.getStatus()).isEqualTo(UserStatus.PENDING);
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
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("邮箱已被注册");

        verify(passwordValidator, never()).validate(any());
    }

    @Test
    @DisplayName("注册失败 - 密码强度不足")
    void shouldThrowException_whenPasswordTooWeak() {
        // Given
        String weakPassword = "weak";
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordValidator).willThrow(IllegalArgumentException.class);

        RegisterRequest request = RegisterRequest.builder()
            .email(TEST_EMAIL)
            .password(weakPassword)
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(IllegalArgumentException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("刷新 Token 成功")
    void shouldRefreshTokenSuccessfully_whenValidRefreshToken() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, UserStatus.ACTIVE, 0, null);

        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(TEST_REFRESH_TOKEN)).willReturn(userId.toString());
        given(jwtTokenProvider.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        // When
        AuthResponse response = authService.refreshToken(TEST_REFRESH_TOKEN);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("刷新 Token 失败 - Token 无效")
    void shouldThrowException_whenInvalidRefreshToken() {
        // Given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(TEST_REFRESH_TOKEN))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("无效的 Refresh Token");
    }

    @Test
    @DisplayName("刷新 Token 失败 - 用户不存在")
    void shouldThrowException_whenUserNotFoundDuringRefresh() {
        // Given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(TEST_REFRESH_TOKEN)).willReturn(UUID.randomUUID().toString());
        given(jwtTokenProvider.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(TEST_REFRESH_TOKEN))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("用户不存在");
    }

    @Test
    @DisplayName("刷新 Token 失败 - 账户状态异常")
    void shouldThrowException_whenAccountInactiveDuringRefresh() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, UserStatus.INACTIVE, 0, null);

        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(TEST_REFRESH_TOKEN)).willReturn(userId.toString());
        given(jwtTokenProvider.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(TEST_REFRESH_TOKEN))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("账户状态异常");
    }

    private User createUser(UUID id, UserStatus status, int failedAttempts, Instant lockedUntil) {
        User user = new User();
        user.setId(id);
        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(TEST_HASHED_PASSWORD);
        user.setFirstName(TEST_FIRST_NAME);
        user.setLastName(TEST_LAST_NAME);
        user.setStatus(status);
        user.setFailedLoginAttempts(failedAttempts);
        user.setLockedUntil(lockedUntil);
        return user;
    }
}
