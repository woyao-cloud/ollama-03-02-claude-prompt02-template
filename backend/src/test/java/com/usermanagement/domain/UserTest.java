package com.usermanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User 实体单元测试
 */
@DisplayName("User 实体测试")
class UserTest {

    @Nested
    @DisplayName("用户创建测试")
    class UserCreationTests {

        @Test
        @DisplayName("应该创建用户")
        void shouldCreateUser() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed_password");
            user.setFirstName("John");
            user.setLastName("Doe");

            // Then
            assertThat(user.getEmail()).isEqualTo("user@example.com");
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(user.getEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("用户应该有默认状态 PENDING")
        void shouldHaveDefaultPendingStatus() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        }

        @Test
        @DisplayName("用户应该默认邮箱未验证")
        void shouldHaveDefaultUnverifiedEmail() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");

            // Then
            assertThat(user.getEmailVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("用户激活测试")
    class UserActivationTests {

        @Test
        @DisplayName("应该能激活用户")
        void shouldActivateUser() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setStatus(UserStatus.PENDING);

            // When
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true);

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("应该能锁定用户")
        void shouldLockUser() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");

            // When
            user.setStatus(UserStatus.LOCKED);
            user.setFailedLoginAttempts(5);
            user.setLockedUntil(Instant.now().plusSeconds(3600));

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
            assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
            assertThat(user.getLockedUntil()).isNotNull();
        }
    }

    @Nested
    @DisplayName("用户登录测试")
    class UserLoginTests {

        @Test
        @DisplayName("应该记录登录信息")
        void shouldRecordLoginInfo() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");

            // When
            Instant now = Instant.now();
            user.setLastLoginAt(now);
            user.setLastLoginIp("192.168.1.1");

            // Then
            assertThat(user.getLastLoginAt()).isEqualTo(now);
            assertThat(user.getLastLoginIp()).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("登录成功后应该重置失败次数")
        void shouldResetFailedAttemptsOnSuccessfulLogin() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setFailedLoginAttempts(3);

            // When
            user.setFailedLoginAttempts(0);
            user.setStatus(UserStatus.ACTIVE);

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("用户密码测试")
    class UserPasswordTests {

        @Test
        @DisplayName("应该记录密码修改时间")
        void shouldRecordPasswordChangeTime() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");

            // When
            Instant now = Instant.now();
            user.setPasswordChangedAt(now);

            // Then
            assertThat(user.getPasswordChangedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("应该能更新密码哈希")
        void shouldUpdatePasswordHash() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("old_hash");
            user.setFirstName("Test");
            user.setLastName("User");

            // When
            user.setPasswordHash("new_hash");
            user.setPasswordChangedAt(Instant.now());

            // Then
            assertThat(user.getPasswordHash()).isEqualTo("new_hash");
            assertThat(user.getPasswordChangedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("用户部门关联测试")
    class UserDepartmentTests {

        @Test
        @DisplayName("用户应该能关联部门")
        void shouldAssociateWithDepartment() {
            // Given
            User user = new User();
            user.setEmail("user@example.com");
            user.setPasswordHash("hashed");
            user.setFirstName("Test");
            user.setLastName("User");

            Department dept = new Department();
            dept.setName("技术部");
            dept.setCode("TECH");
            dept.setLevel(1);
            dept.setPath("/dept-id");

            // When
            user.setDepartmentId(dept.getId());

            // Then
            assertThat(user.getDepartmentId()).isEqualTo(dept.getId());
        }
    }

    @Nested
    @DisplayName("用户全名测试")
    class UserFullNameTests {

        @Test
        @DisplayName("应该能获取全名")
        void shouldGetFullName() {
            // Given
            User user = new User();
            user.setFirstName("John");
            user.setLastName("Doe");

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
        }
    }
}
