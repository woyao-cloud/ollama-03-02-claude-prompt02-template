package com.usermanagement.service;

import com.usermanagement.domain.DataScope;
import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import com.usermanagement.repository.DepartmentRepository;
import com.usermanagement.security.CustomUserDetails;
import com.usermanagement.security.DataScopeEvaluator;
import com.usermanagement.service.cache.CacheEvictionListener;
import com.usermanagement.service.cache.DepartmentCache;
import com.usermanagement.web.dto.DepartmentCreateRequest;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import com.usermanagement.web.dto.DepartmentUpdateRequest;
import com.usermanagement.web.mapper.DepartmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DepartmentService 数据权限测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService 数据权限测试")
class DepartmentServiceWithDataPermissionTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private TreeBuilder<Department> treeBuilder;

    @Mock
    private DepartmentCache departmentCache;

    @Mock
    private CacheEvictionListener cacheEvictionListener;

    @Mock
    private DataScopeEvaluator dataScopeEvaluator;

    private DepartmentServiceImpl departmentService;

    private CustomUserDetails currentUser;
    private UUID deptId;
    private UUID childDeptId;
    private static final UUID TEST_DEPT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentServiceImpl(
            departmentRepository, departmentMapper, treeBuilder, departmentCache, cacheEvictionListener);
        departmentService.setDataScopeEvaluator(dataScopeEvaluator);

        deptId = UUID.randomUUID();
        childDeptId = UUID.randomUUID();
        currentUser = new CustomUserDetails(
            UUID.randomUUID().toString(),
            "test@example.com",
            deptId,
            List.of()
        );
    }

    @Nested
    @DisplayName("获取用户有权限的部门列表测试")
    class GetUserScopedDepartmentsTests {

        @Test
        @DisplayName("ALL 范围返回所有部门")
        void shouldReturnAllDepartments_whenDataScopeIsAll() {
            // Given
            List<Department> allDepts = List.of(
                createDepartment(deptId, "IT", 2, "/1/2"),
                createDepartment(UUID.randomUUID(), "HR", 2, "/1/3")
            );
            given(departmentRepository.findAllOrderedByPath()).willReturn(allDepts);
            when(dataScopeEvaluator.evaluateDataScope(DataScope.ALL, currentUser))
                .thenReturn(List.of());

            // When
            List<Department> result = departmentService.getDepartmentsByDataScope(currentUser, DataScope.ALL);

            // Then
            assertThat(result).isEqualTo(allDepts);
        }

        @Test
        @DisplayName("DEPT 范围返回本部门及下级部门")
        void shouldReturnDeptAndChildDepts_whenDataScopeIsDept() {
            // Given
            Department currentDept = createDepartment(deptId, "IT", 2, "/1/2");
            Department childDept = createDepartment(childDeptId, "DEV", 3, "/1/2/3");
            List<Department> scopedDepts = List.of(currentDept, childDept);

            when(dataScopeEvaluator.evaluateDataScope(DataScope.DEPT, currentUser))
                .thenReturn(List.of(deptId, childDeptId));
            given(departmentRepository.findAllById(List.of(deptId, childDeptId)))
                .willReturn(scopedDepts);

            // When
            List<Department> result = departmentService.getDepartmentsByDataScope(currentUser, DataScope.DEPT);

            // Then
            assertThat(result).containsExactlyInAnyOrder(currentDept, childDept);
        }

        @Test
        @DisplayName("SELF 范围返回空列表")
        void shouldReturnEmptyList_whenDataScopeIsSelf() {
            // Given
            when(dataScopeEvaluator.evaluateDataScope(DataScope.SELF, currentUser))
                .thenReturn(List.of());

            // When
            List<Department> result = departmentService.getDepartmentsByDataScope(currentUser, DataScope.SELF);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("获取用户有权限的部门树测试")
    class GetUserScopedDepartmentTreeTests {

        @Test
        @DisplayName("ALL 范围返回完整部门树")
        void shouldReturnFullTree_whenDataScopeIsAll() {
            // Given
            Department root = createDepartment(deptId, "IT", 2, "/1/2");
            List<Department> allDepts = List.of(root);
            DepartmentTreeResponse fullTree = new DepartmentTreeResponse();
            fullTree.setTree(List.of(createDTO()));

            given(departmentRepository.findAllOrderedByPath()).willReturn(allDepts);
            given(treeBuilder.buildTree(allDepts, null)).willReturn(List.of(root));
            given(departmentMapper.toTreeDto(List.of(root))).willReturn(List.of(createDTO()));
            when(dataScopeEvaluator.evaluateDataScope(DataScope.ALL, currentUser))
                .thenReturn(List.of());

            // When
            DepartmentTreeResponse response = departmentService.getScopedDepartmentTree(currentUser, DataScope.ALL);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTree()).isNotEmpty();
        }

        @Test
        @DisplayName("DEPT 范围返回过滤后的部门树")
        void shouldReturnFilteredTree_whenDataScopeIsDept() {
            // Given
            Department currentDept = createDepartment(deptId, "IT", 2, "/1/2");
            Department childDept = createDepartment(childDeptId, "DEV", 3, "/1/2/3");
            List<Department> scopedDepts = List.of(currentDept, childDept);

            when(dataScopeEvaluator.evaluateDataScope(DataScope.DEPT, currentUser))
                .thenReturn(List.of(deptId, childDeptId));
            given(departmentRepository.findAllById(List.of(deptId, childDeptId)))
                .willReturn(scopedDepts);
            given(treeBuilder.buildTree(scopedDepts, null)).willReturn(scopedDepts);
            given(departmentMapper.toTreeDto(scopedDepts))
                .willReturn(List.of(createDTO(), createDTO()));

            // When
            DepartmentTreeResponse response = departmentService.getScopedDepartmentTree(currentUser, DataScope.DEPT);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTree()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("检查用户是否有部门权限测试")
    class CheckDepartmentPermissionTests {

        @Test
        @DisplayName("用户在部门内时返回 true")
        void shouldReturnTrue_whenUserInDepartment() {
            // Given
            Department dept = createDepartment(deptId, "IT", 2, "/1/2");
            given(departmentRepository.findById(deptId)).willReturn(Optional.of(dept));
            when(dataScopeEvaluator.evaluateDataScope(DataScope.DEPT, currentUser))
                .thenReturn(List.of(deptId));

            // When
            boolean hasPermission = departmentService.hasDepartmentPermission(currentUser, deptId, DataScope.DEPT);

            // Then
            assertThat(hasPermission).isTrue();
        }

        @Test
        @DisplayName("用户不在部门内时返回 false")
        void shouldReturnFalse_whenUserNotInDepartment() {
            // Given
            UUID otherDeptId = UUID.randomUUID();
            given(departmentRepository.findById(otherDeptId)).willReturn(Optional.empty());
            when(dataScopeEvaluator.evaluateDataScope(DataScope.DEPT, currentUser))
                .thenReturn(List.of(deptId));

            // When
            boolean hasPermission = departmentService.hasDepartmentPermission(currentUser, otherDeptId, DataScope.DEPT);

            // Then
            assertThat(hasPermission).isFalse();
        }

        @Test
        @DisplayName("ALL 范围总是返回 true")
        void shouldReturnTrue_whenDataScopeIsAll() {
            // When
            boolean hasPermission = departmentService.hasDepartmentPermission(currentUser, UUID.randomUUID(), DataScope.ALL);

            // Then
            assertThat(hasPermission).isTrue();
        }
    }

    // 辅助方法
    private Department createDepartment(UUID id, String code, int level, String path) {
        Department dept = new Department();
        dept.setId(id);
        dept.setCode(code);
        dept.setLevel(level);
        dept.setPath(path);
        dept.setStatus(DepartmentStatus.ACTIVE);
        return dept;
    }

    private DepartmentDTO createDTO() {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(TEST_DEPT_ID.toString());
        dto.setName("Test Dept");
        dto.setCode("TEST");
        dto.setLevel(2);
        dto.setPath("/1/2");
        dto.setStatus("ACTIVE");
        return dto;
    }
}
