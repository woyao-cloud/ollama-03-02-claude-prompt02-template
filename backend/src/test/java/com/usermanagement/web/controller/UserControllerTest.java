package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.service.UserService;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserUpdateRequest;
import com.usermanagement.web.dto.UserListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("创建用户 API 测试")
    class CreateUserApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该创建用户并返回 201")
        void shouldCreateUserAndReturn201() throws Exception {
            // Given
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword("Password123!");
            request.setFirstName(TEST_FIRST_NAME);
            request.setLastName(TEST_LAST_NAME);

            UserDTO responseDto = new UserDTO();
            responseDto.setId(TEST_USER_ID);
            responseDto.setEmail(TEST_EMAIL);
            responseDto.setFirstName(TEST_FIRST_NAME);
            responseDto.setLastName(TEST_LAST_NAME);
            responseDto.setStatus(UserStatus.PENDING);

            given(userService.createUser(any(UserCreateRequest.class))).willReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.status").value("PENDING"));

            then(userService).should().createUser(any(UserCreateRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("邮箱已存在时返回 400")
        void shouldReturn400WhenEmailExists() throws Exception {
            // Given
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword("Password123!");
            request.setFirstName(TEST_FIRST_NAME);
            request.setLastName(TEST_LAST_NAME);

            given(userService.createUser(any(UserCreateRequest.class)))
                .willThrow(new IllegalArgumentException("邮箱已被注册"));

            // When & Then
            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("请求体无效时返回 400")
        void shouldReturn400WhenInvalidRequest() throws Exception {
            // Given
            UserCreateRequest request = new UserCreateRequest();
            // 缺少必填字段

            // When & Then
            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("获取用户列表 API 测试")
    class GetUserListApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取分页用户列表")
        void shouldGetPaginatedUserList() throws Exception {
            // Given
            UserDTO dto1 = createUserDto("1", UserStatus.ACTIVE);
            UserDTO dto2 = createUserDto("2", UserStatus.PENDING);
            List<UserDTO> content = List.of(dto1, dto2);
            UserListResponse response = new UserListResponse(
                new PageImpl<>(content, PageRequest.of(0, 10), 2)
            );

            given(userService.getUsers(any(), any(), any(), any())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/users")
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按部门筛选")
        void shouldFilterByDepartment() throws Exception {
            // Given
            String departmentId = "550e8400-e29b-41d4-a716-446655440001";
            UserListResponse response = new UserListResponse(new PageImpl<>(List.of()));

            given(userService.getUsers(any(), any(), any(), any())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/users")
                    .param("departmentId", departmentId))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按状态筛选")
        void shouldFilterByStatus() throws Exception {
            // Given
            UserListResponse response = new UserListResponse(new PageImpl<>(List.of()));

            given(userService.getUsers(any(), any(), any(), any())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/users")
                    .param("status", "ACTIVE"))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持分页参数")
        void shouldSupportPaginationParams() throws Exception {
            // Given
            UserListResponse response = new UserListResponse(new PageImpl<>(List.of()));
            given(userService.getUsers(any(), any(), any(), any())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/users")
                    .param("page", "1")
                    .param("size", "20")
                    .param("sortBy", "createdAt")
                    .param("direction", "DESC"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("获取用户详情 API 测试")
    class GetUserApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取用户详情")
        void shouldGetUserById() throws Exception {
            // Given
            UserDTO dto = createUserDto(TEST_USER_ID, UserStatus.ACTIVE);
            given(userService.getUserById(any())).willReturn(dto);

            // When & Then
            mockMvc.perform(get("/api/users/{id}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.firstName").value(TEST_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TEST_LAST_NAME))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @WithMockUser
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            given(userService.getUserById(any()))
                .willThrow(new IllegalArgumentException("用户不存在"));

            // When & Then
            mockMvc.perform(get("/api/users/{id}", TEST_USER_ID))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("UUID 格式无效时返回 400")
        void shouldReturn400WhenInvalidUuid() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("更新用户 API 测试")
    class UpdateUserApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该更新用户并返回 200")
        void shouldUpdateUserAndReturn200() throws Exception {
            // Given
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");
            request.setPhone("1111111111");

            UserDTO responseDto = createUserDto(TEST_USER_ID, UserStatus.ACTIVE);
            responseDto.setFirstName("Updated");
            responseDto.setLastName("Name");

            given(userService.updateUser(any(), any(UserUpdateRequest.class))).willReturn(responseDto);

            // When & Then
            mockMvc.perform(put("/api/users/{id}", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
        }

        @Test
        @WithMockUser
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");

            given(userService.updateUser(any(), any()))
                .willThrow(new IllegalArgumentException("用户不存在"));

            // When & Then
            mockMvc.perform(put("/api/users/{id}", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("删除用户 API 测试")
    class DeleteUserApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该删除用户并返回 204")
        void shouldDeleteUserAndReturn204() throws Exception {
            // Given
            // When & Then
            mockMvc.perform(delete("/api/users/{id}", TEST_USER_ID)
                    .with(csrf()))
                .andExpect(status().isNoContent());

            then(userService).should().deleteUser(any());
        }

        @Test
        @WithMockUser
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            given(userService.deleteUser(any()))
                .willThrow(new IllegalArgumentException("用户不存在"));

            // When & Then
            mockMvc.perform(delete("/api/users/{id}", TEST_USER_ID)
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("更新用户状态 API 测试")
    class UpdateUserStatusApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该更新用户状态并返回 200")
        void shouldUpdateUserStatusAndReturn200() throws Exception {
            // Given
            UserDTO responseDto = createUserDto(TEST_USER_ID, UserStatus.INACTIVE);

            given(userService.updateUserStatus(any(), any(UserStatus.class))).willReturn(responseDto);

            // When & Then
            mockMvc.perform(patch("/api/users/{id}/status", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        }

        @Test
        @WithMockUser
        @DisplayName("状态值无效时返回 400")
        void shouldReturn400WhenInvalidStatus() throws Exception {
            // When & Then
            mockMvc.perform(patch("/api/users/{id}/status", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFoundForStatusUpdate() throws Exception {
            // Given
            given(userService.updateUserStatus(any(), any()))
                .willThrow(new IllegalArgumentException("用户不存在"));

            // When & Then
            mockMvc.perform(patch("/api/users/{id}/status", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("密码重置 API 测试")
    class ResetPasswordApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该重置密码并返回 200")
        void shouldResetPasswordAndReturn200() throws Exception {
            // Given
            // When & Then
            mockMvc.perform(post("/api/users/{id}/password/reset", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"NewPassword123!\"}"))
                .andExpect(status().isOk());

            then(userService).should().resetPassword(any(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("密码强度不足时返回 400")
        void shouldReturn400WhenWeakPassword() throws Exception {
            // Given
            given(userService.resetPassword(any(), any()))
                .willThrow(new IllegalArgumentException("密码强度不足"));

            // When & Then
            mockMvc.perform(post("/api/users/{id}/password/reset", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"weak\"}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("用户不存在时返回 404")
        void shouldReturn404WhenUserNotFoundForPasswordReset() throws Exception {
            // Given
            given(userService.resetPassword(any(), any()))
                .willThrow(new IllegalArgumentException("用户不存在"));

            // When & Then
            mockMvc.perform(post("/api/users/{id}/password/reset", TEST_USER_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"NewPassword123!\"}"))
                .andExpect(status().isNotFound());
        }
    }

    // 辅助方法
    private UserDTO createUserDto(String id, UserStatus status) {
        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setEmail(TEST_EMAIL);
        dto.setFirstName(TEST_FIRST_NAME);
        dto.setLastName(TEST_LAST_NAME);
        dto.setFullName(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        dto.setPhone("1234567890");
        dto.setStatus(status);
        dto.setEmailVerified(true);
        return dto;
    }
}
