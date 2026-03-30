-- V6__performance_indexes.sql
-- B15 性能优化索引
-- 创建日期：2026-03-30

-- ============================================================================
-- 登录相关优化索引
-- ============================================================================

-- 用户登录查询优化 (email + status 复合索引)
CREATE INDEX IF NOT EXISTS idx_user_email_status ON "user"(email, status) WHERE deleted_at IS NULL;

-- 账户锁定查询优化
CREATE INDEX IF NOT EXISTS idx_user_locked ON "user"(locked_until) WHERE locked_until IS NOT NULL;

-- ============================================================================
-- 权限查询优化索引
-- ============================================================================

-- 用户权限查询优化 (通过 user_role + role_permission)
-- 注意：这些索引在 V3 中已创建，此处补充覆盖索引

-- 用户角色权限联合查询索引
CREATE INDEX IF NOT EXISTS idx_ur_user_active ON user_role(user_id)
    WHERE user_id IN (SELECT id FROM "user" WHERE status = 'ACTIVE' AND deleted_at IS NULL);

-- 角色权限状态索引
CREATE INDEX IF NOT EXISTS idx_role_status_active ON role(status) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_perm_status_active ON permission(status) WHERE status = 'ACTIVE';

-- ============================================================================
-- 配置查询优化索引
-- ============================================================================

-- 系统配置查询优化
CREATE INDEX IF NOT EXISTS idx_config_key ON system_config(config_key) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_config_category ON system_config(category, config_key) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_config_status ON system_config(status);

-- 配置历史查询优化
CREATE INDEX IF NOT EXISTS idx_config_history_config ON config_history(config_id, modified_at);
CREATE INDEX IF NOT EXISTS idx_config_history_time ON config_history(modified_at DESC);

-- ============================================================================
-- 审计日志查询优化索引 (覆盖常用查询场景)
-- ============================================================================

-- IP 地址查询索引
CREATE INDEX IF NOT EXISTS idx_audit_ip ON audit_log(ip_address, created_at);

-- 登录审计专用索引
CREATE INDEX IF NOT EXISTS idx_audit_login ON audit_log(operation, created_at)
    WHERE operation IN ('LOGIN', 'LOGOUT');

-- 失败操作查询索引
CREATE INDEX IF NOT EXISTS idx_audit_failed ON audit_log(success, created_at) WHERE success = FALSE;

-- ============================================================================
-- 统计信息更新
-- ============================================================================

ANALYZE "user";
ANALYZE role;
ANALYZE permission;
ANALYZE user_role;
ANALYZE role_permission;
ANALYZE system_config;
ANALYZE config_history;
ANALYZE audit_log;
