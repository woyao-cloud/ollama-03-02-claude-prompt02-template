package com.usermanagement.web.controller;

import com.usermanagement.service.AuthService;
import com.usermanagement.web.dto.AuthResponse;
import com.usermanagement.web.dto.LoginRequest;
import com.usermanagement.web.dto.RefreshTokenRequest;
import com.usermanagement.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器 - 处理用户认证相关 API
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     *
     * POST /api/auth/login
     *
     * @param request 登录请求
     * @return 认证响应
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("用户登录：{}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     *
     * POST /api/auth/register
     *
     * @param request 注册请求
     * @return 认证响应
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("用户注册：{}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 刷新 Token
     *
     * POST /api/auth/refresh
     *
     * @param request 刷新请求
     * @return 新的认证响应
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("刷新 Token");
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登出
     *
     * POST /api/auth/logout
     *
     * @return 空响应
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            logger.info("用户登出：{}", userDetails.getEmail());
        }
        // 清除安全上下文
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
