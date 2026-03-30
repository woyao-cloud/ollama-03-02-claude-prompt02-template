package com.usermanagement.service;

import com.usermanagement.domain.Department;
import com.usermanagement.web.dto.DepartmentCreateRequest;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import com.usermanagement.web.dto.DepartmentUpdateRequest;

import java.util.UUID;

/**
 * 部门服务接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public interface DepartmentService {

    /**
     * 创建部门
     *
     * @param request 创建请求
     * @return 部门 DTO
     */
    DepartmentDTO createDepartment(DepartmentCreateRequest request);

    /**
     * 更新部门
     *
     * @param id      部门 ID
     * @param request 更新请求
     * @return 部门 DTO
     */
    DepartmentDTO updateDepartment(UUID id, DepartmentUpdateRequest request);

    /**
     * 根据 ID 获取部门
     *
     * @param id 部门 ID
     * @return 部门 DTO
     */
    DepartmentDTO getDepartmentById(UUID id);

    /**
     * 获取部门树
     *
     * @param level 层级筛选 (可选)
     * @return 部门树响应
     */
    DepartmentTreeResponse getDepartmentTree(Integer level);

    /**
     * 删除部门
     *
     * @param id 部门 ID
     */
    void deleteDepartment(UUID id);

    /**
     * 检查部门是否有子部门
     *
     * @param id 部门 ID
     * @return 是否有子部门
     */
    boolean hasChildren(UUID id);
}
