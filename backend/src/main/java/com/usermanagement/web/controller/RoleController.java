package com.usermanagement.web.controller;

import com.usermanagement.domain.RoleStatus;
import com.usermanagement.service.RoleService;
import com.usermanagement.web.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 角色管理 REST API 控制器
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * 根据 ID 获取角色
     *
     * @param id 角色 ID
     * @return 角色 DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable UUID id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色 DTO
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleDTO role = roleService.createRole(request);
        logger.info("角色创建成功：{}", role.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    /**
     * 更新角色
     *
     * @param id      角色 ID
     * @param request 更新角色请求
     * @return 角色 DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable UUID id, @Valid @RequestBody RoleUpdateRequest request) {
        RoleDTO role = roleService.updateRole(id, request);
        logger.info("角色更新成功：{}", role.getName());
        return ResponseEntity.ok(role);
    }

    /**
     * 删除角色
     *
     * @param id 角色 ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新角色状态
     *
     * @param id      角色 ID
     * @param request 更新状态请求
     * @return 角色 DTO
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<RoleDTO> updateRoleStatus(@PathVariable UUID id, @Valid @RequestBody RoleStatusUpdateRequest request) {
        RoleDTO role = roleService.updateRoleStatus(id, request.getStatus());
        logger.info("角色状态更新成功：{} -> {}", role.getName(), request.getStatus());
        return ResponseEntity.ok(role);
    }

    /**
     * 为角色分配权限
     *
     * @param id      角色 ID
     * @param request 分配权限请求
     * @return 带权限的角色 DTO
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<RoleWithPermissionsDTO> assignPermissions(@PathVariable UUID id, @Valid @RequestBody AssignPermissionsRequest request) {
        RoleWithPermissionsDTO role = roleService.assignPermissionsToRole(id, request);
        logger.info("角色权限分配成功：{}", role.getName());
        return ResponseEntity.ok(role);
    }

    /**
     * 获取角色带权限的详情
     *
     * @param id 角色 ID
     * @return 带权限的角色 DTO
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<RoleWithPermissionsDTO> getRoleWithPermissions(@PathVariable UUID id) {
        RoleWithPermissionsDTO role = roleService.getRoleWithPermissions(id);
        return ResponseEntity.ok(role);
    }
}
