package com.usermanagement.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新部门请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUpdateRequest {

    /**
     * 部门名称 (可选)
     */
    @Size(max = 100, message = "部门名称长度不能超过 100")
    private String name;

    /**
     * 部门负责人 ID (可选)
     */
    private String managerId;

    /**
     * 部门状态 (可选)
     */
    private String status;

    /**
     * 排序号 (可选)
     */
    private Integer sortOrder;

    /**
     * 描述 (可选)
     */
    @Size(max = 1000, message = "描述长度不能超过 1000")
    private String description;
}
