package com.usermanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Department 实体单元测试
 */
@DisplayName("Department 实体测试")
class DepartmentTest {

    @Nested
    @DisplayName("部门创建测试")
    class DepartmentCreationTests {

        @Test
        @DisplayName("应该创建根部门 (level=1, path=/id)")
        void shouldCreateRootDepartment() {
            // Given
            Department dept = new Department();
            dept.setName("总公司");
            dept.setCode("HQ");

            // When
            dept.setLevel(1);
            dept.setPath("/" + dept.getId());

            // Then
            assertThat(dept.getName()).isEqualTo("总公司");
            assertThat(dept.getCode()).isEqualTo("HQ");
            assertThat(dept.getLevel()).isEqualTo(1);
            assertThat(dept.getPath()).startsWith("/");
            assertThat(dept.getParentId()).isNull();
            assertThat(dept.getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
        }

        @Test
        @DisplayName("应该创建子部门 (继承父部门 path)")
        void shouldCreateChildDepartment() {
            // Given
            Department parent = new Department();
            parent.setName("总公司");
            parent.setCode("HQ");
            parent.setLevel(1);
            parent.setPath("/root-id");

            Department child = new Department();
            child.setName("技术部");
            child.setCode("TECH");
            child.setParentId(parent.getId());

            // When
            child.setLevel(2);
            child.setPath(parent.getPath() + "/" + child.getId());

            // Then
            assertThat(child.getParentId()).isEqualTo(parent.getId());
            assertThat(child.getLevel()).isEqualTo(2);
            assertThat(child.getPath()).startsWith(parent.getPath() + "/");
        }

        @Test
        @DisplayName("部门应该有默认状态 ACTIVE")
        void shouldHaveDefaultActiveStatus() {
            // Given
            Department dept = new Department();
            dept.setName("测试部门");
            dept.setCode("TEST");

            // When/Then
            assertThat(dept.getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("部门层级验证测试")
    class DepartmentLevelValidationTests {

        @Test
        @DisplayName("部门层级应该在 1-5 范围内")
        void shouldValidateLevelRange() {
            // Given
            Department dept = new Department();
            dept.setName("测试部门");
            dept.setCode("TEST");

            // Then
            assertThatThrownBy(() -> {
                dept.setLevel(0); // 无效层级
            }).isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> {
                dept.setLevel(6); // 无效层级
            }).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("应该接受有效层级 1-5")
        void shouldAcceptValidLevels() {
            // Given
            for (int level = 1; level <= 5; level++) {
                Department dept = new Department();
                dept.setName("测试部门" + level);
                dept.setCode("TEST" + level);

                // When
                dept.setLevel(level);

                // Then
                assertThat(dept.getLevel()).isEqualTo(level);
            }
        }
    }

    @Nested
    @DisplayName("部门路径测试")
    class DepartmentPathTests {

        @Test
        @DisplayName("根部门路径应该以 /id 格式")
        void shouldHaveRootPathFormat() {
            // Given
            Department dept = new Department();
            dept.setName("总公司");
            dept.setCode("HQ");
            dept.setLevel(1);
            String expectedPath = "/" + dept.getId();
            dept.setPath(expectedPath);

            // Then
            assertThat(dept.getPath()).isEqualTo(expectedPath);
            assertThat(dept.getPath()).matches("/[a-f0-9-]+");
        }

        @Test
        @DisplayName("子部门路径应该继承父路径")
        void shouldInheritParentPath() {
            // Given
            Department root = new Department();
            root.setName("总公司");
            root.setCode("HQ");
            root.setLevel(1);
            root.setPath("/root-id");

            Department level2 = new Department();
            level2.setName("部门 L2");
            level2.setCode("L2");
            level2.setParentId(root.getId());
            level2.setLevel(2);
            level2.setPath(root.getPath() + "/l2-id");

            Department level3 = new Department();
            level3.setName("部门 L3");
            level3.setCode("L3");
            level3.setParentId(level2.getId());
            level3.setLevel(3);
            level3.setPath(level2.getPath() + "/l3-id");

            // Then
            assertThat(level3.getPath()).isEqualTo("/root-id/l2-id/l3-id");
        }
    }

    @Nested
    @DisplayName("部门状态测试")
    class DepartmentStatusTests {

        @Test
        @DisplayName("应该能切换部门状态")
        void shouldToggleDepartmentStatus() {
            // Given
            Department dept = new Department();
            dept.setName("测试部门");
            dept.setCode("TEST");
            dept.setStatus(DepartmentStatus.ACTIVE);

            // When
            dept.setStatus(DepartmentStatus.INACTIVE);

            // Then
            assertThat(dept.getStatus()).isEqualTo(DepartmentStatus.INACTIVE);
        }

        @Test
        @DisplayName("应该验证状态值")
        void shouldValidateStatusValues() {
            // Then
            assertThat(DepartmentStatus.values()).containsExactlyInAnyOrder(
                DepartmentStatus.ACTIVE,
                DepartmentStatus.INACTIVE
            );
        }
    }

    @Nested
    @DisplayName("部门业务方法测试")
    class DepartmentBusinessMethodTests {

        @Test
        @DisplayName("应该判断是否为根部门")
        void shouldIdentifyRootDepartment() {
            // Given
            Department root = new Department();
            root.setName("总公司");
            root.setCode("HQ");
            root.setLevel(1);
            root.setPath("/root-id");

            Department child = new Department();
            child.setName("子部门");
            child.setCode("CHILD");
            child.setParentId(root.getId());
            child.setLevel(2);
            child.setPath("/root-id/child-id");

            // Then
            assertThat(root.getParentId()).isNull();
            assertThat(child.getParentId()).isNotNull();
        }

        @Test
        @DisplayName("应该判断是否为叶子部门")
        void shouldIdentifyLeafDepartment() {
            // Given
            Department dept = new Department();
            dept.setName("叶子部门");
            dept.setCode("LEAF");
            dept.setLevel(3);
            dept.setPath("/root/l1/leaf");

            // Then
            assertThat(dept.getLevel()).isEqualTo(3);
        }
    }
}
