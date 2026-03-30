package com.usermanagement.service;

import com.usermanagement.domain.Permission;
import com.usermanagement.domain.PermissionStatus;
import com.usermanagement.domain.PermissionType;
import com.usermanagement.repository.PermissionRepository;
import com.usermanagement.web.dto.PermissionDTO;
import com.usermanagement.web.dto.PermissionCreateRequest;
import com.usermanagement.web.mapper.PermissionMapper;
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
 * PermissionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    private PermissionServiceImpl permissionService;

    private static final String TEST_PERMISSION_NAME = "测试权限";
    private static final String TEST_PERMISSION_CODE = "user:read";
    private static final String TEST_RESOURCE = "user";
    private static final String TEST_ACTION = "read";

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(permissionRepository, permissionMapper);
    }

    @Nested
    @DisplayName("创建权限测试")
    class CreatePermissionTests {

        @Test
        @DisplayName("应该创建权限")
        void shouldCreatePermission() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            PermissionDTO dto = createPermissionDTO(permissionId, PermissionStatus.ACTIVE);

            PermissionCreateRequest request = new PermissionCreateRequest();
            request.setName(TEST_PERMISSION_NAME);
            request.setCode(TEST_PERMISSION_CODE);
            request.setType(PermissionType.BUTTON);
            request.setResource(TEST_RESOURCE);
            request.setAction(TEST_ACTION);

            given(permissionRepository.existsByCode(TEST_PERMISSION_CODE)).willReturn(false);
            given(permissionMapper.toEntity(request)).willReturn(permission);
            given(permissionRepository.save(permission)).willReturn(permission);
            given(permissionMapper.toDto(permission)).willReturn(dto);

            // When
            PermissionDTO result = permissionService.createPermission(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(permissionId.toString());
            assertThat(result.getCode()).isEqualTo(TEST_PERMISSION_CODE);

            ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);
            then(permissionRepository).should().save(permissionCaptor.capture());
            Permission savedPermission = permissionCaptor.getValue();
            assertThat(savedPermission.getStatus()).isEqualTo(PermissionStatus.ACTIVE);
        }

        @Test
        @DisplayName("权限代码已存在时抛出异常")
        void shouldThrowExceptionWhenCodeExists() {
            // Given
            PermissionCreateRequest request = new PermissionCreateRequest();
            request.setName(TEST_PERMISSION_NAME);
            request.setCode(TEST_PERMISSION_CODE);

            given(permissionRepository.existsByCode(TEST_PERMISSION_CODE)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> permissionService.createPermission(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限代码已存在");

            verify(permissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("应该设置默认状态为 ACTIVE")
        void shouldSetDefaultStatusToActive() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);

            PermissionCreateRequest request = new PermissionCreateRequest();
            request.setName(TEST_PERMISSION_NAME);
            request.setCode(TEST_PERMISSION_CODE);

            given(permissionRepository.existsByCode(TEST_PERMISSION_CODE)).willReturn(false);
            given(permissionMapper.toEntity(request)).willReturn(permission);
            given(permissionRepository.save(permission)).willReturn(permission);

            // When
            permissionService.createPermission(request);

            // Then
            assertThat(permission.getStatus()).isEqualTo(PermissionStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("获取权限测试")
    class GetPermissionTests {

        @Test
        @DisplayName("应该根据 ID 获取权限")
        void shouldGetPermissionById() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            PermissionDTO dto = createPermissionDTO(permissionId, PermissionStatus.ACTIVE);

            given(permissionRepository.findById(permissionId)).willReturn(Optional.of(permission));
            given(permissionMapper.toDto(permission)).willReturn(dto);

            // When
            PermissionDTO result = permissionService.getPermissionById(permissionId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(permissionId.toString());
            assertThat(result.getCode()).isEqualTo(TEST_PERMISSION_CODE);
        }

        @Test
        @DisplayName("权限不存在时抛出异常")
        void shouldThrowExceptionWhenPermissionNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(permissionRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> permissionService.getPermissionById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限不存在");
        }
    }

    @Nested
    @DisplayName("获取权限列表测试")
    class GetPermissionListTests {

        @Test
        @DisplayName("应该获取所有权限列表")
        void shouldGetAllPermissions() {
            // Given
            UUID permissionId1 = UUID.randomUUID();
            UUID permissionId2 = UUID.randomUUID();
            Permission permission1 = createPermission(permissionId1, PermissionStatus.ACTIVE);
            Permission permission2 = createPermission(permissionId2, PermissionStatus.INACTIVE);
            PermissionDTO dto1 = createPermissionDTO(permissionId1, PermissionStatus.ACTIVE);
            PermissionDTO dto2 = createPermissionDTO(permissionId2, PermissionStatus.INACTIVE);

            List<Permission> permissions = List.of(permission1, permission2);
            given(permissionRepository.findAll()).willReturn(permissions);
            given(permissionMapper.toDto(permission1)).willReturn(dto1);
            given(permissionMapper.toDto(permission2)).willReturn(dto2);

            // When
            List<PermissionDTO> result = permissionService.getAllPermissions();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCode()).isEqualTo(TEST_PERMISSION_CODE);
        }

        @Test
        @DisplayName("应该获取激活状态的权限列表")
        void shouldGetActivePermissions() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            PermissionDTO dto = createPermissionDTO(permissionId, PermissionStatus.ACTIVE);

            List<Permission> permissions = List.of(permission);
            given(permissionRepository.findByStatus(PermissionStatus.ACTIVE)).willReturn(permissions);
            given(permissionMapper.toDto(permission)).willReturn(dto);

            // When
            List<PermissionDTO> result = permissionService.getPermissionsByStatus(PermissionStatus.ACTIVE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(PermissionStatus.ACTIVE);
        }

        @Test
        @DisplayName("应该根据类型获取权限列表")
        void shouldGetPermissionsByType() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            permission.setType(PermissionType.BUTTON);
            PermissionDTO dto = createPermissionDTO(permissionId, PermissionStatus.ACTIVE);

            List<Permission> permissions = List.of(permission);
            given(permissionRepository.findByType(PermissionType.BUTTON)).willReturn(permissions);
            given(permissionMapper.toDto(permission)).willReturn(dto);

            // When
            List<PermissionDTO> result = permissionService.getPermissionsByType(PermissionType.BUTTON);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("应该根据资源获取权限列表")
        void shouldGetPermissionsByResource() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            permission.setResource(TEST_RESOURCE);
            PermissionDTO dto = createPermissionDTO(permissionId, PermissionStatus.ACTIVE);

            List<Permission> permissions = List.of(permission);
            given(permissionRepository.findByResource(TEST_RESOURCE)).willReturn(permissions);
            given(permissionMapper.toDto(permission)).willReturn(dto);

            // When
            List<PermissionDTO> result = permissionService.getPermissionsByResource(TEST_RESOURCE);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("更新权限测试")
    class UpdatePermissionTests {

        @Test
        @DisplayName("应该更新权限信息")
        void shouldUpdatePermission() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            PermissionDTO dto = createPermissionDTO(permissionId, PermissionStatus.ACTIVE);

            com.usermanagement.web.dto.PermissionUpdateRequest request =
                new com.usermanagement.web.dto.PermissionUpdateRequest();
            request.setName("更新后的权限名");
            request.setResource("role");

            given(permissionRepository.findById(permissionId)).willReturn(Optional.of(permission));
            given(permissionMapper.toDto(permission)).willReturn(dto);

            // When
            PermissionDTO result = permissionService.updatePermission(permissionId, request);

            // Then
            assertThat(result).isNotNull();
            verify(permissionRepository).save(permission);
            assertThat(permission.getName()).isEqualTo("更新后的权限名");
            assertThat(permission.getResource()).isEqualTo("role");
        }

        @Test
        @DisplayName("权限不存在时抛出异常")
        void shouldThrowExceptionWhenPermissionNotFoundForUpdate() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            com.usermanagement.web.dto.PermissionUpdateRequest request =
                new com.usermanagement.web.dto.PermissionUpdateRequest();
            request.setName("更新后的权限名");

            given(permissionRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> permissionService.updatePermission(nonExistentId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限不存在");
        }

        @Test
        @DisplayName("更新代码与现有权限冲突时抛出异常")
        void shouldThrowExceptionWhenCodeConflict() {
            // Given
            UUID permissionId = UUID.randomUUID();
            UUID anotherPermissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            String newCode = "user:write";

            com.usermanagement.web.dto.PermissionUpdateRequest request =
                new com.usermanagement.web.dto.PermissionUpdateRequest();
            request.setCode(newCode);

            given(permissionRepository.findById(permissionId)).willReturn(Optional.of(permission));
            given(permissionRepository.existsByCode(newCode)).willReturn(true);
            given(permissionRepository.findByCode(newCode))
                .willReturn(Optional.of(createPermission(anotherPermissionId, PermissionStatus.ACTIVE)));

            // When & Then
            assertThatThrownBy(() -> permissionService.updatePermission(permissionId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限代码已存在");
        }
    }

    @Nested
    @DisplayName("删除权限测试")
    class DeletePermissionTests {

        @Test
        @DisplayName("应该删除权限")
        void shouldDeletePermission() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);

            given(permissionRepository.findById(permissionId)).willReturn(Optional.of(permission));

            // When
            permissionService.deletePermission(permissionId);

            // Then
            verify(permissionRepository).delete(permission);
        }

        @Test
        @DisplayName("删除权限时应先删除角色关联")
        void shouldDeleteRoleAssociationsFirst() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);

            given(permissionRepository.findById(permissionId)).willReturn(Optional.of(permission));

            // When
            permissionService.deletePermission(permissionId);

            // Then
            verify(permissionRepository).delete(permission);
        }

        @Test
        @DisplayName("权限不存在时抛出异常")
        void shouldThrowExceptionWhenPermissionNotFoundForDelete() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(permissionRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> permissionService.deletePermission(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限不存在");
        }
    }

    @Nested
    @DisplayName("更新权限状态测试")
    class UpdatePermissionStatusTests {

        @Test
        @DisplayName("应该更新权限状态为 INACTIVE")
        void shouldUpdatePermissionStatusToInactive() {
            // Given
            UUID permissionId = UUID.randomUUID();
            Permission permission = createPermission(permissionId, PermissionStatus.ACTIVE);
            PermissionDTO dto = createPermissionDTO(permissionId, PermissionStatus.INACTIVE);

            given(permissionRepository.findById(permissionId)).willReturn(Optional.of(permission));
            given(permissionMapper.toDto(permission)).willReturn(dto);

            // When
            PermissionDTO result = permissionService.updatePermissionStatus(permissionId, PermissionStatus.INACTIVE);

            // Then
            assertThat(result.getStatus()).isEqualTo(PermissionStatus.INACTIVE);
            assertThat(permission.getStatus()).isEqualTo(PermissionStatus.INACTIVE);
            verify(permissionRepository).save(permission);
        }

        @Test
        @DisplayName("权限不存在时抛出异常")
        void shouldThrowExceptionWhenPermissionNotFoundForStatusUpdate() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(permissionRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                permissionService.updatePermissionStatus(nonExistentId, PermissionStatus.INACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限不存在");
        }
    }

    // 辅助方法
    private Permission createPermission(UUID id, PermissionStatus status) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName(TEST_PERMISSION_NAME);
        permission.setCode(TEST_PERMISSION_CODE);
        permission.setType(PermissionType.BUTTON);
        permission.setResource(TEST_RESOURCE);
        permission.setAction(TEST_ACTION);
        permission.setStatus(status);
        return permission;
    }

    private PermissionDTO createPermissionDTO(UUID id, PermissionStatus status) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(id.toString());
        dto.setName(TEST_PERMISSION_NAME);
        dto.setCode(TEST_PERMISSION_CODE);
        dto.setType(PermissionType.BUTTON);
        dto.setResource(TEST_RESOURCE);
        dto.setAction(TEST_ACTION);
        dto.setStatus(status);
        return dto;
    }
}
