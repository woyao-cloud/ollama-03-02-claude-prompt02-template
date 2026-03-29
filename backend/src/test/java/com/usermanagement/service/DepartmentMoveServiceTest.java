package com.usermanagement.service;

import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import com.usermanagement.repository.DepartmentRepository;
import com.usermanagement.web.dto.DepartmentMoveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * DepartmentMoveService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentMoveService 测试")
class DepartmentMoveServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentMoveServiceImpl moveService;

    private static final UUID TEST_DEPT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID TEST_PARENT_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
    private static final UUID TEST_NEW_PARENT_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");

    @BeforeEach
    void setUp() {
        moveService = new DepartmentMoveServiceImpl(departmentRepository);
    }

    @Nested
    @DisplayName("移动部门测试")
    class MoveDepartmentTests {

        @Test
        @DisplayName("应该移动部门到新父部门")
        void shouldMoveDepartmentToNewParent() {
            // Given
            Department department = createDepartment(2, TEST_PARENT_ID, "/old/parent/dept");
            Department newParent = createDepartment(1, null, "/new-parent");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_NEW_PARENT_ID.toString());
            request.setSortOrder(1);

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(TEST_NEW_PARENT_ID)).willReturn(Optional.of(newParent));

            // When
            moveService.moveDepartment(TEST_DEPT_ID, request);

            // Then
            assertThat(department.getParentId()).isEqualTo(TEST_NEW_PARENT_ID);
            assertThat(department.getPath()).startsWith("/new-parent/");
            verify(departmentRepository).save(department);
        }

        @Test
        @DisplayName("应该更新部门路径")
        void shouldUpdateDepartmentPath() {
            // Given
            Department department = createDepartment(2, TEST_PARENT_ID, "/old/dept");
            Department newParent = createDepartment(1, null, "/new-parent");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_NEW_PARENT_ID.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(TEST_NEW_PARENT_ID)).willReturn(Optional.of(newParent));

            // When
            moveService.moveDepartment(TEST_DEPT_ID, request);

            // Then
            assertThat(department.getPath()).isEqualTo("/new-parent/" + TEST_DEPT_ID);
        }

        @Test
        @DisplayName("应该更新部门层级")
        void shouldUpdateDepartmentLevel() {
            // Given
            Department department = createDepartment(2, TEST_PARENT_ID, "/old/dept");
            Department newParent = createDepartment(1, null, "/new-parent");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_NEW_PARENT_ID.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(TEST_NEW_PARENT_ID)).willReturn(Optional.of(newParent));

            // When
            moveService.moveDepartment(TEST_DEPT_ID, request);

            // Then
            assertThat(department.getLevel()).isEqualTo(2); // newParent.level + 1
        }

        @Test
        @DisplayName("应该更新排序号")
        void shouldUpdateSortOrder() {
            // Given
            Department department = createDepartment(2, TEST_PARENT_ID, "/old/dept");
            Department newParent = createDepartment(1, null, "/new-parent");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_NEW_PARENT_ID.toString());
            request.setSortOrder(5);

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(TEST_NEW_PARENT_ID)).willReturn(Optional.of(newParent));

            // When
            moveService.moveDepartment(TEST_DEPT_ID, request);

            // Then
            assertThat(department.getSortOrder()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("循环依赖检查测试")
    class CircularDependencyCheckTests {

        @Test
        @DisplayName("应该防止循环依赖 - 不能移动到直接子部门")
        void shouldPreventCircularDependency_DirectChild() {
            // Given
            UUID childId = UUID.randomUUID();
            Department department = createDepartment(2, TEST_PARENT_ID, "/parent/dept");
            Department child = createDepartment(3, TEST_DEPT_ID, "/parent/dept/child");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(childId.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(childId)).willReturn(Optional.of(child));
            given(departmentRepository.findByPathStartingWith("/parent/dept/"))
                .willReturn(List.of(department, child));

            // When & Then
            assertThatThrownBy(() -> moveService.moveDepartment(TEST_DEPT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能将部门移动到其子部门下");
        }

        @Test
        @DisplayName("应该防止循环依赖 - 不能移动到间接子部门")
        void shouldPreventCircularDependency_IndirectChild() {
            // Given
            UUID grandchildId = UUID.randomUUID();
            Department department = createDepartment(1, null, "/dept");
            Department child = createDepartment(2, TEST_DEPT_ID, "/dept/child");
            Department grandchild = createDepartment(3, child.getId(), "/dept/child/grandchild");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(grandchildId.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(grandchildId)).willReturn(Optional.of(grandchild));
            given(departmentRepository.findByPathStartingWith("/dept/"))
                .willReturn(List.of(department, child, grandchild));

            // When & Then
            assertThatThrownBy(() -> moveService.moveDepartment(TEST_DEPT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能将部门移动到其子部门下");
        }

        @Test
        @DisplayName("应该防止部门移动到自身")
        void shouldPreventMoveToSelf() {
            // Given
            Department department = createDepartment(1, null, "/dept");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_DEPT_ID.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));

            // When & Then
            assertThatThrownBy(() -> moveService.moveDepartment(TEST_DEPT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能将部门移动到自身");
        }
    }

    @Nested
    @DisplayName("层级验证测试")
    class LevelValidationTests {

        @Test
        @DisplayName("移动到超过 5 级时抛出异常")
        void shouldThrowExceptionWhenExceedMaxLevel() {
            // Given
            Department department = createDepartment(5, TEST_PARENT_ID, "/l1/l2/l3/l4/dept");
            Department newParent = createDepartment(5, null, "/l1/l2/l3/l4/parent");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_NEW_PARENT_ID.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(TEST_NEW_PARENT_ID)).willReturn(Optional.of(newParent));

            // When & Then
            assertThatThrownBy(() -> moveService.moveDepartment(TEST_DEPT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("部门层级不能超过 5 级");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("部门不存在时抛出异常")
        void shouldThrowExceptionWhenDepartmentNotFound() {
            // Given
            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_NEW_PARENT_ID.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> moveService.moveDepartment(TEST_DEPT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("部门不存在");
        }

        @Test
        @DisplayName("新父部门不存在时抛出异常")
        void shouldThrowExceptionWhenNewParentNotFound() {
            // Given
            Department department = createDepartment(2, TEST_PARENT_ID, "/old/dept");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_NEW_PARENT_ID.toString());

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));
            given(departmentRepository.findById(TEST_NEW_PARENT_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> moveService.moveDepartment(TEST_DEPT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("父部门不存在");
        }

        @Test
        @DisplayName("移动到根节点时应该更新路径")
        void shouldUpdatePathWhenMovingToRoot() {
            // Given
            Department department = createDepartment(2, TEST_PARENT_ID, "/old/parent/dept");

            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(null); // 移动到根节点

            given(departmentRepository.findById(TEST_DEPT_ID)).willReturn(Optional.of(department));

            // When
            moveService.moveDepartment(TEST_DEPT_ID, request);

            // Then
            assertThat(department.getParentId()).isNull();
            assertThat(department.getLevel()).isEqualTo(1);
            assertThat(department.getPath()).isEqualTo("/" + TEST_DEPT_ID);
        }
    }

    // 辅助方法
    private Department createDepartment(int level, UUID parentId, String path) {
        Department dept = new Department();
        dept.setId(TEST_DEPT_ID);
        dept.setName("测试部门");
        dept.setCode("TEST");
        dept.setLevel(level);
        dept.setParentId(parentId);
        dept.setPath(path);
        dept.setStatus(DepartmentStatus.ACTIVE);
        return dept;
    }
}
