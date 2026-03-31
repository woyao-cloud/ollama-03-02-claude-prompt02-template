-- V8__role_data_scope_custom.sql
-- 角色自定义数据权限范围表
-- 创建日期：2026-03-30

-- ============================================================================
-- 角色自定义数据范围表 - 存储角色 CUSTOM 范围的具体权限
-- ============================================================================

CREATE TABLE role_data_scope (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL,
    scope_type VARCHAR(20) NOT NULL CHECK (scope_type IN ('DEPT', 'USER', 'SQL')),
    scope_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

-- 外键约束
ALTER TABLE role_data_scope ADD CONSTRAINT fk_rds_role
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;

-- 索引
CREATE INDEX idx_rds_role_id ON role_data_scope(role_id);
CREATE INDEX idx_rds_scope_type ON role_data_scope(scope_type);

-- 注释
COMMENT ON TABLE role_data_scope IS '角色自定义数据范围配置表';
COMMENT ON COLUMN role_data_scope.scope_type IS '范围类型：DEPT=指定部门，USER=指定用户，SQL=自定义 SQL 条件';
COMMENT ON COLUMN role_data_scope.scope_value IS '范围值：部门 ID 列表/用户 ID 列表/SQL 条件';
