package com.usermanagement.repository;

import com.usermanagement.domain.DataScope;
import com.usermanagement.domain.Role;
import com.usermanagement.domain.RoleStatus;
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
 * RoleRepository 集成测试
 */
@DataJpaTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("RoleRepository 集成测试")
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setName("管理员");
        adminRole.setCode("ROLE_ADMIN");
        adminRole.setDescription("系统管理员角色");
        adminRole.setDataScope(DataScope.ALL);
        adminRole.setStatus(RoleStatus.ACTIVE);
    }

    @Nested
    @DisplayName("角色保存测试")
    class SaveTests {

        @Test
        @DisplayName("应该保存角色")
        void shouldSaveRole() {
            // When
            Role saved = roleRepository.save(adminRole);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCode()).isEqualTo("ROLE_ADMIN");
            assertThat(saved.getDataScope()).isEqualTo(DataScope.ALL);
        }

        @Test
        @DisplayName("应该保存自定义数据权限角色")
        void shouldSaveCustomDataScopeRole() {
            // Given
            adminRole.setDataScope(DataScope.CUSTOM);

            // When
            Role saved = roleRepository.save(adminRole);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getDataScope()).isEqualTo(DataScope.CUSTOM);
        }
    }

    @Nested
    @DisplayName("角色查询测试")
    class FindTests {

        @Test
        @DisplayName("应该根据代码查找角色")
        void shouldFindRoleByCode() {
            // Given
            roleRepository.save(adminRole);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Role> found = roleRepository.findByCode("ROLE_ADMIN");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("管理员");
        }

        @Test
        @DisplayName("应该根据名称查找角色")
        void shouldFindRoleByName() {
            // Given
            roleRepository.save(adminRole);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Role> found = roleRepository.findByName("管理员");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getCode()).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("应该检查代码是否存在")
        void shouldCheckCodeExists() {
            // Given
            roleRepository.save(adminRole);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(roleRepository.existsByCode("ROLE_ADMIN")).isTrue();
            assertThat(roleRepository.existsByCode("ROLE_NONEXISTENT")).isFalse();
        }
    }

    @Nested
    @DisplayName("角色状态查询测试")
    class StatusQueryTests {

        @Test
        @DisplayName("应该根据状态查找角色")
        void shouldFindRolesByStatus() {
            // Given
            roleRepository.save(adminRole);

            Role inactiveRole = new Role();
            inactiveRole.setName("禁用角色");
            inactiveRole.setCode("ROLE_INACTIVE");
            inactiveRole.setStatus(RoleStatus.INACTIVE);
            roleRepository.save(inactiveRole);

            entityManager.flush();
            entityManager.clear();

            // When
            List<Role> activeRoles = roleRepository.findByStatus(RoleStatus.ACTIVE);

            // Then
            assertThat(activeRoles).hasSize(1);
            assertThat(activeRoles.get(0).getStatus()).isEqualTo(RoleStatus.ACTIVE);
        }
    }
}
