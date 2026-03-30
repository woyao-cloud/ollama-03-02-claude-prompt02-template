package com.usermanagement.security;

import com.usermanagement.domain.*;
import com.usermanagement.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * DataScopeEvaluator 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DataScopeEvaluatorTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DataScopeEvaluator evaluator;

    private CustomUserDetails currentUser;
    private UUID deptId;
    private UUID childDeptId;

    @BeforeEach
    void setUp() {
        evaluator = new DataScopeEvaluator(departmentRepository);
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
    @DisplayName("evaluateDataScope - ALL 范围返回空过滤器")
    void shouldReturnEmptyFilter_whenDataScopeIsAll() {
        // When
        List<UUID> result = evaluator.evaluateDataScope(DataScope.ALL, currentUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("evaluateDataScope - SELF 范围返回空过滤器 (由 SQL WHERE 条件处理)")
    void shouldReturnEmptyFilter_whenDataScopeIsSelf() {
        // When
        List<UUID> result = evaluator.evaluateDataScope(DataScope.SELF, currentUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("evaluateDataScope - DEPT 范围返回本部门及下级部门 IDs")
    void shouldReturnDeptAndChildDeptIds_whenDataScopeIsDept() {
        // Given
        Department currentDept = createDepartment(deptId, "IT", 2, "/1/2");
        Department childDept = createDepartment(childDeptId, "DEV", 3, "/1/2/3");

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(currentDept));
        when(departmentRepository.findByPathStartingWith("/1/2/"))
            .thenReturn(List.of(childDept));

        // When
        List<UUID> result = evaluator.evaluateDataScope(DataScope.DEPT, currentUser);

        // Then
        assertThat(result).containsExactlyInAnyOrder(deptId, childDeptId);
    }

    @Test
    @DisplayName("evaluateDataScope - DEPT 范围当部门不存在时返回空列表")
    void shouldReturnEmptyList_whenDepartmentNotFound() {
        // Given
        when(departmentRepository.findById(deptId)).thenReturn(Optional.empty());

        // When
        List<UUID> result = evaluator.evaluateDataScope(DataScope.DEPT, currentUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("evaluateDataScope - CUSTOM 范围返回空过滤器 (待扩展)")
    void shouldReturnEmptyFilter_whenDataScopeIsCustom() {
        // When
        List<UUID> result = evaluator.evaluateDataScope(DataScope.CUSTOM, currentUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUserScopedDepartmentIds - 获取用户有权限的部门 ID 列表")
    void shouldGetUserScopedDepartmentIds() {
        // Given
        Department currentDept = createDepartment(deptId, "IT", 2, "/1/2");
        Department childDept = createDepartment(childDeptId, "DEV", 3, "/1/2/3");

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(currentDept));
        when(departmentRepository.findByPathStartingWith("/1/2/"))
            .thenReturn(List.of(childDept));

        // When
        List<UUID> result = evaluator.getUserScopedDepartmentIds(currentUser, DataScope.DEPT);

        // Then
        assertThat(result).containsExactlyInAnyOrder(deptId, childDeptId);
    }

    @Test
    @DisplayName("buildDeptPathPrefix - 正确构建路径前缀")
    void shouldBuildCorrectPathPrefix() {
        // Given
        Department dept = createDepartment(deptId, "IT", 2, "/1/2");
        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));

        // When
        String prefix = evaluator.buildDeptPathPrefix(currentUser);

        // Then
        assertThat(prefix).isEqualTo("/1/2/");
    }

    @Test
    @DisplayName("buildDeptPathPrefix - 部门不存在时返回空字符串")
    void shouldReturnEmptyString_whenDepartmentNotFoundForPrefix() {
        // Given
        when(departmentRepository.findById(deptId)).thenReturn(Optional.empty());

        // When
        String prefix = evaluator.buildDeptPathPrefix(currentUser);

        // Then
        assertThat(prefix).isEmpty();
    }

    private Department createDepartment(UUID id, String code, int level, String path) {
        Department dept = new Department();
        dept.setId(id);
        dept.setCode(code);
        dept.setLevel(level);
        dept.setPath(path);
        return dept;
    }
}
