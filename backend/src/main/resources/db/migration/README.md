# Flyway 迁移脚本说明

## 目录结构

```
db/migration/
├── V1__initial_schema.sql          # 核心表：用户、部门、角色、权限
├── V2__audit_log_partitions.sql    # 审计日志分区表
├── V3__indexes_and_constraints.sql # 索引和外键约束
├── V4__system_configs.sql          # 系统配置表
└── README.md                       # 本说明文档
```

## 迁移顺序

| 版本 | 描述 | 依赖 |
|------|------|------|
| V1 | 初始表结构 | 无 |
| V2 | 审计日志分区 | V1 |
| V3 | 索引优化 | V1, V2 |
| V4 | 系统配置 | V1 |

## 执行命令

```bash
# 执行迁移
mvn flyway:migrate

# 验证迁移状态
mvn flyway:info

# 清理迁移（开发环境）
mvn flyway:clean
```

## 表结构概览

### 核心业务表

| 表名 | 描述 | 主键 |
|------|------|------|
| `user` | 用户表 | UUID |
| `department` | 部门表（Materialized Path） | UUID |
| `role` | 角色表 | UUID |
| `permission` | 权限表 | UUID |
| `user_role` | 用户 - 角色关联 | (user_id, role_id) |
| `role_permission` | 角色 - 权限关联 | (role_id, permission_id) |

### 审计和配置表

| 表名 | 描述 | 分区策略 |
|------|------|---------|
| `audit_log` | 审计日志 | 按月分区 |
| `system_config` | 系统配置表 | 无 |
| `config_history` | 配置历史 | 无 |

## 设计决策

- **主键**: UUID v4（分布式友好，安全）
- **软删除**: `deleted_at` 字段
- **乐观锁**: `version` 字段
- **审计字段**: `created_at`, `updated_at`
- **部门树**: Materialized Path 模式（支持 5 级层级）

## 添加新迁移

```bash
# 命名格式：V{版本}__{描述}.sql
# 示例：V5__add_oauth_tables.sql
```
