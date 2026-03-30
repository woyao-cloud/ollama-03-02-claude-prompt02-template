package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.DataScope;
import com.usermanagement.domain.RoleStatus;
import com.usermanagement.service.RoleService;
import com.usermanagement.web.dto.*;
import com.usermanagement.web.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoleController 单元测试
 */
@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @MockBean
    private RoleMapper roleMapper;

    private static final String TEST_ROLE_NAME = "测试角色";
    private static final String TEST_ROLE_CODE = "ROLE_TEST";
    private static final String TEST_ROLE_DESCRIPTION = "测试角色描述";

    @Nested
    @DisplayName("获取角色列表测试")
    class GetRoleListTests {

        @Test
        @DisplayName("应该返回角色列表")
        void shouldReturnRoleList() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setId(roleId.toString());
            roleDTO.setName(TEST_ROLE_NAME);
            roleDTO.setCode(TEST_ROLE_CODE);
            roleDTO.setStatus(RoleStatus.ACTIVE);
            roleDTO.setDataScope(DataScope.ALL);

            given(roleService.getAllRoles()).willReturn(List.of(roleDTO));

            // When & Then
            mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(roleId.toString()))
                .andExpect(jsonPath("$[0].name").value(TEST_ROLE_NAME))
                .andExpect(jsonPath("$[0].code").value(TEST_ROLE_CODE));
        }

        @Test
        @DisplayName("应该返回空列表")
        void shouldReturnEmptyList() throws Exception {
            // Given
            given(roleService.getAllRoles()).willReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("获取单个角色测试")
    class GetRoleTests {

        @Test
        @DisplayName("应该根据 ID 获取角色")
        void shouldGetRoleById() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setId(roleId.toString());
            roleDTO.setName(TEST_ROLE_NAME);
            roleDTO.setCode(TEST_ROLE_CODE);
            roleDTO.setStatus(RoleStatus.ACTIVE);

            given(roleService.getRoleById(roleId)).willReturn(roleDTO);

            // When & Then
            mockMvc.perform(get("/api/roles/{id}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value(TEST_ROLE_NAME));
        }

        @Test
        @DisplayName("角色不存在时返回 404")
        void shouldReturn404WhenRoleNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(roleService.getRoleById(nonExistentId))
                .willThrow(new IllegalArgumentException("角色不存在"));

            // When & Then
            mockMvc.perform(get("/api/roles/{id}", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("创建角色测试")
    class CreateRoleTests {

        @Test
        @DisplayName("应该创建角色")
        void shouldCreateRole() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);
            request.setDescription(TEST_ROLE_DESCRIPTION);

            RoleDTO response = new RoleDTO();
            response.setId(roleId.toString());
            response.setName(TEST_ROLE_NAME);
            response.setCode(TEST_ROLE_CODE);
            response.setStatus(RoleStatus.ACTIVE);

            given(roleService.createRole(any(RoleCreateRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value(TEST_ROLE_NAME));
        }

        @Test
        @DisplayName("角色名称已存在时返回 400")
        void shouldReturn400WhenNameExists() throws Exception {
            // Given
            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);

            given(roleService.createRole(any(RoleCreateRequest.class)))
                .willThrow(new IllegalArgumentException("角色名称已存在"));

            // When & Then
            mockMvc.perform(post("/api/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("角色代码已存在时返回 400")
        void shouldReturn400WhenCodeExists() throws Exception {
            // Given
            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);

            given(roleService.createRole(any(RoleCreateRequest.class)))
                .willThrow(new IllegalArgumentException("角色代码已存在"));

            // When & Then
            mockMvc.perform(post("/api/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("请求参数无效时返回 400")
        void shouldReturn400WhenInvalidRequest() throws Exception {
            // Given
            RoleCreateRequest request = new RoleCreateRequest();
            // name 和 code 为空

            // When & Then
            mockMvc.perform(post("/api/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("更新角色测试")
    class UpdateRoleTests {

        @Test
        @DisplayName("应该更新角色")
        void shouldUpdateRole() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            RoleUpdateRequest request = new RoleUpdateRequest();
            request.setName("更新后的角色名");
            request.setDescription("更新后的描述");

            RoleDTO response = new RoleDTO();
            response.setId(roleId.toString());
            response.setName("更新后的角色名");
            response.setCode(TEST_ROLE_CODE);

            given(roleService.updateRole(any(UUID.class), any(RoleUpdateRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/roles/{id}", roleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value("更新后的角色名"));
        }

        @Test
        @DisplayName("角色不存在时返回 404")
        void shouldReturn404WhenRoleNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            RoleUpdateRequest request = new RoleUpdateRequest();
            request.setName("更新后的角色名");

            given(roleService.updateRole(any(UUID.class), any(RoleUpdateRequest.class)))
                .willThrow(new IllegalArgumentException("角色不存在"));

            // When & Then
            mockMvc.perform(put("/api/roles/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("删除角色测试")
    class DeleteRoleTests {

        @Test
        @DisplayName("应该删除角色")
        void shouldDeleteRole() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(delete("/api/roles/{id}", roleId))
                .andExpect(status().isNoContent());

            then(roleService).should().deleteRole(roleId);
        }

        @Test
        @DisplayName("角色不存在时返回 404")
        void shouldReturn404WhenRoleNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(roleService.getRoleById(nonExistentId))
                .willThrow(new IllegalArgumentException("角色不存在"));

            // When & Then
            mockMvc.perform(delete("/api/roles/{id}", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("分配权限测试")
    class AssignPermissionsTests {

        @Test
        @DisplayName("应该为角色分配权限")
        void shouldAssignPermissionsToRole() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID permissionId1 = UUID.randomUUID();
            UUID permissionId2 = UUID.randomUUID();

            AssignPermissionsRequest request = new AssignPermissionsRequest();
            request.setPermissionIds(List.of(permissionId1.toString(), permissionId2.toString()));

            RoleWithPermissionsDTO response = RoleWithPermissionsDTO.builder()
                .id(roleId.toString())
                .name(TEST_ROLE_NAME)
                .permissionIds(List.of(permissionId1.toString(), permissionId2.toString()))
                .build();

            given(roleService.assignPermissionsToRole(any(UUID.class), any(AssignPermissionsRequest.class)))
                .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/roles/{id}/permissions", roleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.permissionIds").isArray())
                .andExpect(jsonPath("$.permissionIds").value(List.of(permissionId1.toString(), permissionId2.toString())));
        }

        @Test
        @DisplayName("权限列表为空时返回 400")
        void shouldReturn400WhenEmptyPermissions() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            AssignPermissionsRequest request = new AssignPermissionsRequest();
            request.setPermissionIds(List.of());

            // When & Then
            mockMvc.perform(put("/api/roles/{id}/permissions", roleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("角色不存在时返回 404")
        void shouldReturn404WhenRoleNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            AssignPermissionsRequest request = new AssignPermissionsRequest();
            request.setPermissionIds(List.of(UUID.randomUUID().toString()));

            given(roleService.assignPermissionsToRole(any(UUID.class), any(AssignPermissionsRequest.class)))
                .willThrow(new IllegalArgumentException("角色不存在"));

            // When & Then
            mockMvc.perform(put("/api/roles/{id}/permissions", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("获取角色带权限测试")
    class GetRoleWithPermissionsTests {

        @Test
        @DisplayName("应该获取角色带权限的详情")
        void shouldGetRoleWithPermissions() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID permissionId = UUID.randomUUID();

            RoleWithPermissionsDTO response = RoleWithPermissionsDTO.builder()
                .id(roleId.toString())
                .name(TEST_ROLE_NAME)
                .permissionIds(List.of(permissionId.toString()))
                .build();

            given(roleService.getRoleWithPermissions(roleId)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/roles/{id}/permissions", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.permissionIds").isArray())
                .andExpect(jsonPath("$.permissionIds").value(List.of(permissionId.toString())));
        }

        @Test
        @DisplayName("角色不存在时返回 404")
        void shouldReturn404WhenRoleNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(roleService.getRoleWithPermissions(nonExistentId))
                .willThrow(new IllegalArgumentException("角色不存在"));

            // When & Then
            mockMvc.perform(get("/api/roles/{id}/permissions", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("更新角色状态测试")
    class UpdateRoleStatusTests {

        @Test
        @DisplayName("应该更新角色状态")
        void shouldUpdateRoleStatus() throws Exception {
            // Given
            UUID roleId = UUID.randomUUID();
            RoleStatusUpdateRequest request = new RoleStatusUpdateRequest();
            request.setStatus(RoleStatus.INACTIVE);

            RoleDTO response = new RoleDTO();
            response.setId(roleId.toString());
            response.setName(TEST_ROLE_NAME);
            response.setStatus(RoleStatus.INACTIVE);

            given(roleService.updateRoleStatus(any(UUID.class), any(RoleStatus.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/roles/{id}/status", roleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        }

        @Test
        @DisplayName("角色不存在时返回 404")
        void shouldReturn404WhenRoleNotFoundForStatusUpdate() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            RoleStatusUpdateRequest request = new RoleStatusUpdateRequest();
            request.setStatus(RoleStatus.INACTIVE);

            given(roleService.updateRoleStatus(any(UUID.class), any(RoleStatus.class)))
                .willThrow(new IllegalArgumentException("角色不存在"));

            // When & Then
            mockMvc.perform(put("/api/roles/{id}/status", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }
}
