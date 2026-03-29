package com.usermanagement.web.mapper;

import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import com.usermanagement.web.dto.DepartmentCreateRequest;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DepartmentMapper 测试
 */
@SpringBootTest
@DisplayName("DepartmentMapper 测试")
class DepartmentMapperTest {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Nested
    @DisplayName("toDto 测试")
    class ToDtoTests {

        @Test
        @DisplayName("应该将 Department 实体映射为 DepartmentDTO")
        void shouldMapDepartmentToDTO() {
            // Given
            Department department = new Department();
            department.setId(UUID.randomUUID());
            department.setName("技术部");
            department.setCode("TECH");
            department.setLevel(2);
            department.setPath("/root/tech");
            department.setStatus(DepartmentStatus.ACTIVE);
            department.setSortOrder(1);
            department.setDescription("技术研发部门");

            // When
            DepartmentDTO dto = departmentMapper.toDto(department);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(department.getId().toString());
            assertThat(dto.getName()).isEqualTo(department.getName());
            assertThat(dto.getCode()).isEqualTo(department.getCode());
            assertThat(dto.getLevel()).isEqualTo(department.getLevel());
            assertThat(dto.getPath()).isEqualTo(department.getPath());
            assertThat(dto.getStatus()).isEqualTo("ACTIVE");
            assertThat(dto.getSortOrder()).isEqualTo(department.getSortOrder());
        }

        @Test
        @DisplayName("应该映射部门负责人信息")
        void shouldMapDepartmentWithManager() {
            // Given
            UUID managerId = UUID.randomUUID();
            Department department = new Department();
            department.setId(UUID.randomUUID());
            department.setName("技术部");
            department.setCode("TECH");
            department.setLevel(2);
            department.setPath("/root/tech");
            department.setManagerId(managerId);
            department.setStatus(DepartmentStatus.ACTIVE);

            // When
            DepartmentDTO dto = departmentMapper.toDto(department);

            // Then
            assertThat(dto.getManagerId()).isEqualTo(managerId.toString());
        }
    }

    @Nested
    @DisplayName("toEntity 测试")
    class ToEntityTests {

        @Test
        @DisplayName("应该将 DepartmentCreateRequest 映射为 Department 实体")
        void shouldMapCreateRequestToEntity() {
            // Given
            DepartmentCreateRequest request = new DepartmentCreateRequest();
            request.setName("技术部");
            request.setCode("TECH");
            request.setLevel(2);
            request.setParentId(UUID.randomUUID().toString());
            request.setManagerId(UUID.randomUUID().toString());
            request.setDescription("技术研发部门");
            request.setSortOrder(1);

            // When
            Department department = departmentMapper.toEntity(request);

            // Then
            assertThat(department).isNotNull();
            assertThat(department.getName()).isEqualTo(request.getName());
            assertThat(department.getCode()).isEqualTo(request.getCode());
            assertThat(department.getLevel()).isEqualTo(request.getLevel());
            assertThat(department.getDescription()).isEqualTo(request.getDescription());
            assertThat(department.getSortOrder()).isEqualTo(request.getSortOrder());
        }
    }

    @Nested
    @DisplayName("updateEntity 测试")
    class UpdateEntityTests {

        @Test
        @DisplayName("应该使用 DepartmentUpdateRequest 更新 Department 实体")
        void shouldUpdateEntityFromRequest() {
            // Given
            Department department = new Department();
            department.setId(UUID.randomUUID());
            department.setName("旧名称");
            department.setCode("OLD");
            department.setLevel(2);
            department.setPath("/root/old");
            department.setStatus(DepartmentStatus.ACTIVE);

            DepartmentUpdateRequest request = new DepartmentUpdateRequest();
            request.setName("新名称");
            request.setDescription("新描述");
            request.setSortOrder(5);

            // When
            departmentMapper.updateEntity(request, department);

            // Then
            assertThat(department.getName()).isEqualTo("新名称");
            assertThat(department.getDescription()).isEqualTo("新描述");
            assertThat(department.getSortOrder()).isEqualTo(5);
            assertThat(department.getCode()).isEqualTo("OLD"); // 不应改变
        }

        @Test
        @DisplayName("应该忽略 null 值字段")
        void shouldIgnoreNullFields() {
            // Given
            Department department = new Department();
            department.setId(UUID.randomUUID());
            department.setName("原名称");
            department.setCode("CODE");
            department.setLevel(2);
            department.setPath("/root/code");
            department.setStatus(DepartmentStatus.ACTIVE);
            department.setDescription("原描述");

            DepartmentUpdateRequest request = new DepartmentUpdateRequest();
            request.setName("新名称");
            // 其他字段为 null

            // When
            departmentMapper.updateEntity(request, department);

            // Then
            assertThat(department.getName()).isEqualTo("新名称");
            assertThat(department.getDescription()).isEqualTo("原描述"); // 保持不变
            assertThat(department.getCode()).isEqualTo("CODE"); // 保持不变
        }
    }

    @Nested
    @DisplayName("toTreeDto 测试")
    class ToTreeDtoTests {

        @Test
        @DisplayName("应该将 Department 列表映射为 DepartmentDTO 列表")
        void shouldMapDepartmentListToDTOList() {
            // Given
            Department dept1 = new Department();
            dept1.setId(UUID.randomUUID());
            dept1.setName("部门 1");
            dept1.setCode("DEPT1");
            dept1.setLevel(1);
            dept1.setPath("/dept1");
            dept1.setStatus(DepartmentStatus.ACTIVE);

            Department dept2 = new Department();
            dept2.setId(UUID.randomUUID());
            dept2.setName("部门 2");
            dept2.setCode("DEPT2");
            dept2.setLevel(1);
            dept2.setPath("/dept2");
            dept2.setStatus(DepartmentStatus.ACTIVE);

            List<Department> departments = List.of(dept1, dept2);

            // When
            List<DepartmentDTO> dtos = departmentMapper.toTreeDto(departments);

            // Then
            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).getName()).isEqualTo("部门 1");
            assertThat(dtos.get(1).getName()).isEqualTo("部门 2");
        }
    }
}
