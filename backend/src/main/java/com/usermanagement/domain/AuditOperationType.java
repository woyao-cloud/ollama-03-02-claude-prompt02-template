package com.usermanagement.domain;

/**
 * 审计操作类型
 */
public enum AuditOperationType {
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    PASSWORD_RESET,
    PERMISSION_CHANGE,
    STATUS_CHANGE
}
