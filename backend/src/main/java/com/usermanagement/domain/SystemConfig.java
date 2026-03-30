package com.usermanagement.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 系统配置实体 - 支持动态配置管理
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "system_config", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key"),
    @Index(name = "idx_config_type", columnList = "config_type"),
    @Index(name = "idx_config_category", columnList = "category"),
    @Index(name = "idx_config_status", columnList = "status")
})
public class SystemConfig extends BaseEntity {

    /**
     * 配置键 - 唯一标识
     */
    @Column(name = "config_key", length = 100, nullable = false, unique = true)
    private String configKey;

    /**
     * 配置值
     */
    @Column(name = "config_value", columnDefinition = "TEXT", nullable = false)
    private String configValue;

    /**
     * 配置类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", length = 50, nullable = false)
    @Builder.Default
    private ConfigType configType = ConfigType.SYSTEM;

    /**
     * 配置分类（与 config_type 兼容，用于更细粒度分类）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    @Builder.Default
    private ConfigType category = ConfigType.SYSTEM;

    /**
     * 配置描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 数据类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 20, nullable = false)
    @Builder.Default
    private DataType dataType = DataType.STRING;

    /**
     * 是否加密存储
     */
    @Column(name = "is_encrypted", nullable = false)
    @Builder.Default
    private Boolean isEncrypted = false;

    /**
     * 是否敏感配置
     */
    @Column(name = "is_sensitive", nullable = false)
    @Builder.Default
    private Boolean isSensitive = false;

    /**
     * 默认值
     */
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    /**
     * 最小值（用于数值类型验证）
     */
    @Column(name = "min_value", length = 50)
    private String minValue;

    /**
     * 最大值（用于数值类型验证）
     */
    @Column(name = "max_value", length = 50)
    private String maxValue;

    /**
     * 正则表达式（用于字符串格式验证）
     */
    @Column(name = "regex_pattern", length = 200)
    private String regexPattern;

    /**
     * 可选值列表（用于下拉选择）
     */
    @Column(name = "options", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> options;

    /**
     * 配置版本号 - 乐观锁
     */
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * 配置状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private ConfigStatus status = ConfigStatus.ACTIVE;

    /**
     * 最后更新用户 ID
     */
    @Column(name = "updated_by")
    private UUID updatedBy;

    /**
     * 判断是否为默认配置
     */
    public boolean isDefault() {
        return this.configValue == null || this.configValue.equals(this.defaultValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SystemConfig that = (SystemConfig) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "id='" + getId() + '\'' +
                ", configKey='" + configKey + '\'' +
                ", configType=" + configType +
                ", dataType=" + dataType +
                ", status=" + status +
                '}';
    }
}
