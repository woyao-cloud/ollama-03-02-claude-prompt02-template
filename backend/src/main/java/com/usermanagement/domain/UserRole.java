package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 用户角色关联实体
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
@Table(name = "user_role")
@EntityListeners(AuditingEntityListener.class)
public class UserRole {

    /**
     * 用户 ID - 复合主键
     */
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * 角色 ID - 复合主键
     */
    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

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
        UserRole userRole = (UserRole) o;
        return Objects.equals(userId, userRole.userId) &&
                Objects.equals(roleId, userRole.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "userId='" + userId + '\'' +
                ", roleId='" + roleId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
