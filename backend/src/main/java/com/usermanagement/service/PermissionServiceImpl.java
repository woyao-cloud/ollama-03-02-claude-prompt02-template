package com.usermanagement.service;

import com.usermanagement.domain.Permission;
import com.usermanagement.domain.PermissionStatus;
import com.usermanagement.domain.PermissionType;
import com.usermanagement.repository.PermissionRepository;
import com.usermanagement.web.dto.*;
import com.usermanagement.web.mapper.PermissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(
        PermissionRepository permissionRepository,
        PermissionMapper permissionMapper
    ) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 创建权限
     *
     * @param request 创建权限请求
     * @return 权限 DTO
     */
    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionCreateRequest request) {
        // 检查权限代码是否已存在
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("权限代码已存在");
        }

        // 创建权限
        Permission permission = permissionMapper.toEntity(request);
        permission.setStatus(PermissionStatus.ACTIVE);
        permission = permissionRepository.save(permission);
        logger.info("权限创建成功：{}", permission.getName());

        return permissionMapper.toDto(permission);
    }

    /**
     * 根据 ID 获取权限
     *
     * @param id 权限 ID
     * @return 权限 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("权限不存在"));
        return permissionMapper.toDto(permission);
    }

    /**
     * 获取所有权限列表
     *
     * @return 权限列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
            .map(permissionMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 根据状态获取权限列表
     *
     * @param status 权限状态
     * @return 权限列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsByStatus(PermissionStatus status) {
        List<Permission> permissions = permissionRepository.findByStatus(status);
        return permissions.stream()
            .map(permissionMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 根据类型获取权限列表
     *
     * @param type 权限类型
     * @return 权限列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsByType(PermissionType type) {
        List<Permission> permissions = permissionRepository.findByType(type);
        return permissions.stream()
            .map(permissionMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 根据资源获取权限列表
     *
     * @param resource 资源名称
     * @return 权限列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsByResource(String resource) {
        List<Permission> permissions = permissionRepository.findByResource(resource);
        return permissions.stream()
            .map(permissionMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 更新权限
     *
     * @param id      权限 ID
     * @param request 更新权限请求
     * @return 权限 DTO
     */
    @Override
    @Transactional
    public PermissionDTO updatePermission(UUID id, PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("权限不存在"));

        // 检查代码是否与现有权限冲突
        if (request.getCode() != null && !request.getCode().equals(permission.getCode())) {
            if (permissionRepository.existsByCode(request.getCode())) {
                Permission existingPermission = permissionRepository.findByCode(request.getCode())
                    .orElse(null);
                if (existingPermission != null && !existingPermission.getId().equals(id)) {
                    throw new IllegalArgumentException("权限代码已存在");
                }
            }
        }

        permissionMapper.updateEntity(request, permission);
        permission = permissionRepository.save(permission);
        logger.info("权限更新成功：{}", permission.getName());

        return permissionMapper.toDto(permission);
    }

    /**
     * 删除权限
     *
     * @param id 权限 ID
     */
    @Override
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("权限不存在"));

        // 先删除角色关联
        // rolePermissionRepository.deleteByPermissionId(id);

        permissionRepository.delete(permission);
        logger.info("权限删除成功：{}", permission.getName());
    }

    /**
     * 更新权限状态
     *
     * @param id     权限 ID
     * @param status 权限状态
     * @return 权限 DTO
     */
    @Override
    @Transactional
    public PermissionDTO updatePermissionStatus(UUID id, PermissionStatus status) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("权限不存在"));

        permission.setStatus(status);
        permission = permissionRepository.save(permission);
        logger.info("权限状态更新成功：{} -> {}", permission.getName(), status);

        return permissionMapper.toDto(permission);
    }
}
