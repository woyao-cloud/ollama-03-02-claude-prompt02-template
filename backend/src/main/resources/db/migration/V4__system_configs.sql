-- V4__system_configs.sql
-- 系统配置表和配置历史表
-- 创建日期：2026-03-29

-- ============================================================================
-- 系统配置表
-- ============================================================================

CREATE TABLE system_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    config_type VARCHAR(20) NOT NULL DEFAULT 'STRING' CHECK (config_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    description TEXT,
    encrypted BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

-- ============================================================================
-- 配置历史表（记录配置变更）
-- ============================================================================

CREATE TABLE config_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_id UUID NOT NULL,
    old_value TEXT,
    new_value TEXT NOT NULL,
    modified_by UUID,
    modified_by_username VARCHAR(100),
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT
);

-- ============================================================================
-- 索引
-- ============================================================================

-- 系统配置表索引
CREATE INDEX idx_config_key ON system_config(config_key);
CREATE INDEX idx_config_category ON system_config(category);
CREATE INDEX idx_config_status ON system_config(status);
CREATE INDEX idx_config_type ON system_config(config_type);

-- 配置历史表索引
CREATE INDEX idx_config_hist_config ON config_history(config_id);
CREATE INDEX idx_config_hist_time ON config_history(modified_at);
CREATE INDEX idx_config_hist_user ON config_history(modified_by);

-- ============================================================================
-- 初始配置数据
-- ============================================================================

-- 认证相关配置
INSERT INTO system_config (config_key, config_value, config_type, category, description) VALUES
    ('auth.jwt.expiration', '86400', 'NUMBER', 'AUTH', 'JWT Token 有效期（秒）- 默认 24 小时'),
    ('auth.jwt.refresh_expiration', '604800', 'NUMBER', 'AUTH', 'JWT Refresh Token 有效期（秒）- 默认 7 天'),
    ('auth.password.min_length', '8', 'NUMBER', 'AUTH', '密码最小长度'),
    ('auth.password.require_uppercase', 'true', 'BOOLEAN', 'AUTH', '密码需要大写字母'),
    ('auth.password.require_lowercase', 'true', 'BOOLEAN', 'AUTH', '密码需要小写字母'),
    ('auth.password.require_digit', 'true', 'BOOLEAN', 'AUTH', '密码需要数字'),
    ('auth.password.require_special', 'false', 'BOOLEAN', 'AUTH', '密码需要特殊字符'),
    ('auth.account.lockout.threshold', '5', 'NUMBER', 'AUTH', '账户锁定阈值（连续失败次数）'),
    ('auth.account.lockout.duration', '900', 'NUMBER', 'AUTH', '账户锁定持续时间（秒）- 默认 15 分钟'),
    ('auth.session.timeout', '1800', 'NUMBER', 'AUTH', '会话超时时间（秒）- 默认 30 分钟');

-- 系统相关配置
INSERT INTO system_config (config_key, config_value, config_type, category, description) VALUES
    ('system.name', '用户管理系统', 'STRING', 'SYSTEM', '系统名称'),
    ('system.version', '1.0.0', 'STRING', 'SYSTEM', '系统版本号'),
    ('system.timezone', 'Asia/Shanghai', 'STRING', 'SYSTEM', '系统时区'),
    ('system.locale', 'zh-CN', 'STRING', 'SYSTEM', '系统默认语言');

-- 用户相关配置
INSERT INTO system_config (config_key, config_value, config_type, category, description) VALUES
    ('user.registration.enabled', 'true', 'BOOLEAN', 'USER', '是否启用用户自助注册'),
    ('user.registration.approval_required', 'false', 'BOOLEAN', 'USER', '用户注册是否需要审批'),
    ('user.registration.email_verification', 'true', 'BOOLEAN', 'USER', '用户注册是否需要邮箱验证'),
    ('user.password.change_interval', '90', 'NUMBER', 'USER', '密码更换周期（天）');

-- ============================================================================
-- 注释说明
-- ============================================================================

COMMENT ON TABLE system_config IS '系统配置表 - 支持动态配置';
COMMENT ON TABLE config_history IS '配置历史表 - 记录配置变更';
COMMENT ON COLUMN system_config.config_type IS '配置值类型：STRING, NUMBER, BOOLEAN, JSON';
COMMENT ON COLUMN system_config.category IS '配置分类：AUTH, SYSTEM, USER, EMAIL, etc.';
COMMENT ON COLUMN system_config.encrypted IS '是否加密存储';
COMMENT ON COLUMN config_history.change_reason IS '变更原因';
