package com.usermanagement.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建部门请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCreateRequest {

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过 100")
    private String name;

    /**
     * 部门代码
     */
    @NotBlank(message = "部门代码不能为空")
    @Size(max = 50, message = "部门代码长度不能超过 50")
    private String code;

    /**
     * 父部门 ID (可选，为空表示根部门)
     */
    private String parentId;

    /**
     * 部门负责人 ID (可选)
     */
    private String managerId;

    /**
     * 部门层级 (1-5, 必填)
     */
    @NotNull(message = "部门层级不能为空")
    private Integer level;

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
