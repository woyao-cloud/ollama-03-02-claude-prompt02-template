package com.usermanagement.security;

import com.usermanagement.domain.DataScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * DataPermissionInterceptor 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DataPermissionInterceptorTest {

    @Mock
    private DataScopeEvaluator evaluator;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private DataPermissionInterceptor interceptor;

    private CustomUserDetails currentUser;
    private UUID deptId;
    private UUID childDeptId;

    @BeforeEach
    void setUp() {
        interceptor = new DataPermissionInterceptor(evaluator);
        deptId = UUID.randomUUID();
        childDeptId = UUID.randomUUID();

        currentUser = new CustomUserDetails(
            UUID.randomUUID().toString(),
            "test@example.com",
            deptId,
            List.of()
        );
    }

    @Test
    @DisplayName("buildDataScopeParams - ALL 范围返回空参数")
    void shouldReturnEmptyParams_whenDataScopeIsAll() {
        // Given
        when(evaluator.evaluateDataScope(DataScope.ALL, currentUser))
            .thenReturn(List.of());

        // When
        Map<String, Object> params = interceptor.buildDataScopeParams(DataScope.ALL, currentUser);

        // Then
        assertThat(params).isEmpty();
    }

    @Test
    @DisplayName("buildDataScopeParams - SELF 范围返回 userId")
    void shouldReturnUserId_whenDataScopeIsSelf() {
        // Given
        when(evaluator.evaluateDataScope(DataScope.SELF, currentUser))
            .thenReturn(List.of());

        // When
        Map<String, Object> params = interceptor.buildDataScopeParams(DataScope.SELF, currentUser);

        // Then
        assertThat(params).containsKey("userId");
        assertThat(params.get("userId")).isEqualTo(currentUser.getUserId());
    }

    @Test
    @DisplayName("buildDataScopeParams - DEPT 范围返回 departmentIds")
    void shouldReturnDepartmentIds_whenDataScopeIsDept() {
        // Given
        List<UUID> deptIds = List.of(deptId, childDeptId);
        when(evaluator.evaluateDataScope(DataScope.DEPT, currentUser))
            .thenReturn(deptIds);

        // When
        Map<String, Object> params = interceptor.buildDataScopeParams(DataScope.DEPT, currentUser);

        // Then
        assertThat(params).containsKey("departmentIds");
        assertThat((List<?>) params.get("departmentIds")).containsExactlyInAnyOrder(deptId, childDeptId);
    }

    @Test
    @DisplayName("buildDataScopeParams - CUSTOM 范围返回空参数")
    void shouldReturnEmptyParams_whenDataScopeIsCustom() {
        // Given
        when(evaluator.evaluateDataScope(DataScope.CUSTOM, currentUser))
            .thenReturn(List.of());

        // When
        Map<String, Object> params = interceptor.buildDataScopeParams(DataScope.CUSTOM, currentUser);

        // Then
        assertThat(params).isEmpty();
    }

    @Test
    @DisplayName("getCurrentUser - 从 SecurityContext 获取用户")
    void shouldGetCurrentUserFromSecurityContext() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);

        // When
        CustomUserDetails user = interceptor.getCurrentUser();

        // Then
        assertThat(user).isEqualTo(currentUser);
    }

    @Test
    @DisplayName("getCurrentUser - 认证为空时返回 null")
    void shouldReturnNull_whenAuthenticationIsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        CustomUserDetails user = interceptor.getCurrentUser();

        // Then
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("getCurrentUser - Principal 不是 CustomUserDetails 时返回 null")
    void shouldReturnNull_whenPrincipalIsNotCustomUserDetails() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(securityContext);

        // When
        CustomUserDetails user = interceptor.getCurrentUser();

        // Then
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("shouldApplyDataScope - 无注解时返回 false")
    void shouldReturnFalse_whenNoAnnotation() {
        // When
        boolean result = interceptor.shouldApplyDataScope(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldApplyDataScope - 有 @DataPermission 注解时返回 true")
    void shouldReturnTrue_whenHasDataPermissionAnnotation() {
        // When
        boolean result = interceptor.shouldApplyDataScope(DataScope.DEPT);

        // Then
        assertThat(result).isTrue();
    }
}
