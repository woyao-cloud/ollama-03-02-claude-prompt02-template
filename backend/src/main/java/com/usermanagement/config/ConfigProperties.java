package com.usermanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置管理属性
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "config")
public class ConfigProperties {

    /**
     * 加密配置
     */
    private Encryption encryption = new Encryption();

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    @Data
    public static class Encryption {
        /**
         * 加密密钥（至少 16 字符）
         */
        private String key = "defaultEncryptionKey123";

        /**
         * 是否启用加密
         */
        private boolean enabled = false;
    }

    @Data
    public static class Cache {
        /**
         * 本地缓存过期时间（秒）
         */
        private Long localExpiration = 300L;

        /**
         * Redis 缓存过期时间（秒）
         */
        private Long redisExpiration = 600L;

        /**
         * 本地缓存最大大小
         */
        private Integer maxSize = 500;
    }
}
