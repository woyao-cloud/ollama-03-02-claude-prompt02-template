package com.usermanagement.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证 Token 提供者 - 生成和验证邮箱验证 Token
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class VerificationTokenProvider {

    private static final int TOKEN_LENGTH = 32;
    private static final long DEFAULT_TOKEN_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 小时

    private final SecureRandom secureRandom;
    private final Map<String, TokenData> tokenStore;
    private long tokenExpiryMs;

    public VerificationTokenProvider() {
        this.secureRandom = new SecureRandom();
        this.tokenStore = new ConcurrentHashMap<>();
        this.tokenExpiryMs = DEFAULT_TOKEN_EXPIRY_MS;
    }

    /**
     * 生成验证 Token
     *
     * @param userId 用户 ID
     * @return 验证 Token
     */
    public String generateToken(UUID userId) {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        tokenStore.put(token, new TokenData(userId, Instant.now().plusMillis(tokenExpiryMs)));

        return token;
    }

    /**
     * 验证 Token
     *
     * @param token 验证 Token
     * @return 用户 ID（如果有效）
     */
    public Optional<UUID> verifyToken(String token) {
        TokenData tokenData = tokenStore.remove(token); // 一次性使用，验证后删除

        if (tokenData == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(tokenData.expiryAt)) {
            return Optional.empty();
        }

        return Optional.of(tokenData.userId);
    }

    /**
     * 清理过期 Token
     */
    public void cleanExpiredTokens() {
        Instant now = Instant.now();
        tokenStore.entrySet().removeIf(entry -> entry.getValue().expiryAt.isBefore(now));
    }

    /**
     * Token 数据
     */
    private static class TokenData {
        private final UUID userId;
        private final Instant expiryAt;

        TokenData(UUID userId, Instant expiryAt) {
            this.userId = userId;
            this.expiryAt = expiryAt;
        }
    }
}
