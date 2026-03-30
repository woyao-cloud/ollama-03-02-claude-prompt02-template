-- V3__indexes_and_constraints.sql
-- 性能优化索引和附加约束
-- 创建日期：2026-03-29

-- ============================================================================
-- 部门表索引
-- ============================================================================

-- 部门树查询核心索引
CREATE INDEX idx_dept_path ON department(path);
CREATE INDEX idx_dept_parent ON department(parent_id);
CREATE INDEX idx_dept_code ON department(code);
CREATE INDEX idx_dept_level ON department(level);

-- 软删除过滤索引
CREATE INDEX idx_dept_deleted ON department(deleted_at) WHERE deleted_at IS NULL;

-- 复合索引：部门树查询优化
CREATE INDEX idx_dept_parent_level ON department(parent_id, level);

-- ============================================================================
-- 用户表索引
-- ============================================================================

-- 基础查询索引
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_department ON "user"(department_id);
CREATE INDEX idx_user_status ON "user"(status);
CREATE INDEX idx_user_created ON "user"(created_at);

-- 软删除过滤索引
CREATE INDEX idx_user_deleted ON "user"(deleted_at) WHERE deleted_at IS NULL;

-- 复合索引：部门用户查询
CREATE INDEX idx_user_dept_status ON "user"(department_id, status);

-- 复合索引：用户状态 + 创建时间（分页查询）
CREATE INDEX idx_user_status_created ON "user"(status, created_at);

-- 登录相关索引
CREATE INDEX idx_user_locked_until ON "user"(locked_until);

-- ============================================================================
-- 角色表索引
-- ============================================================================

CREATE INDEX idx_role_code ON role(code);
CREATE INDEX idx_role_status ON role(status);
CREATE INDEX idx_role_data_scope ON role(data_scope);

-- 软删除过滤索引
CREATE INDEX idx_role_deleted ON role(deleted_at) WHERE deleted_at IS NULL;

-- ============================================================================
-- 权限表索引
-- ============================================================================

CREATE INDEX idx_perm_code ON permission(code);
CREATE INDEX idx_perm_type ON permission(type);
CREATE INDEX idx_perm_parent ON permission(parent_id);
CREATE INDEX idx_perm_resource ON permission(resource);
CREATE INDEX idx_perm_resource_action ON permission(resource, action);

-- 复合索引：权限树查询
CREATE INDEX idx_perm_parent_type ON permission(parent_id, type);

-- ============================================================================
-- 用户 - 角色关联表索引
-- ============================================================================

-- 查询用户角色的核心索引
CREATE INDEX idx_ur_user ON user_role(user_id);
CREATE INDEX idx_ur_role ON user_role(role_id);

-- 复合索引：用户角色批量查询
CREATE INDEX idx_ur_user_role ON user_role(user_id, role_id);

-- ============================================================================
-- 角色 - 权限关联表索引
-- ============================================================================

-- 查询角色权限的核心索引
CREATE INDEX idx_rp_role ON role_permission(role_id);
CREATE INDEX idx_rp_permission ON role_permission(permission_id);

-- 复合索引：角色权限批量查询
CREATE INDEX idx_rp_role_perm ON role_permission(role_id, permission_id);

-- ============================================================================
-- 审计日志表索引
-- ============================================================================

-- 分区表索引（在每个分区上创建）
-- 用户相关审计
CREATE INDEX idx_audit_user ON audit_log(user_id);
CREATE INDEX idx_audit_time ON audit_log(created_at);
CREATE INDEX idx_audit_operation ON audit_log(operation);
CREATE INDEX idx_audit_resource ON audit_log(resource_type, resource_id);

-- 复合索引：用户操作时间查询
CREATE INDEX idx_audit_user_time ON audit_log(user_id, created_at);

-- 复合索引：资源操作查询
CREATE INDEX idx_audit_resource_time ON audit_log(resource_type, resource_id, created_at);

-- 复合索引：操作成功状态查询
CREATE INDEX idx_audit_success_time ON audit_log(success, created_at) WHERE success = FALSE;

-- ============================================================================
-- 外键约束（V1 中部分已创建，补充剩余）
-- ============================================================================

-- 审计日志 - 用户外键（可选，因为用户可能被删除）
ALTER TABLE audit_log ADD CONSTRAINT fk_audit_user
    FOREIGN KEY (user_id) REFERENCES "user"(id);

-- ============================================================================
-- 统计信息更新
-- ============================================================================

ANALYZE department;
ANALYZE "user";
ANALYZE role;
ANALYZE permission;
ANALYZE user_role;
ANALYZE role_permission;
ANALYZE audit_log;
