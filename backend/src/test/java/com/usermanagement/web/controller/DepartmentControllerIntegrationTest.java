package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import com.usermanagement.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DepartmentController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DepartmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department rootDepartment;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();

        // 创建根部门
        rootDepartment = new Department();
        rootDepartment.setName("总公司");
        rootDepartment.setCode("HQ");
        rootDepartment.setLevel(1);
        rootDepartment.setPath("/root");
        rootDepartment.setStatus(DepartmentStatus.ACTIVE);
        rootDepartment = departmentRepository.save(rootDepartment);
    }

    @Nested
    @DisplayName("创建部门 API 集成测试")
    class CreateDepartmentApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("应该创建根部门")
        void shouldCreateRootDepartment() throws Exception {
            // Given
            String request = """
                {
                    "name": "技术部",
                    "code": "TECH",
                    "level": 1
                }
                """;

            // When & Then
            mockMvc.perform(post("/api/departments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("技术部"))
                .andExpect(jsonPath("$.code").value("TECH"))
                .andExpect(jsonPath("$.level").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("应该创建子部门")
        void shouldCreateChildDepartment() throws Exception {
            // Given
            String request = """
                {
                    "name": "研发部",
                    "code": "RD",
                    "level": 2,
                    "parentId": "%s"
                }
                """.formatted(rootDepartment.getId());

            // When & Then
            mockMvc.perform(post("/api/departments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("研发部"))
                .andExpect(jsonPath("$.code").value("RD"))
                .andExpect(jsonPath("$.level").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("部门代码已存在时返回 400")
        void shouldReturn400WhenCodeExists() throws Exception {
            // Given
            String request = """
                {
                    "name": "新总公司",
                    "code": "HQ",
                    "level": 1
                }
                """;

            // When & Then
            mockMvc.perform(post("/api/departments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("获取部门树 API 集成测试")
    class GetDepartmentTreeApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取部门树")
        void shouldGetDepartmentTree() throws Exception {
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
            // When & Then
            mockMvc.perform(get("/api/departments/tree")
                    .param("level", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tree").isArray());
        }
    }

    @Nested
    @DisplayName("获取部门详情 API 集成测试")
    class GetDepartmentApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取部门详情")
        void shouldGetDepartmentById() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/departments/{id}", rootDepartment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rootDepartment.getId().toString()))
                .andExpect(jsonPath("$.name").value("总公司"))
                .andExpect(jsonPath("$.code").value("HQ"));
        }

        @Test
        @WithMockUser
        @DisplayName("部门不存在时返回 404")
        void shouldReturn404WhenDepartmentNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(get("/api/departments/{id}", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("更新部门 API 集成测试")
    class UpdateDepartmentApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("应该更新部门信息")
        void shouldUpdateDepartment() throws Exception {
            // Given
            String request = """
                {
                    "name": "新总公司",
                    "description": "更新后的描述"
                }
                """;

            // When & Then
            mockMvc.perform(put("/api/departments/{id}", rootDepartment.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("新总公司"))
                .andExpect(jsonPath("$.description").value("更新后的描述"));
        }

        @Test
        @WithMockUser
        @DisplayName("应该更新部门负责人")
        void shouldUpdateDepartmentManager() throws Exception {
            // Given
            UUID managerId = UUID.randomUUID();
            String request = """
                {
                    "managerId": "%s"
                }
                """.formatted(managerId);

            // When & Then
            mockMvc.perform(put("/api/departments/{id}", rootDepartment.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("移动部门 API 集成测试")
    class MoveDepartmentApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("应该移动部门到新父部门")
        void shouldMoveDepartment() throws Exception {
            // Given
            Department newParent = new Department();
            newParent.setName("新部门");
            newParent.setCode("NEW");
            newParent.setLevel(1);
            newParent.setPath("/new");
            newParent.setStatus(DepartmentStatus.ACTIVE);
            newParent = departmentRepository.save(newParent);

            Department child = new Department();
            child.setName("子部门");
            child.setCode("CHILD");
            child.setLevel(2);
            child.setParentId(rootDepartment.getId());
            child.setPath("/root/child");
            child.setStatus(DepartmentStatus.ACTIVE);
            child = departmentRepository.save(child);

            String request = """
                {
                    "parentId": "%s"
                }
                """.formatted(newParent.getId());

            // When & Then
            mockMvc.perform(put("/api/departments/{id}/move", child.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("移动到自身时返回 400")
        void shouldReturn400WhenMovingToSelf() throws Exception {
            // Given
            String request = """
                {
                    "parentId": "%s"
                }
                """.formatted(rootDepartment.getId());

            // When & Then
            mockMvc.perform(put("/api/departments/{id}/move", rootDepartment.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("删除部门 API 集成测试")
    class DeleteDepartmentApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("应该删除部门")
        void shouldDeleteDepartment() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/departments/{id}", rootDepartment.getId())
                    .with(csrf()))
                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("部门不存在时返回 404")
        void shouldReturn404WhenDepartmentNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(delete("/api/departments/{id}", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("检查子部门 API 集成测试")
    class HasChildrenApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("应该返回部门没有子部门")
        void shouldReturnNoChildren() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/departments/{id}/children", rootDepartment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasChildren").value(false));
        }

        @Test
        @WithMockUser
        @DisplayName("应该返回部门有子部门")
        void shouldReturnHasChildren() throws Exception {
            // Given
            Department child = new Department();
            child.setName("子部门");
            child.setCode("CHILD");
            child.setLevel(2);
            child.setParentId(rootDepartment.getId());
            child.setPath("/root/child");
            child.setStatus(DepartmentStatus.ACTIVE);
            departmentRepository.save(child);

            // When & Then
            mockMvc.perform(get("/api/departments/{id}/children", rootDepartment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasChildren").value(true));
        }
    }
}
