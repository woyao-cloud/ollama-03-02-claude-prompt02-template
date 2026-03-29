# 数据库设计文档

**版本**: 1.0
**日期**: 2026-03-29
**状态**: 已完成

---

## 1. 数据库架构概览

```
PostgreSQL 15
├── 核心业务表 (6 张)
│   ├── user (用户表)
│   ├── department (部门表)
│   ├── role (角色表)
│   ├── permission (权限表)
│   ├── user_role (用户 - 角色关联)
│   └── role_permission (角色 - 权限关联)
├── 审计日志表 (1 张，分区表)
│   └── audit_log (按月分区)
└── 系统配置表 (2 张)
    ├── system_config (系统配置)
    └── config_history (配置历史)
```

---

## 2. 表结构详细设计

### 2.1 部门表 (department)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| name | VARCHAR(100) | NOT NULL | 部门名称 |
| code | VARCHAR(50) | NOT NULL, UNIQUE | 部门代码 |
| parent_id | UUID | FK | 父部门 ID |
| manager_id | UUID | FK | 部门负责人 ID |
| level | INT | 1-5 | 部门层级 |
| path | VARCHAR(500) | NOT NULL | Materialized Path |
| sort_order | INT | - | 排序号 |
| description | TEXT | - | 描述 |
| status | VARCHAR(20) | ACTIVE/INACTIVE | 状态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| deleted_at | TIMESTAMP | - | 软删除 |
| version | INT | - | 乐观锁 |

**核心索引**:
- `idx_dept_path` - 路径查询
- `idx_dept_parent` - 子部门查询
- `idx_dept_parent_level` - 复合查询

---

### 2.2 用户表 (user)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 邮箱 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| first_name | VARCHAR(100) | NOT NULL | 名 |
| last_name | VARCHAR(100) | NOT NULL | 姓 |
| phone | VARCHAR(20) | - | 手机号 |
| avatar_url | VARCHAR(500) | - | 头像 |
| department_id | UUID | FK | 所属部门 |
| status | VARCHAR(20) | NOT NULL | 用户状态 |
| email_verified | BOOLEAN | - | 邮箱已验证 |
| failed_login_attempts | INT | - | 失败登录次数 |
| locked_until | TIMESTAMP | - | 锁定截止时间 |
| last_login_at | TIMESTAMP | - | 最后登录时间 |
| last_login_ip | VARCHAR(45) | - | 最后登录 IP |
| password_changed_at | TIMESTAMP | - | 密码修改时间 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| deleted_at | TIMESTAMP | - | 软删除 |
| version | INT | - | 乐观锁 |

**核心索引**:
- `idx_user_email` - 邮箱查询
- `idx_user_department` - 部门用户查询
- `idx_user_status_created` - 状态 + 时间分页

---

### 2.3 角色表 (role)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| name | VARCHAR(50) | UNIQUE | 角色名称 |
| code | VARCHAR(50) | UNIQUE | 角色代码 (如 ROLE_ADMIN) |
| description | TEXT | - | 描述 |
| data_scope | VARCHAR(20) | ALL/DEPT/SELF/CUSTOM | 数据权限范围 |
| status | VARCHAR(20) | ACTIVE/INACTIVE | 状态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| deleted_at | TIMESTAMP | - | 软删除 |
| version | INT | - | 乐观锁 |

---

### 2.4 权限表 (permission)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| name | VARCHAR(100) | NOT NULL | 权限名称 |
| code | VARCHAR(100) | UNIQUE | 权限代码 (如 user:create) |
| type | VARCHAR(20) | MENU/ACTION/FIELD/DATA | 权限类型 |
| resource | VARCHAR(50) | NOT NULL | 资源名称 |
| action | VARCHAR(50) | - | 操作 (create/update/delete/read) |
| parent_id | UUID | FK | 父权限 ID |
| icon | VARCHAR(100) | - | 菜单图标 |
| route | VARCHAR(200) | - | 前端路由 |
| sort_order | INT | - | 排序号 |
| status | VARCHAR(20) | ACTIVE/INACTIVE | 状态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| version | INT | - | 乐观锁 |

---

### 2.5 审计日志表 (audit_log) - 分区表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| user_id | UUID | FK | 用户 ID |
| username | VARCHAR(100) | - | 用户名 (冗余) |
| operation | VARCHAR(50) | NOT NULL | 操作类型 |
| resource_type | VARCHAR(50) | NOT NULL | 资源类型 |
| resource_id | UUID | - | 资源 ID |
| old_value | JSONB | - | 操作前数据 |
| new_value | JSONB | - | 操作后数据 |
| description | TEXT | - | 描述 |
| client_ip | VARCHAR(45) | - | 客户端 IP |
| user_agent | TEXT | - | 用户代理 |
| session_id | VARCHAR(100) | - | 会话 ID |
| success | BOOLEAN | NOT NULL | 是否成功 |
| error_message | TEXT | - | 错误消息 |
| execution_time_ms | INT | - | 执行耗时 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 (分区键) |

**分区策略**: 按月分区
**初始分区**: 2026-03 至 2027-02 (12 个月)

---

### 2.6 系统配置表 (system_config)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | UUID | PK | 主键 |
| config_key | VARCHAR(100) | UNIQUE | 配置键 |
| config_value | TEXT | NOT NULL | 配置值 |
| config_type | VARCHAR(20) | STRING/NUMBER/BOOLEAN/JSON | 类型 |
| category | VARCHAR(50) | NOT NULL | 分类 |
| description | TEXT | - | 描述 |
| encrypted | BOOLEAN | - | 是否加密 |
| status | VARCHAR(20) | ACTIVE/INACTIVE | 状态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| version | INT | - | 乐观锁 |

**初始配置**: 17 条（认证、系统、用户三类）

---

## 3. ER 关系图

```
┌─────────────┐       ┌─────────────┐
│  department │       │    role     │
└──────┬──────┘       └──────┬──────┘
       │                     │
       │ 1:N                 │ 1:N
       │                     │
┌──────┴──────┐       ┌──────┴──────────────┐
│    user     │───────│   user_role (M:N)   │
└─────────────┘       └─────────────────────┘

┌─────────────┐       ┌─────────────────────┐
│ permission  │───────│ role_permission(M:N)│
└─────────────┘       └─────────────────────┘

┌─────────────┐
│ audit_log   │ (分区表，独立)
└─────────────┘

┌─────────────┐       ┌─────────────────────┐
│system_config│───────│  config_history     │
└─────────────┘       └─────────────────────┘
```

---

## 4. 设计决策

| 设计点 | 决策 | 理由 |
|--------|------|------|
| 主键类型 | UUID v4 | 分布式友好，不可预测（安全） |
| 软删除 | deleted_at 字段 | 保留审计数据，支持恢复 |
| 乐观锁 | version 字段 | 并发更新保护 |
| 部门树 | Materialized Path | 高效查询子树，支持 5 级 |
| 审计日志 | 按月分区 | 大数据量优化，便于归档 |
| 时间戳 | created_at + updated_at | 审计追踪，数据同步 |

---

## 5. Flyway 迁移脚本

| 版本 | 文件名 | 描述 |
|------|--------|------|
| V1 | V1__initial_schema.sql | 核心表结构 |
| V2 | V2__audit_log_partitions.sql | 审计日志分区 |
| V3 | V3__indexes_and_constraints.sql | 索引和约束 |
| V4 | V4__system_configs.sql | 系统配置表 |

---

## 6. 性能指标

| 场景 | 目标 | 实现方式 |
|------|------|----------|
| 部门树查询 | <100ms (10 万部门) | Materialized Path + 索引 |
| 用户查询 | <200ms | 复合索引优化 |
| 权限查询 | <50ms | 角色 - 权限缓存 |
| 审计日志查询 | <500ms | 分区表 + 索引 |

---

## 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0 | 2026-03-29 | 数据库设计师 | 初始版本，基于 ADR-004 |
