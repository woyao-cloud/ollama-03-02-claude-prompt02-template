package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserUpdateRequest;
import com.usermanagement.web.dto.UserListResponse;
import com.usermanagement.web.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_HASHED_PASSWORD = "hashedPassword123";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
    }

    @Nested
    @DisplayName("创建用户测试")
    class CreateUserTests {

        @Test
        @DisplayName("应该创建用户并加密密码")
        void shouldCreateUserWithHashedPassword() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.PENDING);
            UserDTO dto = createUserDTO(userId, UserStatus.PENDING);

            UserCreateRequest request = new UserCreateRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName(TEST_FIRST_NAME);
            request.setLastName(TEST_LAST_NAME);

            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(TEST_HASHED_PASSWORD);
            given(userMapper.toEntity(request)).willReturn(user);
            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user)).willReturn(dto);

            // When
            UserDTO result = userService.createUser(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId.toString());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPasswordHash()).isEqualTo(TEST_HASHED_PASSWORD);

            verify(passwordEncoder).encode(TEST_PASSWORD);
        }

        @Test
        @DisplayName("邮箱已存在时抛出异常")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName(TEST_FIRST_NAME);
            request.setLastName(TEST_LAST_NAME);

            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("邮箱已被注册");

            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("应该设置默认状态为 PENDING")
        void shouldSetDefaultStatusToPending() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.PENDING);
            UserDTO dto = createUserDTO(userId, UserStatus.PENDING);

            UserCreateRequest request = new UserCreateRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName(TEST_FIRST_NAME);
            request.setLastName(TEST_LAST_NAME);

            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(TEST_HASHED_PASSWORD);
            given(userMapper.toEntity(request)).willReturn(user);
            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user)).willReturn(dto);

            // When
            userService.createUser(request);

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("获取用户测试")
    class GetUserTests {

        @Test
        @DisplayName("应该根据 ID 获取用户")
        void shouldGetUserById() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.ACTIVE);
            UserDTO dto = createUserDTO(userId, UserStatus.ACTIVE);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(dto);

            // When
            UserDTO result = userService.getUserById(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId.toString());
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("更新用户测试")
    class UpdateUserTests {

        @Test
        @DisplayName("应该更新用户信息")
        void shouldUpdateUser() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.ACTIVE);
            UserDTO dto = createUserDTO(userId, UserStatus.ACTIVE);

            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");
            request.setPhone("1111111111");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(dto);

            // When
            UserDTO result = userService.updateUser(userId, request);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(user);
            assertThat(user.getFirstName()).isEqualTo("Updated");
            assertThat(user.getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowExceptionWhenUserNotFoundForUpdate() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");

            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(nonExistentId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("不应该更新邮箱")
        void shouldNotUpdateEmail() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.ACTIVE);
            String originalEmail = user.getEmail();

            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");
            // email 字段不存在于 UpdateRequest 中

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(createUserDTO(userId, UserStatus.ACTIVE));

            // When
            userService.updateUser(userId, request);

            // Then
            assertThat(user.getEmail()).isEqualTo(originalEmail);
        }
    }

    @Nested
    @DisplayName("删除用户测试")
    class DeleteUserTests {

        @Test
        @DisplayName("应该软删除用户")
        void shouldSoftDeleteUser() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.ACTIVE);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            userService.deleteUser(userId);

            // Then
            verify(userRepository).delete(user);
            assertThat(user.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowExceptionWhenUserNotFoundForDelete() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("更新用户状态测试")
    class UpdateStatusTests {

        @Test
        @DisplayName("应该更新用户状态为 ACTIVE")
        void shouldUpdateUserStatusToActive() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.PENDING);
            UserDTO dto = createUserDTO(userId, UserStatus.ACTIVE);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(dto);

            // When
            UserDTO result = userService.updateUserStatus(userId, UserStatus.ACTIVE);

            // Then
            assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("应该更新用户状态为 INACTIVE")
        void shouldUpdateUserStatusToInactive() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.ACTIVE);
            UserDTO dto = createUserDTO(userId, UserStatus.INACTIVE);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(dto);

            // When
            UserDTO result = userService.updateUserStatus(userId, UserStatus.INACTIVE);

            // Then
            assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        @DisplayName("应该更新用户状态为 LOCKED")
        void shouldUpdateUserStatusToLocked() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.ACTIVE);
            UserDTO dto = createUserDTO(userId, UserStatus.LOCKED);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(dto);

            // When
            UserDTO result = userService.updateUserStatus(userId, UserStatus.LOCKED);

            // Then
            assertThat(result.getStatus()).isEqualTo(UserStatus.LOCKED);
            assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowExceptionWhenUserNotFoundForStatusUpdate() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUserStatus(nonExistentId, UserStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("获取用户列表测试")
    class GetUserListTests {

        @Test
        @DisplayName("应该获取分页用户列表")
        void shouldGetPaginatedUserList() {
            // Given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            User user1 = createUser(userId1, UserStatus.ACTIVE);
            User user2 = createUser(userId2, UserStatus.PENDING);
            UserDTO dto1 = createUserDTO(userId1, UserStatus.ACTIVE);
            UserDTO dto2 = createUserDTO(userId2, UserStatus.PENDING);

            Page<User> userPage = new PageImpl<>(List.of(user1, user2));
            Pageable pageable = PageRequest.of(0, 10);

            given(userRepository.findAll(pageable)).willReturn(userPage);
            given(userMapper.toDto(user1)).willReturn(dto1);
            given(userMapper.toDto(user2)).willReturn(dto2);

            // When
            UserListResponse response = userService.getUsers(pageable, null, null, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("应该按部门筛选用户")
        void shouldFilterUsersByDepartment() {
            // Given
            UUID departmentId = UUID.randomUUID();
            UUID userId1 = UUID.randomUUID();
            User user1 = createUser(userId1, UserStatus.ACTIVE);
            user1.setDepartmentId(departmentId);
            UserDTO dto1 = createUserDTO(userId1, UserStatus.ACTIVE);

            Page<User> userPage = new PageImpl<>(List.of(user1));
            Pageable pageable = PageRequest.of(0, 10);

            given(userRepository.findByDepartmentId(departmentId, pageable)).willReturn(userPage);
            given(userMapper.toDto(user1)).willReturn(dto1);

            // When
            UserListResponse response = userService.getUsers(pageable, null, departmentId, null);

            // Then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("应该按状态筛选用户")
        void shouldFilterUsersByStatus() {
            // Given
            UUID userId1 = UUID.randomUUID();
            User user1 = createUser(userId1, UserStatus.ACTIVE);
            UserDTO dto1 = createUserDTO(userId1, UserStatus.ACTIVE);

            Page<User> userPage = new PageImpl<>(List.of(user1));
            Pageable pageable = PageRequest.of(0, 10);

            given(userRepository.findByStatus(UserStatus.ACTIVE, pageable)).willReturn(userPage);
            given(userMapper.toDto(user1)).willReturn(dto1);

            // When
            UserListResponse response = userService.getUsers(pageable, null, null, UserStatus.ACTIVE);

            // Then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("应该同时按部门和状态筛选")
        void shouldFilterUsersByDepartmentAndStatus() {
            // Given
            UUID departmentId = UUID.randomUUID();
            UUID userId1 = UUID.randomUUID();
            User user1 = createUser(userId1, UserStatus.ACTIVE);
            user1.setDepartmentId(departmentId);
            UserDTO dto1 = createUserDTO(userId1, UserStatus.ACTIVE);

            Page<User> userPage = new PageImpl<>(List.of(user1));
            Pageable pageable = PageRequest.of(0, 10);

            given(userRepository.findByDepartmentIdAndStatus(departmentId, UserStatus.ACTIVE, pageable))
                .willReturn(userPage);
            given(userMapper.toDto(user1)).willReturn(dto1);

            // When
            UserListResponse response = userService.getUsers(pageable, null, departmentId, UserStatus.ACTIVE);

            // Then
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("密码重置测试")
    class PasswordResetTests {

        @Test
        @DisplayName("应该重置用户密码")
        void shouldResetUserPassword() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createUser(userId, UserStatus.ACTIVE);
            String newPassword = "NewPassword123!";
            String newHashedPassword = "newHashedPassword123";

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.encode(newPassword)).willReturn(newHashedPassword);

            // When
            userService.resetPassword(userId, newPassword);

            // Then
            assertThat(user.getPasswordHash()).isEqualTo(newHashedPassword);
            assertThat(user.getPasswordChangedAt()).isNotNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowExceptionWhenUserNotFoundForPasswordReset() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword(nonExistentId, "NewPassword123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
        }
    }

    // 辅助方法
    private User createUser(UUID id, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(TEST_HASHED_PASSWORD);
        user.setFirstName(TEST_FIRST_NAME);
        user.setLastName(TEST_LAST_NAME);
        user.setPhone("1234567890");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setDepartmentId(UUID.randomUUID());
        user.setStatus(status);
        user.setEmailVerified(true);
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(Instant.now());
        user.setLastLoginIp("192.168.1.1");
        return user;
    }

    private UserDTO createUserDTO(UUID id, UserStatus status) {
        UserDTO dto = new UserDTO();
        dto.setId(id.toString());
        dto.setEmail(TEST_EMAIL);
        dto.setFirstName(TEST_FIRST_NAME);
        dto.setLastName(TEST_LAST_NAME);
        dto.setFullName(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        dto.setPhone("1234567890");
        dto.setAvatarUrl("https://example.com/avatar.jpg");
        dto.setDepartmentId(UUID.randomUUID().toString());
        dto.setStatus(status);
        dto.setEmailVerified(true);
        dto.setLastLoginAt(Instant.now());
        dto.setLastLoginIp("192.168.1.1");
        return dto;
    }
}
