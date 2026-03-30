package com.usermanagement.web.dto;

import com.usermanagement.domain.PermissionType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新权限请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateRequest {

    /**
     * 权限名称
     */
    @Size(max = 100, message = "权限名称长度不能超过 100 个字符")
    private String name;

    /**
     * 权限代码
     */
    @Size(max = 100, message = "权限代码长度不能超过 100 个字符")
    private String code;

    /**
     * 权限类型
     */
    private PermissionType type;

    /**
     * 资源名称
     */
    @Size(max = 50, message = "资源名称长度不能超过 50 个字符")
    private String resource;

    /**
     * 操作
     */
    @Size(max = 50, message = "操作长度不能超过 50 个字符")
    private String action;

    /**
     * 父权限 ID
     */
    private String parentId;

    /**
     * 图标
     */
    @Size(max = 100, message = "图标长度不能超过 100 个字符")
    private String icon;

    /**
     * 前端路由
     */
    @Size(max = 200, message = "路由长度不能超过 200 个字符")
    private String route;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
