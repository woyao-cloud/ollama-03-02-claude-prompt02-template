package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.security.CustomUserDetails;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.security.PasswordValidator;
import com.usermanagement.web.dto.AuthResponse;
import com.usermanagement.web.dto.LoginRequest;
import com.usermanagement.web.dto.RefreshTokenRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务 - 处理用户登录、刷新 Token 等认证相关操作
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountLockoutService accountLockoutService;
    private final PasswordValidator passwordValidator;
    private final AuditLogService auditLogService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        AccountLockoutService accountLockoutService,
        PasswordValidator passwordValidator,
        AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountLockoutService = accountLockoutService;
        this.passwordValidator = passwordValidator;
        this.auditLogService = auditLogService;
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 认证响应
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();
        String clientIp = getClientIp();
        String userAgent = getUserAgent();

        // 检查账户是否已被锁定
        if (accountLockoutService.isAccountLocked(email)) {
            // 记录登录失败审计日志
            auditLoginFailure(null, email, clientIp, userAgent, "账户已被锁定");
            throw new LockedException("账户已被锁定，请稍后再试");
        }

        // 查找用户 - 使用 JOIN FETCH 避免 N+1 查询
        User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(() -> {
                // 记录登录失败审计日志
                auditLoginFailure(null, email, clientIp, userAgent, "用户不存在");
                return new BadCredentialsException("邮箱或密码错误");
            });

        // 检查用户状态
        if (user.getStatus() == UserStatus.INACTIVE) {
            // 记录登录失败审计日志
            auditLoginFailure(user.getId(), email, clientIp, userAgent, "账户已被禁用");
            throw new BadCredentialsException("账户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 记录登录失败
            accountLockoutService.recordLoginFailure(email);
            // 记录登录失败审计日志
            auditLoginFailure(user.getId(), email, clientIp, userAgent, "密码错误");
            throw new BadCredentialsException("邮箱或密码错误");
        }

        // 重置失败次数并更新登录信息
        accountLockoutService.resetFailedAttempts(email);
        updateLoginInfo(user);

        // 创建认证
        List<GrantedAuthority> authorities = getUserAuthorities(user);
        CustomUserDetails userDetails = new CustomUserDetails(
            user.getId().toString(),
            user.getEmail(),
            user.getDepartmentId(),
            authorities
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            authorities
        );

        // 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // 获取角色列表
        List<String> roles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        // 记录登录成功审计日志
        auditLogService.logLoginAudit(
            user.getId(),
            user.getEmail(),
            clientIp,
            userAgent,
            "SUCCESS",
            null
        );

        logger.info("用户登录成功：{}", email);

        return AuthResponse.success(
            accessToken,
            refreshToken,
            user.getId().toString(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roles
        );
    }

    /**
     * 记录登录失败审计日志
     */
    private void auditLoginFailure(java.util.UUID userId, String email, String clientIp, String userAgent, String errorMessage) {
        try {
            auditLogService.logLoginAudit(
                userId != null ? userId : null,
                email,
                clientIp,
                userAgent,
                "FAILURE",
                errorMessage
            );
        } catch (Exception e) {
            logger.error("记录登录失败审计日志异常", e);
        }
    }

    /**
     * 刷新 Token
     *
     * @param refreshToken Refresh Token
     * @return 新的认证响应
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // 验证 Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("无效的 Refresh Token");
        }

        // 从 Token 中获取用户信息
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 查找用户
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("用户不存在"));

        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("账户状态异常");
        }

        // 生成新的 Token
        List<GrantedAuthority> authorities = getUserAuthorities(user);
        CustomUserDetails userDetails = new CustomUserDetails(userId, email, user.getDepartmentId(), authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            authorities
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        List<String> roles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return AuthResponse.success(
            newAccessToken,
            newRefreshToken,
            userId,
            email,
            user.getFirstName(),
            user.getLastName(),
            roles
        );
    }

    /**
     * 登出
     * 注意：JWT 是无状态的，登出主要是客户端删除 Token
     * 如需实现 Token 黑名单，可在此添加逻辑
     *
     * @param accessToken 访问令牌
     */
    public void logout(String accessToken) {
        // 获取当前用户并记录登出审计日志
        try {
            org.springframework.security.core.context.SecurityContext context =
                org.springframework.security.core.context.SecurityContextHolder.getContext();
            if (context.getAuthentication() != null &&
                context.getAuthentication().getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) context.getAuthentication().getPrincipal();
                String clientIp = getClientIp();
                String userAgent = getUserAgent();

                auditLogService.logLogoutAudit(
                    java.util.UUID.fromString(userDetails.getUserId()),
                    userDetails.getEmail(),
                    clientIp,
                    userAgent
                );
            }
        } catch (Exception e) {
            logger.error("记录登出审计日志异常", e);
        }

        logger.info("用户登出");
    }

    /**
     * 更新登录信息
     */
    private void updateLoginInfo(User user) {
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            logger.debug("获取客户端 IP 失败", e);
        }
        return "unknown";
    }

    /**
     * 获取 User-Agent
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            logger.debug("获取 User-Agent 失败", e);
        }
        return "unknown";
    }

    /**
     * 获取用户权限列表
     *
     * 性能优化：由于使用了 JOIN FETCH，roles 已经预先加载，不会产生 N+1 查询
     */
    private List<GrantedAuthority> getUserAuthorities(User user) {
        // 从数据库加载用户角色和权限（已通过 JOIN FETCH 预先加载）
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
            .collect(Collectors.toList());
    }
}
