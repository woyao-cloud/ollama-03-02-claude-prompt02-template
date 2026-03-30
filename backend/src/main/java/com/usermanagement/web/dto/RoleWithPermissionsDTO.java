package com.usermanagement.web.dto;

import com.usermanagement.domain.DataScope;
import com.usermanagement.domain.RoleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色带权限 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleWithPermissionsDTO {

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
     * 权限 ID 列表
     */
    private List<String> permissionIds;
}
