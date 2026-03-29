package com.usermanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用程序配置属性
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Password password = new Password();
    private final Account account = new Account();

    @Data
    public static class Jwt {
        private String secret;
        private Long expiration;
        private Long refreshExpiration;
        private String issuer;
    }

    @Data
    public static class Password {
        private Integer minLength = 8;
        private Boolean requireUppercase = true;
        private Boolean requireLowercase = true;
        private Boolean requireDigit = true;
        private Boolean requireSpecial = false;
    }

    @Data
    public static class Account {
        private final Lockout lockout = new Lockout();

        @Data
        public static class Lockout {
            private Integer threshold = 5;
            private Integer duration = 900; // 15 分钟
        }
    }
}
