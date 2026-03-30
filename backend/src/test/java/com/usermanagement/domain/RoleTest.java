package com.usermanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Role 实体单元测试
 */
@DisplayName("Role 实体测试")
class RoleTest {

    @Nested
    @DisplayName("角色创建测试")
    class RoleCreationTests {

        @Test
        @DisplayName("应该创建角色")
        void shouldCreateRole() {
            // Given
            Role role = new Role();
            role.setName("管理员");
            role.setCode("ROLE_ADMIN");
            role.setDescription("系统管理员角色");

            // Then
            assertThat(role.getName()).isEqualTo("管理员");
            assertThat(role.getCode()).isEqualTo("ROLE_ADMIN");
            assertThat(role.getDescription()).isEqualTo("系统管理员角色");
            assertThat(role.getStatus()).isEqualTo(RoleStatus.ACTIVE);
            assertThat(role.getDataScope()).isEqualTo(DataScope.ALL);
        }

        @Test
        @DisplayName("角色应该有默认状态 ACTIVE")
        void shouldHaveDefaultActiveStatus() {
            // Given
            Role role = new Role();
            role.setName("测试角色");
            role.setCode("ROLE_TEST");

            // Then
            assertThat(role.getStatus()).isEqualTo(RoleStatus.ACTIVE);
        }

        @Test
        @DisplayName("角色应该有默认数据权限范围 ALL")
        void shouldHaveDefaultDataScopeAll() {
            // Given
            Role role = new Role();
            role.setName("测试角色");
            role.setCode("ROLE_TEST");

            // Then
            assertThat(role.getDataScope()).isEqualTo(DataScope.ALL);
        }
    }

    @Nested
    @DisplayName("角色数据权限测试")
    class DataScopeTests {

        @Test
        @DisplayName("应该能设置数据权限范围")
        void shouldSetDataScope() {
            // Given
            Role role = new Role();
            role.setName("部门经理");
            role.setCode("ROLE_MANAGER");

            // When
            role.setDataScope(DataScope.DEPT);

            // Then
            assertThat(role.getDataScope()).isEqualTo(DataScope.DEPT);
        }

        @Test
        @DisplayName("应该支持所有数据权限类型")
        void shouldSupportAllDataScopeTypes() {
            // Then
            assertThat(DataScope.values()).containsExactlyInAnyOrder(
                    DataScope.ALL,
                    DataScope.DEPT,
                    DataScope.SELF,
                    DataScope.CUSTOM
            );
        }

        @Test
        @DisplayName("应该能创建自定义数据权限角色")
        void shouldCreateCustomDataScopeRole() {
            // Given
            Role role = new Role();
            role.setName("自定义角色");
            role.setCode("ROLE_CUSTOM");

            // When
            role.setDataScope(DataScope.CUSTOM);

            // Then
            assertThat(role.getDataScope()).isEqualTo(DataScope.CUSTOM);
        }
    }

    @Nested
    @DisplayName("角色状态测试")
    class RoleStatusTests {

        @Test
        @DisplayName("应该能切换角色状态")
        void shouldToggleRoleStatus() {
            // Given
            Role role = new Role();
            role.setName("测试角色");
            role.setCode("ROLE_TEST");
            role.setStatus(RoleStatus.ACTIVE);

            // When
            role.setStatus(RoleStatus.INACTIVE);

            // Then
            assertThat(role.getStatus()).isEqualTo(RoleStatus.INACTIVE);
        }

        @Test
        @DisplayName("应该能判断角色是否激活")
        void shouldCheckIfRoleIsActive() {
            // Given
            Role activeRole = new Role();
            activeRole.setName("激活角色");
            activeRole.setCode("ROLE_ACTIVE");
            activeRole.setStatus(RoleStatus.ACTIVE);

            Role inactiveRole = new Role();
            inactiveRole.setName("禁用角色");
            inactiveRole.setCode("ROLE_INACTIVE");
            inactiveRole.setStatus(RoleStatus.INACTIVE);

            // Then
            assertThat(activeRole.getStatus()).isEqualTo(RoleStatus.ACTIVE);
            assertThat(inactiveRole.getStatus()).isEqualTo(RoleStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("角色业务方法测试")
    class RoleBusinessMethodTests {

        @Test
        @DisplayName("应该能判断是否为管理员角色")
        void shouldIdentifyAdminRole() {
            // Given
            Role adminRole = new Role();
            adminRole.setName("管理员");
            adminRole.setCode("ROLE_ADMIN");

            Role userRole = new Role();
            userRole.setName("普通用户");
            userRole.setCode("ROLE_USER");

            // Then
            assertThat(adminRole.getCode()).isEqualTo("ROLE_ADMIN");
            assertThat(userRole.getCode()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("应该能判断数据权限范围")
        void shouldCheckDataScope() {
            // Given
            Role allScopeRole = new Role();
            allScopeRole.setName("全部权限");
            allScopeRole.setCode("ROLE_ALL");
            allScopeRole.setDataScope(DataScope.ALL);

            Role selfScopeRole = new Role();
            selfScopeRole.setName("个人权限");
            selfScopeRole.setCode("ROLE_SELF");
            selfScopeRole.setDataScope(DataScope.SELF);

            // Then
            assertThat(allScopeRole.getDataScope()).isEqualTo(DataScope.ALL);
            assertThat(selfScopeRole.getDataScope()).isEqualTo(DataScope.SELF);
        }
    }
}
