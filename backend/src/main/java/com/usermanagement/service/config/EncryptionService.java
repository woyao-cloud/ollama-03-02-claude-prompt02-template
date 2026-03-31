package com.usermanagement.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 配置加密服务 - 用于敏感配置的加密和解密
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private final SecretKey secretKey;
    private final boolean enabled;

    public EncryptionService(
            @Value("${config.encryption.key:defaultEncryptionKey123}") String encryptionKey,
            @Value("${config.encryption.enabled:false}") boolean enabled) {
        // 确保密钥长度为 16 字节（128 位）
        String key = encryptionKey.length() >= 16 ? encryptionKey.substring(0, 16) : encryptionKey;
        while (key.length() < 16) {
            key += "0";
        }
        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.enabled = enabled;
        log.info("配置加密服务已初始化，启用状态：{}", enabled);
    }

    /**
     * 加密配置值
     */
    public String encrypt(String value) {
        if (!enabled || value == null || value.isEmpty()) {
            return value;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("配置加密失败", e);
            throw new EncryptionException("配置加密失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解密配置值
     */
    public String decrypt(String encryptedValue) {
        if (!enabled || encryptedValue == null || encryptedValue.isEmpty()) {
            return encryptedValue;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("配置解密失败", e);
            throw new EncryptionException("配置解密失败：" + e.getMessage(), e);
        }
    }

    /**
     * 判断加密是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 加密异常
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
