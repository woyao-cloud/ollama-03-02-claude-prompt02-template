package com.usermanagement.service.config;

import com.usermanagement.domain.DataType;
import com.usermanagement.domain.SystemConfig;
import com.usermanagement.web.dto.ConfigCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 配置验证器 - 验证配置值的合法性
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class ConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);

    /**
     * 验证配置值
     */
    public void validateConfigValue(SystemConfig config, String value) {
        if (value == null || value.trim().isEmpty()) {
            if (config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
                throw new ConfigValidationException(config.getConfigKey(), "配置值不能为空");
            }
            return;
        }

        // 根据数据类型验证
        switch (config.getDataType()) {
            case NUMBER:
                validateNumber(config, value);
                break;
            case BOOLEAN:
                validateBoolean(config, value);
                break;
            case JSON:
                validateJson(config, value);
                break;
            case ARRAY:
                validateArray(config, value);
                break;
            default:
                validateString(config, value);
        }

        // 验证范围（仅数字类型）
        if (config.getDataType() == DataType.NUMBER) {
            if (config.getMinValue() != null || config.getMaxValue() != null) {
                validateRange(config, value);
            }
        }

        // 验证正则表达式
        if (config.getRegexPattern() != null && !config.getRegexPattern().trim().isEmpty()) {
            validateRegex(config, value);
        }

        // 验证可选值
        if (config.getOptions() != null && !config.getOptions().isEmpty()) {
            validateOptions(config, value);
        }
    }

    /**
     * 验证创建请求
     */
    public void validateCreateRequest(ConfigCreateRequest request) {
        // 验证配置键格式
        if (!isValidConfigKey(request.getConfigKey())) {
            throw new ConfigValidationException(request.getConfigKey(),
                "配置键只能包含字母、数字、下划线和点，且必须以字母开头");
        }

        // 验证数据类型与配置值匹配
        SystemConfig tempConfig = SystemConfig.builder()
            .configKey(request.getConfigKey())
            .dataType(request.getDataType())
            .minValue(request.getMinValue())
            .maxValue(request.getMaxValue())
            .regexPattern(request.getRegexPattern())
            .options(request.getOptions())
            .build();

        validateConfigValue(tempConfig, request.getConfigValue());
    }

    /**
     * 验证配置键格式
     */
    private boolean isValidConfigKey(String configKey) {
        if (configKey == null || configKey.trim().isEmpty()) {
            return false;
        }
        // 配置键格式：以字母开头，只能包含字母、数字、下划线和点
        return configKey.matches("^[a-zA-Z][a-zA-Z0-9_.]*$");
    }

    /**
     * 验证字符串类型
     */
    private void validateString(SystemConfig config, String value) {
        // 字符串类型基本都有效，只需检查长度
        if (value.length() > 10000) {
            throw new ConfigValidationException(config.getConfigKey(),
                "配置值长度不能超过 10000 字符");
        }
    }

    /**
     * 验证数字类型
     */
    private void validateNumber(SystemConfig config, String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ConfigValidationException(config.getConfigKey(),
                "配置值必须为数字格式");
        }
    }

    /**
     * 验证布尔类型
     */
    private void validateBoolean(SystemConfig config, String value) {
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new ConfigValidationException(config.getConfigKey(),
                "配置值必须为 true 或 false");
        }
    }

    /**
     * 验证 JSON 类型
     */
    private void validateJson(SystemConfig config, String value) {
        try {
            // 简单的 JSON 验证 - 尝试解析
            if (!value.trim().startsWith("{") && !value.trim().startsWith("[")) {
                throw new ConfigValidationException(config.getConfigKey(),
                    "配置值必须为有效的 JSON 格式");
            }
            // 更严格的验证可以引入 Jackson ObjectMapper
        } catch (Exception e) {
            throw new ConfigValidationException(config.getConfigKey(),
                "配置值必须为有效的 JSON 格式：" + e.getMessage());
        }
    }

    /**
     * 验证数组类型
     */
    private void validateArray(SystemConfig config, String value) {
        try {
            if (!value.trim().startsWith("[")) {
                throw new ConfigValidationException(config.getConfigKey(),
                    "配置值必须为有效的数组格式（JSON 数组）");
            }
        } catch (Exception e) {
            throw new ConfigValidationException(config.getConfigKey(),
                "配置值必须为有效的数组格式：" + e.getMessage());
        }
    }

    /**
     * 验证数值范围
     */
    private void validateRange(SystemConfig config, String value) {
        double numValue;
        try {
            numValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ConfigValidationException(config.getConfigKey(),
                "数值格式无效");
        }

        if (config.getMinValue() != null) {
            try {
                double minValue = Double.parseDouble(config.getMinValue());
                if (numValue < minValue) {
                    throw new ConfigValidationException(config.getConfigKey(),
                        "配置值不能小于 " + minValue);
                }
            } catch (NumberFormatException e) {
                log.warn("配置最小值格式无效：{}", config.getMinValue());
            }
        }

        if (config.getMaxValue() != null) {
            try {
                double maxValue = Double.parseDouble(config.getMaxValue());
                if (numValue > maxValue) {
                    throw new ConfigValidationException(config.getConfigKey(),
                        "配置值不能大于 " + maxValue);
                }
            } catch (NumberFormatException e) {
                log.warn("配置最大值格式无效：{}", config.getMaxValue());
            }
        }
    }

    /**
     * 验证正则表达式
     */
    private void validateRegex(SystemConfig config, String value) {
        String regex = config.getRegexPattern();
        if (regex == null || regex.trim().isEmpty()) {
            return;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            if (!pattern.matcher(value).matches()) {
                throw new ConfigValidationException(config.getConfigKey(),
                    "配置值不符合正则表达式：" + regex);
            }
        } catch (PatternSyntaxException e) {
            log.error("配置正则表达式语法错误：{}", regex, e);
            throw new ConfigValidationException(config.getConfigKey(),
                "配置正则表达式语法错误");
        }
    }

    /**
     * 验证可选值
     */
    private void validateOptions(SystemConfig config, String value) {
        if (config.getOptions() == null || config.getOptions().isEmpty()) {
            return;
        }

        boolean matched = config.getOptions().keySet().stream()
            .anyMatch(key -> key.equals(value));

        if (!matched) {
            throw new ConfigValidationException(config.getConfigKey(),
                "配置值必须在可选值范围内：" + config.getOptions().keySet());
        }
    }

    /**
     * 配置验证异常
     */
    public static class ConfigValidationException extends RuntimeException {
        public ConfigValidationException(String configKey, String message) {
            super("配置验证失败 [" + configKey + "]: " + message);
        }
    }
}
