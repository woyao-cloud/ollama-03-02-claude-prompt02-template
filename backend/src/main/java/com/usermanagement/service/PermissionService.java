package com.usermanagement.service;

import com.usermanagement.domain.PermissionStatus;
import com.usermanagement.domain.PermissionType;
import com.usermanagement.web.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * 权限服务接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 创建权限
     *
     * @param request 创建权限请求
     * @return 权限 DTO
     */
    PermissionDTO createPermission(PermissionCreateRequest request);

    /**
     * 根据 ID 获取权限
     *
     * @param id 权限 ID
     * @return 权限 DTO
     */
    PermissionDTO getPermissionById(UUID id);

    /**
     * 获取所有权限列表
     *
     * @return 权限列表
     */
    List<PermissionDTO> getAllPermissions();

    /**
     * 根据状态获取权限列表
     *
     * @param status 权限状态
     * @return 权限列表
     */
    List<PermissionDTO> getPermissionsByStatus(PermissionStatus status);

    /**
     * 根据类型获取权限列表
     *
     * @param type 权限类型
     * @return 权限列表
     */
    List<PermissionDTO> getPermissionsByType(PermissionType type);

    /**
     * 根据资源获取权限列表
     *
     * @param resource 资源名称
     * @return 权限列表
     */
    List<PermissionDTO> getPermissionsByResource(String resource);

    /**
     * 更新权限
     *
     * @param id      权限 ID
     * @param request 更新权限请求
     * @return 权限 DTO
     */
    PermissionDTO updatePermission(UUID id, PermissionUpdateRequest request);

    /**
     * 删除权限
     *
     * @param id 权限 ID
     */
    void deletePermission(UUID id);

    /**
     * 更新权限状态
     *
     * @param id     权限 ID
     * @param status 权限状态
     * @return 权限 DTO
     */
    PermissionDTO updatePermissionStatus(UUID id, PermissionStatus status);
}
