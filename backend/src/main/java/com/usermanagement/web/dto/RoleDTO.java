package com.usermanagement.web.dto;

import com.usermanagement.domain.DataScope;
import com.usermanagement.domain.RoleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 角色响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    /**
     * 角色 ID
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色代码
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * 数据权限范围
     */
    private DataScope dataScope;

    /**
     * 角色状态
     */
    private RoleStatus status;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}
