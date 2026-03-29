package com.usermanagement.security;

import com.usermanagement.domain.*;
import com.usermanagement.repository.PermissionRepository;
import com.usermanagement.repository.RolePermissionRepository;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * CustomConditionParser 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CustomConditionParserTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private Authentication authentication;

    private CustomConditionParser parser;

    private CustomUserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        PermissionEvaluatorImpl permissionEvaluator = new PermissionEvaluatorImpl(
            permissionRepository,
            rolePermissionRepository,
            roleRepository,
            userRoleRepository
        );

        parser = new CustomConditionParser(permissionEvaluator);

        // 创建测试用户
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setDepartmentId(UUID.randomUUID());

        // 创建 UserDetails
        userDetails = new CustomUserDetails(
            user.getId().toString(),
            user.getEmail(),
            user.getDepartmentId(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("parse - 解析 permitAll 表达式")
    void shouldParsePermitAllExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, "permitAll()");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - 解析 denyAll 表达式")
    void shouldParseDenyAllExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, "denyAll()");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("parse - 解析 isAuthenticated 表达式")
    void shouldParseIsAuthenticatedExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, "isAuthenticated()");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - 解析 isAnonymous 表达式返回 false")
    void shouldParseIsAnonymousExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, "isAnonymous()");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("parse - 解析空表达式返回 true")
    void shouldReturnTrueForEmptyExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, "");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - 解析 null 表达式返回 true")
    void shouldReturnTrueForNullExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - 解析未知表达式返回 false")
    void shouldReturnFalseForUnknownExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, "unknownExpression()");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasRole - 用户有角色时返回 true")
    void shouldReturnTrueWhenUserHasRole() {
        // Given
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.hasRole(authentication, "USER");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasRole - 用户没有角色时返回 false")
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
        // Given
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.hasRole(authentication, "ADMIN");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasAnyRole - 用户有任何一个角色时返回 true")
    void shouldReturnTrueWhenUserHasAnyRole() {
        // Given
        when(authentication.getAuthorities())
            .thenReturn(List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_EDITOR")
            ));

        // When
        boolean result = parser.hasAnyRole(authentication, "ADMIN", "EDITOR");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasAnyRole - 用户没有匹配角色时返回 false")
    void shouldReturnFalseWhenUserHasNoMatchingRole() {
        // Given
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.hasAnyRole(authentication, "ADMIN", "MANAGER");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasAuthority - 用户有权限时返回 true")
    void shouldReturnTrueWhenUserHasAuthority() {
        // Given
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.hasAuthority(authentication, "ROLE_USER");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasAnyAuthority - 用户有任何一个权限时返回 true")
    void shouldReturnTrueWhenUserHasAnyAuthority() {
        // Given
        when(authentication.getAuthorities())
            .thenReturn(List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("user:read")
            ));

        // When
        boolean result = parser.hasAnyAuthority(authentication, "ROLE_ADMIN", "ROLE_USER");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isFullyAuthenticated - 已认证用户返回 true")
    void shouldReturnTrueForFullyAuthenticated() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = parser.parse(authentication, "isFullyAuthenticated()");

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_USER", "USER"})
    @DisplayName("hasRole - 支持带 ROLE_ 前缀和不带前缀")
    void shouldSupportRoleWithAndWithoutPrefix(String role) {
        // Given
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.hasRole(authentication, role);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasPermission - 用户有权限时返回 true")
    void shouldReturnTrueWhenUserHasPermission() {
        // Given
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode("user:read");
        permission.setResource("user");
        permission.setAction("read");

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("ROLE_USER");
        role.setDataScope(DataScope.ALL);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(userRole));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(createRolePermission(role.getId(), permission.getId())));
        when(permissionRepository.findAllById(List.of(permission.getId())))
            .thenReturn(List.of(permission));

        // When
        boolean result = parser.hasPermission(authentication, "user", "read");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - hasPermission 表达式")
    void shouldParseHasPermissionExpression() {
        // Given
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode("user:read");
        permission.setResource("user");
        permission.setAction("read");

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("ROLE_USER");
        role.setDataScope(DataScope.ALL);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(userRole));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(createRolePermission(role.getId(), permission.getId())));
        when(permissionRepository.findAllById(List.of(permission.getId())))
            .thenReturn(List.of(permission));

        // When
        boolean result = parser.parse(authentication, "hasPermission('user', 'read')");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - hasRole 表达式")
    void shouldParseHasRoleExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.parse(authentication, "hasRole('USER')");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - hasAnyRole 表达式")
    void shouldParseHasAnyRoleExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.parse(authentication, "hasAnyRole('ADMIN', 'USER')");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - hasAuthority 表达式")
    void shouldParseHasAuthorityExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.parse(authentication, "hasAuthority('ROLE_USER')");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("parse - hasAnyAuthority 表达式")
    void shouldParseHasAnyAuthorityExpression() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        boolean result = parser.parse(authentication, "hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')");

        // Then
        assertThat(result).isTrue();
    }

    private RolePermission createRolePermission(UUID roleId, UUID permissionId) {
        RolePermission rp = new RolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        return rp;
    }
}
