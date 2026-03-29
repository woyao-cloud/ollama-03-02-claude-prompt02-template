package com.usermanagement.security;

import com.usermanagement.domain.*;
import com.usermanagement.repository.PermissionRepository;
import com.usermanagement.repository.RolePermissionRepository;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 权限评估器实现 - 用于 Spring Security @PreAuthorize 表达式
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(PermissionEvaluatorImpl.class);

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public PermissionEvaluatorImpl(
        PermissionRepository permissionRepository,
        RolePermissionRepository rolePermissionRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String permissionStr = (String) permission;

        // 如果目标是 User 实体，检查数据权限
        if (targetDomainObject instanceof User) {
            return hasDataPermission(userDetails, (User) targetDomainObject, permissionStr);
        }

        // 检查操作权限
        return hasOperationPermission(userDetails, permissionStr);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String permissionStr = (String) permission;

        // 检查操作权限
        return hasOperationPermission(userDetails, permissionStr);
    }

    /**
     * 检查用户是否拥有指定操作权限
     */
    private boolean hasOperationPermission(CustomUserDetails userDetails, String action) {
        UUID userId = UUID.fromString(userDetails.getUserId());

        // 获取用户所有角色
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        if (userRoles.isEmpty()) {
            return false;
        }

        // 获取所有角色 ID
        List<UUID> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());

        // 加载完整角色信息并检查是否有 ADMIN 角色
        List<Role> roles = roleRepository.findAllById(roleIds);
        for (Role role : roles) {
            if (isAdminRole(role)) {
                return true;
            }
        }

        // 获取所有角色权限关联
        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleIdIn(roleIds);
        if (rolePermissions.isEmpty()) {
            return false;
        }

        // 获取所有权限 ID
        List<UUID> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toList());

        // 加载权限详情
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        // 检查是否有所需权限
        return permissions.stream()
            .anyMatch(p -> p.getAction() != null && p.getAction().equalsIgnoreCase(action));
    }

    /**
     * 检查数据权限
     */
    private boolean hasDataPermission(CustomUserDetails userDetails, User targetUser, String action) {
        UUID currentUserId = UUID.fromString(userDetails.getUserId());

        // 如果是自己的数据，直接返回 true
        if (Objects.equals(currentUserId, targetUser.getId())) {
            return true;
        }

        // 获取用户所有角色
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(currentUserId);
        if (userRoles.isEmpty()) {
            return false;
        }

        // 加载完整角色信息
        List<UUID> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());
        List<Role> roles = roleRepository.findAllById(roleIds);

        // 检查角色的数据权限范围
        for (Role role : roles) {
            // 检查是否是 ADMIN 角色
            if (isAdminRole(role)) {
                return true;
            }

            // 根据数据范围判断
            DataScope dataScope = role.getDataScope();
            if (dataScope == null) {
                dataScope = DataScope.ALL;
            }

            switch (dataScope) {
                case ALL:
                    return true;
                case DEPT:
                    // 检查是否同一部门
                    if (Objects.equals(userDetails.getDepartmentId(), targetUser.getDepartmentId())) {
                        return true;
                    }
                    break;
                case SELF:
                    // SELF 只能访问自己的数据，已在上面检查
                    return false;
                case CUSTOM:
                    // 自定义数据权限需要额外配置
                    // 简化实现：返回 false
                    return false;
                default:
                    return false;
            }
        }

        return false;
    }

    /**
     * 判断是否是管理员角色
     */
    private boolean isAdminRole(Role role) {
        return "ROLE_ADMIN".equals(role.getCode());
    }

    /**
     * 获取用户的所有权限代码
     */
    public Set<String> getUserPermissions(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        if (userRoles.isEmpty()) {
            return Set.of();
        }

        List<UUID> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());

        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleIdIn(roleIds);
        if (rolePermissions.isEmpty()) {
            return Set.of();
        }

        List<UUID> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toList());

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        return permissions.stream()
            .map(Permission::getCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
}
