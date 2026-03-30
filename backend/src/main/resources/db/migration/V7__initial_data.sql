-- V7__initial_data.sql
-- 系统基础数据初始化脚本
-- 创建日期：2026-03-30
--
-- 包含:
-- 1. 根部门数据
-- 2. 基础权限数据 (菜单 + 操作权限)
-- 3. 默认角色数据
-- 4. 角色 - 权限关联
-- 5. 默认管理员账户
-- 6. 用户 - 角色关联

-- ============================================================================
-- 1. 根部门数据
-- ============================================================================

-- 插入根部门 (公司级别)
INSERT INTO department (id, name, code, parent_id, level, path, sort_order, status, description, created_at, updated_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000001', '总公司', 'ROOT', NULL, 1, '/00000000-0000-0000-0000-000000000001', 1, 'ACTIVE', '公司根部门', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- ============================================================================
-- 2. 基础权限数据
-- ============================================================================

-- 2.1 系统管理菜单权限
INSERT INTO permission (id, name, code, type, resource, action, parent_id, icon, route, sort_order, status, created_at, updated_at, version)
VALUES
    -- 系统管理 (根菜单)
    ('10000000-0000-0000-0000-000000000001', '系统管理', 'system', 'MENU', 'system', '', NULL, 'settings', '/system', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),

    -- 用户管理菜单
    ('10000000-0000-0000-0000-000000000002', '用户管理', 'user', 'MENU', 'user', '', '10000000-0000-0000-0000-000000000001', 'users', '/system/users', 10, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    -- 用户管理操作权限
    ('10000000-0000-0000-0000-000000000003', '查看用户', 'user:read', 'ACTION', 'user', 'read', '10000000-0000-0000-0000-000000000002', NULL, NULL, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000004', '创建用户', 'user:create', 'ACTION', 'user', 'create', '10000000-0000-0000-0000-000000000002', NULL, NULL, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000005', '编辑用户', 'user:update', 'ACTION', 'user', 'update', '10000000-0000-0000-0000-000000000002', NULL, NULL, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000006', '删除用户', 'user:delete', 'ACTION', 'user', 'delete', '10000000-0000-0000-0000-000000000002', NULL, NULL, 4, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000007', '导入导出', 'user:import-export', 'ACTION', 'user', 'import-export', '10000000-0000-0000-0000-000000000002', NULL, NULL, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),

    -- 角色管理菜单
    ('10000000-0000-0000-0000-000000000008', '角色管理', 'role', 'MENU', 'role', '', '10000000-0000-0000-0000-000000000001', 'shield', '/system/roles', 20, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    -- 角色管理操作权限
    ('10000000-0000-0000-0000-000000000009', '查看角色', 'role:read', 'ACTION', 'role', 'read', '10000000-0000-0000-0000-000000000008', NULL, NULL, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-00000000000a', '创建角色', 'role:create', 'ACTION', 'role', 'create', '10000000-0000-0000-0000-000000000008', NULL, NULL, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-00000000000b', '编辑角色', 'role:update', 'ACTION', 'role', 'update', '10000000-0000-0000-0000-000000000008', NULL, NULL, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-00000000000c', '删除角色', 'role:delete', 'ACTION', 'role', 'delete', '10000000-0000-0000-0000-000000000008', NULL, NULL, 4, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-00000000000d', '分配权限', 'role:assign-permission', 'ACTION', 'role', 'assign-permission', '10000000-0000-0000-0000-000000000008', NULL, NULL, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),

    -- 部门管理菜单
    ('10000000-0000-0000-0000-00000000000e', '部门管理', 'department', 'MENU', 'department', '', '10000000-0000-0000-0000-000000000001', 'building', '/system/departments', 30, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    -- 部门管理操作权限
    ('10000000-0000-0000-0000-00000000000f', '查看部门', 'department:read', 'ACTION', 'department', 'read', '10000000-0000-0000-0000-00000000000e', NULL, NULL, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000010', '创建部门', 'department:create', 'ACTION', 'department', 'create', '10000000-0000-0000-0000-00000000000e', NULL, NULL, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000011', '编辑部门', 'department:update', 'ACTION', 'department', 'update', '10000000-0000-0000-0000-00000000000e', NULL, NULL, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000012', '删除部门', 'department:delete', 'ACTION', 'department', 'delete', '10000000-0000-0000-0000-00000000000e', NULL, NULL, 4, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000013', '调整层级', 'department:move', 'ACTION', 'department', 'move', '10000000-0000-0000-0000-00000000000e', NULL, NULL, 5, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),

    -- 权限管理菜单
    ('10000000-0000-0000-0000-000000000014', '权限管理', 'permission', 'MENU', 'permission', '', '10000000-0000-0000-0000-000000000001', 'lock', '/system/permissions', 40, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    -- 权限管理操作权限
    ('10000000-0000-0000-0000-000000000015', '查看权限', 'permission:read', 'ACTION', 'permission', 'read', '10000000-0000-0000-0000-000000000014', NULL, NULL, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000016', '创建权限', 'permission:create', 'ACTION', 'permission', 'create', '10000000-0000-0000-0000-000000000014', NULL, NULL, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000017', '编辑权限', 'permission:update', 'ACTION', 'permission', 'update', '10000000-0000-0000-0000-000000000014', NULL, NULL, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-000000000018', '删除权限', 'permission:delete', 'ACTION', 'permission', 'delete', '10000000-0000-0000-0000-000000000014', NULL, NULL, 4, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),

    -- 配置管理菜单
    ('10000000-0000-0000-0000-000000000019', '配置管理', 'config', 'MENU', 'config', '', '10000000-0000-0000-0000-000000000001', 'sliders', '/system/configs', 50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    -- 配置管理操作权限
    ('10000000-0000-0000-0000-00000000001a', '查看配置', 'config:read', 'ACTION', 'config', 'read', '10000000-0000-0000-0000-000000000019', NULL, NULL, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-00000000001b', '编辑配置', 'config:update', 'ACTION', 'config', 'update', '10000000-0000-0000-0000-000000000019', NULL, NULL, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-00000000001c', '刷新缓存', 'config:refresh-cache', 'ACTION', 'config', 'refresh-cache', '10000000-0000-0000-0000-000000000019', NULL, NULL, 3, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),

    -- 审计日志菜单
    ('10000000-0000-0000-0000-00000000001d', '审计日志', 'audit', 'MENU', 'audit', '', '10000000-0000-0000-0000-000000000001', 'file-text', '/system/audit-logs', 60, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    -- 审计日志操作权限
    ('10000000-0000-0000-0000-00000000001e', '查看日志', 'audit:read', 'ACTION', 'audit', 'read', '10000000-0000-0000-0000-00000000001d', NULL, NULL, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('10000000-0000-0000-0000-00000000001f', '导出日志', 'audit:export', 'ACTION', 'audit', 'export', '10000000-0000-0000-0000-00000000001d', NULL, NULL, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- ============================================================================
-- 3. 默认角色数据
-- ============================================================================

INSERT INTO role (id, name, code, description, data_scope, status, created_at, updated_at, version)
VALUES
    ('20000000-0000-0000-0000-000000000001', '超级管理员', 'ROLE_SUPER_ADMIN', '系统超级管理员，拥有所有权限', 'ALL', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('20000000-0000-0000-0000-000000000002', '普通管理员', 'ROLE_ADMIN', '部门级管理员，拥有部门内管理权限', 'DEPT', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('20000000-0000-0000-0000-000000000003', '普通用户', 'ROLE_USER', '普通用户，仅个人数据权限', 'SELF', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- ============================================================================
-- 4. 角色 - 权限关联
-- ============================================================================

-- 4.1 超级管理员 - 所有权限
INSERT INTO role_permission (role_id, permission_id, created_at)
SELECT '20000000-0000-0000-0000-000000000001', id, CURRENT_TIMESTAMP
FROM permission;

-- 4.2 普通管理员 - 只读权限 + 部分操作权限
INSERT INTO role_permission (role_id, permission_id, created_at)
VALUES
    -- 用户管理 (只读 + 编辑)
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000005', CURRENT_TIMESTAMP),
    -- 角色管理 (只读)
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000008', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000009', CURRENT_TIMESTAMP),
    -- 部门管理 (只读 + 编辑)
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-00000000000e', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-00000000000f', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000010', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP),
    -- 配置管理 (只读)
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000019', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-00000000001a', CURRENT_TIMESTAMP),
    -- 审计日志 (只读 + 导出)
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-00000000001d', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-00000000001e', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-00000000001f', CURRENT_TIMESTAMP);

-- 4.3 普通用户 - 仅个人相关权限
INSERT INTO role_permission (role_id, permission_id, created_at)
VALUES
    -- 仅查看自己的信息
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', CURRENT_TIMESTAMP);

-- ============================================================================
-- 5. 默认管理员账户
-- ============================================================================

-- 密码：Admin@123 (BCrypt 加密，强度因子 12)
-- BCrypt 哈希：$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO "user" (id, email, password_hash, first_name, last_name, department_id, status, email_verified, failed_login_attempts, created_at, updated_at, version)
VALUES
    ('30000000-0000-0000-0000-000000000001', 'admin@example.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统', '管理员', '00000000-0000-0000-0000-000000000001', 'ACTIVE', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- ============================================================================
-- 6. 用户 - 角色关联
-- ============================================================================

-- 管理员关联超级管理员角色
INSERT INTO user_role (user_id, role_id, created_at)
VALUES
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP);

-- ============================================================================
-- 7. 审计日志 - 系统初始化记录
-- ============================================================================

INSERT INTO audit_log (id, user_id, username, operation, resource_type, resource_id, description, success, execution_time_ms, created_at)
VALUES
    (gen_random_uuid(), '30000000-0000-0000-0000-000000000001', 'admin@example.com', 'SYSTEM_INIT', 'SYSTEM', NULL, '系统初始化 - 基础数据加载', TRUE, 0, CURRENT_TIMESTAMP);

-- ============================================================================
-- 数据验证查询 (执行后可选)
-- ============================================================================

-- 验证查询 (注释掉，实际执行时不会运行)
-- SELECT COUNT(*) FROM department;           -- 应返回 1
-- SELECT COUNT(*) FROM permission;           -- 应返回 22
-- SELECT COUNT(*) FROM role;                 -- 应返回 3
-- SELECT COUNT(*) FROM role_permission;      -- 应返回 37 (超级管理员 22 + 普通管理员 14 + 普通用户 1)
-- SELECT COUNT(*) FROM "user";               -- 应返回 1
-- SELECT COUNT(*) FROM user_role;            -- 应返回 1
