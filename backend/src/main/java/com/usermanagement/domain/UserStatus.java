package com.usermanagement.domain;

/**
 * 用户状态枚举
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public enum UserStatus {
    /**
     * 激活 - 可以正常登录
     */
    ACTIVE,

    /**
     * 禁用 - 管理员禁用
     */
    INACTIVE,

    /**
     * 待激活 - 注册后待邮箱验证或审批
     */
    PENDING,

    /**
     * 锁定 - 登录失败次数过多
     */
    LOCKED
}
