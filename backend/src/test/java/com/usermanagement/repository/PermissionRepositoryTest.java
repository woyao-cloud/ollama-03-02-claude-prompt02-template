package com.usermanagement.repository;

import com.usermanagement.domain.Permission;
import com.usermanagement.domain.PermissionStatus;
import com.usermanagement.domain.PermissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PermissionRepository 集成测试
 */
@DataJpaTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("PermissionRepository 集成测试")
class PermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PermissionRepository permissionRepository;

    private Permission createPermission;

    @BeforeEach
    void setUp() {
        createPermission = new Permission();
        createPermission.setName("创建用户");
        createPermission.setCode("user:create");
        createPermission.setType(PermissionType.ACTION);
        createPermission.setResource("user");
        createPermission.setAction("create");
        createPermission.setStatus(PermissionStatus.ACTIVE);
    }

    @Nested
    @DisplayName("权限保存测试")
    class SaveTests {

        @Test
        @DisplayName("应该保存权限")
        void shouldSavePermission() {
            // When
            Permission saved = permissionRepository.save(createPermission);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCode()).isEqualTo("user:create");
            assertThat(saved.getType()).isEqualTo(PermissionType.ACTION);
        }

        @Test
        @DisplayName("应该保存菜单权限")
        void shouldSaveMenuPermission() {
            // Given
            Permission menuPerm = new Permission();
            menuPerm.setName("用户管理");
            menuPerm.setCode("user:menu");
            menuPerm.setType(PermissionType.MENU);
            menuPerm.setResource("user");
            menuPerm.setRoute("/users");
            menuPerm.setIcon("user");

            // When
            Permission saved = permissionRepository.save(menuPerm);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getType()).isEqualTo(PermissionType.MENU);
            assertThat(saved.getRoute()).isEqualTo("/users");
        }
    }

    @Nested
    @DisplayName("权限查询测试")
    class FindTests {

        @Test
        @DisplayName("应该根据代码查找权限")
        void shouldFindPermissionByCode() {
            // Given
            permissionRepository.save(createPermission);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Permission> found = permissionRepository.findByCode("user:create");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("创建用户");
        }

        @Test
        @DisplayName("应该检查代码是否存在")
        void shouldCheckCodeExists() {
            // Given
            permissionRepository.save(createPermission);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(permissionRepository.existsByCode("user:create")).isTrue();
            assertThat(permissionRepository.existsByCode("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("权限类型查询测试")
    class TypeQueryTests {

        @Test
        @DisplayName("应该根据类型查找权限")
        void shouldFindPermissionsByType() {
            // Given
            permissionRepository.save(createPermission);

            Permission menuPerm = new Permission();
            menuPerm.setName("用户管理");
            menuPerm.setCode("user:menu");
            menuPerm.setType(PermissionType.MENU);
            menuPerm.setResource("user");
            permissionRepository.save(menuPerm);

            entityManager.flush();
            entityManager.clear();

            // When
            List<Permission> actionPerms = permissionRepository.findByType(PermissionType.ACTION);

            // Then
            assertThat(actionPerms).hasSize(1);
            assertThat(actionPerms.get(0).getType()).isEqualTo(PermissionType.ACTION);
        }

        @Test
        @DisplayName("应该根据资源查找权限")
        void shouldFindPermissionsByResource() {
            // Given
            permissionRepository.save(createPermission);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Permission> userPerms = permissionRepository.findByResource("user");

            // Then
            assertThat(userPerms).hasSize(1);
            assertThat(userPerms.get(0).getResource()).isEqualTo("user");
        }
    }

    @Nested
    @DisplayName("权限状态查询测试")
    class StatusQueryTests {

        @Test
        @DisplayName("应该根据状态查找权限")
        void shouldFindPermissionsByStatus() {
            // Given
            permissionRepository.save(createPermission);

            Permission inactivePerm = new Permission();
            inactivePerm.setName("禁用权限");
            inactivePerm.setCode("disabled:perm");
            inactivePerm.setType(PermissionType.ACTION);
            inactivePerm.setResource("disabled");
            inactivePerm.setStatus(PermissionStatus.INACTIVE);
            permissionRepository.save(inactivePerm);

            entityManager.flush();
            entityManager.clear();

            // When
            List<Permission> activePerms = permissionRepository.findByStatus(PermissionStatus.ACTIVE);

            // Then
            assertThat(activePerms).hasSize(1);
            assertThat(activePerms.get(0).getStatus()).isEqualTo(PermissionStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("权限树查询测试")
    class PermissionTreeTests {

        @Test
        @DisplayName("应该根据父权限 ID 查找子权限")
        void shouldFindPermissionsByParentId() {
            // Given
            Permission menuPerm = new Permission();
            menuPerm.setName("用户管理");
            menuPerm.setCode("user:menu");
            menuPerm.setType(PermissionType.MENU);
            menuPerm.setResource("user");
            Permission savedMenu = permissionRepository.save(menuPerm);

            createPermission.setParentId(savedMenu.getId());
            permissionRepository.save(createPermission);

            entityManager.flush();
            entityManager.clear();

            // When
            List<Permission> children = permissionRepository.findByParentId(savedMenu.getId());

            // Then
            assertThat(children).hasSize(1);
            assertThat(children.get(0).getParentId()).isEqualTo(savedMenu.getId());
        }
    }
}
