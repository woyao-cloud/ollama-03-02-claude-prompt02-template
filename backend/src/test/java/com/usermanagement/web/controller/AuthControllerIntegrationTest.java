package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.web.dto.AuthResponse;
import com.usermanagement.web.dto.LoginRequest;
import com.usermanagement.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = createUser(
            "test@example.com",
            "password123",
            "John",
            "Doe",
            UserStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("登录成功 - 返回 Access Token 和 Refresh Token")
    void shouldLoginSuccessfully_whenValidCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
            .email("test@example.com")
            .password("password123")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType", is("Bearer")))
            .andExpect(jsonPath("$.userId").isNotEmpty())
            .andExpect(jsonPath("$.email", is("test@example.com")))
            .andExpect(jsonPath("$.firstName", is("John")))
            .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    @DisplayName("登录失败 - 邮箱不存在")
    void shouldReturn401_whenEmailNotFound() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
            .email("nonexistent@example.com")
            .password("password123")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void shouldReturn401_whenInvalidPassword() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
            .email("test@example.com")
            .password("wrongpassword")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("登录失败 - 邮箱格式不正确")
    void shouldReturn400_whenInvalidEmailFormat() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
            .email("invalid-email")
            .password("password123")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("注册成功 - 返回 Token")
    void shouldRegisterSuccessfully_whenValidRequest() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .email("newuser@example.com")
            .password("password123")
            .firstName("Jane")
            .lastName("Smith")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.email", is("newuser@example.com")))
            .andExpect(jsonPath("$.firstName", is("Jane")))
            .andExpect(jsonPath("$.lastName", is("Smith")));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void shouldReturn400_whenEmailExists() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .firstName("Jane")
            .lastName("Smith")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("注册失败 - 密码长度不足")
    void shouldReturn400_whenPasswordTooShort() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .email("newuser@example.com")
            .password("12345")
            .firstName("Jane")
            .lastName("Smith")
            .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("刷新 Token 成功")
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
            .email("test@example.com")
            .password("password123")
            .build();

        // 先登录获取 refresh token
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        AuthResponse loginResponseObj = objectMapper.readValue(loginResponse, AuthResponse.class);
        String refreshToken = loginResponseObj.getRefreshToken();

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    com.usermanagement.web.dto.RefreshTokenRequest.builder()
                        .refreshToken(refreshToken)
                        .build()
                )))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("刷新 Token 失败 - Token 为空")
    void shouldReturn400_whenRefreshTokenEmpty() throws Exception {
        // Given
        com.usermanagement.web.dto.RefreshTokenRequest request =
            com.usermanagement.web.dto.RefreshTokenRequest.builder()
                .refreshToken("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("登出成功")
    void shouldLogoutSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk());
    }

    private User createUser(String email, String password, String firstName, String lastName, UserStatus status) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setStatus(status);
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }
}
