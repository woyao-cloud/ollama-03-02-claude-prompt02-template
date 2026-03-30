package com.usermanagement.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Department DTO 测试
 */
@DisplayName("Department DTO 测试")
class DepartmentDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("DepartmentDTO 测试")
    class DepartmentDTOTests {

        @Test
        @DisplayName("应该创建 DepartmentDTO")
        void shouldCreateDepartmentDTO() {
            // Given
            DepartmentDTO dto = new DepartmentDTO();
            dto.setId("550e8400-e29b-41d4-a716-446655440000");
            dto.setName("技术部");
            dto.setCode("TECH");
            dto.setLevel(2);
            dto.setPath("/root/tech");
            dto.setStatus("ACTIVE");

            // Then
            assertThat(dto.getId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
            assertThat(dto.getName()).isEqualTo("技术部");
            assertThat(dto.getCode()).isEqualTo("TECH");
            assertThat(dto.getLevel()).isEqualTo(2);
            assertThat(dto.getPath()).isEqualTo("/root/tech");
            assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("应该序列化 DepartmentDTO 为 JSON")
        void shouldSerializeDepartmentDTOToJson() throws Exception {
            // Given
            DepartmentDTO dto = new DepartmentDTO();
            dto.setId("550e8400-e29b-41d4-a716-446655440000");
            dto.setName("技术部");
            dto.setCode("TECH");
            dto.setLevel(2);
            dto.setPath("/root/tech");
            dto.setStatus("ACTIVE");

            // When
            String json = objectMapper.writeValueAsString(dto);

            // Then
            assertThat(json).contains("\"id\":\"550e8400-e29b-41d4-a716-446655440000\"");
            assertThat(json).contains("\"name\":\"技术部\"");
            assertThat(json).contains("\"code\":\"TECH\"");
        }
    }

    @Nested
    @DisplayName("DepartmentCreateRequest 测试")
    class DepartmentCreateRequestTests {

        @Test
        @DisplayName("应该创建 DepartmentCreateRequest")
        void shouldCreateDepartmentCreateRequest() {
            // Given
            DepartmentCreateRequest request = new DepartmentCreateRequest();
            request.setName("技术部");
            request.setCode("TECH");
            request.setParentId("550e8400-e29b-41d4-a716-446655440000");
            request.setLevel(2);
            request.setManagerId("660e8400-e29b-41d4-a716-446655440001");
            request.setDescription("技术研发部门");
            request.setSortOrder(1);

            // Then
            assertThat(request.getName()).isEqualTo("技术部");
            assertThat(request.getCode()).isEqualTo("TECH");
            assertThat(request.getParentId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
            assertThat(request.getLevel()).isEqualTo(2);
            assertThat(request.getManagerId()).isEqualTo("660e8400-e29b-41d4-a716-446655440001");
        }
    }

    @Nested
    @DisplayName("DepartmentUpdateRequest 测试")
    class DepartmentUpdateRequestTests {

        @Test
        @DisplayName("应该创建 DepartmentUpdateRequest")
        void shouldCreateDepartmentUpdateRequest() {
            // Given
            DepartmentUpdateRequest request = new DepartmentUpdateRequest();
            request.setName("新技术部");
            request.setManagerId("660e8400-e29b-41d4-a716-446655440001");
            request.setStatus("INACTIVE");
            request.setDescription("更新后的描述");
            request.setSortOrder(2);

            // Then
            assertThat(request.getName()).isEqualTo("新技术部");
            assertThat(request.getManagerId()).isEqualTo("660e8400-e29b-41d4-a716-446655440001");
            assertThat(request.getStatus()).isEqualTo("INACTIVE");
        }
    }

    @Nested
    @DisplayName("DepartmentTreeResponse 测试")
    class DepartmentTreeResponseTests {

        @Test
        @DisplayName("应该创建 DepartmentTreeResponse")
        void shouldCreateDepartmentTreeResponse() {
            // Given
            DepartmentDTO root = new DepartmentDTO();
            root.setId("root-id");
            root.setName("总公司");
            root.setLevel(1);

            DepartmentDTO child = new DepartmentDTO();
            child.setId("child-id");
            child.setName("技术部");
            child.setLevel(2);

            DepartmentTreeResponse response = new DepartmentTreeResponse();
            response.setTree(List.of(root));
            response.setTotal(1);

            // Then
            assertThat(response.getTree()).hasSize(1);
            assertThat(response.getTotal()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("DepartmentMoveRequest 测试")
    class DepartmentMoveRequestTests {

        @Test
        @DisplayName("应该创建 DepartmentMoveRequest")
        void shouldCreateDepartmentMoveRequest() {
            // Given
            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId("new-parent-id");
            request.setSortOrder(1);

            // Then
            assertThat(request.getParentId()).isEqualTo("new-parent-id");
            assertThat(request.getSortOrder()).isEqualTo(1);
        }
    }
}
