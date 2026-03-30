package com.usermanagement.web.dto;

import com.usermanagement.domain.ConfigStatus;
import com.usermanagement.domain.ConfigType;
import com.usermanagement.domain.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 创建配置请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCreateRequest {

    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    private String configKey;

    /**
     * 配置值
     */
    @NotBlank(message = "配置值不能为空")
    private String configValue;

    /**
     * 配置类型
     */
    @NotNull(message = "配置类型不能为空")
    private ConfigType configType;

    /**
     * 配置分类
     */
    @NotNull(message = "配置分类不能为空")
    private ConfigType category;

    /**
     * 数据类型
     */
    @NotNull(message = "数据类型不能为空")
    private DataType dataType;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否加密
     */
    @Builder.Default
    private Boolean isEncrypted = false;

    /**
     * 是否敏感
     */
    @Builder.Default
    private Boolean isSensitive = false;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 最小值
     */
    private String minValue;

    /**
     * 最大值
     */
    private String maxValue;

    /**
     * 正则表达式
     */
    private String regexPattern;

    /**
     * 可选值列表
     */
    private Map<String, Object> options;

    /**
     * 配置状态
     */
    @Builder.Default
    private ConfigStatus status = ConfigStatus.ACTIVE;
}
