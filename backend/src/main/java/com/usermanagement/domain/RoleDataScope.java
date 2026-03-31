package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 角色自定义数据范围配置实体
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
@Table(name = "role_data_scope")
@EntityListeners(AuditingEntityListener.class)
public class RoleDataScope {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 角色 ID
     */
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    /**
     * 范围类型：DEPT=指定部门，USER=指定用户，SQL=自定义 SQL 条件
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", length = 20, nullable = false)
    private ScopeType scopeType;

    /**
     * 范围值：部门 ID 列表/用户 ID 列表/SQL 条件
     */
    @Column(name = "scope_value", columnDefinition = "TEXT", nullable = false)
    private String scopeValue;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 版本号
     */
    @Version
    @Column(name = "version")
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleDataScope that = (RoleDataScope) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RoleDataScope{" +
                "id='" + id + '\'' +
                ", roleId='" + roleId + '\'' +
                ", scopeType=" + scopeType +
                ", scopeValue='" + scopeValue + '\'' +
                '}';
    }

    /**
     * 范围类型枚举
     */
    public enum ScopeType {
        /**
         * 指定部门
         */
        DEPT,
        /**
         * 指定用户
         */
        USER,
        /**
         * 自定义 SQL 条件
         */
        SQL
    }
}
