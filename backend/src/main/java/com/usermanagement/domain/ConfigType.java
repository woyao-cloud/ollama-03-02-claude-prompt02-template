package com.usermanagement.domain;

/**
 * 配置类型枚举
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public enum ConfigType {
    /**
     * 邮件配置
     */
    EMAIL,

    /**
     * 安全策略配置
     */
    SECURITY,

    /**
     * 性能配置
     */
    PERFORMANCE,

    /**
     * 系统参数配置
     */
    SYSTEM,

    /**
     * 功能开关配置
     */
    FEATURE,

    /**
     * 认证配置
     */
    AUTH,

    /**
     * 用户相关配置
     */
    USER
}
