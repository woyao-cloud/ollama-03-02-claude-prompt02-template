package com.usermanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Permission 实体单元测试
 */
@DisplayName("Permission 实体测试")
class PermissionTest {

    @Nested
    @DisplayName("权限创建测试")
    class PermissionCreationTests {

        @Test
        @DisplayName("应该创建权限")
        void shouldCreatePermission() {
            // Given
            Permission permission = new Permission();
            permission.setName("创建用户");
            permission.setCode("user:create");
            permission.setType(PermissionType.ACTION);
            permission.setResource("user");
            permission.setAction("create");

            // Then
            assertThat(permission.getName()).isEqualTo("创建用户");
            assertThat(permission.getCode()).isEqualTo("user:create");
            assertThat(permission.getType()).isEqualTo(PermissionType.ACTION);
            assertThat(permission.getResource()).isEqualTo("user");
            assertThat(permission.getAction()).isEqualTo("create");
            assertThat(permission.getStatus()).isEqualTo(PermissionStatus.ACTIVE);
        }

        @Test
        @DisplayName("权限应该有默认状态 ACTIVE")
        void shouldHaveDefaultActiveStatus() {
            // Given
            Permission permission = new Permission();
            permission.setName("测试权限");
            permission.setCode("test:read");
            permission.setType(PermissionType.ACTION);
            permission.setResource("test");

            // Then
            assertThat(permission.getStatus()).isEqualTo(PermissionStatus.ACTIVE);
        }

        @Test
        @DisplayName("应该创建菜单权限")
        void shouldCreateMenuPermission() {
            // Given
            Permission menu = new Permission();
            menu.setName("用户管理");
            menu.setCode("user:menu");
            menu.setType(PermissionType.MENU);
            menu.setResource("user");
            menu.setRoute("/users");
            menu.setIcon("user-icon");

            // Then
            assertThat(menu.getType()).isEqualTo(PermissionType.MENU);
            assertThat(menu.getRoute()).isEqualTo("/users");
            assertThat(menu.getIcon()).isEqualTo("user-icon");
        }
    }

    @Nested
    @DisplayName("权限类型测试")
    class PermissionTypeTests {

        @Test
        @DisplayName("应该支持所有权限类型")
        void shouldSupportAllPermissionTypes() {
            // Then
            assertThat(PermissionType.values()).containsExactlyInAnyOrder(
                    PermissionType.MENU,
                    PermissionType.ACTION,
                    PermissionType.FIELD,
                    PermissionType.DATA
            );
        }

        @Test
        @DisplayName("应该能创建字段权限")
        void shouldCreateFieldPermission() {
            // Given
            Permission field = new Permission();
            field.setName("查看手机号");
            field.setCode("user:phone:view");
            field.setType(PermissionType.FIELD);
            field.setResource("user");
            field.setAction("read");

            // Then
            assertThat(field.getType()).isEqualTo(PermissionType.FIELD);
        }

        @Test
        @DisplayName("应该能创建数据权限")
        void shouldCreateDataPermission() {
            // Given
            Permission data = new Permission();
            data.setName("部门数据权限");
            data.setCode("dept:data");
            data.setType(PermissionType.DATA);
            data.setResource("department");

            // Then
            assertThat(data.getType()).isEqualTo(PermissionType.DATA);
        }
    }

    @Nested
    @DisplayName("权限树测试")
    class PermissionTreeTests {

        @Test
        @DisplayName("权限应该能设置父权限")
        void shouldSetParentPermission() {
            // Given
            Permission parent = new Permission();
            parent.setName("用户管理");
            parent.setCode("user:menu");
            parent.setType(PermissionType.MENU);
            parent.setResource("user");

            Permission child = new Permission();
            child.setName("创建用户");
            child.setCode("user:create");
            child.setType(PermissionType.ACTION);
            child.setResource("user");
            child.setAction("create");

            // When
            child.setParentId(parent.getId());

            // Then
            assertThat(child.getParentId()).isEqualTo(parent.getId());
        }

        @Test
        @DisplayName("应该能判断是否为叶子权限")
        void shouldIdentifyLeafPermission() {
            // Given
            Permission menu = new Permission();
            menu.setName("用户管理");
            menu.setCode("user:menu");
            menu.setType(PermissionType.MENU);
            menu.setResource("user");

            Permission action = new Permission();
            action.setName("创建用户");
            action.setCode("user:create");
            action.setType(PermissionType.ACTION);
            action.setResource("user");
            action.setAction("create");
            action.setParentId(menu.getId());

            // Then
            assertThat(menu.getParentId()).isNull();
            assertThat(action.getParentId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("权限状态测试")
    class PermissionStatusTests {

        @Test
        @DisplayName("应该能切换权限状态")
        void shouldTogglePermissionStatus() {
            // Given
            Permission permission = new Permission();
            permission.setName("测试权限");
            permission.setCode("test:read");
            permission.setType(PermissionType.ACTION);
            permission.setResource("test");
            permission.setStatus(PermissionStatus.ACTIVE);

            // When
            permission.setStatus(PermissionStatus.INACTIVE);

            // Then
            assertThat(permission.getStatus()).isEqualTo(PermissionStatus.INACTIVE);
        }

        @Test
        @DisplayName("应该能判断权限是否激活")
        void shouldCheckIfPermissionIsActive() {
            // Given
            Permission activePerm = new Permission();
            activePerm.setName("激活权限");
            activePerm.setCode("active:read");
            activePerm.setType(PermissionType.ACTION);
            activePerm.setResource("active");
            activePerm.setStatus(PermissionStatus.ACTIVE);

            Permission inactivePerm = new Permission();
            inactivePerm.setName("禁用权限");
            inactivePerm.setCode("inactive:read");
            inactivePerm.setType(PermissionType.ACTION);
            inactivePerm.setResource("inactive");
            inactivePerm.setStatus(PermissionStatus.INACTIVE);

            // Then
            assertThat(activePerm.getStatus()).isEqualTo(PermissionStatus.ACTIVE);
            assertThat(inactivePerm.getStatus()).isEqualTo(PermissionStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("权限排序测试")
    class PermissionSortTests {

        @Test
        @DisplayName("权限应该能设置排序号")
        void shouldSetSortOrder() {
            // Given
            Permission perm1 = new Permission();
            perm1.setName("权限 1");
            perm1.setCode("perm:1");
            perm1.setType(PermissionType.MENU);
            perm1.setResource("perm");
            perm1.setSortOrder(1);

            Permission perm2 = new Permission();
            perm2.setName("权限 2");
            perm2.setCode("perm:2");
            perm2.setType(PermissionType.MENU);
            perm2.setResource("perm");
            perm2.setSortOrder(2);

            // Then
            assertThat(perm1.getSortOrder()).isEqualTo(1);
            assertThat(perm2.getSortOrder()).isEqualTo(2);
            assertThat(perm1.getSortOrder()).isLessThan(perm2.getSortOrder());
        }
    }
}
