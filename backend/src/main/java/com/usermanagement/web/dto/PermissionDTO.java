package com.usermanagement.web.dto;

import com.usermanagement.domain.PermissionStatus;
import com.usermanagement.domain.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 权限响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    /**
     * 权限 ID
     */
    private String id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限代码
     */
    private String code;

    /**
     * 权限类型
     */
    private PermissionType type;

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 操作
     */
    private String action;

    /**
     * 父权限 ID
     */
    private String parentId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 前端路由
     */
    private String route;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 权限状态
     */
    private PermissionStatus status;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}
