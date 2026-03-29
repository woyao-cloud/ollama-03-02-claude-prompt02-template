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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * PermissionEvaluator 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionEvaluatorTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private Authentication authentication;

    private PermissionEvaluator permissionEvaluator;

    private CustomUserDetails userDetails;
    private User user;
    private Role role;
    private Permission permission;

    @BeforeEach
    void setUp() {
        permissionEvaluator = new PermissionEvaluatorImpl(
            permissionRepository,
            rolePermissionRepository,
            roleRepository,
            userRoleRepository
        );

        // 创建测试用户
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setDepartmentId(UUID.randomUUID());

        // 创建测试角色
        role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("ROLE_USER");
        role.setDataScope(DataScope.ALL);

        // 创建测试权限
        permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode("user:read");
        permission.setResource("user");
        permission.setAction("read");
        permission.setType(PermissionType.OPERATION);

        // 创建 UserDetails
        userDetails = new CustomUserDetails(
            user.getId().toString(),
            user.getEmail(),
            user.getDepartmentId(),
            List.of()
        );
    }

    @Test
    @DisplayName("hasPermission - 用户拥有权限时返回 true")
    void shouldReturnTrue_whenUserHasPermission() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), role.getId())));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(createRolePermission(role.getId(), permission.getId())));
        when(permissionRepository.findAllById(List.of(permission.getId())))
            .thenReturn(List.of(permission));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            "user",
            "read"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("hasPermission - 用户没有权限时返回 false")
    void shouldReturnFalse_whenUserDoesNotHavePermission() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), role.getId())));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of()); // 角色没有权限

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            "user",
            "delete"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("hasPermission - ADMIN 角色自动拥有所有权限")
    void shouldReturnTrue_whenUserHasAdminRole() {
        // Given
        Role adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setCode("ROLE_ADMIN");
        adminRole.setDataScope(DataScope.ALL);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), adminRole.getId())));
        when(roleRepository.findAllById(List.of(adminRole.getId())))
            .thenReturn(List.of(adminRole));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            "any",
            "action"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("hasPermission - 目标对象为 User 实体时检查数据权限")
    void shouldCheckDataPermission_whenTargetIsUserEntity() {
        // Given
        User targetUser = new User();
        targetUser.setId(UUID.randomUUID());
        targetUser.setEmail("target@example.com");
        targetUser.setDepartmentId(user.getDepartmentId()); // 同一部门

        role.setDataScope(DataScope.DEPT);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), role.getId())));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(createRolePermission(role.getId(), permission.getId())));
        when(permissionRepository.findAllById(List.of(permission.getId())))
            .thenReturn(List.of(permission));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            targetUser,
            "read"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("hasPermission - SELF 数据权限只能访问自己的数据")
    void shouldReturnFalse_whenDataScopeIsSelfAndTargetIsNotOwn() {
        // Given
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");
        otherUser.setDepartmentId(UUID.randomUUID()); // 不同部门

        role.setDataScope(DataScope.SELF);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), role.getId())));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(createRolePermission(role.getId(), permission.getId())));
        when(permissionRepository.findAllById(List.of(permission.getId())))
            .thenReturn(List.of(permission));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            otherUser,
            "read"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("hasPermission - 认证为 null 时返回 false")
    void shouldReturnFalse_whenAuthenticationIsNull() {
        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            null,
            "user",
            "read"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("hasPermission - principal 不是 CustomUserDetails 时返回 false")
    void shouldReturnFalse_whenPrincipalIsNotCustomUserDetails() {
        // Given
        when(authentication.getPrincipal()).thenReturn("not-a-custom-user-details");

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            "user",
            "read"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("hasPermission - 权限代码匹配时返回 true")
    void shouldReturnTrue_whenPermissionCodeMatches() {
        // Given
        Permission perm = new Permission();
        perm.setId(UUID.randomUUID());
        perm.setCode("user:read");
        perm.setResource("user");
        perm.setAction("read");
        perm.setType(PermissionType.OPERATION);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), role.getId())));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(createRolePermission(role.getId(), perm.getId())));
        when(permissionRepository.findAllById(List.of(perm.getId())))
            .thenReturn(List.of(perm));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            "user",
            "read"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("hasPermission - ALL 数据权限可以访问所有数据")
    void shouldReturnTrue_whenDataScopeIsAll() {
        // Given
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");
        otherUser.setDepartmentId(UUID.randomUUID()); // 不同部门

        role.setDataScope(DataScope.ALL);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), role.getId())));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(createRolePermission(role.getId(), permission.getId())));
        when(permissionRepository.findAllById(List.of(permission.getId())))
            .thenReturn(List.of(permission));

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            otherUser,
            "read"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("getUserPermissions - 获取用户所有权限代码")
    void shouldGetAllUserPermissions() {
        // Given
        Permission perm1 = new Permission();
        perm1.setId(UUID.randomUUID());
        perm1.setCode("user:read");
        perm1.setResource("user");
        perm1.setAction("read");

        Permission perm2 = new Permission();
        perm2.setId(UUID.randomUUID());
        perm2.setCode("user:write");
        perm2.setResource("user");
        perm2.setAction("write");

        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of(createUserRole(user.getId(), role.getId())));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
        when(rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId())))
            .thenReturn(List.of(
                createRolePermission(role.getId(), perm1.getId()),
                createRolePermission(role.getId(), perm2.getId())
            ));
        when(permissionRepository.findAllById(List.of(perm1.getId(), perm2.getId())))
            .thenReturn(List.of(perm1, perm2));

        // When
        Set<String> permissions = ((PermissionEvaluatorImpl) permissionEvaluator)
            .getUserPermissions(user.getId());

        // Then
        assertThat(permissions).containsExactlyInAnyOrder("user:read", "user:write");
    }

    @Test
    @DisplayName("hasPermission - 用户没有角色时返回 false")
    void shouldReturnFalse_whenUserHasNoRoles() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findAllByUserId(user.getId()))
            .thenReturn(List.of());

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication,
            "user",
            "read"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    private UserRole createUserRole(UUID userId, UUID roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }

    private RolePermission createRolePermission(UUID roleId, UUID permissionId) {
        RolePermission rp = new RolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        return rp;
    }
}
