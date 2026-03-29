package com.usermanagement.domain;

/**
 * 权限类型枚举
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public enum PermissionType {
    /**
     * 菜单权限 - 控制菜单可见性
     */
    MENU,

    /**
     * 操作权限 - 控制按钮/操作可见性
     */
    ACTION,

    /**
     * 字段权限 - 控制字段可见性
     */
    FIELD,

    /**
     * 数据权限 - 控制数据范围
     */
    DATA
}
