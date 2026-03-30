package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 配置变更历史实体 - 记录配置变更审计
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
@Table(name = "config_history", indexes = {
    @Index(name = "idx_config_hist_config", columnList = "config_id"),
    @Index(name = "idx_config_hist_time", columnList = "changed_at"),
    @Index(name = "idx_config_hist_user", columnList = "changed_by")
})
public class ConfigHistory extends BaseEntity {

    /**
     * 配置 ID
     */
    @Column(name = "config_id", nullable = false)
    private UUID configId;

    /**
     * 旧值
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * 新值
     */
    @Column(name = "new_value", columnDefinition = "TEXT", nullable = false)
    private String newValue;

    /**
     * 变更类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 20, nullable = false)
    private ChangeType changeType;

    /**
     * 变更人 ID
     */
    @Column(name = "changed_by")
    private UUID changedBy;

    /**
     * 变更人邮箱
     */
    @Column(name = "changed_by_email", length = 255)
    private String changedByEmail;

    /**
     * 变更原因
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * 配置键快照（便于查询）
     */
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConfigHistory that = (ConfigHistory) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return "ConfigHistory{" +
                "id='" + getId() + '\'' +
                ", configKey='" + configKey + '\'' +
                ", changeType=" + changeType +
                ", changedAt=" + getCreatedAt() +
                '}';
    }
}
