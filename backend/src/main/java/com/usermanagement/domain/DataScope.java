package com.usermanagement.domain;

/**
 * 数据权限范围枚举
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public enum DataScope {
    /**
     * 全部数据权限
     */
    ALL,

    /**
     * 本部门及下级部门数据权限
     */
    DEPT,

    /**
     * 仅个人数据权限
     */
    SELF,

    /**
     * 自定义数据权限
     */
    CUSTOM
}
