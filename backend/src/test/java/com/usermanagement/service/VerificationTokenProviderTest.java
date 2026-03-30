package com.usermanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VerificationTokenProvider 单元测试
 */
class VerificationTokenProviderTest {

    private VerificationTokenProvider tokenProvider;

    private static final long TOKEN_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 小时

    @BeforeEach
    void setUp() {
        tokenProvider = new VerificationTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "tokenExpiryMs", TOKEN_EXPIRY_MS);
    }

    @Test
    @DisplayName("生成 Token 成功 - 返回随机字符串")
    void shouldGenerateTokenSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        String token = tokenProvider.generateToken(userId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("验证 Token 成功 - 返回用户 ID")
    void shouldVerifyTokenSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId);

        // When
        Optional<UUID> result = tokenProvider.verifyToken(token);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(userId);
    }

    @Test
    @DisplayName("验证 Token 失败 - Token 不存在")
    void shouldReturnEmpty_whenTokenNotFound() {
        // Given
        String invalidToken = "invalid-token";

        // When
        Optional<UUID> result = tokenProvider.verifyToken(invalidToken);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Token 过期后验证失败")
    void shouldReturnEmpty_whenTokenExpired() throws InterruptedException {
        // Given
        UUID userId = UUID.randomUUID();
        VerificationTokenProvider shortLivedProvider = new VerificationTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "tokenExpiryMs", 100L); // 100ms expiry
        ReflectionTestUtils.setField(shortLivedProvider, "tokenStore", new ConcurrentHashMap<>());

        String token = shortLivedProvider.generateToken(userId);

        // Wait for token to expire
        Thread.sleep(150);

        // When
        Optional<UUID> result = shortLivedProvider.verifyToken(token);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("验证 Token 后 Token 被删除 (一次性使用)")
    void shouldDeleteTokenAfterVerification() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId);

        // When
        tokenProvider.verifyToken(token);

        // Then - token should be removed after verification
        Optional<UUID> secondVerification = tokenProvider.verifyToken(token);
        assertThat(secondVerification).isEmpty();
    }

    @Test
    @DisplayName("生成不同用户的 Token 不相同")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        // When
        String token1 = tokenProvider.generateToken(userId1);
        String token2 = tokenProvider.generateToken(userId2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("清理过期 Token")
    void shouldCleanExpiredTokens() throws InterruptedException {
        // Given
        VerificationTokenProvider provider = new VerificationTokenProvider();
        ReflectionTestUtils.setField(provider, "tokenExpiryMs", 50L);
        ReflectionTestUtils.setField(provider, "tokenStore", new ConcurrentHashMap<>());

        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId);

        Thread.sleep(100);

        // When
        provider.cleanExpiredTokens();

        // Then
        Optional<UUID> result = provider.verifyToken(token);
        assertThat(result).isEmpty();
    }
}
