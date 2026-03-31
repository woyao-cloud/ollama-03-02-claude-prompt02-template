package com.usermanagement.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * UserRole 复合主键类
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserRoleId implements Serializable {

    /**
     * 用户 ID
     */
    private UUID userId;

    /**
     * 角色 ID
     */
    private UUID roleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }

    @Override
    public String toString() {
        return "UserRoleId{" +
                "userId='" + userId + '\'' +
                ", roleId='" + roleId + '\'' +
                '}';
    }
}
