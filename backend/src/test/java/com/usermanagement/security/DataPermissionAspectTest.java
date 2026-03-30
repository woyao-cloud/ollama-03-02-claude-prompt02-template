package com.usermanagement.security;

import com.usermanagement.domain.*;
import com.usermanagement.repository.RolePermissionRepository;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRoleRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * DataPermissionAspect 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DataPermissionAspectTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private DataScopeEvaluator dataScopeEvaluator;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private DataPermissionAspect aspect;

    private CustomUserDetails userDetails;
    private User currentUser;

    @BeforeEach
    void setUp() {
        aspect = new DataPermissionAspect(roleRepository, userRoleRepository, dataScopeEvaluator);

        // 创建测试用户
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("test@example.com");
        currentUser.setDepartmentId(UUID.randomUUID());

        // 创建 UserDetails
        userDetails = new CustomUserDetails(
            currentUser.getId().toString(),
            currentUser.getEmail(),
            currentUser.getDepartmentId(),
            List.of()
        );

        // 设置 SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("aroundDataPermission - 没有 @DataPermission 注解时直接执行")
    void shouldProceed_whenNoDataPermissionAnnotation() throws Throwable {
        // Given
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("methodWithoutAnnotation"));
        when(joinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.aroundDataPermission(joinPoint);

        // Then
        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("aroundDataPermission - ALL 数据权限不过滤")
    void shouldNotFilter_whenDataScopeIsAll() throws Throwable {
        // Given
        setupDataPermissionAnnotation();
        setupUserRole(DataScope.ALL);

        List<User> allUsers = List.of(currentUser, createUser("other@example.com"));
        when(joinPoint.proceed()).thenReturn(allUsers);

        // When
        Object result = aspect.aroundDataPermission(joinPoint);

        // Then
        assertThat((List<?>) result).isEqualTo(allUsers);
    }

    @Test
    @DisplayName("aroundDataPermission - SELF 数据权限只返回自己的数据")
    void shouldFilterOnlySelf_whenDataScopeIsSelf() throws Throwable {
        // Given
        setupDataPermissionAnnotation();
        setupUserRole(DataScope.SELF);

        User otherUser = createUser("other@example.com");
        List<User> allUsers = List.of(currentUser, otherUser);
        when(joinPoint.proceed()).thenReturn(allUsers);

        // When
        Object result = aspect.aroundDataPermission(joinPoint);

        // Then
        assertThat((List<?>) result).containsOnly(currentUser);
    }

    @Test
    @DisplayName("aroundDataPermission - DEPT 数据权限返回本部门数据")
    void shouldFilterByDepartment_whenDataScopeIsDept() throws Throwable {
        // Given
        setupDataPermissionAnnotation();
        setupUserRole(DataScope.DEPT);

        User sameDeptUser = createUser("same@example.com", currentUser.getDepartmentId());
        User otherDeptUser = createUser("other@example.com", UUID.randomUUID());
        List<User> allUsers = List.of(currentUser, sameDeptUser, otherDeptUser);
        when(joinPoint.proceed()).thenReturn(allUsers);

        // When
        Object result = aspect.aroundDataPermission(joinPoint);

        // Then
        assertThat((List<?>) result).containsExactlyInAnyOrder(currentUser, sameDeptUser);
    }

    @Test
    @DisplayName("applyDataScope - SELF 范围过滤单个用户")
    void shouldApplySelfScopeToSingleUser() {
        // Given
        User ownUser = currentUser;
        User otherUser = createUser("other@example.com");

        // When & Then
        assertThat(aspect.applyDataScope(List.of(ownUser, otherUser), DataScope.SELF, currentUser))
            .containsOnly(ownUser);
    }

    @Test
    @DisplayName("applyDataScope - DEPT 范围过滤部门用户")
    void shouldApplyDeptScopeToUsers() {
        // Given
        UUID deptId = UUID.randomUUID();
        User deptUser1 = createUser("dept1@example.com", deptId);
        User deptUser2 = createUser("dept2@example.com", deptId);
        User otherUser = createUser("other@example.com", UUID.randomUUID());
        User currentUser = createUser("current@example.com", deptId);

        List<User> allUsers = List.of(deptUser1, deptUser2, otherUser, currentUser);

        // When
        List<User> filtered = aspect.applyDataScope(allUsers, DataScope.DEPT, currentUser);

        // Then
        assertThat(filtered).containsExactlyInAnyOrder(deptUser1, deptUser2, currentUser);
    }

    @Test
    @DisplayName("applyDataScope - ALL 范围返回所有用户")
    void shouldReturnAll_whenDataScopeIsAll() {
        // Given
        List<User> allUsers = List.of(
            createUser("user1@example.com"),
            createUser("user2@example.com")
        );
        User currentUser = createUser("current@example.com");

        // When
        List<User> filtered = aspect.applyDataScope(allUsers, DataScope.ALL, currentUser);

        // Then
        assertThat(filtered).isEqualTo(allUsers);
    }

    @Test
    @DisplayName("getCurrentUser - 从 SecurityContext 获取当前用户")
    void shouldGetCurrentUserFromSecurityContext() {
        // When
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        // Then
        assertThat(principal.getUserId()).isEqualTo(currentUser.getId().toString());
    }

    @Test
    @DisplayName("aroundDataPermission - 返回值为 null 时不处理")
    void shouldReturnNull_whenResultIsNull() throws Throwable {
        // Given
        setupDataPermissionAnnotation();
        setupUserRole(DataScope.ALL);
        when(joinPoint.proceed()).thenReturn(null);

        // When
        Object result = aspect.aroundDataPermission(joinPoint);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("aroundDataPermission - 返回非 List/非 User 类型时不处理")
    void shouldNotFilterNonUserTypes() throws Throwable {
        // Given
        setupDataPermissionAnnotation();
        setupUserRole(DataScope.SELF);
        when(joinPoint.proceed()).thenReturn("string-result");

        // When
        Object result = aspect.aroundDataPermission(joinPoint);

        // Then
        assertThat(result).isEqualTo("string-result");
    }

    private void setupDataPermissionAnnotation() throws NoSuchMethodException {
        Method mockMethod = TestService.class.getMethod("methodWithAnnotation");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(mockMethod);
    }

    private void setupUserRole(DataScope dataScope) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("ROLE_USER");
        role.setDataScope(dataScope);

        UserRole userRole = new UserRole();
        userRole.setUserId(currentUser.getId());
        userRole.setRoleId(role.getId());

        when(userRoleRepository.findAllByUserId(currentUser.getId()))
            .thenReturn(List.of(userRole));
        when(roleRepository.findAllById(List.of(role.getId())))
            .thenReturn(List.of(role));
    }

    private User createUser(String email) {
        return createUser(email, UUID.randomUUID());
    }

    private User createUser(String email, UUID departmentId) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setDepartmentId(departmentId);
        return user;
    }

    // 测试用服务类
    static class TestService {
        @DataPermission
        public List<User> methodWithAnnotation() {
            return List.of();
        }
    }

    private void methodWithoutAnnotation() {
        // Empty method for testing
    }
}
