package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.service.ApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ApprovalController 集成测试
 */
@WebMvcTest(ApprovalController.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalService approvalService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("获取待审批用户列表")
    @WithMockUser(roles = "ADMIN")
    void shouldGetPendingUsersSuccessfully() throws Exception {
        // Given
        User user1 = createUser(testUserId, UserStatus.PENDING);
        User user2 = createUser(UUID.randomUUID(), UserStatus.PENDING);
        Page<User> page = new PageImpl<>(Arrays.asList(user1, user2), PageRequest.of(0, 10), 2);

        given(approvalService.getPendingUsers(any())).willReturn(page);

        // When & Then
        mockMvc.perform(get("/api/approvals/pending")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(2));

        then(approvalService).should().getPendingUsers(any());
    }

    @Test
    @DisplayName("获取待审批用户详情")
    @WithMockUser(roles = "ADMIN")
    void shouldGetPendingUserSuccessfully() throws Exception {
        // Given
        User user = createUser(testUserId, UserStatus.PENDING);
        given(approvalService.getPendingUserById(testUserId)).willReturn(java.util.Optional.of(user));

        // When & Then
        mockMvc.perform(get("/api/approvals/pending/{userId}", testUserId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("pending@example.com"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("审批通过用户")
    @WithMockUser(roles = "ADMIN")
    void shouldApproveUserSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/approvals/{userId}/approve", testUserId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("用户审批通过"));

        then(approvalService).should().approveUser(testUserId);
    }

    @Test
    @DisplayName("审批拒绝用户 - 带原因")
    @WithMockUser(roles = "ADMIN")
    void shouldRejectUserWithReason() throws Exception {
        // Given
        Map<String, String> request = Map.of("reason", "资料不完整");

        // When & Then
        mockMvc.perform(post("/api/approvals/{userId}/reject", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("用户审批拒绝"));

        then(approvalService).should().rejectUser(testUserId, "资料不完整");
    }

    @Test
    @DisplayName("审批拒绝用户 - 无原因")
    @WithMockUser(roles = "ADMIN")
    void shouldRejectUserWithoutReason() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/approvals/{userId}/reject", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("用户审批拒绝"));

        then(approvalService).should().rejectUser(testUserId, "未提供原因");
    }

    @Test
    @DisplayName("获取待审批用户 - 未授权")
    void shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/approvals/pending"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("审批用户 - 普通用户无权限")
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_whenNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/approvals/{userId}/approve", testUserId)
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    private User createUser(UUID id, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail("pending@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setStatus(status);
        user.setEmailVerified(false);
        return user;
    }
}
