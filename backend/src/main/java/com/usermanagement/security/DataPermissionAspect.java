package com.usermanagement.security;

import com.usermanagement.domain.*;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRoleRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限切面 - 自动过滤数据权限
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Aspect
@Component
public class DataPermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(DataPermissionAspect.class);

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public DataPermissionAspect(
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * 环绕通知 - 处理数据权限过滤
     */
    @Around("@annotation(com.usermanagement.security.DataPermission)")
    public Object aroundDataPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DataPermission dataPermission = method.getAnnotation(DataPermission.class);

        if (dataPermission == null) {
            return joinPoint.proceed();
        }

        // 获取当前用户
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) {
            logger.warn("未找到当前用户，跳过数据权限过滤");
            return joinPoint.proceed();
        }

        // 获取用户的数据权限范围
        DataScope dataScope = getUserDataScope(currentUser);

        // 执行目标方法
        Object result = joinPoint.proceed();

        // 根据数据范围过滤结果
        return filterByDataScope(result, dataScope, currentUser);
    }

    /**
     * 获取当前用户的数据权限范围
     */
    private DataScope getUserDataScope(CustomUserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUserId());
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);

        if (userRoles.isEmpty()) {
            return DataScope.SELF;
        }

        List<UUID> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());

        List<Role> roles = roleRepository.findAllById(roleIds);

        // 如果有 ADMIN 角色，返回 ALL
        for (Role role : roles) {
            if ("ROLE_ADMIN".equals(role.getCode())) {
                return DataScope.ALL;
            }
        }

        // 返回最高权限范围（优先级：ALL > DEPT > CUSTOM > SELF）
        if (roles.stream().anyMatch(r -> r.getDataScope() == DataScope.ALL)) {
            return DataScope.ALL;
        }
        if (roles.stream().anyMatch(r -> r.getDataScope() == DataScope.DEPT)) {
            return DataScope.DEPT;
        }
        if (roles.stream().anyMatch(r -> r.getDataScope() == DataScope.CUSTOM)) {
            return DataScope.CUSTOM;
        }

        return DataScope.SELF;
    }

    /**
     * 根据数据范围过滤结果
     */
    private Object filterByDataScope(Object result, DataScope dataScope, CustomUserDetails currentUser) {
        if (result == null) {
            return null;
        }

        // 如果是列表
        if (result instanceof List) {
            List<?> list = (List<?>) result;
            if (list.isEmpty() || !(list.get(0) instanceof User)) {
                return result;
            }

            return applyDataScope(
                list.stream().map(u -> (User) u).collect(Collectors.toList()),
                dataScope,
                currentUser
            );
        }

        // 如果是单个 User 对象
        if (result instanceof User) {
            User user = (User) result;
            if (!matchesDataScope(user, dataScope, currentUser)) {
                return null;
            }
            return user;
        }

        // 其他类型不处理
        return result;
    }

    /**
     * 应用数据范围过滤
     */
    public List<User> applyDataScope(List<User> users, DataScope dataScope, CustomUserDetails currentUser) {
        if (dataScope == DataScope.ALL) {
            return users;
        }

        return users.stream()
            .filter(user -> matchesDataScope(user, dataScope, currentUser))
            .collect(Collectors.toList());
    }

    /**
     * 判断用户是否匹配数据范围
     */
    private boolean matchesDataScope(User user, DataScope dataScope, CustomUserDetails currentUser) {
        UUID currentUserId = UUID.fromString(currentUser.getUserId());

        // 自己的数据总是匹配
        if (Objects.equals(user.getId(), currentUserId)) {
            return true;
        }

        switch (dataScope) {
            case ALL:
                return true;
            case SELF:
                return false;
            case DEPT:
                return Objects.equals(user.getDepartmentId(), currentUser.getDepartmentId());
            case CUSTOM:
                // 自定义范围需要额外配置，当前实现返回 false
                return false;
            default:
                return false;
        }
    }

    /**
     * 从 SecurityContext 获取当前用户
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        } catch (Exception e) {
            logger.debug("无法获取当前用户：{}", e.getMessage());
        }
        return null;
    }
}
