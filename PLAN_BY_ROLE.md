# 全栈用户管理系统 - 按角色区分的项目计划书

**版本**: 2.0（按角色划分）
**日期**: 2026-03-28
**状态**: 规划中

---

## 1. 项目概述

### 1.1 项目目标
构建一个企业级全栈用户管理系统，提供用户注册、登录、权限管理、角色分配、部门管理、审计日志和系统配置等核心功能。

### 1.2 角色分工

| 角色 | 人数 | 主要职责 |
|------|------|----------|
| **数据库设计师** | 1人 | 数据库设计、迁移脚本、索引优化 |
| **后端开发工程师** | 2人 | Spring Boot开发、API实现、业务逻辑 |
| **前端开发工程师** | 1-2人 | Next.js开发、UI组件、用户交互 |
| **DevOps工程师** | 0.5人 | 部署、CI/CD、监控配置 |
| **测试工程师** | 1人 | 测试用例、自动化测试、性能测试 |

### 1.3 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端 | Spring Boot | 3.5 + JDK 21 |
| 后端 | Spring Data JPA | 3.5 |
| 后端 | Spring Security | 6.x |
| 数据库 | PostgreSQL | 15 |
| 迁移工具 | Flyway | 10.x |
| 前端 | Next.js | 16+ |
| 前端 | TypeScript | 5+ |
| 前端 | Tailwind CSS | 3.x |
| 前端 | shadcn/ui | 最新 |
| 前端 | Zustand | 4.x |
| 基础设施 | Docker | 24+ |
| 基础设施 | Kubernetes | 1.28+ |

---

## 2. 数据库设计师任务

### 2.1 数据库设计阶段 (Week 1-2)

#### 任务 D1: 核心表结构设计 (Week 1, Day 1-3)
**负责人**: 数据库设计师
**依赖**: 无

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| D1.1 | 用户表设计 | 用户表DDL | 包含用户基本信息、状态、审计字段 |
| D1.2 | 角色权限表设计 | 角色权限表DDL | RBAC三表结构（用户-角色-权限） |
| D1.3 | 部门表设计 | 部门表DDL | Materialized Path模式支持5级层级 |

**详细设计**:
```sql
-- 用户表
users (id, username, email, password_hash, status, dept_id, created_at, updated_at)

-- 角色表
roles (id, name, description, data_scope, parent_id, created_at)

-- 权限表
permissions (id, code, name, type, resource, action)

-- 用户角色关联表
user_roles (user_id, role_id)

-- 角色权限关联表
role_permissions (role_id, permission_id)

-- 部门表 (Materialized Path)
departments (id, name, code, parent_id, path, level, sort_order, created_at)
```

#### 任务 D2: 配置与审计表设计 (Week 1, Day 4-5)
**负责人**: 数据库设计师
**依赖**: D1

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| D2.1 | 系统配置表设计 | 配置表DDL | 支持动态配置、配置历史 |
| D2.2 | 审计日志表设计 | 审计表DDL | 支持操作日志、登录日志 |
| D2.3 | 索引设计文档 | 索引设计文档 | 所有查询字段都有适当索引 |

**详细设计**:
```sql
-- 系统配置表
system_configs (id, config_key, config_value, config_type, category, encrypted, created_at, updated_at)

-- 配置历史表
config_history (id, config_id, old_value, new_value, modified_by, modified_at)

-- 审计日志表
audit_logs (id, user_id, operation, resource_type, resource_id, old_data, new_data, ip_address, user_agent, created_at)
```

#### 任务 D3: Flyway迁移脚本编写 (Week 2, Day 1-3)
**负责人**: 数据库设计师
**依赖**: D1, D2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| D3.1 | V1__Initial_schema.sql | 初始迁移脚本 | 所有核心表创建完成 |
| D3.2 | V2__Add_roles_permissions.sql | 权限相关迁移 | 角色权限表创建完成 |
| D3.3 | V3__Add_audit_tables.sql | 审计相关迁移 | 审计表创建完成 |
| D3.4 | 迁移脚本测试 | 测试报告 | 所有脚本可正常执行和回滚 |

**迁移脚本结构**:
```
db/migration/
├── V1__Initial_schema.sql          # 用户、部门、配置表
├── V2__Add_roles_permissions.sql    # 角色权限表
├── V3__Add_audit_tables.sql         # 审计日志表
├── V4__Add_performance_indexes.sql  # 性能优化索引
└── V5__Add_constraints.sql          # 外键约束
```

#### 任务 D4: 性能优化设计 (Week 2, Day 4-5)
**负责人**: 数据库设计师
**依赖**: D3

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| D4.1 | 部门树查询优化 | 物化路径索引 | 10万部门查询<100ms |
| D4.2 | 权限查询优化 | 复合索引设计 | 权限查询<50ms |
| D4.3 | 审计日志分区策略 | 分区方案文档 | 支持按时间分区 |

**关键索引**:
```sql
-- 部门树查询索引
CREATE INDEX idx_dept_path ON departments(path);
CREATE INDEX idx_dept_parent ON departments(parent_id);

-- 权限查询索引
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_role_perms_role ON role_permissions(role_id);

-- 审计日志索引
CREATE INDEX idx_audit_user ON audit_logs(user_id, created_at);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id);
```

---

## 3. 后端开发工程师任务

### 3.1 基础架构阶段 (Week 1-2)

#### 任务 B1: 项目骨架搭建 (Week 1, Day 1-2)
**负责人**: 后端开发工程师(主)
**依赖**: 无

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B1.1 | Spring Boot项目初始化 | 项目骨架 | 多模块结构，依赖配置完成 |
| B1.2 | 配置文件设置 | application.yml | 多环境配置(dev/test/prod) |
| B1.3 | Docker配置 | Dockerfile | 容器化配置完成 |

**项目结构**:
```
backend/
├── src/main/java/com/usermanagement/
│   ├── Application.java
│   ├── config/           # 配置类
│   ├── domain/           # JPA实体
│   ├── repository/       # Repository接口
│   ├── service/          # 业务服务
│   ├── web/              # Controller + DTO
│   ├── security/         # 安全配置
│   └── infrastructure/   # 基础设施
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── db/migration/     # Flyway迁移脚本
└── src/test/
```

#### 任务 B2: 核心实体开发 (Week 1, Day 3-5)
**负责人**: 后端开发工程师(主)
**依赖**: B1

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B2.1 | 用户实体 | User.java | JPA注解正确，验证规则完整 |
| B2.2 | 部门实体 | Department.java | Materialized Path实现 |
| B2.3 | 角色权限实体 | Role.java, Permission.java | RBAC模型实现 |
| B2.4 | Repository接口 | *Repository.java | Spring Data接口定义 |

**实体类示例**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles")
    private Set<Role> roles;

    // audit fields
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
```

### 3.2 认证授权模块 (Week 2-3)

#### 任务 B3: JWT认证实现 (Week 2, Day 3-5)
**负责人**: 后端开发工程师(副)
**依赖**: B2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B3.1 | JWT工具类 | JwtTokenProvider.java | 生成/验证Token |
| B3.2 | 认证过滤器 | JwtAuthenticationFilter.java | 过滤器链集成 |
| B3.3 | 安全配置 | SecurityConfig.java | Spring Security配置 |
| B3.4 | 认证Controller | AuthController.java | 登录/刷新Token API |

#### 任务 B4: 权限控制实现 (Week 3, Day 1-3)
**负责人**: 后端开发工程师(副)
**依赖**: B3

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B4.1 | 权限表达式 | PermissionEvaluator.java | SpEL表达式支持 |
| B4.2 | 方法级权限 | @PreAuthorize配置 | 注解权限控制 |
| B4.3 | 数据权限拦截器 | DataPermissionInterceptor.java | 数据范围过滤 |

#### 任务 B5: 密码策略实现 (Week 3, Day 4-5)
**负责人**: 后端开发工程师(副)
**依赖**: B4

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B5.1 | 密码加密 | BCrypt配置 | 强度因子12 |
| B5.2 | 账户锁定 | AccountLockoutService.java | 5次失败锁定15分钟 |
| B5.3 | 密码策略验证 | PasswordPolicyValidator.java | 复杂度验证 |

### 3.3 用户管理模块 (Week 4-5)

#### 任务 B6: 用户CRUD服务 (Week 4, Day 1-3)
**负责人**: 后端开发工程师(主)
**依赖**: B2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B6.1 | UserService | 用户服务层 | CRUD方法完整 |
| B6.2 | UserController | REST API | CRUD端点实现 |
| B6.3 | DTO与验证 | UserDTO, UserRequest | 字段验证注解 |

#### 任务 B7: 批量操作功能 (Week 4, Day 4-5)
**负责人**: 后端开发工程师(主)
**依赖**: B6

| 子任务 | 描述 | 交付物 | 验收物 |
|--------|------|--------|--------|
| B7.1 | 批量导入 | Excel/CSV导入 | 支持1000用户<5分钟 |
| B7.2 | 批量导出 | Excel导出 | 支持大数据量导出 |
| B7.3 | 导入验证 | ImportValidator.java | 数据验证和错误报告 |

#### 任务 B8: 用户注册服务 (Week 5, Day 1-3)
**负责人**: 后端开发工程师(副)
**依赖**: B6

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B8.1 | 注册流程 | RegistrationService.java | 自助注册流程 |
| B8.2 | 邮箱验证 | EmailVerificationService.java | 验证邮件发送 |
| B8.3 | 审批流程 | ApprovalService.java | 待审批状态管理 |

### 3.4 部门管理模块 (Week 5-6)

#### 任务 B9: 部门服务 (Week 5, Day 4-5, Week 6 Day 1-2)
**负责人**: 后端开发工程师(主)
**依赖**: B2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B9.1 | DepartmentService | 部门服务层 | CRUD + 树形操作 |
| B9.2 | 树形查询 | TreeBuilder.java | 物化路径查询 |
| B9.3 | 层级调整 | DepartmentMoveService.java | 防循环依赖检查 |
| B9.4 | DepartmentController | REST API | 部门管理端点 |

#### 任务 B10: 部门缓存 (Week 6, Day 3-4)
**负责人**: 后端开发工程师(副)
**依赖**: B9

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B10.1 | Redis缓存 | DepartmentCache.java | 部门树缓存 |
| B10.2 | 缓存失效 | CacheEvictionListener.java | 变更时自动失效 |

### 3.5 角色权限管理模块 (Week 6-7)

#### 任务 B11: 角色管理服务 (Week 6, Day 5, Week 7 Day 1-2)
**负责人**: 后端开发工程师(副)
**依赖**: B4

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B11.1 | RoleService | 角色服务层 | CRUD + 权限分配 |
| B11.2 | 角色继承 | RoleHierarchyService.java | 权限继承计算 |
| B11.3 | PermissionService | 权限服务层 | 权限管理 |
| B11.4 | Controller | RoleController, PermissionController | REST API |

#### 任务 B12: 数据权限实现 (Week 7, Day 3-5)
**负责人**: 后端开发工程师(主)
**依赖**: B11

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B12.1 | 数据范围枚举 | DataScope.java | ALL/DEPT/SELF/CUSTOM |
| B12.2 | 数据权限拦截 | DataPermissionAspect.java | AOP拦截实现 |
| B12.3 | 自定义条件解析 | CustomConditionParser.java | 复杂条件支持 |

### 3.6 审计日志模块 (Week 8)

#### 任务 B13: 审计服务 (Week 8, Day 1-3)
**负责人**: 后端开发工程师(副)
**依赖**: B2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B13.1 | AuditLogService | 审计服务层 | 日志记录查询 |
| B13.2 | AOP日志拦截 | AuditAspect.java | 自动记录操作 |
| B13.3 | 异步日志写入 | AuditLogProducer.java | Kafka异步写入 |
| B13.4 | AuditController | REST API | 日志查询端点 |

### 3.7 系统配置模块 (Week 8)

#### 任务 B14: 配置服务 (Week 8, Day 4-5)
**负责人**: 后端开发工程师(副)
**依赖**: B2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B14.1 | ConfigService | 配置服务层 | 配置CRUD |
| B14.2 | 动态刷新 | @RefreshScope配置 | 运行时刷新 |
| B14.3 | 配置缓存 | ConfigCache.java | 多级缓存 |
| B14.4 | ConfigController | REST API | 配置管理端点 |

### 3.8 性能优化 (Week 9)

#### 任务 B15: 性能优化 (Week 9)
**负责人**: 后端开发工程师(主+副)
**依赖**: B3-B14

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| B15.1 | Redis Pipeline优化 | AuthService优化 | 批量操作 |
| B15.2 | 连接池调优 | HikariCP配置 | 连接池优化 |
| B15.3 | JVM优化 | JVM参数配置 | G1GC调优 |
| B15.4 | 虚拟线程启用 | application.yml配置 | 并发性能提升 |

**性能目标**:
- 登录接口: <100ms (P95)
- 吞吐量: 10,000 TPS
- 部门树查询: <100ms (10万部门)

---

## 4. 前端开发工程师任务

### 4.1 基础架构阶段 (Week 1-2)

#### 任务 F1: 项目初始化 (Week 1, Day 1-2)
**负责人**: 前端开发工程师(主)
**依赖**: 无

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F1.1 | Next.js项目初始化 | 项目骨架 | TypeScript配置 |
| F1.2 | 依赖安装 | package.json | shadcn/ui, Tailwind, Zustand |
| F1.3 | 项目结构 | 目录结构 | 按功能组织 |

**项目结构**:
```
frontend/
├── app/                    # App Router
│   ├── (auth)/            # 认证路由组
│   ├── (dashboard)/       # 仪表板路由组
│   ├── layout.tsx
│   └── page.tsx
├── components/
│   ├── ui/                # shadcn组件
│   ├── forms/             # 表单组件
│   ├── layout/            # 布局组件
│   └── data-display/      # 数据展示
├── lib/
│   ├── api/               # API客户端
│   ├── utils/             # 工具函数
│   └── schemas/           # Zod验证
├── stores/                # Zustand状态管理
└── types/                 # TypeScript类型
```

#### 任务 F2: 基础组件开发 (Week 1, Day 3-5)
**负责人**: 前端开发工程师(主)
**依赖**: F1

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F2.1 | 布局组件 | Layout, Sidebar, Header | 响应式设计 |
| F2.2 | 通用UI组件 | Button, Card, Modal等 | shadcn/ui集成 |
| F2.3 | 表单组件 | Input, Select, Form | React Hook Form |
| F2.4 | 类型定义 | types/*.ts | 完整TypeScript类型 |

#### 任务 F3: API客户端与状态管理 (Week 2, Day 1-3)
**负责人**: 前端开发工程师(副)
**依赖**: F1

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F3.1 | Axios配置 | api/client.ts | 拦截器配置 |
| F3.2 | API方法 | api/*.ts | 按模块组织 |
| F3.3 | 状态管理 | stores/*.ts | Zustand Store |
| F3.4 | 验证Schema | schemas/*.ts | Zod验证规则 |

### 4.2 认证授权模块 (Week 2-3)

#### 任务 F4: 认证页面 (Week 2, Day 4-5, Week 3 Day 1)
**负责人**: 前端开发工程师(主)
**依赖**: F2, F3, B3

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F4.1 | 登录页面 | app/(auth)/login/page.tsx | 表单验证 |
| F4.2 | Token管理 | stores/authStore.ts | JWT存储刷新 |
| F4.3 | 路由保护 | middleware.ts | 未登录跳转 |
| F4.4 | 权限守卫 | components/PermissionGuard.tsx | 权限控制 |

### 4.3 用户管理模块 (Week 3-4)

#### 任务 F5: 用户管理界面 (Week 3, Day 2-5)
**负责人**: 前端开发工程师(副)
**依赖**: F4, B6

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F5.1 | 用户列表 | users/page.tsx | 表格分页筛选 |
| F5.2 | 用户表单 | users/components/UserForm.tsx | 新增编辑 |
| F5.3 | 批量导入 | users/components/ImportModal.tsx | 上传进度 |
| F5.4 | 用户详情 | users/[id]/page.tsx | 详情展示 |

#### 任务 F6: 用户注册界面 (Week 4, Day 1-2)
**负责人**: 前端开发工程师(副)
**依赖**: F5, B8

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F6.1 | 注册页面 | (auth)/register/page.tsx | 三步流程 |
| F6.2 | 邮箱验证 | (auth)/verify/page.tsx | 验证页面 |

### 4.4 部门管理模块 (Week 4-5)

#### 任务 F7: 部门管理界面 (Week 4, Day 3-5, Week 5 Day 1-2)
**负责人**: 前端开发工程师(主)
**依赖**: F2, B9

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F7.1 | 部门树组件 | departments/components/DeptTree.tsx | 树形展示 |
| F7.2 | 部门表单 | departments/components/DeptForm.tsx | 新增编辑 |
| F7.3 | 拖拽排序 | departments/hooks/useDragDrop.ts | 拖拽调整 |
| F7.4 | 部门列表 | departments/page.tsx | 主页面 |

### 4.5 角色权限管理模块 (Week 5-6)

#### 任务 F8: 角色管理界面 (Week 5, Day 3-5)
**负责人**: 前端开发工程师(副)
**依赖**: F7, B11

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F8.1 | 角色列表 | roles/page.tsx | 表格展示 |
| F8.2 | 角色表单 | roles/components/RoleForm.tsx | 权限分配 |
| F8.3 | 权限树 | roles/components/PermissionTree.tsx | 权限选择 |
| F8.4 | 数据范围 | roles/components/DataScopeSelect.tsx | 范围选择 |

#### 任务 F9: 权限配置界面 (Week 6, Day 1-2)
**负责人**: 前端开发工程师(副)
**依赖**: F8, B12

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F9.1 | 权限列表 | permissions/page.tsx | 权限管理 |
| F9.2 | 前端权限控制 | hooks/usePermission.ts | 按钮级控制 |

### 4.6 审计日志与系统配置模块 (Week 6-7)

#### 任务 F10: 审计日志界面 (Week 6, Day 3-4)
**负责人**: 前端开发工程师(主)
**依赖**: F2, B13

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F10.1 | 日志列表 | audit-logs/page.tsx | 筛选导出 |
| F10.2 | 日志详情 | audit-logs/components/LogDetail.tsx | 详情弹窗 |

#### 任务 F11: 系统配置界面 (Week 6, Day 5, Week 7 Day 1)
**负责人**: 前端开发工程师(主)
**依赖**: F2, B14

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F11.1 | 配置列表 | settings/configs/page.tsx | 分类展示 |
| F11.2 | 配置编辑 | settings/configs/components/ConfigForm.tsx | 动态表单 |
| F11.3 | 配置历史 | settings/configs/history/page.tsx | 历史版本 |

### 4.7 仪表板与优化 (Week 7-8)

#### 任务 F12: 仪表板与导航 (Week 7, Day 2-3)
**负责人**: 前端开发工程师(副)
**依赖**: F4-F11

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F12.1 | 仪表板 | (dashboard)/page.tsx | 数据概览 |
| F12.2 | 动态菜单 | components/DynamicMenu.tsx | 权限菜单 |
| F12.3 | 面包屑导航 | components/Breadcrumb.tsx | 路径导航 |

#### 任务 F13: 性能优化 (Week 7, Day 4-5, Week 8)
**负责人**: 前端开发工程师(主+副)
**依赖**: F12

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| F13.1 | 代码分割 | next.config.js优化 | 按需加载 |
| F13.2 | 图片优化 | Image组件配置 | WebP格式 |
| F13.3 | 缓存策略 | API缓存配置 | SWR/React Query |
| F13.4 | 构建优化 | bundle分析 | 体积<500KB |

---

## 5. DevOps工程师任务

### 5.1 基础设施搭建 (Week 1-2)

#### 任务 V1: Docker环境配置 (Week 1, Day 3-5)
**负责人**: DevOps工程师
**依赖**: B1, F1

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| V1.1 | Docker Compose | docker-compose.yml | 本地开发环境 |
| V1.2 | 服务容器化 | Dockerfile | 前后端镜像 |
| V1.3 | 网络配置 | docker network | 服务间通信 |

**Docker Compose服务**:
- PostgreSQL (数据库)
- Redis (缓存)
- Kafka (消息队列，可选)
- Backend (Spring Boot)
- Frontend (Next.js)

### 5.2 CI/CD流水线 (Week 2-3)

#### 任务 V2: CI/CD配置 (Week 2, Day 1-5)
**负责人**: DevOps工程师
**依赖**: V1

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| V2.1 | GitHub Actions | .github/workflows/*.yml | 自动化流水线 |
| V2.2 | 构建流程 | build.yml | 编译打包 |
| V2.3 | 测试流程 | test.yml | 自动化测试 |
| V2.4 | 部署流程 | deploy.yml | 多环境部署 |

### 5.3 Kubernetes部署 (Week 9-10)

#### 任务 V3: K8s配置 (Week 9, Day 1-3)
**负责人**: DevOps工程师
**依赖**: V2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| V3.1 | Deployment | k8s/*-deployment.yaml | 应用部署 |
| V3.2 | Service | k8s/*-service.yaml | 服务暴露 |
| V3.3 | ConfigMap/Secret | k8s/config.yaml | 配置管理 |
| V3.4 | Ingress | k8s/ingress.yaml | 路由配置 |

#### 任务 V4: 监控告警 (Week 9, Day 4-5, Week 10)
**负责人**: DevOps工程师
**依赖**: V3

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| V4.1 | Prometheus | prometheus.yml | 指标收集 |
| V4.2 | Grafana | dashboards/*.json | 可视化面板 |
| V4.3 | 告警规则 | alertmanager/*.yml | 告警配置 |
| V4.4 | 日志聚合 | ELK/Fluentd配置 | 日志收集 |

---

## 6. 测试工程师任务

### 6.1 测试计划与设计 (Week 2-3)

#### 任务 T1: 测试计划 (Week 2, Day 1-2)
**负责人**: 测试工程师
**依赖**: 需求文档

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| T1.1 | 测试计划 | TEST_PLAN.md | 测试策略 |
| T1.2 | 测试用例 | 按模块分类 | 覆盖所有功能 |
| T1.3 | 测试数据 | SQL脚本 | 测试数据准备 |

### 6.2 自动化测试 (Week 3-8)

#### 任务 T2: 后端测试 (Week 3-5)
**负责人**: 测试工程师
**依赖**: T1

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| T2.1 | 单元测试 | *Test.java | 覆盖率≥85% |
| T2.2 | 集成测试 | *IntegrationTest.java | 关键流程覆盖 |
| T2.3 | API测试 | Postman/Newman | 100%接口覆盖 |

#### 任务 T3: 前端测试 (Week 5-7)
**负责人**: 测试工程师
**依赖**: T2

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| T3.1 | 组件测试 | *.test.tsx | 关键组件覆盖 |
| T3.2 | E2E测试 | Playwright | 核心流程覆盖 |
| T3.3 | 视觉测试 | Storybook | UI组件文档 |

### 6.3 性能与安全测试 (Week 8-9)

#### 任务 T4: 性能测试 (Week 8)
**负责人**: 测试工程师
**依赖**: T2, T3

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| T4.1 | 压力测试 | k6脚本 | 10,000 TPS |
| T4.2 | 负载测试 | JMeter/k6 | 并发测试 |
| T4.3 | 性能报告 | 性能测试报告 | 达标确认 |

#### 任务 T5: 安全测试 (Week 9)
**负责人**: 测试工程师
**依赖**: T4

| 子任务 | 描述 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| T5.1 | 渗透测试 | OWASP ZAP | 无高危漏洞 |
| T5.2 | 代码审计 | SonarQube | 无blocker |
| T5.3 | 安全报告 | 安全测试报告 | 通过审核 |

---

## 7. 里程碑时间线

### 7.1 甘特图

```
角色          Week1  Week2  Week3  Week4  Week5  Week6  Week7  Week8  Week9  Week10
数据库设计    [D1-D4]
后端开发             [B1-B5][B6-B8][B9-B10][B11-B12][B13-B14][    B15    ]
前端开发             [F1-F4][F5-F7][F8-F11][F12-F13]
DevOps                      [V1-V2]               [      V3-V4      ]
测试工程师                  [T1-T2][  T3   ][ T4  ][ T5 ]
```

### 7.2 里程碑交付

| 里程碑 | 时间 | 数据库 | 后端 | 前端 | DevOps | 测试 |
|--------|------|--------|------|------|--------|------|
| **M1 基础架构** | W1-W2 | D1-D4完成 | B1-B2完成 | F1-F3完成 | V1完成 | T1完成 |
| **M2 认证授权** | W2-W3 | - | B3-B5完成 | F4完成 | V2完成 | T2开始 |
| **M3 用户管理** | W3-W4 | - | B6-B8完成 | F5-F6完成 | - | T2继续 |
| **M4 部门管理** | W4-W6 | - | B9-B10完成 | F7完成 | - | T2完成 |
| **M5 角色权限** | W5-W7 | - | B11-B12完成 | F8-F9完成 | - | T3开始 |
| **M6 审计配置** | W6-W8 | - | B13-B14完成 | F10-F11完成 | - | T3完成 |
| **M7 仪表板** | W7-W8 | - | - | F12-F13完成 | - | - |
| **M8 性能优化** | W8-W9 | - | B15完成 | F13完成 | - | T4完成 |
| **M9 K8s部署** | W9-W10 | - | - | - | V3-V4完成 | - |
| **M10 安全测试** | W9-W10 | - | - | - | - | T5完成 |

**预计上线日期**: Week 10 (2026-06-08)

---

## 8. 依赖关系矩阵

### 8.1 跨角色依赖

| 依赖方 | 被依赖方 | 依赖内容 | 时间 |
|--------|----------|----------|------|
| 后端 | 数据库 | 表结构设计完成 | Week 1 |
| 前端 | 后端 | API接口定义完成 | Week 2 |
| 测试 | 后端 | 功能开发完成 | Week 3+ |
| DevOps | 前后端 | Dockerfile完成 | Week 1-2 |
| 后端 | DevOps | 环境配置完成 | Week 2 |

### 8.2 关键路径

```
数据库设计(D) -> 后端开发(B) -> 前端开发(F) -> 测试(T) -> 上线
       |             |             |            |
       +-------------> DevOps(V) -->+------------+
```

---

## 9. 沟通计划

### 9.1 例行会议

| 会议 | 频率 | 参与者 | 目的 |
|------|------|--------|------|
| 每日站会 | 每日 | 全体成员 | 进度同步，阻塞问题 |
| 后端评审 | 每周二 | 后端+DB | 代码评审，技术决策 |
| 前端评审 | 每周四 | 前端团队 | 组件评审，UI确认 |
| 集成评审 | 每周五 | 全体成员 | 集成问题，里程碑检查 |

### 9.2 文档协作

| 文档类型 | 负责人 | 更新频率 |
|----------|--------|----------|
| API文档 | 后端 | 随开发更新 |
| UI组件文档 | 前端 | 组件完成时 |
| 部署文档 | DevOps | 部署配置变更时 |
| 测试报告 | 测试 | 每周汇总 |

---

## 10. 成功标准

### 10.1 功能标准
- [ ] 所有FRD功能需求实现
- [ ] 用户验收测试通过率 ≥ 95%
- [ ] 生产环境无P0/P1缺陷

### 10.2 性能标准
- [ ] 登录接口响应时间 < 100ms (P95)
- [ ] 支持10,000 TPS登录
- [ ] API平均响应时间 < 200ms (P95)

### 10.3 质量标准
- [ ] 后端测试覆盖率 ≥ 85%
- [ ] 前端测试覆盖率 ≥ 80%
- [ ] 安全扫描无高危漏洞
- [ ] 系统可用性 ≥ 99.9%

---

## 11. 附录

### 11.1 参考文档

| 文档 | 路径 |
|------|------|
| 系统架构 | `prompts/architecture/SYSTEM_ARCHITECTURE.md` |
| 后端架构 | `prompts/architecture/BACKEND_ARCHITECTURE.md` |
| 前端架构 | `prompts/architecture/FRONTEND_ARCHITECTURE.md` |
| 用户故事 | `prompts/requirements/USER_STORIES.md` |
| 非功能需求 | `prompts/requirements/NON_FUNCTIONAL_REQUIREMENTS.md` |

### 11.2 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 2.0 | 2026-03-28 | 系统架构师 | 按角色重新划分任务，明确各角色职责和依赖关系 |
