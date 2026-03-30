package com.usermanagement.service;

import com.usermanagement.domain.DataScope;
import com.usermanagement.domain.Permission;
import com.usermanagement.domain.PermissionStatus;
import com.usermanagement.domain.PermissionType;
import com.usermanagement.domain.Role;
import com.usermanagement.domain.RolePermission;
import com.usermanagement.domain.RoleStatus;
import com.usermanagement.repository.PermissionRepository;
import com.usermanagement.repository.RolePermissionRepository;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.web.dto.RoleCreateRequest;
import com.usermanagement.web.dto.RoleDTO;
import com.usermanagement.web.dto.RoleUpdateRequest;
import com.usermanagement.web.dto.RoleWithPermissionsDTO;
import com.usermanagement.web.dto.AssignPermissionsRequest;
import com.usermanagement.web.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * RoleService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RoleMapper roleMapper;

    private RoleServiceImpl roleService;

    private static final String TEST_ROLE_NAME = "测试角色";
    private static final String TEST_ROLE_CODE = "ROLE_TEST";
    private static final String TEST_ROLE_DESCRIPTION = "测试角色描述";

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(roleRepository, permissionRepository, rolePermissionRepository, roleMapper);
    }

    @Nested
    @DisplayName("创建角色测试")
    class CreateRoleTests {

        @Test
        @DisplayName("应该创建角色")
        void shouldCreateRole() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            RoleDTO dto = createRoleDTO(roleId, RoleStatus.ACTIVE);

            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);
            request.setDescription(TEST_ROLE_DESCRIPTION);

            given(roleRepository.existsByName(TEST_ROLE_NAME)).willReturn(false);
            given(roleRepository.existsByCode(TEST_ROLE_CODE)).willReturn(false);
            given(roleMapper.toEntity(request)).willReturn(role);
            given(roleRepository.save(role)).willReturn(role);
            given(roleMapper.toDto(role)).willReturn(dto);

            // When
            RoleDTO result = roleService.createRole(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(roleId.toString());
            assertThat(result.getName()).isEqualTo(TEST_ROLE_NAME);
            assertThat(result.getCode()).isEqualTo(TEST_ROLE_CODE);

            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            then(roleRepository).should().save(roleCaptor.capture());
            Role savedRole = roleCaptor.getValue();
            assertThat(savedRole.getStatus()).isEqualTo(RoleStatus.ACTIVE);
            assertThat(savedRole.getDataScope()).isEqualTo(DataScope.ALL);
        }

        @Test
        @DisplayName("角色名称已存在时抛出异常")
        void shouldThrowExceptionWhenNameExists() {
            // Given
            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);

            given(roleRepository.existsByName(TEST_ROLE_NAME)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色名称已存在");

            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("角色代码已存在时抛出异常")
        void shouldThrowExceptionWhenCodeExists() {
            // Given
            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);

            given(roleRepository.existsByName(TEST_ROLE_NAME)).willReturn(false);
            given(roleRepository.existsByCode(TEST_ROLE_CODE)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色代码已存在");

            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("应该设置默认数据权限范围为 ALL")
        void shouldSetDefaultDataScopeToAll() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);

            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);

            given(roleRepository.existsByName(TEST_ROLE_NAME)).willReturn(false);
            given(roleRepository.existsByCode(TEST_ROLE_CODE)).willReturn(false);
            given(roleMapper.toEntity(request)).willReturn(role);
            given(roleRepository.save(role)).willReturn(role);

            // When
            roleService.createRole(request);

            // Then
            assertThat(role.getDataScope()).isEqualTo(DataScope.ALL);
        }

        @Test
        @DisplayName("应该设置默认状态为 ACTIVE")
        void shouldSetDefaultStatusToActive() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);

            RoleCreateRequest request = new RoleCreateRequest();
            request.setName(TEST_ROLE_NAME);
            request.setCode(TEST_ROLE_CODE);

            given(roleRepository.existsByName(TEST_ROLE_NAME)).willReturn(false);
            given(roleRepository.existsByCode(TEST_ROLE_CODE)).willReturn(false);
            given(roleMapper.toEntity(request)).willReturn(role);
            given(roleRepository.save(role)).willReturn(role);

            // When
            roleService.createRole(request);

            // Then
            assertThat(role.getStatus()).isEqualTo(RoleStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("获取角色测试")
    class GetRoleTests {

        @Test
        @DisplayName("应该根据 ID 获取角色")
        void shouldGetRoleById() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            RoleDTO dto = createRoleDTO(roleId, RoleStatus.ACTIVE);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(roleMapper.toDto(role)).willReturn(dto);

            // When
            RoleDTO result = roleService.getRoleById(roleId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(roleId.toString());
            assertThat(result.getName()).isEqualTo(TEST_ROLE_NAME);
        }

        @Test
        @DisplayName("角色不存在时抛出异常")
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(roleRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.getRoleById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("获取角色列表测试")
    class GetRoleListTests {

        @Test
        @DisplayName("应该获取所有角色列表")
        void shouldGetAllRoles() {
            // Given
            UUID roleId1 = UUID.randomUUID();
            UUID roleId2 = UUID.randomUUID();
            Role role1 = createRole(roleId1, RoleStatus.ACTIVE, DataScope.ALL);
            Role role2 = createRole(roleId2, RoleStatus.INACTIVE, DataScope.DEPT);
            RoleDTO dto1 = createRoleDTO(roleId1, RoleStatus.ACTIVE);
            RoleDTO dto2 = createRoleDTO(roleId2, RoleStatus.INACTIVE);

            List<Role> roles = List.of(role1, role2);
            given(roleRepository.findAll()).willReturn(roles);
            given(roleMapper.toDto(role1)).willReturn(dto1);
            given(roleMapper.toDto(role2)).willReturn(dto2);

            // When
            List<RoleDTO> result = roleService.getAllRoles();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo(TEST_ROLE_NAME);
        }

        @Test
        @DisplayName("应该获取激活状态的角色列表")
        void shouldGetActiveRoles() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            RoleDTO dto = createRoleDTO(roleId, RoleStatus.ACTIVE);

            List<Role> roles = List.of(role);
            given(roleRepository.findByStatus(RoleStatus.ACTIVE)).willReturn(roles);
            given(roleMapper.toDto(role)).willReturn(dto);

            // When
            List<RoleDTO> result = roleService.getRolesByStatus(RoleStatus.ACTIVE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(RoleStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("更新角色测试")
    class UpdateRoleTests {

        @Test
        @DisplayName("应该更新角色信息")
        void shouldUpdateRole() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            RoleDTO dto = createRoleDTO(roleId, RoleStatus.ACTIVE);

            RoleUpdateRequest request = new RoleUpdateRequest();
            request.setName("更新后的角色名");
            request.setDescription("更新后的描述");
            request.setDataScope(DataScope.DEPT);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(roleMapper.toDto(role)).willReturn(dto);

            // When
            RoleDTO result = roleService.updateRole(roleId, request);

            // Then
            assertThat(result).isNotNull();
            verify(roleRepository).save(role);
            assertThat(role.getName()).isEqualTo("更新后的角色名");
            assertThat(role.getDescription()).isEqualTo("更新后的描述");
            assertThat(role.getDataScope()).isEqualTo(DataScope.DEPT);
        }

        @Test
        @DisplayName("角色不存在时抛出异常")
        void shouldThrowExceptionWhenRoleNotFoundForUpdate() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            RoleUpdateRequest request = new RoleUpdateRequest();
            request.setName("更新后的角色名");

            given(roleRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.updateRole(nonExistentId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色不存在");
        }

        @Test
        @DisplayName("更新名称与现有角色冲突时抛出异常")
        void shouldThrowExceptionWhenNameConflict() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID anotherRoleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            String newName = "另一个角色名";

            RoleUpdateRequest request = new RoleUpdateRequest();
            request.setName(newName);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(roleRepository.existsByName(newName)).willReturn(true);
            given(roleRepository.findByName(newName)).willReturn(Optional.of(createRole(anotherRoleId, RoleStatus.ACTIVE, DataScope.ALL)));

            // When & Then
            assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色名称已存在");
        }

        @Test
        @DisplayName("更新代码与现有角色冲突时抛出异常")
        void shouldThrowExceptionWhenCodeConflict() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID anotherRoleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            String newCode = "ROLE_ANOTHER";

            RoleUpdateRequest request = new RoleUpdateRequest();
            request.setCode(newCode);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(roleRepository.existsByCode(newCode)).willReturn(true);
            given(roleRepository.findByCode(newCode)).willReturn(Optional.of(createRole(anotherRoleId, RoleStatus.ACTIVE, DataScope.ALL)));

            // When & Then
            assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色代码已存在");
        }
    }

    @Nested
    @DisplayName("删除角色测试")
    class DeleteRoleTests {

        @Test
        @DisplayName("应该删除角色")
        void shouldDeleteRole() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));

            // When
            roleService.deleteRole(roleId);

            // Then
            verify(roleRepository).delete(role);
        }

        @Test
        @DisplayName("删除角色时应先删除权限关联")
        void shouldDeletePermissionAssociationsFirst() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(rolePermissionRepository.findByRoleId(roleId)).willReturn(List.of());

            // When
            roleService.deleteRole(roleId);

            // Then
            verify(rolePermissionRepository).deleteByRoleId(roleId);
            verify(roleRepository).delete(role);
        }

        @Test
        @DisplayName("角色不存在时抛出异常")
        void shouldThrowExceptionWhenRoleNotFoundForDelete() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(roleRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.deleteRole(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("更新角色状态测试")
    class UpdateRoleStatusTests {

        @Test
        @DisplayName("应该更新角色状态为 INACTIVE")
        void shouldUpdateRoleStatusToInactive() {
            // Given
            UUID roleId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            RoleDTO dto = createRoleDTO(roleId, RoleStatus.INACTIVE);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(roleMapper.toDto(role)).willReturn(dto);

            // When
            RoleDTO result = roleService.updateRoleStatus(roleId, RoleStatus.INACTIVE);

            // Then
            assertThat(result.getStatus()).isEqualTo(RoleStatus.INACTIVE);
            assertThat(role.getStatus()).isEqualTo(RoleStatus.INACTIVE);
            verify(roleRepository).save(role);
        }

        @Test
        @DisplayName("角色不存在时抛出异常")
        void shouldThrowExceptionWhenRoleNotFoundForStatusUpdate() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(roleRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.updateRoleStatus(nonExistentId, RoleStatus.INACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("分配权限测试")
    class AssignPermissionsTests {

        @Test
        @DisplayName("应该为角色分配权限")
        void shouldAssignPermissionsToRole() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID permissionId1 = UUID.randomUUID();
            UUID permissionId2 = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            Permission permission1 = createPermission(permissionId1, "user:read", PermissionType.BUTTON);
            Permission permission2 = createPermission(permissionId2, "user:write", PermissionType.BUTTON);

            AssignPermissionsRequest request = new AssignPermissionsRequest();
            request.setPermissionIds(List.of(permissionId1.toString(), permissionId2.toString()));

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(permissionRepository.findAllById(List.of(permissionId1, permissionId2)))
                .willReturn(List.of(permission1, permission2));
            given(rolePermissionRepository.findByRoleId(roleId)).willReturn(List.of());

            // When
            RoleWithPermissionsDTO result = roleService.assignPermissionsToRole(roleId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPermissionIds()).hasSize(2);
            verify(rolePermissionRepository).deleteByRoleId(roleId);
            verify(rolePermissionRepository).saveAll(any());
        }

        @Test
        @DisplayName("角色不存在时抛出异常")
        void shouldThrowExceptionWhenRoleNotFoundForPermissionAssignment() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            AssignPermissionsRequest request = new AssignPermissionsRequest();
            request.setPermissionIds(List.of(UUID.randomUUID().toString()));

            given(roleRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.assignPermissionsToRole(nonExistentId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色不存在");
        }

        @Test
        @DisplayName("权限不存在时抛出异常")
        void shouldThrowExceptionWhenPermissionNotFound() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID invalidPermissionId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);

            AssignPermissionsRequest request = new AssignPermissionsRequest();
            request.setPermissionIds(List.of(invalidPermissionId.toString()));

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(permissionRepository.findAllById(List.of(invalidPermissionId))).willReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> roleService.assignPermissionsToRole(roleId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限不存在");
        }

        @Test
        @DisplayName("应该获取角色带权限的详情")
        void shouldGetRoleWithPermissions() {
            // Given
            UUID roleId = UUID.randomUUID();
            UUID permissionId = UUID.randomUUID();
            Role role = createRole(roleId, RoleStatus.ACTIVE, DataScope.ALL);
            Permission permission = createPermission(permissionId, "user:read", PermissionType.BUTTON);
            RolePermission rolePermission = RolePermission.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .build();

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(rolePermissionRepository.findByRoleId(roleId)).willReturn(List.of(rolePermission));
            given(permissionRepository.findById(permissionId)).willReturn(Optional.of(permission));

            // When
            RoleWithPermissionsDTO result = roleService.getRoleWithPermissions(roleId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPermissionIds()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("检查角色代码测试")
    class CheckRoleCodeTests {

        @Test
        @DisplayName("应该检查角色代码是否存在")
        void shouldCheckRoleCodeExists() {
            // Given
            given(roleRepository.existsByCode(TEST_ROLE_CODE)).willReturn(true);

            // When
            boolean exists = roleService.existsByCode(TEST_ROLE_CODE);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("应该检查角色代码不存在")
        void shouldCheckRoleCodeNotExists() {
            // Given
            given(roleRepository.existsByCode(TEST_ROLE_CODE)).willReturn(false);

            // When
            boolean exists = roleService.existsByCode(TEST_ROLE_CODE);

            // Then
            assertThat(exists).isFalse();
        }
    }

    // 辅助方法
    private Role createRole(UUID id, RoleStatus status, DataScope dataScope) {
        Role role = new Role();
        role.setId(id);
        role.setName(TEST_ROLE_NAME);
        role.setCode(TEST_ROLE_CODE);
        role.setDescription(TEST_ROLE_DESCRIPTION);
        role.setDataScope(dataScope);
        role.setStatus(status);
        return role;
    }

    private RoleDTO createRoleDTO(UUID id, RoleStatus status) {
        RoleDTO dto = new RoleDTO();
        dto.setId(id.toString());
        dto.setName(TEST_ROLE_NAME);
        dto.setCode(TEST_ROLE_CODE);
        dto.setDescription(TEST_ROLE_DESCRIPTION);
        dto.setDataScope(DataScope.ALL);
        dto.setStatus(status);
        return dto;
    }

    private Permission createPermission(UUID id, String code, PermissionType type) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName("测试权限");
        permission.setCode(code);
        permission.setType(type);
        permission.setResource("user");
        permission.setAction("read");
        permission.setStatus(PermissionStatus.ACTIVE);
        return permission;
    }
}
