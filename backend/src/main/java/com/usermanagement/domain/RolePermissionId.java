package com.usermanagement.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * RolePermission 复合主键类
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RolePermissionId implements Serializable {

    /**
     * 角色 ID
     */
    private UUID roleId;

    /**
     * 权限 ID
     */
    private UUID permissionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePermissionId that = (RolePermissionId) o;
        return Objects.equals(roleId, that.roleId) &&
                Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }

    @Override
    public String toString() {
        return "RolePermissionId{" +
                "roleId='" + roleId + '\'' +
                ", permissionId='" + permissionId + '\'' +
                '}';
    }
}
