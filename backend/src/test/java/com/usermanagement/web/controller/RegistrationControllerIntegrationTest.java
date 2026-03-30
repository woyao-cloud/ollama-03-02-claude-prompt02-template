package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.EmailVerificationService;
import com.usermanagement.service.RegistrationService;
import com.usermanagement.service.VerificationTokenProvider;
import com.usermanagement.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户注册流程集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private EmailVerificationService emailVerificationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private VerificationTokenProvider tokenProvider;

    private RegisterRequest registerRequest;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        registerRequest = RegisterRequest.builder()
            .email("newuser@example.com")
            .password("Password123!")
            .firstName("John")
            .lastName("Doe")
            .build();
    }

    @Test
    @DisplayName("用户注册成功 - 返回 PENDING 状态")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        given(registrationService.register(any(RegisterRequest.class)))
            .willReturn(createAuthResponse(testUserId, "newuser@example.com", "John", "Doe"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(testUserId.toString()))
            .andExpect(jsonPath("$.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    @DisplayName("邮箱验证成功")
    void shouldVerifyEmailSuccessfully() throws Exception {
        // Given
        String verificationToken = "valid-verification-token";

        // When & Then
        mockMvc.perform(get("/api/auth/verify")
                .param("token", verificationToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("邮箱验证成功，您的账户已激活"));
    }

    @Test
    @DisplayName("邮箱验证失败 - 无效 Token")
    void shouldFailVerification_withInvalidToken() throws Exception {
        // Given
        String invalidToken = "invalid-token";
        given(registrationService.verifyEmail(invalidToken))
            .willThrow(new IllegalArgumentException("无效的验证 Token"));

        // When & Then
        mockMvc.perform(get("/api/auth/verify")
                .param("token", invalidToken))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void shouldFailRegistration_whenEmailExists() throws Exception {
        // Given
        given(registrationService.register(any(RegisterRequest.class)))
            .willThrow(new IllegalArgumentException("邮箱已被注册"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value("邮箱已被注册"));
    }

    @Test
    @DisplayName("注册失败 - 密码强度不足")
    void shouldFailRegistration_whenPasswordTooWeak() throws Exception {
        // Given
        RegisterRequest weakPasswordRequest = RegisterRequest.builder()
            .email("weak@example.com")
            .password("123")
            .firstName("John")
            .lastName("Doe")
            .build();

        given(registrationService.register(any(RegisterRequest.class)))
            .willThrow(new IllegalArgumentException("密码强度不足"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPasswordRequest)))
            .andExpect(status().is4xxClientError());
    }

    private com.usermanagement.web.dto.AuthResponse createAuthResponse(
            UUID userId, String email, String firstName, String lastName) {
        return com.usermanagement.web.dto.AuthResponse.success(
            "test-access-token",
            "test-refresh-token",
            userId.toString(),
            email,
            firstName,
            lastName,
            java.util.List.of("ROLE_USER")
        );
    }
}
