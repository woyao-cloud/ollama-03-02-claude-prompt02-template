package com.usermanagement.repository;

import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DepartmentRepository 集成测试
 */
@DataJpaTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("DepartmentRepository 集成测试")
class DepartmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department rootDepartment;
    private Department childDepartment;

    @BeforeEach
    void setUp() {
        // 创建根部门
        rootDepartment = new Department();
        rootDepartment.setName("总公司");
        rootDepartment.setCode("HQ");
        rootDepartment.setLevel(1);
        rootDepartment.setPath("/root-id");
        rootDepartment.setStatus(DepartmentStatus.ACTIVE);

        // 创建子部门
        childDepartment = new Department();
        childDepartment.setName("技术部");
        childDepartment.setCode("TECH");
        childDepartment.setLevel(2);
        childDepartment.setPath("/root-id/tech-id");
        childDepartment.setStatus(DepartmentStatus.ACTIVE);
    }

    @Nested
    @DisplayName("部门保存测试")
    class SaveTests {

        @Test
        @DisplayName("应该保存根部门")
        void shouldSaveRootDepartment() {
            // When
            Department saved = departmentRepository.save(rootDepartment);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("总公司");
            assertThat(saved.getLevel()).isEqualTo(1);
            assertThat(saved.getParentId()).isNull();
        }

        @Test
        @DisplayName("应该保存子部门")
        void shouldSaveChildDepartment() {
            // Given
            entityManager.persist(rootDepartment);
            entityManager.flush();

            childDepartment.setParentId(rootDepartment.getId());
            childDepartment.setPath(rootDepartment.getPath() + "/" + childDepartment.getId());

            // When
            Department saved = departmentRepository.save(childDepartment);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getParentId()).isEqualTo(rootDepartment.getId());
            assertThat(saved.getLevel()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("部门查询测试")
    class FindTests {

        @Test
        @DisplayName("应该根据 ID 查找部门")
        void shouldFindDepartmentById() {
            // Given
            Department saved = departmentRepository.save(rootDepartment);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Department> found = departmentRepository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("总公司");
        }

        @Test
        @DisplayName("应该根据代码查找部门")
        void shouldFindDepartmentByCode() {
            // Given
            departmentRepository.save(rootDepartment);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Department> found = departmentRepository.findByCode("HQ");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getCode()).isEqualTo("HQ");
        }

        @Test
        @DisplayName("未找到部门时返回空 Optional")
        void shouldReturnEmptyWhenNotFound() {
            // When
            Optional<Department> found = departmentRepository.findByCode("NON_EXISTENT");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("部门路径查询测试")
    class PathQueryTests {

        @Test
        @DisplayName("应该根据路径前缀查找子部门")
        void shouldFindChildrenByPathPrefix() {
            // Given
            departmentRepository.save(rootDepartment);
            departmentRepository.save(childDepartment);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Department> children = departmentRepository.findByPathStartingWith(rootDepartment.getPath() + "/");

            // Then
            assertThat(children).hasSize(1);
            assertThat(children.get(0).getCode()).isEqualTo("TECH");
        }

        @Test
        @DisplayName("应该查找指定层级的部门")
        void shouldFindDepartmentsByLevel() {
            // Given
            departmentRepository.save(rootDepartment);
            departmentRepository.save(childDepartment);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Department> level1Depts = departmentRepository.findByLevel(1);

            // Then
            assertThat(level1Depts).hasSize(1);
            assertThat(level1Depts.get(0).getLevel()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("部门状态测试")
    class StatusTests {

        @Test
        @DisplayName("应该根据状态查找部门")
        void shouldFindDepartmentsByStatus() {
            // Given
            rootDepartment.setStatus(DepartmentStatus.ACTIVE);
            childDepartment.setStatus(DepartmentStatus.INACTIVE);

            departmentRepository.save(rootDepartment);
            departmentRepository.save(childDepartment);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Department> activeDepts = departmentRepository.findByStatus(DepartmentStatus.ACTIVE);

            // Then
            assertThat(activeDepts).hasSize(1);
            assertThat(activeDepts.get(0).getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("部门删除测试")
    class DeleteTests {

        @Test
        @DisplayName("应该软删除部门")
        void shouldSoftDeleteDepartment() {
            // Given
            Department saved = departmentRepository.save(rootDepartment);
            entityManager.flush();
            entityManager.clear();

            // When
            saved.setDeletedAt(java.time.Instant.now());
            departmentRepository.save(saved);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<Department> found = departmentRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getDeletedAt()).isNotNull();
        }
    }
}
