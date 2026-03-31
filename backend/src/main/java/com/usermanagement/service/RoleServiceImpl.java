package com.usermanagement.service;

import com.usermanagement.domain.Permission;
import com.usermanagement.domain.Role;
import com.usermanagement.domain.RolePermission;
import com.usermanagement.domain.RolePermissionId;
import com.usermanagement.domain.RoleStatus;
import com.usermanagement.repository.PermissionRepository;
import com.usermanagement.repository.RolePermissionRepository;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.web.dto.*;
import com.usermanagement.web.mapper.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(
        RoleRepository roleRepository,
        PermissionRepository permissionRepository,
        RolePermissionRepository rolePermissionRepository,
        RoleMapper roleMapper
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleMapper = roleMapper;
    }

    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色 DTO
     */
    @Override
    @Transactional
    public RoleDTO createRole(RoleCreateRequest request) {
        // 检查角色名称是否已存在
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("角色名称已存在");
        }

        // 检查角色代码是否已存在
        if (roleRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("角色代码已存在");
        }

        // 创建角色
        Role role = roleMapper.toEntity(request);
        role.setStatus(RoleStatus.ACTIVE);
        role = roleRepository.save(role);
        logger.info("角色创建成功：{}", role.getName());

        return roleMapper.toDto(role);
    }

    /**
     * 根据 ID 获取角色
     *
     * @param id 角色 ID
     * @return 角色 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        return roleMapper.toDto(role);
    }

    /**
     * 获取所有角色列表
     *
     * 性能优化：使用 JOIN FETCH 避免 N+1 查询
     *
     * @return 角色列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        // 使用 JOIN FETCH 一次性加载角色和权限，避免 N+1 查询
        List<Role> roles = roleRepository.findAllWithPermissions();
        return roles.stream()
            .map(roleMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 根据状态获取角色列表
     *
     * @param status 角色状态
     * @return 角色列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getRolesByStatus(RoleStatus status) {
        List<Role> roles = roleRepository.findByStatus(status);
        return roles.stream()
            .map(roleMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 更新角色
     *
     * @param id      角色 ID
     * @param request 更新角色请求
     * @return 角色 DTO
     */
    @Override
    @Transactional
    public RoleDTO updateRole(UUID id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在"));

        // 检查名称是否与现有角色冲突
        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                Role existingRole = roleRepository.findByName(request.getName())
                    .orElse(null);
                if (existingRole != null && !existingRole.getId().equals(id)) {
                    throw new IllegalArgumentException("角色名称已存在");
                }
            }
        }

        // 检查代码是否与现有角色冲突
        if (request.getCode() != null && !request.getCode().equals(role.getCode())) {
            if (roleRepository.existsByCode(request.getCode())) {
                Role existingRole = roleRepository.findByCode(request.getCode())
                    .orElse(null);
                if (existingRole != null && !existingRole.getId().equals(id)) {
                    throw new IllegalArgumentException("角色代码已存在");
                }
            }
        }

        roleMapper.updateEntity(request, role);
        role = roleRepository.save(role);
        logger.info("角色更新成功：{}", role.getName());

        return roleMapper.toDto(role);
    }

    /**
     * 删除角色
     *
     * @param id 角色 ID
     */
    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在"));

        // 先删除权限关联
        rolePermissionRepository.deleteByRoleId(id);

        roleRepository.delete(role);
        logger.info("角色删除成功：{}", role.getName());
    }

    /**
     * 更新角色状态
     *
     * @param id     角色 ID
     * @param status 角色状态
     * @return 角色 DTO
     */
    @Override
    @Transactional
    public RoleDTO updateRoleStatus(UUID id, RoleStatus status) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在"));

        role.setStatus(status);
        role = roleRepository.save(role);
        logger.info("角色状态更新成功：{} -> {}", role.getName(), status);

        return roleMapper.toDto(role);
    }

    /**
     * 为角色分配权限
     *
     * @param roleId  角色 ID
     * @param request 分配权限请求
     * @return 带权限的角色 DTO
     */
    @Override
    @Transactional
    public RoleWithPermissionsDTO assignPermissionsToRole(UUID roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在"));

        // 解析权限 ID 列表
        List<UUID> permissionIds = request.getPermissionIds().stream()
            .map(UUID::fromString)
            .collect(Collectors.toList());

        // 验证权限是否存在
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new IllegalArgumentException("权限不存在");
        }

        // 删除旧的权限关联
        rolePermissionRepository.deleteByRoleId(roleId);

        // 创建新的权限关联
        List<RolePermission> newAssociations = new ArrayList<>();
        for (Permission permission : permissions) {
            RolePermissionId id = new RolePermissionId();
            id.setRoleId(roleId);
            id.setPermissionId(permission.getId());

            RolePermission association = RolePermission.builder()
                .id(id)
                .role(role)
                .permission(permission)
                .build();
            newAssociations.add(association);
        }
        rolePermissionRepository.saveAll(newAssociations);

        logger.info("角色权限分配成功：{} - {} 个权限", role.getName(), permissions.size());

        return buildRoleWithPermissionsDto(role, permissions);
    }

    /**
     * 获取角色带权限的详情
     *
     * 性能优化：使用 JOIN FETCH 避免 N+1 查询
     *
     * @param roleId 角色 ID
     * @return 带权限的角色 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public RoleWithPermissionsDTO getRoleWithPermissions(UUID roleId) {
        // 使用 JOIN FETCH 一次性加载角色和权限，避免 N+1 查询
        Role role = roleRepository.findByIdWithPermissions(roleId)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在"));

        return buildRoleWithPermissionsDto(role, role.getPermissions());
    }

    /**
     * 检查角色代码是否存在
     *
     * @param code 角色代码
     * @return 是否存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return roleRepository.existsByCode(code);
    }

    /**
     * 构建带权限的角色 DTO
     *
     * @param role        角色实体
     * @param permissions 权限列表
     * @return 带权限的角色 DTO
     */
    private RoleWithPermissionsDTO buildRoleWithPermissionsDto(Role role, List<Permission> permissions) {
        List<String> permissionIds = permissions.stream()
            .map(p -> p.getId().toString())
            .collect(Collectors.toList());

        return RoleWithPermissionsDTO.builder()
            .id(role.getId().toString())
            .name(role.getName())
            .code(role.getCode())
            .description(role.getDescription())
            .dataScope(role.getDataScope())
            .status(role.getStatus())
            .permissionIds(permissionIds)
            .build();
    }
}
