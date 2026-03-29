package com.usermanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRole 实体单元测试
 */
@DisplayName("UserRole 实体测试")
class UserRoleTest {

    @Nested
    @DisplayName("用户角色关联创建测试")
    class UserRoleCreationTests {

        @Test
        @DisplayName("应该创建用户角色关联")
        void shouldCreateUserRole() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");

            Role role = new Role();
            role.setName("管理员");
            role.setCode("ROLE_ADMIN");

            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());

            // Then
            assertThat(userRole.getUserId()).isEqualTo(user.getId());
            assertThat(userRole.getRoleId()).isEqualTo(role.getId());
        }

        @Test
        @DisplayName("用户角色关联应该有创建时间")
        void shouldHaveCreatedAt() {
            // Given
            UserRole userRole = new UserRole();
            userRole.setUserId(UUID.randomUUID());
            userRole.setRoleId(UUID.randomUUID());

            // Then
            assertThat(userRole.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("用户角色关联业务测试")
    class UserRoleBusinessTests {

        @Test
        @DisplayName("应该能判断是否相等")
        void shouldCheckEquality() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();

            UserRole userRole1 = new UserRole();
            userRole1.setUserId(userId);
            userRole1.setRoleId(roleId);

            UserRole userRole2 = new UserRole();
            userRole2.setUserId(userId);
            userRole2.setRoleId(roleId);

            // Then
            assertThat(userRole1).usingRecursiveComparison()
                    .ignoringFields("createdAt")
                    .isEqualTo(userRole2);
        }

        @Test
        @DisplayName("应该能生成正确的 toString")
        void shouldGenerateToString() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();

            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);

            // Then
            String str = userRole.toString();
            assertThat(str).contains("userId");
            assertThat(str).contains("roleId");
        }
    }
}
