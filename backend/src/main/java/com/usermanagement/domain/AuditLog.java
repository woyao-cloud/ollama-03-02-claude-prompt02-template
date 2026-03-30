package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 审计日志实体 - 记录系统操作日志
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_operation_type", columnList = "operation_type"),
    @Index(name = "idx_audit_resource_type", columnList = "resource_type"),
    @Index(name = "idx_audit_created_at", columnList = "created_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * 操作用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * 操作用户邮箱
     */
    @Column(name = "user_email", length = 255)
    private String userEmail;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", length = 50, nullable = false)
    private AuditOperationType operationType;

    /**
     * 资源类型 (如：USER, ROLE, PERMISSION, DEPARTMENT)
     */
    @Column(name = "resource_type", length = 50, nullable = false)
    private String resourceType;

    /**
     * 资源 ID
     */
    @Column(name = "resource_id")
    private UUID resourceId;

    /**
     * 操作描述
     */
    @Column(name = "operation_description", length = 500)
    private String operationDescription;

    /**
     * 操作前的数据 (JSON)
     */
    @Column(name = "old_value", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> oldValue;

    /**
     * 操作后的数据 (JSON)
     */
    @Column(name = "new_value", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> newValue;

    /**
     * 客户端 IP 地址
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 客户端 User-Agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 操作结果 (SUCCESS/FAILURE)
     */
    @Column(name = "operation_result", length = 20, nullable = false)
    @Builder.Default
    private String operationResult = "SUCCESS";

    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
