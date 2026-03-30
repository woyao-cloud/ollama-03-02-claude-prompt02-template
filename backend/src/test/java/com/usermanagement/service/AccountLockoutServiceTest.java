package com.usermanagement.service;

import com.usermanagement.config.AppProperties;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AccountLockoutService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AccountLockoutServiceTest {

    @Mock
    private UserRepository userRepository;

    private AccountLockoutService accountLockoutService;

    private AppProperties appProperties;

    private static final String TEST_EMAIL = "test@example.com";
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        accountLockoutService = new AccountLockoutService(userRepository, appProperties);
    }

    @Test
    @DisplayName("记录登录失败 - 失败次数增加")
    void shouldIncrementFailedAttempts_whenLoginFails() {
        // Given
        User user = createUser(3);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        accountLockoutService.recordLoginFailure(TEST_EMAIL);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(4);
    }

    @Test
    @DisplayName("记录登录失败 - 达到阈值后锁定账户")
    void shouldLockAccount_whenThresholdReached() {
        // Given
        appProperties.getAccount().getLockout().setThreshold(5);
        User user = createUser(4);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        accountLockoutService.recordLoginFailure(TEST_EMAIL);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(savedUser.getLockedUntil()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("检查账户锁定 - 未锁定返回 false")
    void shouldReturnFalse_whenAccountNotLocked() {
        // Given
        User user = createUser(2);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        boolean isLocked = accountLockoutService.isAccountLocked(TEST_EMAIL);

        // Then
        assertThat(isLocked).isFalse();
    }

    @Test
    @DisplayName("检查账户锁定 - 已锁定返回 true")
    void shouldReturnTrue_whenAccountLocked() {
        // Given
        User user = createUser(5);
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(Instant.now().plusSeconds(900));
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        boolean isLocked = accountLockoutService.isAccountLocked(TEST_EMAIL);

        // Then
        assertThat(isLocked).isTrue();
    }

    @Test
    @DisplayName("检查账户锁定 - 锁定已过期返回 false")
    void shouldReturnFalse_whenLockExpired() {
        // Given
        User user = createUser(5);
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(Instant.now().minusSeconds(100)); // 已过期
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        boolean isLocked = accountLockoutService.isAccountLocked(TEST_EMAIL);

        // Then
        assertThat(isLocked).isFalse();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("检查账户锁定 - 用户不存在返回 false")
    void shouldReturnFalse_whenUserNotFound() {
        // Given
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        // When
        boolean isLocked = accountLockoutService.isAccountLocked(TEST_EMAIL);

        // Then
        assertThat(isLocked).isFalse();
    }

    @Test
    @DisplayName("重置失败次数 - 成功重置")
    void shouldResetFailedAttempts_whenLoginSuccess() {
        // Given
        User user = createUser(3);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        accountLockoutService.resetFailedAttempts(TEST_EMAIL);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(savedUser.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("登录失败达到阈值抛出 LockedException")
    void shouldThrowLockedException_whenAccountLockedAfterFailure() {
        // Given
        appProperties.getAccount().getLockout().setThreshold(5);
        User user = createUser(4);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> accountLockoutService.recordLoginFailure(TEST_EMAIL))
            .isInstanceOf(LockedException.class)
            .hasMessage("账户已被锁定，请稍后再试");
    }

    @Test
    @DisplayName("自定义锁定阈值配置生效")
    void shouldUseCustomThreshold_fromAppProperties() {
        // Given
        appProperties.getAccount().getLockout().setThreshold(3);
        User user = createUser(2);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        accountLockoutService.recordLoginFailure(TEST_EMAIL);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    @DisplayName("自定义锁定时长配置生效")
    void shouldUseCustomLockoutDuration_fromAppProperties() {
        // Given
        int customDuration = 1800; // 30 分钟
        appProperties.getAccount().getLockout().setDuration(customDuration);
        appProperties.getAccount().getLockout().setThreshold(5);
        User user = createUser(4);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

        // When
        accountLockoutService.recordLoginFailure(TEST_EMAIL);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        Instant expectedUnlockTime = Instant.now().plusSeconds(customDuration);
        assertThat(savedUser.getLockedUntil()).isCloseTo(expectedUnlockTime, within(5000));
    }

    private User createUser(int failedAttempts) {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_EMAIL);
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(failedAttempts);
        return user;
    }

    private static long within(long milliseconds) {
        return milliseconds;
    }
}
