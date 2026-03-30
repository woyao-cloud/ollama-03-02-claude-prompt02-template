-- V1__initial_schema.sql
-- 初始数据库表结构 - 核心表
-- 创建日期：2026-03-29

-- ============================================================================
-- 1. 部门表 (department) - 先创建，因为用户表依赖它
-- ============================================================================

CREATE TABLE department (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    parent_id UUID,
    manager_id UUID,
    level INT NOT NULL CHECK (level BETWEEN 1 AND 5),
    path VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version INT DEFAULT 0
);

-- 自引用外键（部门树）
ALTER TABLE department ADD CONSTRAINT fk_dept_parent
    FOREIGN KEY (parent_id) REFERENCES department(id);

-- 约束：防止循环依赖
ALTER TABLE department ADD CONSTRAINT chk_no_self_parent
    CHECK (parent_id IS NULL OR parent_id != id);

-- ============================================================================
-- 2. 用户表 (user)
-- ============================================================================

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    department_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'LOCKED')),
    email_verified BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    password_changed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version INT DEFAULT 0,

    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_phone_format CHECK (phone IS NULL OR phone ~ '^1[3-9]\d{9}$')
);

-- 用户 - 部门外键
ALTER TABLE "user" ADD CONSTRAINT fk_user_department
    FOREIGN KEY (department_id) REFERENCES department(id);

-- ============================================================================
-- 3. 角色表 (role)
-- ============================================================================

CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    data_scope VARCHAR(20) NOT NULL DEFAULT 'ALL' CHECK (data_scope IN ('ALL', 'DEPT', 'SELF', 'CUSTOM')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version INT DEFAULT 0
);

-- ============================================================================
-- 4. 权限表 (permission)
-- ============================================================================

CREATE TABLE permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('MENU', 'ACTION', 'FIELD', 'DATA')),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50),
    parent_id UUID,
    icon VARCHAR(100),
    route VARCHAR(200),
    sort_order INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

-- 权限自引用外键（权限树）
ALTER TABLE permission ADD CONSTRAINT fk_perm_parent
    FOREIGN KEY (parent_id) REFERENCES permission(id);

-- ============================================================================
-- 5. 用户 - 角色关联表 (user_role)
-- ============================================================================

CREATE TABLE user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

ALTER TABLE user_role ADD CONSTRAINT fk_ur_user
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE;

ALTER TABLE user_role ADD CONSTRAINT fk_ur_role
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;

-- ============================================================================
-- 6. 角色 - 权限关联表 (role_permission)
-- ============================================================================

CREATE TABLE role_permission (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

ALTER TABLE role_permission ADD CONSTRAINT fk_rp_role
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;

ALTER TABLE role_permission ADD CONSTRAINT fk_rp_permission
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE;

-- ============================================================================
-- 注释说明
-- ============================================================================

COMMENT ON TABLE department IS '部门表 - 支持 5 级层级的树形结构';
COMMENT ON TABLE "user" IS '用户表 - 系统用户账户';
COMMENT ON TABLE role IS '角色表 - RBAC 权限模型';
COMMENT ON TABLE permission IS '权限表 - 细粒度权限定义';
COMMENT ON TABLE user_role IS '用户 - 角色关联表';
COMMENT ON TABLE role_permission IS '角色 - 权限关联表';

COMMENT ON COLUMN department.path IS 'Materialized Path - 如/1/2/5/10';
COMMENT ON COLUMN department.level IS '部门层级：1=公司，2=一级部门，3=二级部门，4=三级部门，5=四级部门';
COMMENT ON COLUMN role.data_scope IS '数据权限范围：ALL=全部，DEPT=本部门，SELF=仅自己，CUSTOM=自定义';
COMMENT ON COLUMN permission.type IS '权限类型：MENU=菜单，ACTION=操作，FIELD=字段，DATA=数据';
COMMENT ON COLUMN "user".status IS '用户状态：ACTIVE=激活，INACTIVE=禁用，PENDING=待激活，LOCKED=锁定';
