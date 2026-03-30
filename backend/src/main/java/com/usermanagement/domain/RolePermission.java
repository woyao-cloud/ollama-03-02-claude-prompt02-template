package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 角色权限关联实体
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
@Table(name = "role_permission")
@EntityListeners(AuditingEntityListener.class)
public class RolePermission {

    /**
     * 角色 ID - 复合主键
     */
    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    /**
     * 权限 ID - 复合主键
     */
    @Id
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePermission that = (RolePermission) o;
        return Objects.equals(roleId, that.roleId) &&
                Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }

    @Override
    public String toString() {
        return "RolePermission{" +
                "roleId='" + roleId + '\'' +
                ", permissionId='" + permissionId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
