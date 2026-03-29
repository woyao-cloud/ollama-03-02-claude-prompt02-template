package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserStatusUpdateRequest;
import com.usermanagement.web.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = createUser("test@example.com", UserStatus.ACTIVE);
    }

    @Nested
    @DisplayName("创建用户 API 集成测试")
    class CreateUserApiIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该创建用户并返回 201")
        void shouldCreateUserAndReturn201() throws Exception {
            // Given
            UserCreateRequest request = UserCreateRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .phone("1234567890")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("邮箱已存在时返回 400")
        void shouldReturn400WhenEmailExists() throws Exception {
            // Given
            UserCreateRequest request = UserCreateRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("Duplicate")
                .lastName("User")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("密码强度不足时返回 400")
        void shouldReturn400WhenWeakPassword() throws Exception {
            // Given
            UserCreateRequest request = UserCreateRequest.builder()
                .email("newuser@example.com")
                .password("weak")
                .firstName("New")
                .lastName("User")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("获取用户列表 API 集成测试")
    class GetUserListApiIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该获取分页用户列表")
        void shouldGetPaginatedUserList() throws Exception {
            // Given
            createUser("user2@example.com", UserStatus.ACTIVE);
            createUser("user3@example.com", UserStatus.PENDING);

            // When & Then
            mockMvc.perform(get("/api/users")
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该按状态筛选用户")
        void shouldFilterUsersByStatus() throws Exception {
            // Given
            createUser("active@example.com", UserStatus.ACTIVE);
            createUser("pending@example.com", UserStatus.PENDING);

            // When & Then
            mockMvc.perform(get("/api/users")
                    .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2)); // testUser + active@example.com
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该支持分页")
        void shouldSupportPagination() throws Exception {
            // Given
            for (int i = 0; i < 15; i++) {
                createUser("user" + i + "@example.com", UserStatus.ACTIVE);
            }

            // When & Then
            mockMvc.perform(get("/api/users")
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(16)) // 15 + testUser
                .andExpect(jsonPath("$.totalPages").value(2));
        }
    }

    @Nested
    @DisplayName("获取用户详情 API 集成测试")
    class GetUserApiIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该获取用户详情")
        void shouldGetUserById() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(get("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("更新用户 API 集成测试")
    class UpdateUserApiIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该更新用户信息")
        void shouldUpdateUser() throws Exception {
            // Given
            UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .phone("9999999999")
                .build();

            // When & Then
            mockMvc.perform(put("/api/users/{id}", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.phone").value("9999999999"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("Updated")
                .build();
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(put("/api/users/{id}", nonExistentId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("删除用户 API 集成测试")
    class DeleteUserApiIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该软删除用户")
        void shouldSoftDeleteUser() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                    .with(csrf()))
                .andExpect(status().isNoContent());

            // 验证软删除 - 用户不应再被查询到
            mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(delete("/api/users/{id}", nonExistentId)
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("更新用户状态 API 集成测试")
    class UpdateUserStatusApiIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该更新用户状态为 INACTIVE")
        void shouldUpdateUserStatusToInactive() throws Exception {
            // Given
            UserStatusUpdateRequest request = UserStatusUpdateRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();

            // When & Then
            mockMvc.perform(patch("/api/users/{id}/status", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该更新用户状态为 LOCKED")
        void shouldUpdateUserStatusToLocked() throws Exception {
            // Given
            UserStatusUpdateRequest request = UserStatusUpdateRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

            // When & Then
            mockMvc.perform(patch("/api/users/{id}/status", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            UserStatusUpdateRequest request = UserStatusUpdateRequest.builder()
                .status(UserStatus.ACTIVE)
                .build();
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(patch("/api/users/{id}/status", nonExistentId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("密码重置 API 集成测试")
    class ResetPasswordApiIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("应该重置用户密码")
        void shouldResetUserPassword() throws Exception {
            // Given
            String newPassword = "NewPassword123!";
            String requestBody = "{\"password\":\"" + newPassword + "\"}";

            // When & Then
            mockMvc.perform(post("/api/users/{id}/password/reset", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("密码强度不足时返回 400")
        void shouldReturn400WhenWeakPassword() throws Exception {
            // Given
            String weakPassword = "weak";
            String requestBody = "{\"password\":\"" + weakPassword + "\"}";

            // When & Then
            mockMvc.perform(post("/api/users/{id}/password/reset", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            String requestBody = "{\"password\":\"NewPassword123!\"}";

            // When & Then
            mockMvc.perform(post("/api/users/{id}/password/reset", nonExistentId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("权限控制测试")
    class AuthorizationTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("普通用户不能创建用户")
        void shouldNotCreateUserAsRegularUser() throws Exception {
            // Given
            UserCreateRequest request = UserCreateRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .build();

            // When & Then
            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("普通用户可以获取用户列表")
        void shouldGetUserListAsRegularUser() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("普通用户不能删除用户")
        void shouldNotDeleteUserAsRegularUser() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    // 辅助方法
    private User createUser(String email, UserStatus status) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setStatus(status);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
}
