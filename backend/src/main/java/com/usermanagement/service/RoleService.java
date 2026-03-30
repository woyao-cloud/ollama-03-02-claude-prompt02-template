package com.usermanagement.service;

import com.usermanagement.domain.RoleStatus;
import com.usermanagement.web.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * 角色服务接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色 DTO
     */
    RoleDTO createRole(RoleCreateRequest request);

    /**
     * 根据 ID 获取角色
     *
     * @param id 角色 ID
     * @return 角色 DTO
     */
    RoleDTO getRoleById(UUID id);

    /**
     * 获取所有角色列表
     *
     * @return 角色列表
     */
    List<RoleDTO> getAllRoles();

    /**
     * 根据状态获取角色列表
     *
     * @param status 角色状态
     * @return 角色列表
     */
    List<RoleDTO> getRolesByStatus(RoleStatus status);

    /**
     * 更新角色
     *
     * @param id      角色 ID
     * @param request 更新角色请求
     * @return 角色 DTO
     */
    RoleDTO updateRole(UUID id, RoleUpdateRequest request);

    /**
     * 删除角色
     *
     * @param id 角色 ID
     */
    void deleteRole(UUID id);

    /**
     * 更新角色状态
     *
     * @param id     角色 ID
     * @param status 角色状态
     * @return 角色 DTO
     */
    RoleDTO updateRoleStatus(UUID id, RoleStatus status);

    /**
     * 为角色分配权限
     *
     * @param roleId  角色 ID
     * @param request 分配权限请求
     * @return 带权限的角色 DTO
     */
    RoleWithPermissionsDTO assignPermissionsToRole(UUID roleId, AssignPermissionsRequest request);

    /**
     * 获取角色带权限的详情
     *
     * @param roleId 角色 ID
     * @return 带权限的角色 DTO
     */
    RoleWithPermissionsDTO getRoleWithPermissions(UUID roleId);

    /**
     * 检查角色代码是否存在
     *
     * @param code 角色代码
     * @return 是否存在
     */
    boolean existsByCode(String code);
}
