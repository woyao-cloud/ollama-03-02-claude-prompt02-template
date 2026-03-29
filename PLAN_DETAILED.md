# 全栈用户管理系统 - 详细实施计划

**版本**: 3.0
**日期**: 2026-03-29
**状态**: 待执行

---

## 目录

1. [执行策略](#1-执行策略)
2. [阶段1: 基础架构搭建 (Week 1-2)](#阶段1-基础架构搭建)
3. [阶段2: 认证授权模块 (Week 2-3)](#阶段2-认证授权模块)
4. [阶段3: 用户管理模块 (Week 3-4)](#阶段3-用户管理模块)
5. [阶段4: 部门管理模块 (Week 4-6)](#阶段4-部门管理模块)
6. [阶段5: 角色权限管理 (Week 5-7)](#阶段5-角色权限管理)
7. [阶段6: 审计日志与配置 (Week 6-8)](#阶段6-审计日志与配置)
8. [阶段7: 性能优化 (Week 8-9)](#阶段7-性能优化)
9. [阶段8: 部署与上线 (Week 9-10)](#阶段8-部署与上线)
10. [风险缓解计划](#风险缓解计划)

---

## 1. 执行策略

### 1.1 开发模式
- **敏捷迭代**: 2周一个冲刺
- **每日站会**: 同步进度和阻塞问题
- **代码评审**: 所有PR必须评审通过
- **持续集成**: 每次提交触发CI/CD

### 1.2 分支策略
```
main (保护分支)
  └── develop (开发分支)
        ├── feature/M1-database-design
        ├── feature/M1-backend-skeleton
        ├── feature/M1-frontend-skeleton
        └── feature/M2-authentication
```

### 1.3 提交流范
1. 从 `develop` 创建功能分支
2. 开发完成后提交PR到 `develop`
3. PR必须通过：CI检查 + 代码评审 + 测试通过
4.  squash merge 到 `develop`
5. 里程碑完成后 merge 到 `main`

---

## 阶段1: 基础架构搭建

**时间**: Week 1-2 (2026-03-30 ~ 2026-04-12)
**目标**: 建立项目基础结构，数据库设计，开发环境就绪
**成功标准**: 前后端项目可运行，数据库迁移成功，API可访问

### 任务清单

#### Week 1

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | DB | D1.1 | 用户表设计 | 4h | - | DDL完成，包含审计字段 |
| D1 | DB | D1.2 | 角色权限表设计 | 4h | D1.1 | RBAC三表结构完成 |
| D2 | DB | D1.3 | 部门表设计(Materialized Path) | 6h | D1.2 | 支持5级层级 |
| D2 | BE | B1.1 | Spring Boot项目初始化 | 4h | - | 多模块结构 |
| D3 | DB | D2.1 | 系统配置表设计 | 4h | D1.3 | 支持动态配置 |
| D3 | DB | D2.2 | 审计日志表设计 | 4h | D2.1 | 支持操作日志 |
| D3 | BE | B1.2 | 配置文件设置(multi-env) | 4h | B1.1 | dev/test/prod配置 |
| D4 | BE | B2.1 | 用户实体开发 | 6h | B1.2, D1.1 | JPA注解正确 |
| D4 | FE | F1.1 | Next.js项目初始化 | 4h | - | TypeScript配置 |
| D5 | BE | B2.2 | 部门实体开发 | 6h | B2.1, D1.3 | Materialized Path实现 |
| D5 | FE | F1.2 | 依赖安装(shadcn/ui等) | 4h | F1.1 | package.json完成 |

#### Week 2

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | DB | D3.1 | V1__Initial_schema.sql | 6h | D2.2 | 迁移脚本完成 |
| D1 | BE | B2.3 | 角色权限实体开发 | 6h | B2.2, D1.2 | RBAC实体完成 |
| D2 | DB | D3.2 | V2__Add_roles_permissions.sql | 4h | D3.1 | 权限表迁移完成 |
| D2 | DB | D3.3 | V3__Add_audit_tables.sql | 4h | D3.2 | 审计表迁移完成 |
| D2 | BE | B2.4 | Repository接口定义 | 4h | B2.3 | 所有Repository完成 |
| D3 | FE | F2.1 | 布局组件开发 | 6h | F1.2 | Layout/Sidebar/Header |
| D3 | FE | F2.2 | 通用UI组件集成 | 6h | F2.1 | shadcn组件可用 |
| D4 | FE | F3.1 | Axios配置 | 4h | F2.2 | 拦截器配置完成 |
| D4 | FE | F3.2 | API客户端方法 | 6h | F3.1 | 按模块组织 |
| D5 | DO | V1.1 | Docker Compose配置 | 8h | B2.4, F3.2 | 本地环境可启动 |
| D5 | TE | T1.1 | 测试计划编写 | 6h | - | TEST_PLAN.md完成 |

### Week 2 结束检查清单

- [ ] 后端应用 `java -jar backend.jar` 成功启动
- [ ] 前端应用 `npm run dev` 成功运行
- [ ] Flyway迁移 `mvn flyway:migrate` 成功执行
- [ ] Docker Compose `docker-compose up` 所有服务启动
- [ ] 健康检查端点 `GET /actuator/health` 返回UP

### 交付物

1. `backend/` - Spring Boot项目
2. `frontend/` - Next.js项目
3. `docker-compose.yml` - 本地开发环境
4. `backend/src/main/resources/db/migration/` - Flyway迁移脚本
5. `docs/DEVELOPMENT_SETUP.md` - 开发环境文档

---

## 阶段2: 认证授权模块

**时间**: Week 2-3 (2026-04-13 ~ 2026-04-19)
**目标**: 实现JWT认证、权限控制、密码策略
**成功标准**: 登录/登出功能正常，权限控制生效，密码策略验证通过

### 任务清单

#### Week 3

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | BE | B3.1 | JWT TokenProvider实现 | 6h | B2.4 | 生成/验证Token |
| D1 | BE | B3.2 | JWT认证过滤器 | 6h | B3.1 | 过滤器链集成 |
| D2 | BE | B3.3 | SecurityConfig配置 | 6h | B3.2 | Spring Security配置 |
| D2 | BE | B3.4 | AuthController(登录/刷新) | 6h | B3.3 | REST端点实现 |
| D3 | BE | B4.1 | PermissionEvaluator实现 | 6h | B3.4 | SpEL表达式支持 |
| D3 | BE | B4.2 | 方法级权限注解(@PreAuthorize) | 6h | B4.1 | 注解权限控制 |
| D4 | BE | B4.3 | DataPermissionInterceptor | 8h | B4.2 | 数据范围过滤 |
| D4 | BE | B5.1 | BCrypt密码加密配置 | 4h | B4.3 | 强度因子12 |
| D5 | BE | B5.2 | 账户锁定服务 | 6h | B5.1 | 5次失败锁定15分钟 |
| D5 | BE | B5.3 | 密码策略验证器 | 6h | B5.2 | 复杂度验证实现 |
| D5 | FE | F4.1 | 登录页面开发 | 8h | F3.2, B3.4 | 表单验证完成 |
| D5 | FE | F4.2 | Token管理Store | 4h | F4.1 | JWT存储刷新机制 |

### Week 3 结束检查清单

- [ ] POST `/api/auth/login` 返回JWT Token
- [ ] POST `/api/auth/refresh` 刷新Token成功
- [ ] 未认证请求返回401
- [ ] @PreAuthorize("hasRole('ADMIN')") 权限控制生效
- [ ] 5次错误密码后账户锁定15分钟
- [ ] 登录页面功能正常，错误提示清晰

### 交付物

1. `backend/src/.../security/` - 安全配置
2. `backend/src/.../web/controller/AuthController.java`
3. `frontend/app/(auth)/login/page.tsx`
4. `frontend/stores/authStore.ts`

---

## 阶段3: 用户管理模块

**时间**: Week 3-4 (2026-04-20 ~ 2026-05-03)
**目标**: 实现用户CRUD、批量导入、用户自助注册
**成功标准**: 用户管理功能完整，批量导入<5分钟/1000用户

### 任务清单

#### Week 4

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | BE | B6.1 | UserService开发 | 8h | B5.3 | CRUD方法完整 |
| D1 | BE | B6.2 | UserController REST API | 8h | B6.1 | CRUD端点实现 |
| D2 | BE | B6.3 | UserDTO与验证注解 | 6h | B6.2 | 字段验证完整 |
| D2 | BE | B7.1 | 批量导入Excel/CSV | 8h | B6.3 | 支持1000用户<5分钟 |
| D3 | BE | B7.2 | 批量导出功能 | 6h | B7.1 | 大数据量导出 |
| D3 | BE | B7.3 | 导入验证与错误报告 | 6h | B7.2 | 数据验证完整 |
| D4 | FE | F5.1 | 用户列表页面 | 8h | F4.2, B6.2 | 表格分页筛选 |
| D4 | FE | F5.2 | 用户表单组件 | 8h | F5.1 | 新增编辑功能 |
| D5 | FE | F5.3 | 批量导入Modal | 6h | F5.2, B7.1 | 上传进度显示 |
| D5 | FE | F5.4 | 用户详情页面 | 6h | F5.3 | 详情展示 |
| D5 | BE | B8.1 | 用户注册服务 | 8h | B6.3 | 自助注册流程 |

#### Week 5 (前半周)

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | BE | B8.2 | 邮箱验证服务 | 6h | B8.1 | 验证邮件发送 |
| D1 | BE | B8.3 | 审批流程服务 | 6h | B8.2 | 待审批状态管理 |
| D2 | FE | F6.1 | 注册页面(三步流程) | 8h | F5.4, B8.1 | 注册流程完成 |
| D2 | FE | F6.2 | 邮箱验证页面 | 6h | F6.1, B8.2 | 验证页面完成 |

### Week 5 前半周结束检查清单

- [ ] GET `/api/users` 分页查询用户
- [ ] POST `/api/users` 创建用户
- [ ] PUT `/api/users/{id}` 更新用户
- [ ] DELETE `/api/users/{id}` 删除用户
- [ ] POST `/api/users/import` 批量导入
- [ ] POST `/api/users/export` 批量导出
- [ ] POST `/api/auth/register` 用户注册
- [ ] 用户列表页面功能完整
- [ ] 注册流程三步完成

### 交付物

1. `backend/src/.../service/UserService.java`
2. `backend/src/.../web/controller/UserController.java`
3. `frontend/app/(dashboard)/users/page.tsx`
4. `frontend/app/(auth)/register/page.tsx`

---

## 阶段4: 部门管理模块

**时间**: Week 4-6 (2026-05-04 ~ 2026-05-17)
**目标**: 实现五级部门树形结构管理
**成功标准**: 部门树查询<100ms(10万部门)，支持拖拽调整

### 任务清单

#### Week 5 (后半周)

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D3 | BE | B9.1 | DepartmentService开发 | 8h | B6.3 | CRUD + 树形操作 |
| D4 | BE | B9.2 | 树形查询算法 | 8h | B9.1 | 物化路径查询 |
| D4 | BE | B9.3 | 层级调整服务 | 8h | B9.2 | 防循环依赖检查 |
| D5 | BE | B9.4 | DepartmentController | 6h | B9.3 | REST API完成 |

#### Week 6

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | BE | B10.1 | Redis部门树缓存 | 6h | B9.4 | 部门树缓存 |
| D1 | BE | B10.2 | 缓存失效监听 | 6h | B10.1 | 变更时自动失效 |
| D2 | FE | F7.1 | 部门树组件开发 | 8h | F5.4, B9.4 | 树形展示 |
| D2 | FE | F7.2 | 部门表单组件 | 8h | F7.1 | 新增编辑功能 |
| D3 | FE | F7.3 | 拖拽排序功能 | 8h | F7.2 | 拖拽调整层级 |
| D3 | FE | F7.4 | 部门列表页面 | 6h | F7.3 | 主页面完成 |
| D4 | TE | T2.1 | 后端单元测试 | 8h | B10.2 | 覆盖率≥85% |
| D4 | TE | T2.2 | 集成测试 | 6h | T2.1 | 关键流程覆盖 |
| D5 | TE | T2.3 | API测试(Postman) | 8h | T2.2 | 100%接口覆盖 |

### Week 6 结束检查清单

- [ ] GET `/api/departments/tree` 返回完整树
- [ ] POST `/api/departments` 创建部门
- [ ] PUT `/api/departments/{id}/move` 调整层级
- [ ] 部门树查询性能 < 100ms (10万部门)
- [ ] 部门树Redis缓存生效
- [ ] 部门树形展示功能正常
- [ ] 拖拽调整层级功能正常

### 交付物

1. `backend/src/.../service/DepartmentService.java`
2. `backend/src/.../infrastructure/cache/DepartmentCache.java`
3. `frontend/app/(dashboard)/departments/page.tsx`
4. `frontend/components/DeptTree.tsx`

---

## 阶段5: 角色权限管理

**时间**: Week 5-7 (2026-05-11 ~ 2026-05-24)
**目标**: 实现角色管理、权限分配、数据权限控制
**成功标准**: RBAC权限生效，数据权限过滤正常

### 任务清单

#### Week 6-7

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| W6-D5 | BE | B11.1 | RoleService开发 | 8h | B9.4 | CRUD + 权限分配 |
| W7-D1 | BE | B11.2 | 角色继承服务 | 6h | B11.1 | 权限继承计算 |
| W7-D1 | BE | B11.3 | PermissionService | 6h | B11.2 | 权限管理 |
| W7-D2 | BE | B11.4 | Role/PermissionController | 6h | B11.3 | REST API完成 |
| W7-D2 | BE | B12.1 | DataScope枚举定义 | 4h | B11.4 | ALL/DEPT/SELF/CUSTOM |
| W7-D3 | BE | B12.2 | 数据权限AOP拦截 | 8h | B12.1 | AOP拦截实现 |
| W7-D3 | BE | B12.3 | 自定义条件解析器 | 6h | B12.2 | 复杂条件支持 |
| W7-D4 | FE | F8.1 | 角色列表页面 | 6h | F7.4, B11.4 | 表格展示 |
| W7-D4 | FE | F8.2 | 角色表单组件 | 8h | F8.1 | 权限分配 |
| W7-D5 | FE | F8.3 | 权限树组件 | 8h | F8.2 | 权限选择 |
| W7-D5 | FE | F8.4 | 数据范围选择器 | 6h | F8.3, B12.1 | 范围选择 |
| W7-D5 | FE | F9.1 | 权限列表页面 | 6h | F8.4 | 权限管理 |
| W7-D5 | FE | F9.2 | 前端权限控制Hook | 6h | F9.1 | 按钮级控制 |

### Week 7 结束检查清单

- [ ] GET `/api/roles` 角色列表
- [ ] POST `/api/roles` 创建角色
- [ ] PUT `/api/roles/{id}/permissions` 分配权限
- [ ] GET `/api/permissions` 权限列表
- [ ] 数据权限拦截器生效
- [ ] 角色管理页面功能完整
- [ ] 权限树组件功能正常
- [ ] usePermission Hook可用

### 交付物

1. `backend/src/.../service/RoleService.java`
2. `backend/src/.../security/DataPermissionInterceptor.java`
3. `frontend/app/(dashboard)/roles/page.tsx`
4. `frontend/hooks/usePermission.ts`

---

## 阶段6: 审计日志与配置

**时间**: Week 6-8 (2026-05-18 ~ 2026-05-31)
**目标**: 实现操作日志记录和查询，动态配置管理
**成功标准**: 审计日志自动记录，配置动态刷新

### 任务清单

#### Week 8

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | BE | B13.1 | AuditLogService开发 | 6h | B12.3 | 日志记录查询 |
| D1 | BE | B13.2 | AOP日志拦截器 | 6h | B13.1 | 自动记录操作 |
| D2 | BE | B13.3 | 异步日志写入(Kafka) | 6h | B13.2 | Kafka异步写入 |
| D2 | BE | B13.4 | AuditController | 6h | B13.3 | 日志查询端点 |
| D3 | BE | B14.1 | ConfigService开发 | 6h | B13.4 | 配置CRUD |
| D3 | BE | B14.2 | @RefreshScope配置刷新 | 6h | B14.1 | 运行时刷新 |
| D4 | BE | B14.3 | 配置多级缓存 | 6h | B14.2 | Caffeine + Redis |
| D4 | BE | B14.4 | ConfigController | 6h | B14.3 | 配置管理端点 |
| D5 | FE | F10.1 | 审计日志列表页面 | 8h | F9.2, B13.4 | 筛选导出 |
| D5 | FE | F10.2 | 日志详情组件 | 6h | F10.1 | 详情弹窗 |

#### Week 9 (前半周)

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | FE | F11.1 | 配置列表页面 | 6h | F10.2, B14.4 | 分类展示 |
| D1 | FE | F11.2 | 配置编辑表单 | 8h | F11.1 | 动态表单 |
| D2 | FE | F11.3 | 配置历史页面 | 6h | F11.2 | 历史版本 |
| D2 | FE | F12.1 | 仪表板页面 | 8h | F11.3 | 数据概览 |
| D2 | FE | F12.2 | 动态菜单组件 | 8h | F12.1 | 权限菜单 |

### Week 9 前半周结束检查清单

- [ ] GET `/api/audit-logs` 日志查询
- [ ] 敏感操作自动记录审计日志
- [ ] GET `/api/configs` 配置查询
- [ ] PUT `/api/configs/{key}` 配置更新
- [ ] 配置变更后@RefreshScope刷新
- [ ] 审计日志页面功能完整
- [ ] 配置管理页面功能完整
- [ ] 仪表板页面展示正常

### 交付物

1. `backend/src/.../service/AuditLogService.java`
2. `backend/src/.../infrastructure/audit/AuditAspect.java`
3. `backend/src/.../service/ConfigService.java`
4. `frontend/app/(dashboard)/audit-logs/page.tsx`
5. `frontend/app/(dashboard)/settings/configs/page.tsx`
6. `frontend/app/(dashboard)/page.tsx` (仪表板)

---

## 阶段7: 性能优化

**时间**: Week 8-9 (2026-06-01 ~ 2026-06-14)
**目标**: 实现10,000 TPS登录性能
**成功标准**: 登录接口<100ms P95，10,000 TPS达标

### 任务清单

#### Week 9 (后半周) - Week 10 (前半周)

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| W9-D3 | BE | B15.1 | Redis Pipeline优化 | 6h | B14.4 | 批量操作 |
| W9-D3 | BE | B15.2 | 异步审计日志优化 | 6h | B15.1 | Kafka异步 |
| W9-D4 | BE | B15.3 | JWT生成优化 | 6h | B15.2 | 算法优化 |
| W9-D4 | BE | B15.4 | 数据库连接池调优 | 6h | B15.3 | HikariCP优化 |
| W9-D5 | BE | B15.5 | 关键索引优化 | 6h | B15.4 | 索引优化完成 |
| W9-D5 | BE | B15.6 | 查询语句优化 | 6h | B15.5 | SQL优化 |
| W10-D1 | BE | B15.7 | 多级缓存架构 | 6h | B15.6 | Caffeine + Redis |
| W10-D1 | BE | B15.8 | 缓存预热机制 | 6h | B15.7 | 启动预热 |
| W10-D2 | BE | B15.9 | G1GC参数调优 | 6h | B15.8 | JVM优化 |
| W10-D2 | BE | B15.10 | 虚拟线程启用 | 4h | B15.9 | JDK21特性 |
| W10-D3 | FE | F13.1 | 代码分割优化 | 6h | F12.2 | 按需加载 |
| W10-D3 | FE | F13.2 | 图片优化配置 | 6h | F13.1 | WebP格式 |
| W10-D4 | TE | T4.1 | k6压力测试脚本 | 8h | B15.10 | 性能测试脚本 |
| W10-D4 | TE | T4.2 | 压力测试执行 | 8h | T4.1 | 10,000 TPS测试 |
| W10-D5 | TE | T4.3 | 性能报告生成 | 6h | T4.2 | 性能报告完成 |

### Week 10 前半周结束检查清单

- [ ] 登录接口响应时间 < 100ms (P95)
- [ ] 系统支持10,000 TPS登录
- [ ] Redis Pipeline批量操作生效
- [ ] 虚拟线程已启用
- [ ] G1GC参数调优完成
- [ ] 前端bundle体积 < 500KB
- [ ] 性能测试报告达标

### 性能指标

| 指标 | 目标值 | 测试工具 |
|------|--------|----------|
| 登录接口P95 | < 100ms | k6 |
| 登录吞吐量 | ≥ 10,000 TPS | k6 |
| API平均P95 | < 200ms | k6 |
| 部门树查询 | < 100ms | JMeter |
| 配置读取 | < 10ms | 应用日志 |

### 交付物

1. `backend/src/main/resources/application-performance.yml`
2. `frontend/next.config.js` (优化配置)
3. `tests/performance/k6-login.js`
4. `docs/PERFORMANCE_REPORT.md`

---

## 阶段8: 部署与上线

**时间**: Week 9-10 (2026-06-15 ~ 2026-06-21)
**目标**: 完成K8s部署配置，安全测试通过，生产环境就绪
**成功标准**: 生产环境部署成功，安全扫描无高危漏洞

### 任务清单

#### Week 10 (后半周)

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | DO | V3.1 | K8s Deployment配置 | 8h | B15.10 | 应用部署完成 |
| D1 | DO | V3.2 | K8s Service配置 | 6h | V3.1 | 服务暴露完成 |
| D2 | DO | V3.3 | ConfigMap/Secret配置 | 6h | V3.2 | 配置管理完成 |
| D2 | DO | V3.4 | Ingress配置 | 6h | V3.3 | 路由配置完成 |
| D3 | DO | V4.1 | Prometheus配置 | 6h | V3.4 | 指标收集完成 |
| D3 | DO | V4.2 | Grafana仪表板配置 | 6h | V4.1 | 可视化面板 |
| D4 | DO | V4.3 | 告警规则配置 | 6h | V4.2 | Alertmanager |
| D4 | DO | V4.4 | 日志聚合配置(ELK) | 6h | V4.3 | 日志收集完成 |
| D5 | TE | T5.1 | OWASP ZAP安全扫描 | 8h | V4.4 | 渗透测试 |
| D5 | TE | T5.2 | SonarQube代码审计 | 6h | T5.1 | 无blocker |

#### Week 11 (上线周)

| 天数 | 角色 | 任务ID | 任务名称 | 预估工时 | 依赖 | 验收标准 |
|------|------|--------|----------|----------|------|----------|
| D1 | DO | V5.1 | 生产数据迁移脚本 | 6h | T5.2 | 迁移脚本完成 |
| D1 | DO | V5.2 | 数据验证与回滚计划 | 6h | V5.1 | 验证计划完成 |
| D2 | DO | V5.3 | 部署流程演练 | 6h | V5.2 | 演练成功 |
| D2 | DO | V5.4 | 故障切换演练 | 6h | V5.3 | 演练成功 |
| D3 | ALL | V6.1 | 用户操作手册编写 | 6h | - | 手册完成 |
| D3 | ALL | V6.2 | 运维手册编写 | 6h | V6.1 | 手册完成 |
| D4 | ALL | V6.3 | API文档生成 | 6h | V6.2 | OpenAPI文档 |
| D4 | ALL | V6.4 | 上线前评审 | 4h | V6.3 | 评审通过 |
| D5 | DO | V7.1 | 正式上线部署 | 8h | V6.4 | 部署成功 |
| D5 | ALL | V7.2 | 上线后监控 | 4h | V7.1 | 监控正常 |

### Week 11 结束检查清单

- [ ] K8s集群部署成功
- [ ] Prometheus + Grafana监控就绪
- [ ] ELK日志聚合运行正常
- [ ] OWASP ZAP无高危漏洞
- [ ] SonarQube无blocker问题
- [ ] 数据迁移脚本验证通过
- [ ] 用户手册、运维手册完成
- [ ] API文档(OpenAPI)生成完成
- [ ] 生产环境部署成功
- [ ] 上线后监控正常

### 交付物

1. `k8s/*-deployment.yaml` - K8s部署配置
2. `k8s/*-service.yaml` - K8s服务配置
3. `k8s/configmap.yaml` - ConfigMap配置
4. `k8s/ingress.yaml` - Ingress配置
5. `monitoring/prometheus.yml` - Prometheus配置
6. `monitoring/grafana-dashboards/` - Grafana仪表板
7. `docs/USER_MANUAL.md` - 用户手册
8. `docs/OPERATIONS_MANUAL.md` - 运维手册
9. `docs/API_DOCUMENTATION.md` - API文档

---

## 风险缓解计划

### 高风险项

| 风险 | 可能性 | 影响 | 缓解措施 | 负责人 | 触发条件 |
|------|--------|------|----------|--------|----------|
| 10,000 TPS不达标 | 中 | 高 | Week 8预留优化时间，提前压测 | BE Lead | Week 9压测未达标 |
| 部门树性能问题 | 中 | 中 | Materialized Path + Redis缓存 | DB/BE | 查询>100ms |
| 数据权限实现复杂 | 高 | 中 | 简化CUSTOM范围，先实现ALL/DEPT/SELF | BE | Week 7未完成 |
| 人员变动 | 低 | 高 | 文档完善，知识共享，交叉培训 | PM | 人员请假>3天 |
| 需求变更 | 高 | 中 | 每周需求评审，变更控制流程 | PM | 新需求提出 |

### 应急预案

1. **性能不达标**:
   - 启用Redis集群
   - 增加数据库只读副本
   - 启用CDN加速静态资源
   - 降级非核心功能

2. **延期风险**:
   - 优先核心功能(MVP)
   - 延迟非必须功能到V2
   - 增加开发人员

3. **技术难题**:
   - 外部技术顾问咨询
   - 技术预研并行进行
   - 备选方案准备

---

## 质量门禁

### 每个阶段出口标准

| 阶段 | 代码覆盖率 | 测试通过率 | 安全扫描 | 性能基准 | 文档完整度 |
|------|-----------|-----------|----------|----------|-----------|
| M1 | ≥50% | 100% | - | - | ≥60% |
| M2-M5 | ≥70% | 100% | 中危以下 | - | ≥70% |
| M6-M7 | ≥80% | 100% | 中危以下 | - | ≥80% |
| M8 | ≥85% | 100% | 低危以下 | 达标 | ≥90% |
| M9-M10 | ≥85% | 100% | 无漏洞 | 达标 | 100% |

### 代码提交检查清单

- [ ] 单元测试通过
- [ ] 代码规范检查通过
- [ ] 静态分析无blocker
- [ ] PR评审通过
- [ ] 功能验证通过

---

## 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0 | 2026-03-28 | 系统架构师 | 初始版本，基础计划 |
| 2.0 | 2026-03-28 | 系统架构师 | 按角色重新划分任务 |
| 3.0 | 2026-03-29 | 系统架构师 | 详细实施计划，包含具体任务、工时、依赖 |
