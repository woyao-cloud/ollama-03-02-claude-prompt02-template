package com.usermanagement.service;

import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TreeBuilder 单元测试
 */
@DisplayName("TreeBuilder 测试")
class TreeBuilderTest {

    private final TreeBuilder<Department> treeBuilder = new TreeBuilder<>();

    @Nested
    @DisplayName("构建树形结构测试")
    class BuildTreeTests {

        @Test
        @DisplayName("应该构建单层树")
        void shouldBuildSingleLevelTree() {
            // Given
            Department root = createDepartment(null, 1, "/root");
            root.setId(UUID.randomUUID());
            List<Department> departments = List.of(root);

            // When
            List<Department> result = treeBuilder.buildTree(departments, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(root);
        }

        @Test
        @DisplayName("应该构建两层树形结构")
        void shouldBuildTwoLevelTree() {
            // Given
            UUID rootId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();

            Department root = createDepartment(null, 1, "/root");
            root.setId(rootId);

            Department child = createDepartment(rootId, 2, "/root/child");
            child.setId(childId);

            List<Department> departments = List.of(root, child);

            // When
            List<Department> result = treeBuilder.buildTree(departments, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(rootId);
        }

        @Test
        @DisplayName("应该构建多层树形结构")
        void shouldBuildMultiLevelTree() {
            // Given
            UUID rootId = UUID.randomUUID();
            UUID level2Id = UUID.randomUUID();
            UUID level3Id = UUID.randomUUID();

            Department root = createDepartment(null, 1, "/root");
            root.setId(rootId);

            Department level2 = createDepartment(rootId, 2, "/root/l2");
            level2.setId(level2Id);

            Department level3 = createDepartment(level2Id, 3, "/root/l2/l3");
            level3.setId(level3Id);

            List<Department> departments = List.of(root, level2, level3);

            // When
            List<Department> result = treeBuilder.buildTree(departments, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(rootId);
        }

        @Test
        @DisplayName("应该构建多根节点的树")
        void shouldBuildMultiRootTree() {
            // Given
            UUID root1Id = UUID.randomUUID();
            UUID root2Id = UUID.randomUUID();

            Department root1 = createDepartment(null, 1, "/root1");
            root1.setId(root1Id);

            Department root2 = createDepartment(null, 1, "/root2");
            root2.setId(root2Id);

            List<Department> departments = List.of(root1, root2);

            // When
            List<Department> result = treeBuilder.buildTree(departments, null);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("应该支持按层级筛选")
        void shouldSupportLevelFilter() {
            // Given
            UUID rootId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();

            Department root = createDepartment(null, 1, "/root");
            root.setId(rootId);

            Department child = createDepartment(rootId, 2, "/root/child");
            child.setId(childId);

            List<Department> departments = List.of(root, child);

            // When
            List<Department> result = treeBuilder.buildTree(departments, 1);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("空值和边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空列表应该返回空结果")
        void shouldReturnEmptyForEmptyList() {
            // Given
            List<Department> departments = List.of();

            // When
            List<Department> result = treeBuilder.buildTree(departments, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 列表应该返回空结果")
        void shouldReturnEmptyForNullList() {
            // When
            List<Department> result = treeBuilder.buildTree(null, null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // 辅助方法
    private Department createDepartment(UUID parentId, int level, String path) {
        Department dept = new Department();
        dept.setId(UUID.randomUUID());
        dept.setName("部门" + level);
        dept.setCode("CODE" + level);
        dept.setParentId(parentId);
        dept.setLevel(level);
        dept.setPath(path);
        dept.setStatus(DepartmentStatus.ACTIVE);
        return dept;
    }
}
