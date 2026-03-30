package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.service.DepartmentMoveService;
import com.usermanagement.service.DepartmentService;
import com.usermanagement.web.dto.DepartmentCreateRequest;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentMoveRequest;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import com.usermanagement.web.dto.DepartmentUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DepartmentController 单元测试
 */
@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private DepartmentMoveService departmentMoveService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_DEPT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_PARENT_ID = "660e8400-e29b-41d4-a716-446655440001";

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("创建部门 API 测试")
    class CreateDepartmentApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该创建部门并返回 201")
        void shouldCreateDepartmentAndReturn201() throws Exception {
            // Given
            DepartmentCreateRequest request = new DepartmentCreateRequest();
            request.setName("技术部");
            request.setCode("TECH");
            request.setLevel(2);
            request.setParentId(TEST_PARENT_ID);

            DepartmentDTO responseDto = new DepartmentDTO();
            responseDto.setId(TEST_DEPT_ID);
            responseDto.setName("技术部");
            responseDto.setCode("TECH");
            responseDto.setLevel(2);
            responseDto.setStatus("ACTIVE");

            given(departmentService.createDepartment(any(DepartmentCreateRequest.class))).willReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/api/departments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_DEPT_ID))
                .andExpect(jsonPath("$.name").value("技术部"))
                .andExpect(jsonPath("$.code").value("TECH"));

            then(departmentService).should().createDepartment(any(DepartmentCreateRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("部门代码已存在时返回 400")
        void shouldReturn400WhenCodeExists() throws Exception {
            // Given
            DepartmentCreateRequest request = new DepartmentCreateRequest();
            request.setName("技术部");
            request.setCode("TECH");
            request.setLevel(1);

            given(departmentService.createDepartment(any()))
                .willThrow(new IllegalArgumentException("部门代码已存在"));

            // When & Then
            mockMvc.perform(post("/api/departments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("父部门不存在时返回 400")
        void shouldReturn400WhenParentNotFound() throws Exception {
            // Given
            DepartmentCreateRequest request = new DepartmentCreateRequest();
            request.setName("子部门");
            request.setCode("CHILD");
            request.setLevel(2);
            request.setParentId(TEST_PARENT_ID);

            given(departmentService.createDepartment(any()))
                .willThrow(new IllegalArgumentException("父部门不存在"));

            // When & Then
            mockMvc.perform(post("/api/departments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("请求体无效时返回 400")
        void shouldReturn400WhenInvalidRequest() throws Exception {
            // Given
            DepartmentCreateRequest request = new DepartmentCreateRequest();
            // 缺少必填字段 name 和 code

            // When & Then
            mockMvc.perform(post("/api/departments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("获取部门树 API 测试")
    class GetDepartmentTreeApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取完整部门树")
        void shouldGetFullDepartmentTree() throws Exception {
            // Given
            DepartmentDTO root = createDepartmentDTO("root", "总公司", 1);
            DepartmentDTO child = createDepartmentDTO("child", "技术部", 2);

            DepartmentTreeResponse response = new DepartmentTreeResponse();
            response.setTree(List.of(root));
            response.setTotal(1);

            given(departmentService.getDepartmentTree(null)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/departments/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tree").isArray())
                .andExpect(jsonPath("$.total").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按层级筛选")
        void shouldFilterByLevel() throws Exception {
            // Given
            DepartmentDTO root = createDepartmentDTO("root", "总公司", 1);
            DepartmentTreeResponse response = new DepartmentTreeResponse();
            response.setTree(List.of(root));
            response.setTotal(1);

            given(departmentService.getDepartmentTree(1)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/departments/tree")
                    .param("level", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tree").isArray());
        }
    }

    @Nested
    @DisplayName("获取部门详情 API 测试")
    class GetDepartmentApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取部门详情")
        void shouldGetDepartmentById() throws Exception {
            // Given
            DepartmentDTO dto = createDepartmentDTO(TEST_DEPT_ID, "技术部", 2);

            given(departmentService.getDepartmentById(any())).willReturn(dto);

            // When & Then
            mockMvc.perform(get("/api/departments/{id}", TEST_DEPT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_DEPT_ID))
                .andExpect(jsonPath("$.name").value("技术部"))
                .andExpect(jsonPath("$.level").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("部门不存在时返回 404")
        void shouldReturn404WhenDepartmentNotFound() throws Exception {
            // Given
            given(departmentService.getDepartmentById(any()))
                .willThrow(new IllegalArgumentException("部门不存在"));

            // When & Then
            mockMvc.perform(get("/api/departments/{id}", TEST_DEPT_ID))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("UUID 格式无效时返回 400")
        void shouldReturn400WhenInvalidUuid() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/departments/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("更新部门 API 测试")
    class UpdateDepartmentApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该更新部门并返回 200")
        void shouldUpdateDepartmentAndReturn200() throws Exception {
            // Given
            DepartmentUpdateRequest request = new DepartmentUpdateRequest();
            request.setName("新名称");
            request.setDescription("新描述");
            request.setSortOrder(5);

            DepartmentDTO responseDto = createDepartmentDTO(TEST_DEPT_ID, "新名称", 2);

            given(departmentService.updateDepartment(any(), any(DepartmentUpdateRequest.class))).willReturn(responseDto);

            // When & Then
            mockMvc.perform(put("/api/departments/{id}", TEST_DEPT_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("新名称"));

            then(departmentService).should().updateDepartment(any(), any(DepartmentUpdateRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("部门不存在时返回 404")
        void shouldReturn404WhenDepartmentNotFound() throws Exception {
            // Given
            DepartmentUpdateRequest request = new DepartmentUpdateRequest();
            request.setName("新名称");

            given(departmentService.updateDepartment(any(), any()))
                .willThrow(new IllegalArgumentException("部门不存在"));

            // When & Then
            mockMvc.perform(put("/api/departments/{id}", TEST_DEPT_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("删除部门 API 测试")
    class DeleteDepartmentApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该删除部门并返回 204")
        void shouldDeleteDepartmentAndReturn204() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/departments/{id}", TEST_DEPT_ID)
                    .with(csrf()))
                .andExpect(status().isNoContent());

            then(departmentService).should().deleteDepartment(any());
        }

        @Test
        @WithMockUser
        @DisplayName("部门不存在时返回 404")
        void shouldReturn404WhenDepartmentNotFound() throws Exception {
            // Given
            given(departmentService.deleteDepartment(any()))
                .willThrow(new IllegalArgumentException("部门不存在"));

            // When & Then
            mockMvc.perform(delete("/api/departments/{id}", TEST_DEPT_ID)
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("移动部门 API 测试")
    class MoveDepartmentApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该移动部门并返回 200")
        void shouldMoveDepartmentAndReturn200() throws Exception {
            // Given
            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_PARENT_ID);
            request.setSortOrder(1);

            DepartmentDTO responseDto = createDepartmentDTO(TEST_DEPT_ID, "技术部", 2);

            given(departmentMoveService.moveDepartment(any(), any(DepartmentMoveRequest.class))).willReturn(responseDto);

            // When & Then
            mockMvc.perform(put("/api/departments/{id}/move", TEST_DEPT_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_DEPT_ID));

            then(departmentMoveService).should().moveDepartment(any(), any(DepartmentMoveRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("循环依赖时返回 400")
        void shouldReturn400WhenCircularDependency() throws Exception {
            // Given
            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_PARENT_ID);

            given(departmentMoveService.moveDepartment(any(), any()))
                .willThrow(new IllegalArgumentException("不能将部门移动到其子部门下"));

            // When & Then
            mockMvc.perform(put("/api/departments/{id}/move", TEST_DEPT_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("超过最大层级时返回 400")
        void shouldReturn400WhenExceedMaxLevel() throws Exception {
            // Given
            DepartmentMoveRequest request = new DepartmentMoveRequest();
            request.setParentId(TEST_PARENT_ID);

            given(departmentMoveService.moveDepartment(any(), any()))
                .willThrow(new IllegalArgumentException("部门层级不能超过 5 级"));

            // When & Then
            mockMvc.perform(put("/api/departments/{id}/move", TEST_DEPT_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("检查子部门 API 测试")
    class HasChildrenApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该返回部门有子部门")
        void shouldReturnHasChildren() throws Exception {
            // Given
            given(departmentService.hasChildren(any())).willReturn(true);

            // When & Then
            mockMvc.perform(get("/api/departments/{id}/children", TEST_DEPT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasChildren").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("应该返回部门没有子部门")
        void shouldReturnNoChildren() throws Exception {
            // Given
            given(departmentService.hasChildren(any())).willReturn(false);

            // When & Then
            mockMvc.perform(get("/api/departments/{id}/children", TEST_DEPT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasChildren").value(false));
        }
    }

    // 辅助方法
    private DepartmentDTO createDepartmentDTO(String id, String name, int level) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCode("CODE");
        dto.setLevel(level);
        dto.setPath("/path");
        dto.setStatus("ACTIVE");
        return dto;
    }
}
