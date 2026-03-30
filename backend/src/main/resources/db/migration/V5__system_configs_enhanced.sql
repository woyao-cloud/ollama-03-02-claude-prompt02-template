-- V5__system_configs_enhanced.sql
-- 增强系统配置表结构，添加更多字段支持
-- 创建日期：2026-03-30

-- ============================================================================
-- 修改系统配置表 - 添加新字段
-- ============================================================================

-- 添加数据类型字段
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS data_type VARCHAR(20) DEFAULT 'STRING';

-- 添加是否敏感字段
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS is_sensitive BOOLEAN DEFAULT FALSE;

-- 添加默认值字段
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS default_value TEXT;

-- 添加最小值字段（用于数值类型验证）
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS min_value VARCHAR(50);

-- 添加最大值字段（用于数值类型验证）
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS max_value VARCHAR(50);

-- 添加正则表达式字段（用于字符串格式验证）
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS regex_pattern VARCHAR(200);

-- 添加可选值列表字段（JSON 格式）
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS options JSONB;

-- 添加 version 字段（如果不存在）
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS version INT DEFAULT 1;

-- 添加 updated_by 字段
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS updated_by UUID;

-- 添加 deleted_at 字段（软删除支持）
ALTER TABLE system_config ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- 更新现有数据的 data_type
UPDATE system_config SET data_type = config_type::VARCHAR(20) WHERE data_type IS NULL OR data_type = 'STRING';

-- 更新现有数据的 version
UPDATE system_config SET version = 1 WHERE version IS NULL OR version = 0;

-- 添加配置状态枚举类型（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'config_status') THEN
        CREATE TYPE config_status AS ENUM ('ACTIVE', 'INACTIVE');
    END IF;
END $$;

-- 添加配置类型枚举类型（扩展）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'config_type_enum') THEN
        CREATE TYPE config_type_enum AS ENUM ('EMAIL', 'SECURITY', 'PERFORMANCE', 'SYSTEM', 'FEATURE', 'AUTH', 'USER');
    END IF;
END $$;

-- ============================================================================
-- 修改配置历史表 - 增强字段
-- ============================================================================

-- 添加 config_key 字段（便于查询）
ALTER TABLE config_history ADD COLUMN IF NOT EXISTS config_key VARCHAR(100);

-- 添加 changed_by_email 字段
ALTER TABLE config_history ADD COLUMN IF NOT EXISTS changed_by_email VARCHAR(255);

-- 添加 reason 字段（同 change_reason 兼容）
ALTER TABLE config_history ADD COLUMN IF NOT EXISTS reason VARCHAR(500);

-- 添加 deleted_at 字段
ALTER TABLE config_history ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- 迁移现有数据
UPDATE config_history SET config_key = (SELECT config_key FROM system_config WHERE id = config_history.config_id LIMIT 1)
WHERE config_key IS NULL;

UPDATE config_history SET reason = change_reason WHERE reason IS NULL AND change_reason IS NOT NULL;

-- ============================================================================
-- 更新初始配置数据 - 添加更多配置项
-- ============================================================================

-- 邮件配置
INSERT INTO system_config (config_key, config_value, config_type, category, data_type, description, is_sensitive) VALUES
    ('mail.smtp.host', 'smtp.example.com', 'EMAIL', 'EMAIL', 'STRING', 'SMTP 服务器主机', FALSE),
    ('mail.smtp.port', '587', 'EMAIL', 'EMAIL', 'NUMBER', 'SMTP 服务器端口', FALSE),
    ('mail.smtp.username', 'noreply@example.com', 'EMAIL', 'EMAIL', 'STRING', 'SMTP 用户名', FALSE),
    ('mail.smtp.password', '', 'EMAIL', 'EMAIL', 'STRING', 'SMTP 密码', TRUE),
    ('mail.smtp.protocol', 'smtp', 'EMAIL', 'EMAIL', 'STRING', 'SMTP 协议', FALSE),
    ('mail.from.address', 'noreply@example.com', 'EMAIL', 'EMAIL', 'STRING', '发件人地址', FALSE),
    ('mail.from.name', '用户管理系统', 'EMAIL', 'EMAIL', 'STRING', '发件人名称', FALSE),
    ('mail.transport.protocol', 'smtp', 'EMAIL', 'EMAIL', 'STRING', '邮件传输协议', FALSE),
    ('mail.smtp.auth', 'true', 'EMAIL', 'EMAIL', 'BOOLEAN', 'SMTP 认证开关', FALSE),
    ('mail.smtp.starttls.enable', 'true', 'EMAIL', 'EMAIL', 'BOOLEAN', 'STARTTLS 加密', FALSE),
    ('mail.smtp.connectiontimeout', '5000', 'EMAIL', 'EMAIL', 'NUMBER', '连接超时时间（毫秒）', FALSE),
    ('mail.smtp.timeout', '10000', 'EMAIL', 'EMAIL', 'NUMBER', '发送超时时间（毫秒）', FALSE);

-- 安全策略配置
INSERT INTO system_config (config_key, config_value, config_type, category, data_type, description, min_value, max_value) VALUES
    ('security.password.min_length', '8', 'SECURITY', 'SECURITY', 'NUMBER', '密码最小长度', '6', '128'),
    ('security.password.max_length', '128', 'SECURITY', 'SECURITY', 'NUMBER', '密码最大长度', '8', '256'),
    ('security.password.require_uppercase', 'true', 'SECURITY', 'SECURITY', 'BOOLEAN', '密码需要大写字母', NULL, NULL),
    ('security.password.require_lowercase', 'true', 'SECURITY', 'SECURITY', 'BOOLEAN', '密码需要小写字母', NULL, NULL),
    ('security.password.require_digit', 'true', 'SECURITY', 'SECURITY', 'BOOLEAN', '密码需要数字', NULL, NULL),
    ('security.password.require_special', 'false', 'SECURITY', 'SECURITY', 'BOOLEAN', '密码需要特殊字符', NULL, NULL),
    ('security.password.expiration_days', '90', 'SECURITY', 'SECURITY', 'NUMBER', '密码过期天数', '1', '365'),
    ('security.password.history_count', '5', 'SECURITY', 'SECURITY', 'NUMBER', '历史密码检查数量', '1', '20'),
    ('security.login.max_attempts', '5', 'SECURITY', 'SECURITY', 'NUMBER', '最大登录尝试次数', '1', '20'),
    ('security.login.lockout_duration', '900', 'SECURITY', 'SECURITY', 'NUMBER', '账户锁定持续时间（秒）', '60', '3600'),
    ('security.session.timeout', '1800', 'SECURITY', 'SECURITY', 'NUMBER', '会话超时时间（秒）', '300', '28800'),
    ('security.session.concurrent_limit', '3', 'SECURITY', 'SECURITY', 'NUMBER', '并发会话限制', '1', '10'),
    ('security.captcha.enabled', 'false', 'SECURITY', 'SECURITY', 'BOOLEAN', '验证码开关', NULL, NULL),
    ('security.captcha.threshold', '3', 'SECURITY', 'SECURITY', 'NUMBER', '验证码触发阈值（失败次数）', '1', '10');

-- 性能配置
INSERT INTO system_config (config_key, config_value, config_type, category, data_type, description) VALUES
    ('performance.cache.user.ttl', '300', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '用户缓存 TTL（秒）'),
    ('performance.cache.department.ttl', '600', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '部门缓存 TTL（秒）'),
    ('performance.cache.role.ttl', '300', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '角色缓存 TTL（秒）'),
    ('performance.cache.permission.ttl', '300', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '权限缓存 TTL（秒）'),
    ('performance.cache.config.ttl', '300', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '配置缓存 TTL（秒）'),
    ('performance.api.rate_limit', '100', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', 'API 速率限制（次/分钟）'),
    ('performance.api.max_page_size', '100', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '分页最大页面大小'),
    ('performance.db.pool.size', '10', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '数据库连接池大小'),
    ('performance.db.pool.max_size', '20', 'PERFORMANCE', 'PERFORMANCE', 'NUMBER', '数据库连接池最大大小');

-- 系统参数配置
INSERT INTO system_config (config_key, config_value, config_type, category, data_type, description) VALUES
    ('system.name', '用户管理系统', 'SYSTEM', 'SYSTEM', 'STRING', '系统名称'),
    ('system.version', '1.0.0', 'SYSTEM', 'SYSTEM', 'STRING', '系统版本号'),
    ('system.timezone', 'Asia/Shanghai', 'SYSTEM', 'SYSTEM', 'STRING', '系统时区'),
    ('system.locale', 'zh-CN', 'SYSTEM', 'SYSTEM', 'STRING', '系统默认语言'),
    ('system.logo_url', '/assets/logo.png', 'SYSTEM', 'SYSTEM', 'STRING', '系统 Logo URL'),
    ('system.footer_text', '版权所有', 'SYSTEM', 'SYSTEM', 'STRING', '页脚文本'),
    ('system.registration.enabled', 'true', 'SYSTEM', 'SYSTEM', 'BOOLEAN', '用户注册开关'),
    ('system.maintenance.mode', 'false', 'SYSTEM', 'SYSTEM', 'BOOLEAN', '维护模式开关');

-- 用户相关配置
INSERT INTO system_config (config_key, config_value, config_type, category, data_type, description) VALUES
    ('user.registration.enabled', 'true', 'USER', 'USER', 'BOOLEAN', '是否启用用户自助注册'),
    ('user.registration.approval_required', 'false', 'USER', 'USER', 'BOOLEAN', '用户注册是否需要审批'),
    ('user.registration.email_verification', 'true', 'USER', 'USER', 'BOOLEAN', '用户注册是否需要邮箱验证'),
    ('user.password.change_interval', '90', 'USER', 'USER', 'NUMBER', '密码更换周期（天）'),
    ('user.profile.avatar.max_size', '5242880', 'USER', 'USER', 'NUMBER', '头像最大大小（字节）-5MB'),
    ('user.profile.avatar.allowed_types', '["image/jpeg","image/png","image/gif"]', 'USER', 'USER', 'ARRAY', '头像允许的文件类型');

-- 功能开关配置
INSERT INTO system_config (config_key, config_value, config_type, category, data_type, description) VALUES
    ('feature.audit_log.enabled', 'true', 'FEATURE', 'FEATURE', 'BOOLEAN', '审计日志功能开关'),
    ('feature.email_notification.enabled', 'true', 'FEATURE', 'FEATURE', 'BOOLEAN', '邮件通知功能开关'),
    ('feature.two_factor_auth.enabled', 'false', 'FEATURE', 'FEATURE', 'BOOLEAN', '双因素认证开关'),
    ('feature.sso.enabled', 'false', 'FEATURE', 'FEATURE', 'BOOLEAN', '单点登录开关'),
    ('feature.data_export.enabled', 'true', 'FEATURE', 'FEATURE', 'BOOLEAN', '数据导出功能开关');

-- ============================================================================
-- 添加注释
-- ============================================================================

COMMENT ON COLUMN system_config.data_type IS '配置数据类型：STRING, NUMBER, BOOLEAN, JSON, ARRAY';
COMMENT ON COLUMN system_config.is_sensitive IS '是否敏感配置（敏感配置在日志中脱敏）';
COMMENT ON COLUMN system_config.default_value IS '默认值';
COMMENT ON COLUMN system_config.min_value IS '最小值（用于数值类型验证）';
COMMENT ON COLUMN system_config.max_value IS '最大值（用于数值类型验证）';
COMMENT ON COLUMN system_config.regex_pattern IS '正则表达式（用于字符串格式验证）';
COMMENT ON COLUMN system_config.options IS '可选值列表（JSON 格式）';
COMMENT ON COLUMN config_history.config_key IS '配置键快照（便于查询）';
COMMENT ON COLUMN config_history.changed_by_email IS '变更人邮箱';
COMMENT ON COLUMN config_history.reason IS '变更原因';
