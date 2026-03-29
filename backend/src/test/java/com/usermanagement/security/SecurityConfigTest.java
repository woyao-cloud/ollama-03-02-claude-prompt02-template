package com.usermanagement.security;

import com.usermanagement.config.AppProperties;
import com.usermanagement.domain.*;
import com.usermanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityConfig 单元测试
 */
class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsServiceImpl userDetailsService;
    private PermissionEvaluatorImpl permissionEvaluator;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getJwt().setSecret("test-secret-key-for-testing-only-12345678901234567890");
        appProperties.getJwt().setExpiration(3600000L);
        appProperties.getJwt().setRefreshExpiration(86400000L);
        appProperties.getJwt().setIssuer("test-issuer");

        jwtTokenProvider = new JwtTokenProvider(appProperties);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
        userDetailsService = new UserDetailsServiceImpl(null); // Mock for unit test

        // 创建 PermissionEvaluatorImpl 的 stub 依赖
        PermissionRepository permissionRepository = new PermissionRepositoryStub();
        RolePermissionRepository rolePermissionRepository = new RolePermissionRepositoryStub();
        RoleRepository roleRepository = new RoleRepositoryStub();
        UserRoleRepository userRoleRepository = new UserRoleRepositoryStub();

        permissionEvaluator = new PermissionEvaluatorImpl(
            permissionRepository,
            rolePermissionRepository,
            roleRepository,
            userRoleRepository
        );

        securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService, permissionEvaluator);
    }

    @Test
    @DisplayName("PasswordEncoder 使用 BCrypt 强度 12")
    void shouldUseBCryptWithStrength12() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder.encode("password")).startsWith("$2a$12$");
    }

    @Test
    @DisplayName("PasswordEncoder 匹配密码")
    void shouldMatchPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("PasswordEncoder 不匹配错误密码")
    void shouldNotMatchWrongPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Then
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("MethodSecurityExpressionHandler 配置正确")
    void shouldConfigureMethodSecurityExpressionHandler() {
        // When
        MethodSecurityExpressionHandler expressionHandler = securityConfig.methodSecurityExpressionHandler();

        // Then
        assertThat(expressionHandler).isNotNull();
    }

    // Stub implementations for testing
    static class PermissionRepositoryStub implements PermissionRepository {
        @Override
        public Optional<Permission> findByCode(String code) {
            return Optional.empty();
        }

        @Override
        public java.util.List<Permission> findAllById(Iterable<UUID> ids) {
            return java.util.List.of();
        }

        @Override
        public <S extends Permission> S save(S entity) {
            return null;
        }

        @Override
        public <S extends Permission> java.util.List<S> saveAll(Iterable<S> entities) {
            return java.util.List.of();
        }

        @Override
        public Optional<Permission> findById(UUID uuid) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(UUID uuid) {
            return false;
        }

        @Override
        public java.util.List<Permission> findAll() {
            return java.util.List.of();
        }

        @Override
        public java.util.List<Permission> findAllById(java.util.List<UUID> uuids) {
            return java.util.List.of();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(UUID uuid) {
        }

        @Override
        public void delete(Permission entity) {
        }

        @Override
        public void deleteAll(Iterable<? extends Permission> entities) {
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> uuids) {
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public java.util.List<Permission> findByStatus(PermissionStatus status) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<Permission> findByType(PermissionType type) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<Permission> findByResource(String resource) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<Permission> findByParentId(UUID parentId) {
            return java.util.List.of();
        }

        @Override
        public boolean existsByCode(String code) {
            return false;
        }

        @Override
        public java.util.List<Permission> findByResourceAndStatus(String resource, PermissionStatus status) {
            return java.util.List.of();
        }
    }

    static class RolePermissionRepositoryStub implements RolePermissionRepository {
        @Override
        public java.util.List<RolePermission> findByRoleId(UUID roleId) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<RolePermission> findAllByRoleIdIn(java.util.List<UUID> roleIds) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<RolePermission> findByPermissionId(UUID permissionId) {
            return java.util.List.of();
        }

        @Override
        public void deleteByRoleId(UUID roleId) {
        }

        @Override
        public void deleteByPermissionId(UUID permissionId) {
        }

        @Override
        public boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId) {
            return false;
        }

        @Override
        public <S extends RolePermission> S save(S entity) {
            return null;
        }

        @Override
        public <S extends RolePermission> java.util.List<S> saveAll(Iterable<S> entities) {
            return java.util.List.of();
        }

        @Override
        public Optional<RolePermission> findById(UUID uuid) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(UUID uuid) {
            return false;
        }

        @Override
        public java.util.List<RolePermission> findAll() {
            return java.util.List.of();
        }

        @Override
        public java.util.List<RolePermission> findAllById(java.util.List<UUID> uuids) {
            return java.util.List.of();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void delete(RolePermission entity) {
        }

        @Override
        public void deleteAll(Iterable<? extends RolePermission> entities) {
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> uuids) {
        }

        @Override
        public void deleteAll() {
        }
    }

    static class RoleRepositoryStub implements RoleRepository {
        @Override
        public Optional<Role> findByCode(String code) {
            return Optional.empty();
        }

        @Override
        public Optional<Role> findByName(String name) {
            return Optional.empty();
        }

        @Override
        public java.util.List<Role> findByStatus(RoleStatus status) {
            return java.util.List.of();
        }

        @Override
        public boolean existsByCode(String code) {
            return false;
        }

        @Override
        public boolean existsByName(String name) {
            return false;
        }

        @Override
        public <S extends Role> S save(S entity) {
            return null;
        }

        @Override
        public <S extends Role> java.util.List<S> saveAll(Iterable<S> entities) {
            return java.util.List.of();
        }

        @Override
        public Optional<Role> findById(UUID uuid) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(UUID uuid) {
            return false;
        }

        @Override
        public java.util.List<Role> findAll() {
            return java.util.List.of();
        }

        @Override
        public java.util.List<Role> findAllById(java.util.List<UUID> uuids) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<Role> findAllById(Iterable<UUID> iterable) {
            return java.util.List.of();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(UUID uuid) {
        }

        @Override
        public void delete(Role entity) {
        }

        @Override
        public void deleteAll(Iterable<? extends Role> entities) {
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> uuids) {
        }

        @Override
        public void deleteAll() {
        }
    }

    static class UserRoleRepositoryStub implements UserRoleRepository {
        @Override
        public java.util.List<UserRole> findByUserId(UUID userId) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<UserRole> findAllByUserId(UUID userId) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<UserRole> findByRoleId(UUID roleId) {
            return java.util.List.of();
        }

        @Override
        public void deleteByUserId(UUID userId) {
        }

        @Override
        public void deleteByRoleId(UUID roleId) {
        }

        @Override
        public boolean existsByUserIdAndRoleId(UUID userId, UUID roleId) {
            return false;
        }

        @Override
        public <S extends UserRole> S save(S entity) {
            return null;
        }

        @Override
        public <S extends UserRole> java.util.List<S> saveAll(Iterable<S> entities) {
            return java.util.List.of();
        }

        @Override
        public Optional<UserRole> findById(UUID uuid) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(UUID uuid) {
            return false;
        }

        @Override
        public java.util.List<UserRole> findAll() {
            return java.util.List.of();
        }

        @Override
        public java.util.List<UserRole> findAllById(java.util.List<UUID> uuids) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<UserRole> findAllById(Iterable<UUID> iterable) {
            return java.util.List.of();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(UUID uuid) {
        }

        @Override
        public void delete(UserRole entity) {
        }

        @Override
        public void deleteAll(Iterable<? extends UserRole> entities) {
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> uuids) {
        }

        @Override
        public void deleteAll() {
        }
    }
}
