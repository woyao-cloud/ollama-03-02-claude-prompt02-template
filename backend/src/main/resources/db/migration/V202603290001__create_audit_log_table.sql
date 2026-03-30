-- 创建审计日志表
CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    user_email VARCHAR(255),
    operation_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID,
    operation_description VARCHAR(500),
    old_value JSONB,
    new_value JSONB,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500),
    operation_result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_audit_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_operation_type ON audit_log(operation_type);
CREATE INDEX idx_audit_resource_type ON audit_log(resource_type);
CREATE INDEX idx_audit_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_user_email ON audit_log(user_email);
CREATE INDEX idx_audit_operation_result ON audit_log(operation_result);

-- 添加注释
COMMENT ON TABLE audit_log IS '审计日志表 - 记录系统操作日志';
COMMENT ON COLUMN audit_log.id IS '审计日志 ID';
COMMENT ON COLUMN audit_log.user_id IS '操作用户 ID';
COMMENT ON COLUMN audit_log.user_email IS '操作用户邮箱';
COMMENT ON COLUMN audit_log.operation_type IS '操作类型 (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.)';
COMMENT ON COLUMN audit_log.resource_type IS '资源类型 (USER, ROLE, PERMISSION, DEPARTMENT)';
COMMENT ON COLUMN audit_log.resource_id IS '资源 ID';
COMMENT ON COLUMN audit_log.operation_description IS '操作描述';
COMMENT ON COLUMN audit_log.old_value IS '操作前的数据 (JSON)';
COMMENT ON COLUMN audit_log.new_value IS '操作后的数据 (JSON)';
COMMENT ON COLUMN audit_log.client_ip IS '客户端 IP 地址';
COMMENT ON COLUMN audit_log.user_agent IS '客户端 User-Agent';
COMMENT ON COLUMN audit_log.operation_result IS '操作结果 (SUCCESS/FAILURE)';
COMMENT ON COLUMN audit_log.error_message IS '错误信息';
COMMENT ON COLUMN audit_log.created_at IS '创建时间';
