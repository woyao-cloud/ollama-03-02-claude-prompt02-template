package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.security.CustomUserDetails;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.security.PasswordValidator;
import com.usermanagement.web.dto.AuthResponse;
import com.usermanagement.web.dto.LoginRequest;
import com.usermanagement.web.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务 - 处理用户登录、注册、登出等认证相关操作
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountLockoutService accountLockoutService;
    private final PasswordValidator passwordValidator;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtTokenProvider jwtTokenProvider,
        AccountLockoutService accountLockoutService,
        PasswordValidator passwordValidator
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountLockoutService = accountLockoutService;
        this.passwordValidator = passwordValidator;
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

        // 检查账户是否已被锁定
        if (accountLockoutService.isAccountLocked(email)) {
            throw new LockedException("账户已被锁定，请稍后再试");
        }

        // 查找用户
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("邮箱或密码错误"));

        // 检查用户状态
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadCredentialsException("账户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 记录登录失败
            accountLockoutService.recordLoginFailure(email);
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

        // 创建认证
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
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
     * 登出
     * 注意：JWT 是无状态的，登出主要是客户端删除 Token
     * 如需实现 Token 黑名单，可在此添加逻辑
     *
     * @param accessToken 访问令牌
     */
    public void logout(String accessToken) {
        // TODO: 实现 Token 黑名单机制
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
     * 获取用户权限列表
     * 注意：当前实现仅返回默认角色，后续需要从数据库加载实际角色
     */
    private List<GrantedAuthority> getUserAuthorities(User user) {
        // TODO: 从数据库加载用户角色和权限
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
