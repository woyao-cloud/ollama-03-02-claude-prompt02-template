package com.usermanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RolePermission 实体单元测试
 */
@DisplayName("RolePermission 实体测试")
class RolePermissionTest {

    @Nested
    @DisplayName("角色权限关联创建测试")
    class RolePermissionCreationTests {

        @Test
        @DisplayName("应该创建角色权限关联")
        void shouldCreateRolePermission() {
            // Given
            Role role = new Role();
            role.setName("管理员");
            role.setCode("ROLE_ADMIN");

            Permission permission = new Permission();
            permission.setName("创建用户");
            permission.setCode("user:create");
            permission.setType(PermissionType.ACTION);
            permission.setResource("user");

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());

            // Then
            assertThat(rolePermission.getRoleId()).isEqualTo(role.getId());
            assertThat(rolePermission.getPermissionId()).isEqualTo(permission.getId());
        }

        @Test
        @DisplayName("角色权限关联应该有创建时间")
        void shouldHaveCreatedAt() {
            // Given
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(UUID.randomUUID());
            rolePermission.setPermissionId(UUID.randomUUID());

            // Then
            assertThat(rolePermission.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("角色权限关联业务测试")
    class RolePermissionBusinessTests {

        @Test
        @DisplayName("应该能判断是否相等")
        void shouldCheckEquality() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID permissionId = UUID.randomUUID();

            RolePermission rp1 = new RolePermission();
            rp1.setRoleId(roleId);
            rp1.setPermissionId(permissionId);

            RolePermission rp2 = new RolePermission();
            rp2.setRoleId(roleId);
            rp2.setPermissionId(permissionId);

            // Then
            assertThat(rp1).usingRecursiveComparison()
                    .ignoringFields("createdAt")
                    .isEqualTo(rp2);
        }

        @Test
        @DisplayName("应该能生成正确的 toString")
        void shouldGenerateToString() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID permissionId = UUID.randomUUID();

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);

            // Then
            String str = rolePermission.toString();
            assertThat(str).contains("roleId");
            assertThat(str).contains("permissionId");
        }
    }
}
