-- V2__audit_log_partitions.sql
-- 审计日志分区表
-- 创建日期：2026-03-29

-- ============================================================================
-- 审计日志主表（分区表模板）
-- ============================================================================

CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    username VARCHAR(100),
    operation VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID,
    old_value JSONB,
    new_value JSONB,
    description TEXT,
    client_ip VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(100),
    success BOOLEAN NOT NULL,
    error_message TEXT,
    execution_time_ms INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (created_at);

-- ============================================================================
-- 创建初始分区（2026 年 3 月 -12 月）
-- ============================================================================

CREATE TABLE audit_log_2026_03 PARTITION OF audit_log
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');

CREATE TABLE audit_log_2026_04 PARTITION OF audit_log
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');

CREATE TABLE audit_log_2026_05 PARTITION OF audit_log
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE audit_log_2026_06 PARTITION OF audit_log
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE TABLE audit_log_2026_07 PARTITION OF audit_log
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE audit_log_2026_08 PARTITION OF audit_log
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

CREATE TABLE audit_log_2026_09 PARTITION OF audit_log
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

CREATE TABLE audit_log_2026_10 PARTITION OF audit_log
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

CREATE TABLE audit_log_2026_11 PARTITION OF audit_log
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

CREATE TABLE audit_log_2026_12 PARTITION OF audit_log
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- ============================================================================
-- 2027 年分区（预创建）
-- ============================================================================

CREATE TABLE audit_log_2027_01 PARTITION OF audit_log
    FOR VALUES FROM ('2027-01-01') TO ('2027-02-01');

CREATE TABLE audit_log_2027_02 PARTITION OF audit_log
    FOR VALUES FROM ('2027-02-01') TO ('2027-03-01');

-- ============================================================================
-- 注释说明
-- ============================================================================

COMMENT ON TABLE audit_log IS '审计日志表 - 按时间分区';
COMMENT ON COLUMN audit_log.operation IS '操作类型：CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.';
COMMENT ON COLUMN audit_log.resource_type IS '资源类型：USER, ROLE, PERMISSION, DEPARTMENT, etc.';
COMMENT ON COLUMN audit_log.old_value IS '操作前的数据 (JSONB)';
COMMENT ON COLUMN audit_log.new_value IS '操作后的数据 (JSONB)';
COMMENT ON COLUMN audit_log.success IS '操作是否成功';
COMMENT ON COLUMN audit_log.execution_time_ms IS '执行耗时 (毫秒)';
