package com.usermanagement.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义条件解析器 - 解析 Spring Security SpEL 表达式
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class CustomConditionParser {

    private static final Logger logger = LoggerFactory.getLogger(CustomConditionParser.class);

    private static final Pattern HAS_PERMISSION_PATTERN = Pattern.compile("hasPermission\\(['\"]([^'\"]+)['\"],\\s*['\"]([^'\"]+)['\"]\\)");
    private static final Pattern HAS_ROLE_PATTERN = Pattern.compile("hasRole\\(['\"]([^'\"]+)['\"]\\)");
    private static final Pattern HAS_ANY_ROLE_PATTERN = Pattern.compile("hasAnyRole\\(([^)]+)\\)");
    private static final Pattern HAS_AUTHORITY_PATTERN = Pattern.compile("hasAuthority\\(['\"]([^'\"]+)['\"]\\)");
    private static final Pattern HAS_ANY_AUTHORITY_PATTERN = Pattern.compile("hasAnyAuthority\\(([^)]+)\\)");

    private final PermissionEvaluatorImpl permissionEvaluator;

    public CustomConditionParser(PermissionEvaluatorImpl permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    /**
     * 解析权限表达式
     *
     * @param authentication 认证信息
     * @param expression 表达式字符串
     * @return 表达式评估结果
     */
    public boolean parse(Authentication authentication, String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        String trimmedExpression = expression.trim();

        // hasPermission('resource', 'action')
        Matcher hasPermissionMatcher = HAS_PERMISSION_PATTERN.matcher(trimmedExpression);
        if (hasPermissionMatcher.matches()) {
            String resource = hasPermissionMatcher.group(1);
            String action = hasPermissionMatcher.group(2);
            return hasPermission(authentication, resource, action);
        }

        // hasRole('ROLE')
        Matcher hasRoleMatcher = HAS_ROLE_PATTERN.matcher(trimmedExpression);
        if (hasRoleMatcher.matches()) {
            String role = hasRoleMatcher.group(1);
            return hasRole(authentication, role);
        }

        // hasAnyRole('ROLE1', 'ROLE2')
        Matcher hasAnyRoleMatcher = HAS_ANY_ROLE_PATTERN.matcher(trimmedExpression);
        if (hasAnyRoleMatcher.matches()) {
            String roles = hasAnyRoleMatcher.group(1);
            return hasAnyRole(authentication, parseQuotedStrings(roles));
        }

        // hasAuthority('AUTHORITY')
        Matcher hasAuthorityMatcher = HAS_AUTHORITY_PATTERN.matcher(trimmedExpression);
        if (hasAuthorityMatcher.matches()) {
            String authority = hasAuthorityMatcher.group(1);
            return hasAuthority(authentication, authority);
        }

        // hasAnyAuthority('AUTH1', 'AUTH2')
        Matcher hasAnyAuthorityMatcher = HAS_ANY_AUTHORITY_PATTERN.matcher(trimmedExpression);
        if (hasAnyAuthorityMatcher.matches()) {
            String authorities = hasAnyAuthorityMatcher.group(1);
            return hasAnyAuthority(authentication, parseQuotedStrings(authorities));
        }

        // isAnonymous()
        if ("isAnonymous()".equals(trimmedExpression)) {
            return isAnonymous(authentication);
        }

        // isAuthenticated()
        if ("isAuthenticated()".equals(trimmedExpression)) {
            return isAuthenticated(authentication);
        }

        // isFullyAuthenticated()
        if ("isFullyAuthenticated()".equals(trimmedExpression)) {
            return isFullyAuthenticated(authentication);
        }

        // denyAll()
        if ("denyAll()".equals(trimmedExpression)) {
            return false;
        }

        // permitAll()
        if ("permitAll()".equals(trimmedExpression)) {
            return true;
        }

        logger.debug("未知表达式：{}", trimmedExpression);
        return false;
    }

    /**
     * 检查权限
     */
    public boolean hasPermission(Authentication authentication, String resource, String action) {
        if (authentication == null) {
            return false;
        }
        return permissionEvaluator.hasPermission(authentication, resource, action);
    }

    /**
     * 检查角色
     */
    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String normalizedRole = normalizeRole(role);

        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(normalizedRole));
    }

    /**
     * 检查是否有任何一个角色
     */
    public boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || roles == null || roles.length == 0) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (String role : roles) {
            String normalizedRole = normalizeRole(role);
            if (authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(normalizedRole))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查权限（别名）
     */
    public boolean hasAuthority(Authentication authentication, String authority) {
        return hasRole(authentication, authority);
    }

    /**
     * 检查是否有任何一个权限
     */
    public boolean hasAnyAuthority(Authentication authentication, String... authorities) {
        return hasAnyRole(authentication, authorities);
    }

    /**
     * 检查是否是匿名认证
     */
    public boolean isAnonymous(Authentication authentication) {
        if (authentication == null) {
            return true;
        }
        Object principal = authentication.getPrincipal();
        return principal == null ||
            "anonymousUser".equals(principal) ||
            principal instanceof String && ((String) principal).equals("anonymousUser");
    }

    /**
     * 检查是否已认证
     */
    public boolean isAuthenticated(Authentication authentication) {
        return !isAnonymous(authentication);
    }

    /**
     * 检查是否完全认证（非 remember-me）
     */
    public boolean isFullyAuthenticated(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        // 简化实现：只要不是匿名就认为是完全认证
        return isAuthenticated(authentication);
    }

    /**
     * 标准化角色名称 - 添加 ROLE_ 前缀
     */
    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String trimmed = role.trim();
        if (trimmed.startsWith("ROLE_")) {
            return trimmed;
        }
        return "ROLE_" + trimmed;
    }

    /**
     * 解析逗号分隔的带引号字符串
     */
    private String[] parseQuotedStrings(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[0];
        }

        return input.split(",\\s*")
            .stream()
            .map(s -> s.trim().replace("'", "").replace("\"", ""))
            .toArray(String[]::new);
    }
}
