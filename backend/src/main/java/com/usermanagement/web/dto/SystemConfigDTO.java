package com.usermanagement.web.dto;

import com.usermanagement.domain.ConfigStatus;
import com.usermanagement.domain.ConfigType;
import com.usermanagement.domain.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 系统配置响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {

    /**
     * 配置 ID
     */
    private String id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值（敏感配置会被脱敏）
     */
    private String configValue;

    /**
     * 配置类型
     */
    private ConfigType configType;

    /**
     * 配置分类
     */
    private ConfigType category;

    /**
     * 数据类型
     */
    private DataType dataType;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否加密
     */
    private Boolean isEncrypted;

    /**
     * 是否敏感
     */
    private Boolean isSensitive;

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
     * 版本号
     */
    private Integer version;

    /**
     * 配置状态
     */
    private ConfigStatus status;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 最后更新用户 ID
     */
    private String updatedBy;
}
