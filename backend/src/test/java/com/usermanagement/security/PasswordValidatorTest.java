package com.usermanagement.security;

import com.usermanagement.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PasswordValidator 单元测试
 */
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;
    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        passwordValidator = new PasswordValidator(appProperties);
    }

    @Test
    @DisplayName("密码验证通过 - 满足所有策略要求")
    void shouldValidateSuccessfully_whenPasswordMeetsAllRequirements() {
        // Given
        String validPassword = "Test1234!";

        // When
        boolean isValid = passwordValidator.validate(validPassword);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("密码验证失败 - 长度不足")
    void shouldFailValidation_whenPasswordTooShort() {
        // Given
        String shortPassword = "Test1!";

        // When & Then
        assertThatThrownBy(() -> passwordValidator.validate(shortPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("密码长度不能少于");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test1234!",      // 缺少大写字母
        "TEST1234!",      // 缺少小写字母
        "Testtest!",      // 缺少数字
        "Test1234"        // 缺少特殊字符 (requireSpecial=false 时应该通过)
    })
    @DisplayName("密码验证失败 - 缺少必要字符类型")
    void shouldFailValidation_whenMissingRequiredCharacterType(String password) {
        // When & Then
        if ("Test1234".equals(password)) {
            // requireSpecial 默认为 false，所以应该通过
            assertThat(passwordValidator.validate(password)).isTrue();
        } else {
            assertThatThrownBy(() -> passwordValidator.validate(password))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("密码验证通过 - 特殊字符非必需时")
    void shouldValidateSuccessfully_whenSpecialCharNotRequired() {
        // Given
        appProperties.getPassword().setRequireSpecial(false);
        String password = "Test1234";

        // When
        boolean isValid = passwordValidator.validate(password);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("密码验证通过 - 特殊字符为必需时")
    void shouldValidateSuccessfully_whenSpecialCharRequired() {
        // Given
        appProperties.getPassword().setRequireSpecial(true);
        String password = "Test1234!";

        // When
        boolean isValid = passwordValidator.validate(password);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("密码验证失败 - 特殊字符为必需但缺少")
    void shouldFailValidation_whenSpecialCharRequiredButMissing() {
        // Given
        appProperties.getPassword().setRequireSpecial(true);
        String password = "Test1234";

        // When & Then
        assertThatThrownBy(() -> passwordValidator.validate(password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("特殊字符");
    }

    @Test
    @DisplayName("密码验证失败 - 缺少大写字母")
    void shouldFailValidation_whenMissingUppercase() {
        // Given
        appProperties.getPassword().setRequireUppercase(true);
        String password = "test1234!";

        // When & Then
        assertThatThrownBy(() -> passwordValidator.validate(password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("大写字母");
    }

    @Test
    @DisplayName("密码验证失败 - 缺少小写字母")
    void shouldFailValidation_whenMissingLowercase() {
        // Given
        appProperties.getPassword().setRequireLowercase(true);
        String password = "TEST1234!";

        // When & Then
        assertThatThrownBy(() -> passwordValidator.validate(password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("小写字母");
    }

    @Test
    @DisplayName("密码验证失败 - 缺少数字")
    void shouldFailValidation_whenMissingDigit() {
        // Given
        appProperties.getPassword().setRequireDigit(true);
        String password = "TestTest!";

        // When & Then
        assertThatThrownBy(() -> passwordValidator.validate(password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("数字");
    }

    @Test
    @DisplayName("密码验证通过 - 大写字母非必需时")
    void shouldValidateSuccessfully_whenUppercaseNotRequired() {
        // Given
        appProperties.getPassword().setRequireUppercase(false);
        String password = "test1234!";

        // When
        boolean isValid = passwordValidator.validate(password);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("密码验证通过 - 小写字母非必需时")
    void shouldValidateSuccessfully_whenLowercaseNotRequired() {
        // Given
        appProperties.getPassword().setRequireLowercase(false);
        String password = "TEST1234!";

        // When
        boolean isValid = passwordValidator.validate(password);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("密码验证通过 - 数字非必需时")
    void shouldValidateSuccessfully_whenDigitNotRequired() {
        // Given
        appProperties.getPassword().setRequireDigit(false);
        String password = "TestTest!";

        // When
        boolean isValid = passwordValidator.validate(password);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("自定义最小长度配置生效")
    void shouldUseCustomMinLength_fromAppProperties() {
        // Given
        appProperties.getPassword().setMinLength(12);
        String password = "Test1234!"; // 9 个字符，小于 12

        // When & Then
        assertThatThrownBy(() -> passwordValidator.validate(password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("密码长度不能少于 12");
    }
}
