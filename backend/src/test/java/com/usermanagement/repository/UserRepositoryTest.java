package com.usermanagement.repository;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 集成测试
 */
@DataJpaTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("UserRepository 集成测试")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashed_password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Nested
    @DisplayName("用户保存测试")
    class SaveTests {

        @Test
        @DisplayName("应该保存用户")
        void shouldSaveUser() {
            // When
            User saved = userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo("test@example.com");
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("应该保存待激活用户")
        void shouldSavePendingUser() {
            // Given
            testUser.setStatus(UserStatus.PENDING);
            testUser.setEmailVerified(false);

            // When
            User saved = userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(saved.getEmailVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("用户查询测试")
    class FindTests {

        @Test
        @DisplayName("应该根据 ID 查找用户")
        void shouldFindUserById() {
            // Given
            User saved = userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<User> found = userRepository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("应该根据邮箱查找用户")
        void shouldFindUserByEmail() {
            // Given
            userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<User> found = userRepository.findByEmail("test@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getFirstName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("未找到用户时返回空 Optional")
        void shouldReturnEmptyWhenNotFound() {
            // When
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("应该检查邮箱是否存在")
        void shouldCheckEmailExists() {
            // Given
            userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("用户状态查询测试")
    class StatusQueryTests {

        @Test
        @DisplayName("应该根据状态查找用户")
        void shouldFindUsersByStatus() {
            // Given
            userRepository.save(testUser);

            User inactiveUser = new User();
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setPasswordHash("hashed");
            inactiveUser.setFirstName("Inactive");
            inactiveUser.setLastName("User");
            inactiveUser.setStatus(UserStatus.INACTIVE);
            userRepository.save(inactiveUser);

            entityManager.flush();
            entityManager.clear();

            // When
            List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

            // Then
            assertThat(activeUsers).hasSize(1);
            assertThat(activeUsers.get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("应该根据部门查找用户")
        void shouldFindUsersByDepartment() {
            // Given
            testUser.setDepartmentId(java.util.UUID.randomUUID());
            userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // When
            List<User> users = userRepository.findByDepartmentId(testUser.getDepartmentId());

            // Then
            assertThat(users).hasSize(1);
            assertThat(users.get(0).getDepartmentId()).isEqualTo(testUser.getDepartmentId());
        }
    }

    @Nested
    @DisplayName("分页查询测试")
    class PaginationTests {

        @Test
        @DisplayName("应该分页查询所有用户")
        void shouldFindAllUsersPaginated() {
            // Given
            userRepository.save(testUser);

            User user2 = new User();
            user2.setEmail("user2@example.com");
            user2.setPasswordHash("hashed");
            user2.setFirstName("User");
            user2.setLastName("Two");
            user2.setStatus(UserStatus.ACTIVE);
            userRepository.save(user2);

            entityManager.flush();
            entityManager.clear();

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findAll(pageable);

            // Then
            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("应该按状态分页查询用户")
        void shouldFindUsersByStatusPaginated() {
            // Given
            userRepository.save(testUser);

            User inactiveUser = new User();
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setPasswordHash("hashed");
            inactiveUser.setFirstName("Inactive");
            inactiveUser.setLastName("User");
            inactiveUser.setStatus(UserStatus.INACTIVE);
            userRepository.save(inactiveUser);

            entityManager.flush();
            entityManager.clear();

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByStatus(UserStatus.ACTIVE, pageable);

            // Then
            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("应该按部门分页查询用户")
        void shouldFindUsersByDepartmentPaginated() {
            // Given
            UUID departmentId = UUID.randomUUID();
            testUser.setDepartmentId(departmentId);
            userRepository.save(testUser);

            entityManager.flush();
            entityManager.clear();

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByDepartmentId(departmentId, pageable);

            // Then
            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getDepartmentId()).isEqualTo(departmentId);
        }

        @Test
        @DisplayName("应该按部门和状态分页查询用户")
        void shouldFindUsersByDepartmentAndStatusPaginated() {
            // Given
            UUID departmentId = UUID.randomUUID();
            testUser.setDepartmentId(departmentId);
            testUser.setStatus(UserStatus.ACTIVE);
            userRepository.save(testUser);

            User otherStatusUser = new User();
            otherStatusUser.setEmail("other@example.com");
            otherStatusUser.setPasswordHash("hashed");
            otherStatusUser.setFirstName("Other");
            otherStatusUser.setLastName("User");
            otherStatusUser.setDepartmentId(departmentId);
            otherStatusUser.setStatus(UserStatus.INACTIVE);
            userRepository.save(otherStatusUser);

            entityManager.flush();
            entityManager.clear();

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByDepartmentIdAndStatus(departmentId, UserStatus.ACTIVE, pageable);

            // Then
            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("软删除测试")
    class SoftDeleteTests {

        @Test
        @DisplayName("应该软删除用户")
        void shouldSoftDeleteUser() {
            // Given
            User saved = userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // When
            saved.setDeletedAt(java.time.Instant.now());
            userRepository.delete(saved);
            entityManager.flush();
            entityManager.clear();

            // Then - 使用 @Where 注解，软删除的用户不会被查询到
            Optional<User> found = userRepository.findById(saved.getId());
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("软删除后邮箱检查应返回 false")
        void shouldReturnFalseForDeletedUserEmail() {
            // Given
            testUser.setEmail("todelete@example.com");
            User saved = userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            // When
            saved.setDeletedAt(java.time.Instant.now());
            userRepository.delete(saved);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(userRepository.existsByEmail("todelete@example.com")).isFalse();
        }
    }
}
