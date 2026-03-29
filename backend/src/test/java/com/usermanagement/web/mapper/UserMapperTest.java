package com.usermanagement.web.mapper;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserMapper 单元测试
 */
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Nested
    @DisplayName("User 转 UserDTO 测试")
    class ToDtoTests {

        @Test
        @DisplayName("应该将 User 映射为 UserDTO")
        void shouldMapUserToUserDTO() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            User user = createUser(userId, UserStatus.ACTIVE, now);

            // When
            UserDTO dto = userMapper.toDto(user);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(userId.toString());
            assertThat(dto.getEmail()).isEqualTo("test@example.com");
            assertThat(dto.getFirstName()).isEqualTo("John");
            assertThat(dto.getLastName()).isEqualTo("Doe");
            assertThat(dto.getFullName()).isEqualTo("John Doe");
            assertThat(dto.getPhone()).isEqualTo("1234567890");
            assertThat(dto.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(dto.getEmailVerified()).isTrue();
            assertThat(dto.getDepartmentId()).isEqualTo(user.getDepartmentId().toString());
        }

        @Test
        @DisplayName("应该处理 null 值")
        void shouldHandleNullValues() {
            // Given
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("test@example.com");
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setStatus(UserStatus.PENDING);
            user.setEmailVerified(false);
            user.setDepartmentId(null);
            user.setPhone(null);

            // When
            UserDTO dto = userMapper.toDto(user);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getPhone()).isNull();
            assertThat(dto.getDepartmentId()).isNull();
            assertThat(dto.getEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("应该映射最后登录信息")
        void shouldMapLastLoginInfo() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant lastLoginAt = Instant.now();
            String lastLoginIp = "192.168.1.1";

            User user = new User();
            user.setId(userId);
            user.setEmail("test@example.com");
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true);
            user.setLastLoginAt(lastLoginAt);
            user.setLastLoginIp(lastLoginIp);

            // When
            UserDTO dto = userMapper.toDto(user);

            // Then
            assertThat(dto.getLastLoginAt()).isEqualTo(lastLoginAt);
            assertThat(dto.getLastLoginIp()).isEqualTo(lastLoginIp);
        }
    }

    @Nested
    @DisplayName("UserCreateRequest 转 User 测试")
    class ToEntityTests {

        @Test
        @DisplayName("应该将 UserCreateRequest 映射为 User")
        void shouldMapCreateRequestToUser() {
            // Given
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("Password123!");
            request.setFirstName("Jane");
            request.setLastName("Smith");
            request.setPhone("0987654321");
            request.setDepartmentId(UUID.randomUUID().toString());

            // When
            User user = userMapper.toEntity(request);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getEmail()).isEqualTo("newuser@example.com");
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getPhone()).isEqualTo("0987654321");
            assertThat(user.getDepartmentId()).isNotNull();
            // 注意：密码不应在此处设置，由 Service 层处理加密
        }

        @Test
        @DisplayName("应该处理空部门 ID")
        void shouldHandleNullDepartmentId() {
            // Given
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("Password123!");
            request.setFirstName("Jane");
            request.setLastName("Smith");
            request.setDepartmentId(null);

            // When
            User user = userMapper.toEntity(request);

            // Then
            assertThat(user.getDepartmentId()).isNull();
        }
    }

    @Nested
    @DisplayName("UserUpdateRequest 更新 User 测试")
    class UpdateEntityTests {

        @Test
        @DisplayName("应该用 UserUpdateRequest 更新 User")
        void shouldUpdateUserFromUpdateRequest() {
            // Given
            User user = createUser(UUID.randomUUID(), UserStatus.ACTIVE, Instant.now());

            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");
            request.setPhone("1111111111");
            request.setDepartmentId(UUID.randomUUID().toString());

            // When
            userMapper.updateEntity(request, user);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Updated");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getPhone()).isEqualTo("1111111111");
            assertThat(user.getEmail()).isEqualTo("test@example.com"); // 不应改变
        }

        @Test
        @DisplayName("应该只更新非空字段")
        void shouldOnlyUpdateNonNullFields() {
            // Given
            User user = createUser(UUID.randomUUID(), UserStatus.ACTIVE, Instant.now());
            String originalPhone = user.getPhone();

            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");
            // lastName, phone, departmentId 都为 null

            // When
            userMapper.updateEntity(request, user);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Updated");
            assertThat(user.getLastName()).isEqualTo("Doe"); // 保持不变
            assertThat(user.getPhone()).isEqualTo(originalPhone); // 保持不变
        }
    }

    @Nested
    @DisplayName("UserStatus 映射测试")
    class StatusMappingTests {

        @Test
        @DisplayName("应该映射所有用户状态")
        void shouldMapAllUserStatuses() {
            // Given & When & Then
            for (UserStatus status : UserStatus.values()) {
                User user = createUser(UUID.randomUUID(), status, Instant.now());
                UserDTO dto = userMapper.toDto(user);
                assertThat(dto.getStatus()).isEqualTo(status);
            }
        }
    }

    // 辅助方法
    private User createUser(UUID id, UserStatus status, Instant lastLoginAt) {
        User user = new User();
        user.setId(id);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("1234567890");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setDepartmentId(UUID.randomUUID());
        user.setStatus(status);
        user.setEmailVerified(true);
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(lastLoginAt);
        user.setLastLoginIp("192.168.1.1");
        return user;
    }
}
