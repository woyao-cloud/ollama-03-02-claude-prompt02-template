# 数据库设计文档

## 用户角色权限管理系统 - PostgreSQL 数据库设计

**文档版本**: 1.0
**最后更新**: 2026-03-24
**编写人**: 数据库设计专家
**依据**: FRD v1.1, ADR-004, ADR-007, DATA_FLOW_AND_API

---

## 目录

1. [概述](#概述)
2. [ER图](#er图)
3. [表结构定义](#表结构定义)
4. [索引设计](#索引设计)
5. [分区策略](#分区策略)
6. [命名规范](#命名规范)
7. [性能优化](#性能优化)
8. [备份与恢复](#备份与恢复)

---

## 概述

### 数据库架构

```
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL 15 集群                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │              主库 (Master) - 写入                   │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐  │    │
│  │  │  users  │ │departments│ │ roles  │ │permissions│    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘  │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐  │    │
│  │  │user_roles│ │role_perm│ │sessions │ │audit_log│    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘  │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       │ 流复制                              │
│              ┌────────┴────────┐                          │
│         ┌────┴────┐       ┌────┴────┐                     │
│         │ Replica │       │ Replica │                     │
│         │  (读)   │       │  (读)   │                     │
│         └─────────┘       └─────────┘                     │
│                                                             │
│  审计日志分区表 (audit_log)                                  │
│  ┌────────────────────────────────────────────────────┐    │
│  │ 主表 (模板) → audit_log_YYYY_MM 分区               │    │
│  │ ├── audit_log_2026_03                              │    │
│  │ ├── audit_log_2026_04                              │    │
│  │ └── ...                                            │    │
│  └────────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 设计目标

| 目标 | 描述 | 实现方式 |
|------|------|----------|
| 高性能 | 支持1000万用户，50000并发 | 索引优化、分区、读写分离 |
| 高可用 | 99.9%可用性 | 主从复制、自动故障转移 |
| 安全性 | 数据安全，防止未授权访问 | UUID主键、软删除、审计日志 |
| 可扩展 | 支持业务增长 | 分区表、预留字段、JSONB扩展 |
| 可维护 | 易于维护和监控 | 规范命名、注释、Flyway迁移 |

### 核心设计原则

1. **使用UUID作为主键**: 分布式友好，安全性高，防止ID遍历攻击
2. **软删除机制**: 保留历史数据，支持审计和恢复
3. **时间戳字段**: 所有表包含 `created_at` 和 `updated_at`
4. **乐观锁控制**: 使用 `version` 字段防止并发更新冲突
5. **Materialized Path**: 部门表使用路径字段实现高效树形查询
6. **RBAC四级权限**: 菜单、操作、字段、数据四级权限控制

---

## ER图

### 完整实体关系图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              数据库实体关系图                                      │
└─────────────────────────────────────────────────────────────────────────────────┘

┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   users      │         │ departments  │         │    roles     │
├──────────────┤         ├──────────────┤         ├──────────────┤
│ PK id        │◄───────│ PK id        │         │ PK id        │
│ FK dept_id   │────────►│ FK parent_id │         │ name         │
│ email        │         │ code         │         │ code         │
│ password_hash│         │ path         │         │ data_scope   │
│ status       │         │ level        │         └──────┬───────┘
│ created_at   │         │ status       │                │
└──────┬───────┘         └──────────────┘                │
       │                                                 │
       │                                                 │
       │    ┌──────────────┐     ┌──────────────┐       │
       │    │ user_roles   │     │ permissions  │◄──────┘
       │    ├──────────────┤     ├──────────────┤
       └───►│ FK user_id   │     │ PK id        │
            │ FK role_id   │     │ code         │
            └──────────────┘     │ type         │
                                 │ resource     │
            ┌──────────────┐     │ action       │
            │role_permissions    └──────┬───────┘
            ├──────────────┤            │
            │ FK role_id   │◄───────────┘
            │ FK perm_id   │
            └──────────────┘

┌──────────────┐         ┌──────────────┐
│user_sessions │         │  audit_log   │
├──────────────┤         ├──────────────┤
│ PK id        │         │ PK id        │
│ FK user_id   │         │ FK user_id   │
│ token        │         │ operation    │
│ expires_at   │         │ resource_type│
│ client_ip    │         │ old_value    │
│ user_agent   │         │ new_value    │
└──────────────┘         └──────────────┘
```

### 关系说明

| 关系 | 类型 | 描述 |
|------|------|------|
| users ↔ departments | N:1 | 用户属于一个部门 |
| departments ↔ departments | 1:N | 部门自关联，支持树形结构 |
| users ↔ roles | N:M | 通过 user_roles 关联表 |
| roles ↔ permissions | N:M | 通过 role_permissions 关联表 |
| users ↔ user_sessions | 1:N | 用户可有多个会话 |
| users ↔ audit_log | 1:N | 用户操作审计记录 |

---

## 表结构定义

### 1. users (用户表)

**描述**: 存储系统用户的基本信息、认证状态和登录记录

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    department_id UUID REFERENCES departments(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    last_login_ip VARCHAR(45),
    password_changed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'LOCKED')),
    CONSTRAINT chk_users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_users_phone_format CHECK (phone IS NULL OR phone ~ '^1[3-9]\d{9}$')
);
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| id | UUID | PK | 主键，自动生成 |
| email | VARCHAR(255) | UK, NOT NULL | 邮箱地址，全局唯一 |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt加密后的密码 |
| first_name | VARCHAR(100) | NOT NULL | 名 |
| last_name | VARCHAR(100) | NOT NULL | 姓 |
| phone | VARCHAR(20) | - | 手机号（11位） |
| avatar_url | VARCHAR(500) | - | 头像URL |
| department_id | UUID | FK | 所属部门ID |
| status | VARCHAR(20) | NOT NULL | 状态: ACTIVE/INACTIVE/PENDING/LOCKED |
| email_verified | BOOLEAN | NOT NULL, DEFAULT FALSE | 邮箱是否验证 |
| failed_login_attempts | INT | NOT NULL, DEFAULT 0 | 连续登录失败次数 |
| locked_until | TIMESTAMP WITH TIME ZONE | - | 锁定截止时间 |
| last_login_at | TIMESTAMP WITH TIME ZONE | - | 最后登录时间 |
| last_login_ip | VARCHAR(45) | - | 最后登录IP（支持IPv6） |
| password_changed_at | TIMESTAMP WITH TIME ZONE | - | 密码修改时间 |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 更新时间 |
| deleted_at | TIMESTAMP WITH TIME ZONE | - | 删除时间（软删除） |
| version | INT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |

---

### 2. departments (部门表)

**描述**: 使用Materialized Path模式存储树形部门结构，支持最多5级层级

```sql
CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    parent_id UUID REFERENCES departments(id),
    manager_id UUID REFERENCES users(id),
    level INT NOT NULL CHECK (level BETWEEN 1 AND 5),
    path VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_departments_code UNIQUE (code),
    CONSTRAINT chk_departments_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_no_self_parent CHECK (parent_id IS NULL OR parent_id != id),
    CONSTRAINT chk_departments_level CHECK (level >= 1 AND level <= 5)
);
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| name | VARCHAR(100) | NOT NULL | 部门名称 |
| code | VARCHAR(50) | UK, NOT NULL | 部门编码，如 DEPT-001 |
| parent_id | UUID | FK | 父部门ID（根节点为NULL） |
| manager_id | UUID | FK | 部门负责人ID |
| level | INT | NOT NULL, CHECK 1-5 | 层级：1=公司，2=一级部门... |
| path | VARCHAR(500) | NOT NULL | Materialized Path，如 /1/2/5 |
| sort_order | INT | DEFAULT 0 | 排序号（越大越靠前） |
| description | TEXT | - | 部门描述 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态 |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | 更新时间 |
| deleted_at | TIMESTAMP WITH TIME ZONE | - | 删除时间（软删除） |
| version | INT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |

**Materialized Path示例**:

```
公司(根) id=1, path=/1, level=1
├── 研发中心 id=2, path=/1/2, level=2
│   ├── 后端组 id=3, path=/1/2/3, level=3
│   └── 前端组 id=4, path=/1/2/4, level=3
└── 销售中心 id=5, path=/1/5, level=2
    └── 华东区 id=6, path=/1/5/6, level=3

查询研发中心所有子部门:
SELECT * FROM departments WHERE path LIKE '/1/2/%';
```

---

### 3. roles (角色表)

**描述**: 定义系统角色，包含数据权限范围配置

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    data_scope VARCHAR(20) NOT NULL DEFAULT 'ALL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT uq_roles_code UNIQUE (code),
    CONSTRAINT chk_roles_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_roles_data_scope CHECK (data_scope IN ('ALL', 'DEPT', 'SELF', 'CUSTOM'))
);
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| name | VARCHAR(50) | UK, NOT NULL | 角色名称 |
| code | VARCHAR(50) | UK, NOT NULL | 角色编码，如 ROLE_ADMIN |
| description | TEXT | - | 角色描述 |
| data_scope | VARCHAR(20) | NOT NULL, DEFAULT 'ALL' | 数据权限范围 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态 |
| is_system | BOOLEAN | NOT NULL, DEFAULT FALSE | 是否系统预设角色（不可删除） |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | 更新时间 |
| deleted_at | TIMESTAMP WITH TIME ZONE | - | 删除时间（软删除） |
| version | INT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |

**数据权限范围说明**:

| 范围 | 说明 |
|------|------|
| ALL | 可访问全部数据 |
| DEPT | 可访问本部门及子部门数据 |
| SELF | 只能访问自己的数据 |
| CUSTOM | 自定义数据范围 |

---

### 4. permissions (权限表)

**描述**: 定义系统权限，支持四级权限模型（菜单、操作、字段、数据）

```sql
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50),
    parent_id UUID REFERENCES permissions(id),
    icon VARCHAR(100),
    route VARCHAR(200),
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,

    CONSTRAINT uq_permissions_code UNIQUE (code),
    CONSTRAINT chk_permissions_type CHECK (type IN ('MENU', 'ACTION', 'FIELD', 'DATA')),
    CONSTRAINT chk_permissions_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| name | VARCHAR(100) | NOT NULL | 权限名称 |
| code | VARCHAR(100) | UK, NOT NULL | 权限编码，如 user:create |
| type | VARCHAR(20) | NOT NULL | 权限类型: MENU/ACTION/FIELD/DATA |
| resource | VARCHAR(50) | NOT NULL | 资源类型，如 user |
| action | VARCHAR(50) | - | 操作类型，如 create/read/update/delete |
| parent_id | UUID | FK | 父权限ID（用于菜单层级） |
| icon | VARCHAR(100) | - | 菜单图标（仅MENU类型） |
| route | VARCHAR(200) | - | 前端路由（仅MENU类型） |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序号 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态 |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | 更新时间 |
| version | INT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |

**四级权限说明**:

| 类型 | 说明 | 示例 |
|------|------|------|
| MENU | 菜单权限，控制导航显示 | system:user:menu |
| ACTION | 操作权限，控制按钮/功能 | system:user:create |
| FIELD | 字段权限，控制字段显示/编辑 | system:user:phone:read |
| DATA | 数据权限，控制数据范围（通过data_scope实现） | - |

---

### 5. role_permissions (角色权限关联表)

**描述**: 角色与权限的多对多关联表

```sql
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (role_id, permission_id)
);
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| role_id | UUID | PK, FK | 角色ID |
| permission_id | UUID | PK, FK | 权限ID |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

---

### 6. user_roles (用户角色关联表)

**描述**: 用户与角色的多对多关联表

```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, role_id)
);
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| user_id | UUID | PK, FK | 用户ID |
| role_id | UUID | PK, FK | 角色ID |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

---

### 7. user_sessions (用户会话表)

**描述**: 存储用户登录会话信息，支持分布式会话管理

```sql
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token VARCHAR(500) NOT NULL,
    refresh_token VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    refresh_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    client_ip VARCHAR(45),
    user_agent TEXT,
    device_info VARCHAR(200),
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_sessions_access_token UNIQUE (access_token),
    CONSTRAINT uq_sessions_refresh_token UNIQUE (refresh_token)
);
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| user_id | UUID | NOT NULL, FK | 用户ID |
| access_token | VARCHAR(500) | UK, NOT NULL | Access Token |
| refresh_token | VARCHAR(500) | UK, NOT NULL | Refresh Token |
| expires_at | TIMESTAMP WITH TIME ZONE | NOT NULL | Access Token过期时间 |
| refresh_expires_at | TIMESTAMP WITH TIME ZONE | NOT NULL | Refresh Token过期时间 |
| client_ip | VARCHAR(45) | - | 客户端IP |
| user_agent | TEXT | - | 浏览器User-Agent |
| device_info | VARCHAR(200) | - | 设备信息 |
| is_valid | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否有效（登出时设为FALSE） |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| last_accessed_at | TIMESTAMP WITH TIME ZONE | - | 最后访问时间 |

---

### 8. audit_logs (审计日志表)

**描述**: 记录系统敏感操作日志，按月分区存储

```sql
-- 主表（模板）
CREATE TABLE audit_logs (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- 创建分区（按年-月）
CREATE TABLE audit_logs_2026_03 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');

CREATE TABLE audit_logs_2026_04 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');

CREATE TABLE audit_logs_2026_05 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

-- 默认分区（处理未来数据）
CREATE TABLE audit_logs_default PARTITION OF audit_logs DEFAULT;
```

**字段说明**:

| 字段 | 类型 | 约束 | 描述 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| user_id | UUID | FK | 操作用户ID |
| username | VARCHAR(100) | - | 用户名（冗余存储） |
| operation | VARCHAR(50) | NOT NULL | 操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT等 |
| resource_type | VARCHAR(50) | NOT NULL | 资源类型：USER/ROLE/PERMISSION/DEPARTMENT等 |
| resource_id | UUID | - | 资源ID |
| old_value | JSONB | - | 操作前数据（JSON格式） |
| new_value | JSONB | - | 操作后数据（JSON格式） |
| description | TEXT | - | 操作描述 |
| client_ip | VARCHAR(45) | - | 客户端IP |
| user_agent | TEXT | - | 浏览器User-Agent |
| session_id | VARCHAR(100) | - | 会话ID |
| success | BOOLEAN | NOT NULL | 操作是否成功 |
| error_message | TEXT | - | 错误信息（失败时） |
| execution_time_ms | INT | - | 执行耗时（毫秒） |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 操作时间 |

---

## 索引设计

### users 表索引

```sql
-- 唯一索引
CREATE UNIQUE INDEX idx_users_email ON users(email);

-- 查询优化索引
CREATE INDEX idx_users_department ON users(department_id);
CREATE INDEX idx_users_status ON users(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_created_at ON users(created_at DESC);
CREATE INDEX idx_users_active ON users(id) WHERE deleted_at IS NULL AND status = 'ACTIVE';

-- 复合索引（常见查询场景）
CREATE INDEX idx_users_dept_status ON users(department_id, status) WHERE deleted_at IS NULL;
```

### departments 表索引

```sql
-- 唯一索引
CREATE UNIQUE INDEX idx_departments_code ON departments(code);

-- Materialized Path查询索引（关键）
CREATE INDEX idx_departments_path ON departments(path);
CREATE INDEX idx_departments_path_pattern ON departments USING btree (path varchar_pattern_ops);

-- 层级查询索引
CREATE INDEX idx_departments_parent ON departments(parent_id);
CREATE INDEX idx_departments_level ON departments(level);
CREATE INDEX idx_departments_level_status ON departments(level, status) WHERE deleted_at IS NULL;

-- 复合索引
CREATE INDEX idx_departments_parent_sort ON departments(parent_id, sort_order DESC);
```

### roles 表索引

```sql
-- 唯一索引
CREATE UNIQUE INDEX idx_roles_name ON roles(name);
CREATE UNIQUE INDEX idx_roles_code ON roles(code);

-- 状态过滤索引
CREATE INDEX idx_roles_status ON roles(status) WHERE deleted_at IS NULL;
```

### permissions 表索引

```sql
-- 唯一索引
CREATE UNIQUE INDEX idx_permissions_code ON permissions(code);

-- 类型查询索引
CREATE INDEX idx_permissions_type ON permissions(type);
CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_type_resource ON permissions(type, resource);

-- 父权限索引
CREATE INDEX idx_permissions_parent ON permissions(parent_id);

-- 状态过滤索引
CREATE INDEX idx_permissions_status ON permissions(status);
```

### role_permissions 表索引

```sql
-- 主键已包含 (role_id, permission_id)
-- 反向查询索引
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);
```

### user_roles 表索引

```sql
-- 主键已包含 (user_id, role_id)
-- 反向查询索引
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
```

### user_sessions 表索引

```sql
-- 唯一索引
CREATE UNIQUE INDEX idx_sessions_access_token ON user_sessions(access_token);
CREATE UNIQUE INDEX idx_sessions_refresh_token ON user_sessions(refresh_token);

-- 查询索引
CREATE INDEX idx_sessions_user ON user_sessions(user_id);
CREATE INDEX idx_sessions_expires ON user_sessions(expires_at);
CREATE INDEX idx_sessions_user_valid ON user_sessions(user_id, is_valid) WHERE is_valid = TRUE;
```

### audit_logs 表索引

```sql
-- 分区表索引（在每个分区上创建）
-- 用户查询
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);

-- 时间查询
CREATE INDEX idx_audit_logs_time ON audit_logs(created_at DESC);

-- 资源查询
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);

-- 操作类型查询
CREATE INDEX idx_audit_logs_operation ON audit_logs(operation, created_at DESC);

-- 复合索引
CREATE INDEX idx_audit_logs_user_time ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_success_time ON audit_logs(success, created_at DESC);
```

---

## 分区策略

### 审计日志分区

**分区方案**: 按 `created_at` 字段按月分区

**分区管理脚本**:

```sql
-- 创建未来分区（自动化脚本）
CREATE OR REPLACE FUNCTION create_audit_log_partition(year INT, month INT)
RETURNS TEXT AS $$
DECLARE
    partition_name TEXT;
    start_date DATE;
    end_date DATE;
BEGIN
    partition_name := 'audit_logs_' || year || '_' || LPAD(month::TEXT, 2, '0');
    start_date := make_date(year, month, 1);
    end_date := start_date + INTERVAL '1 month';

    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_logs
         FOR VALUES FROM (%L) TO (%L)',
        partition_name, start_date, end_date
    );

    -- 在分区上创建索引
    EXECUTE format(
        'CREATE INDEX IF NOT EXISTS idx_%s_user ON %I(user_id)',
        partition_name, partition_name
    );

    EXECUTE format(
        'CREATE INDEX IF NOT EXISTS idx_%s_time ON %I(created_at DESC)',
        partition_name, partition_name
    );

    RETURN partition_name;
END;
$$ LANGUAGE plpgsql;

-- 自动创建未来3个月的分区
SELECT create_audit_log_partition(
    EXTRACT(YEAR FROM CURRENT_DATE + INTERVAL '1 month')::INT,
    EXTRACT(MONTH FROM CURRENT_DATE + INTERVAL '1 month')::INT
);
```

**分区维护策略**:

| 操作 | 策略 |
|------|------|
| 创建 | 每月1日自动创建下月分区 |
| 归档 | 3个月前的分区数据归档到冷存储 |
| 删除 | 3年前的分区数据可删除（合规要求保留3年） |
| 备份 | 每月分区独立备份 |

---

## 命名规范

### 表命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 实体表 | 小写复数 | users, departments, roles |
| 关联表 | {表1}_{表2} | user_roles, role_permissions |
| 审计表 | {实体}_audits / audit_logs | audit_logs |
| 分区表 | {表}_{YYYY}_{MM} | audit_logs_2026_03 |

### 列命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 主键 | id | id |
| 外键 | {表名}_id | department_id, user_id |
| 时间戳 | {action}_at | created_at, updated_at, deleted_at |
| 布尔 | {description} | is_active, email_verified |
| 状态 | status | status |
| 计数 | {description}_count | failed_login_attempts |

### 索引命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 主键 | pk_{表名} | pk_users |
| 外键 | fk_{表名}_{列名} | fk_users_department |
| 唯一 | uq_{表名}_{列名} | uq_users_email |
| 普通 | idx_{表名}_{列名} | idx_users_status |
| 复合 | idx_{表名}_{列1}_{列2} | idx_users_dept_status |
| 部分 | idx_{表名}_{列}_active | idx_users_email_active |

---

## 性能优化

### 查询优化建议

#### 1. 用户登录查询

```sql
-- 使用覆盖索引，快速定位用户
SELECT id, email, password_hash, status, failed_login_attempts, locked_until
FROM users
WHERE email = 'user@example.com'
AND deleted_at IS NULL;

-- 索引: idx_users_email
```

#### 2. 部门子树查询（Materialized Path）

```sql
-- 高效查询某部门所有子部门
SELECT * FROM departments
WHERE path LIKE '/1/2/%'
AND deleted_at IS NULL
ORDER BY path;

-- 索引: idx_departments_path_pattern
```

#### 3. 用户权限查询

```sql
-- 查询用户的所有权限
SELECT DISTINCT p.*
FROM permissions p
JOIN role_permissions rp ON p.id = rp.permission_id
JOIN user_roles ur ON rp.role_id = ur.role_id
WHERE ur.user_id = ?
AND p.status = 'ACTIVE'
AND p.deleted_at IS NULL;

-- 索引: idx_user_roles_role, idx_role_permissions_permission
```

#### 4. 审计日志时间范围查询

```sql
-- 查询某用户的近期操作日志（利用分区裁剪）
SELECT * FROM audit_logs
WHERE user_id = ?
AND created_at >= '2026-03-01'
AND created_at < '2026-04-01'
ORDER BY created_at DESC
LIMIT 100;

-- 自动使用 audit_logs_2026_03 分区
```

### 批量操作优化

```sql
-- 批量插入（使用UNNEST优化）
INSERT INTO user_roles (user_id, role_id)
SELECT * FROM UNNEST(?::uuid[], ?::uuid[])
AS t(user_id, role_id);

-- 批量更新（使用CASE语句）
UPDATE users
SET status = CASE id
    WHEN ? THEN 'ACTIVE'
    WHEN ? THEN 'INACTIVE'
END
WHERE id IN (?, ?);
```

### 统计信息维护

```sql
-- 定期更新统计信息
ANALYZE users;
ANALYZE departments;
ANALYZE audit_logs;

-- 设置自动清理
ALTER TABLE audit_logs SET (autovacuum_vacuum_scale_factor = 0.1);
```

---

## 备份与恢复

### 备份策略

| 类型 | 频率 | 保留期 | 工具 |
|------|------|--------|------|
| 全量备份 | 每日 | 30天 | pg_dump |
| 增量备份 | 每小时 | 7天 | WAL归档 |
| 审计日志 | 按月 | 3年 | pg_dump + 冷存储 |

### 关键表备份命令

```bash
# 备份业务数据（不包含审计日志）
pg_dump -h localhost -U postgres -d usermanagement \
  --exclude-table=audit_logs \
  --exclude-table=audit_logs_* \
  -F c -f backup_business_$(date +%Y%m%d).dump

# 备份本月审计日志
pg_dump -h localhost -U postgres -d usermanagement \
  -t audit_logs_$(date +%Y_%m) \
  -F c -f backup_audit_$(date +%Y%m%d).dump
```

### 恢复策略

```bash
# 恢复业务数据
pg_restore -h localhost -U postgres -d usermanagement \
  --clean --if-exists \
  backup_business_20260324.dump

# 恢复审计日志（添加到现有表）
pg_restore -h localhost -U postgres -d usermanagement \
  --data-only \
  backup_audit_20260324.dump
```

---

## 附录

### A. 表统计信息

| 表名 | 预估行数 | 增长速率 | 主要操作 |
|------|----------|----------|----------|
| users | 1000万 | +1000/天 | 读写均衡 |
| departments | <1000 | 低频 | 读多写少 |
| roles | <100 | 极低 | 读多写少 |
| permissions | <1000 | 极低 | 读多写少 |
| user_roles | 1000万+ | +1000/天 | 读写均衡 |
| role_permissions | <10万 | 低频 | 读多写少 |
| user_sessions | <5万 | 高频（登录时创建） | 读多写少 |
| audit_logs | 1000万/月 | 高频（操作即记录） | 写多读少 |

### B. 存储估算

| 表名 | 单行大小 | 总行数 | 总大小 |
|------|----------|--------|--------|
| users | ~500 bytes | 1000万 | ~5 GB |
| departments | ~300 bytes | 1000 | ~1 MB |
| roles | ~200 bytes | 100 | ~20 KB |
| permissions | ~250 bytes | 1000 | ~250 KB |
| user_roles | ~50 bytes | 1000万 | ~500 MB |
| role_permissions | ~50 bytes | 10万 | ~5 MB |
| user_sessions | ~500 bytes | 5万 | ~25 MB |
| audit_logs | ~2 KB | 1000万/月 | ~20 GB/月 |
| **总计** | - | - | **~300 GB/年** |

### C. 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0 | 2026-03-24 | 数据库设计专家 | 初始版本，完整数据库设计方案 |
