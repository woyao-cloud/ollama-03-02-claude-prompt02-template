package com.usermanagement.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PasswordEncoderConfig 集成测试
 */
@SpringBootTest
class PasswordEncoderConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppProperties appProperties;

    @Test
    @DisplayName("PasswordEncoder Bean 存在")
    void shouldHavePasswordEncoderBean() {
        // Then
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    @DisplayName("使用 BCrypt 加密")
    void shouldUseBCrypt() {
        // Given
        String rawPassword = "testPassword123";

        // When
        String encoded = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encoded).startsWith("$2");
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("BCrypt 强度因子为 12")
    void shouldUseStrength12() {
        // Given
        String rawPassword = "testPassword123";

        // When
        String encoded = passwordEncoder.encode(rawPassword);

        // Then
        // BCrypt 格式：$2a$12$... 其中 12 是 strength
        assertThat(encoded).contains("$2a$12$");
    }

    @Test
    @DisplayName("每次加密生成不同哈希")
    void shouldGenerateDifferentHashes() {
        // Given
        String rawPassword = "testPassword123";

        // When
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encoded1).isNotEqualTo(encoded2);
        assertThat(passwordEncoder.matches(rawPassword, encoded1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, encoded2)).isTrue();
    }

    @Test
    @DisplayName("密码策略配置加载正确")
    void shouldLoadPasswordPolicyCorrectly() {
        // Then
        assertThat(appProperties.getPassword().getMinLength()).isEqualTo(8);
        assertThat(appProperties.getPassword().getRequireUppercase()).isTrue();
        assertThat(appProperties.getPassword().getRequireLowercase()).isTrue();
        assertThat(appProperties.getPassword().getRequireDigit()).isTrue();
        assertThat(appProperties.getPassword().getRequireSpecial()).isFalse();
    }

    @Test
    @DisplayName("账户锁定配置加载正确")
    void shouldLoadAccountLockoutConfigCorrectly() {
        // Then
        assertThat(appProperties.getAccount().getLockout().getThreshold()).isEqualTo(5);
        assertThat(appProperties.getAccount().getLockout().getDuration()).isEqualTo(900);
    }
}
