package com.usermanagement.service;

import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentMoveRequest;

import java.util.UUID;

/**
 * 部门移动服务接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public interface DepartmentMoveService {

    /**
     * 移动部门到新父部门
     *
     * @param id      部门 ID
     * @param request 移动请求
     * @return 更新后的部门 DTO
     */
    DepartmentDTO moveDepartment(UUID id, DepartmentMoveRequest request);
}
