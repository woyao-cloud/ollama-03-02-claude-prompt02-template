package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.security.CustomUserDetails;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.security.PasswordValidator;
import com.usermanagement.web.dto.AuthResponse;
import com.usermanagement.web.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户注册服务 - 处理用户自助注册流程
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordValidator passwordValidator;
    private final EmailVerificationService emailVerificationService;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            PasswordValidator passwordValidator,
            EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordValidator = passwordValidator;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 认证响应
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 验证密码强度
        passwordValidator.validate(request.getPassword());

        // 创建用户
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .status(UserStatus.PENDING)
            .emailVerified(false)
            .failedLoginAttempts(0)
            .build();

        user = userRepository.save(user);
        logger.info("用户注册成功：{}", user.getEmail());

        // 发送验证邮件
        try {
            emailVerificationService.sendVerificationEmail(user);
        } catch (Exception e) {
            logger.error("发送验证邮件失败：{}", user.getEmail(), e);
            // 邮件发送失败不影响注册流程，用户稍后可重新发送验证邮件
        }

        // 创建认证
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails userDetails = new CustomUserDetails(
            user.getId().toString(),
            user.getEmail(),
            user.getDepartmentId(),
            authorities
        );

        // 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
            )
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
            )
        );

        return AuthResponse.success(
            accessToken,
            refreshToken,
            user.getId().toString(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            List.of("ROLE_USER")
        );
    }

    /**
     * 验证邮箱
     *
     * @param token 验证 Token
     */
    @Transactional
    public void verifyEmail(String token) {
        // 验证 Token 并获取用户 ID
        Optional<UUID> userIdOpt = emailVerificationService.verifyToken(token);
        if (userIdOpt.isEmpty()) {
            throw new IllegalArgumentException("无效的验证 Token");
        }

        UUID userId = userIdOpt.get();

        // 查找用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 检查用户是否已激活
        if (user.getStatus() == UserStatus.ACTIVE && user.getEmailVerified()) {
            throw new IllegalArgumentException("用户已激活");
        }

        // 更新用户状态
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);

        logger.info("用户邮箱验证成功：{}", user.getEmail());
    }
}
