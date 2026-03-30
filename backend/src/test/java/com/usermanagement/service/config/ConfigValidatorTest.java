package com.usermanagement.service.config;

import com.usermanagement.domain.DataType;
import com.usermanagement.domain.SystemConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ConfigValidator 单元测试
 */
class ConfigValidatorTest {

    private ConfigValidator configValidator;

    @BeforeEach
    void setUp() {
        configValidator = new ConfigValidator();
    }

    @Nested
    @DisplayName("字符串类型验证测试")
    class StringValidationTests {

        @Test
        @DisplayName("应该验证有效的字符串")
        void shouldValidateValidString() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "testValue");
        }

        @Test
        @DisplayName("空字符串应该通过验证（如果允许）")
        void shouldAllowEmptyString() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null, null, null);
            config.setConfigValue("defaultValue");

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, ""))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class);
        }

        @Test
        @DisplayName("超长字符串应该抛出异常")
        void shouldRejectTooLongString() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null, null, null);
            String longValue = "a".repeat(10001);

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, longValue))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("长度不能超过");
        }
    }

    @Nested
    @DisplayName("数字类型验证测试")
    class NumberValidationTests {

        @Test
        @DisplayName("应该验证有效的整数")
        void shouldValidateValidInteger() {
            // Given
            SystemConfig config = createConfig(DataType.NUMBER, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "123");
            configValidator.validateConfigValue(config, "0");
            configValidator.validateConfigValue(config, "-456");
        }

        @Test
        @DisplayName("应该验证有效的小数")
        void shouldValidateValidDecimal() {
            // Given
            SystemConfig config = createConfig(DataType.NUMBER, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "123.45");
            configValidator.validateConfigValue(config, "-0.01");
        }

        @Test
        @DisplayName("无效数字应该抛出异常")
        void shouldRejectInvalidNumber() {
            // Given
            SystemConfig config = createConfig(DataType.NUMBER, null, null, null, null);

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "abc"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("必须为数字");

            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "12.34.56"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class);
        }

        @Test
        @DisplayName("应该验证数值范围")
        void shouldValidateNumberRange() {
            // Given
            SystemConfig config = createConfig(DataType.NUMBER, "1", "100", null, null);

            // When & Then
            configValidator.validateConfigValue(config, "50");
            configValidator.validateConfigValue(config, "1");
            configValidator.validateConfigValue(config, "100");

            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "0"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("不能小于");

            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "101"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("不能大于");
        }
    }

    @Nested
    @DisplayName("布尔类型验证测试")
    class BooleanValidationTests {

        @Test
        @DisplayName("应该验证 true 值")
        void shouldValidateTrue() {
            // Given
            SystemConfig config = createConfig(DataType.BOOLEAN, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "true");
            configValidator.validateConfigValue(config, "TRUE");
            configValidator.validateConfigValue(config, "True");
        }

        @Test
        @DisplayName("应该验证 false 值")
        void shouldValidateFalse() {
            // Given
            SystemConfig config = createConfig(DataType.BOOLEAN, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "false");
            configValidator.validateConfigValue(config, "FALSE");
            configValidator.validateConfigValue(config, "False");
        }

        @Test
        @DisplayName("无效布尔值应该抛出异常")
        void shouldRejectInvalidBoolean() {
            // Given
            SystemConfig config = createConfig(DataType.BOOLEAN, null, null, null, null);

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "yes"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("必须为 true 或 false");

            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "1"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class);
        }
    }

    @Nested
    @DisplayName("JSON 类型验证测试")
    class JsonValidationTests {

        @Test
        @DisplayName("应该验证有效的 JSON 对象")
        void shouldValidateValidJsonObject() {
            // Given
            SystemConfig config = createConfig(DataType.JSON, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "{\"key\":\"value\"}");
            configValidator.validateConfigValue(config, "{\"nested\":{\"key\":\"value\"}}");
        }

        @Test
        @DisplayName("应该验证有效的 JSON 数组")
        void shouldValidateValidJsonArray() {
            // Given
            SystemConfig config = createConfig(DataType.JSON, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "[1,2,3]");
            configValidator.validateConfigValue(config, "[{\"id\":1},{\"id\":2}]");
        }

        @Test
        @DisplayName("无效 JSON 应该抛出异常")
        void shouldRejectInvalidJson() {
            // Given
            SystemConfig config = createConfig(DataType.JSON, null, null, null, null);

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "not json"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("必须为有效的 JSON 格式");

            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "{key:value}"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class);
        }
    }

    @Nested
    @DisplayName("数组类型验证测试")
    class ArrayValidationTests {

        @Test
        @DisplayName("应该验证有效的数组格式")
        void shouldValidateValidArray() {
            // Given
            SystemConfig config = createConfig(DataType.ARRAY, null, null, null, null);

            // When & Then
            configValidator.validateConfigValue(config, "[\"a\",\"b\",\"c\"]");
            configValidator.validateConfigValue(config, "[1,2,3]");
        }

        @Test
        @DisplayName("无效数组应该抛出异常")
        void shouldRejectInvalidArray() {
            // Given
            SystemConfig config = createConfig(DataType.ARRAY, null, null, null, null);

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "not array"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("必须为有效的数组格式");
        }
    }

    @Nested
    @DisplayName("正则表达式验证测试")
    class RegexValidationTests {

        @Test
        @DisplayName("应该验证符合正则的值")
        void shouldValidateMatchingRegex() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null, "^[a-z]+$", null);

            // When & Then
            configValidator.validateConfigValue(config, "abc");
            configValidator.validateConfigValue(config, "test");
        }

        @Test
        @DisplayName("不符合正则应该抛出异常")
        void shouldRejectNonMatchingRegex() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null, "^[0-9]+$", null);

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "abc"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("不符合正则表达式");
        }

        @Test
        @DisplayName("应该验证邮箱格式")
        void shouldValidateEmailFormat() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null,
                    "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", null);

            // When & Then
            configValidator.validateConfigValue(config, "test@example.com");
            configValidator.validateConfigValue(config, "user.name@domain.org");

            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "invalid-email"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class);
        }
    }

    @Nested
    @DisplayName("可选值验证测试")
    class OptionsValidationTests {

        @Test
        @DisplayName("应该验证在可选值范围内的值")
        void shouldValidateValueInOptions() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null, null,
                    Map.of("option1", "选项 1", "option2", "选项 2", "option3", "选项 3"));

            // When & Then
            configValidator.validateConfigValue(config, "option1");
            configValidator.validateConfigValue(config, "option2");
            configValidator.validateConfigValue(config, "option3");
        }

        @Test
        @DisplayName("不在可选值范围内应该抛出异常")
        void shouldRejectValueNotInOptions() {
            // Given
            SystemConfig config = createConfig(DataType.STRING, null, null, null,
                    Map.of("option1", "选项 1", "option2", "选项 2"));

            // When & Then
            assertThatThrownBy(() -> configValidator.validateConfigValue(config, "invalid"))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class)
                    .hasMessageContaining("必须在可选值范围内");
        }
    }

    @Nested
    @DisplayName("配置键验证测试")
    class ConfigKeyValidationTests {

        @Test
        @DisplayName("应该验证有效的配置键")
        void shouldValidateValidConfigKey() {
            // Given & When & Then
            assertThat(configValidator.isValidConfigKey("mail.smtp.host")).isTrue();
            assertThat(configValidator.isValidConfigKey("auth.password.min_length")).isTrue();
            assertThat(configValidator.isValidConfigKey("system.name")).isTrue();
            assertThat(configValidator.isValidConfigKey("feature_1")).isTrue();
        }

        @Test
        @DisplayName("无效配置键应该返回 false")
        void shouldRejectInvalidConfigKey() {
            // Given & When & Then
            assertThat(configValidator.isValidConfigKey(null)).isFalse();
            assertThat(configValidator.isValidConfigKey("")).isFalse();
            assertThat(configValidator.isValidConfigKey("123invalid")).isFalse();
            assertThat(configValidator.isValidConfigKey("invalid-key")).isFalse();
            assertThat(configValidator.isValidConfigKey("invalid key")).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"valid.key", "valid_key", "valid123", "a.b.c"})
        @DisplayName("应该接受有效的配置键格式")
        void shouldAcceptValidConfigKeyFormats(String configKey) {
            // When & Then
            assertThat(configValidator.isValidConfigKey(configKey)).isTrue();
        }
    }

    // 辅助方法
    private SystemConfig createConfig(DataType dataType, String minValue, String maxValue,
                                       String regexPattern, Map<String, Object> options) {
        return SystemConfig.builder()
                .configKey("test.config")
                .dataType(dataType)
                .minValue(minValue)
                .maxValue(maxValue)
                .regexPattern(regexPattern)
                .options(options)
                .configValue("default")
                .build();
    }
}
