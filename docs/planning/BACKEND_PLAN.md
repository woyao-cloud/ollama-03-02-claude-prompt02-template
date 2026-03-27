# Spring Boot 后端实施计划

## 项目概述

基于 BACKEND_ARCHITECTURE.md、API_SPECIFICATION.md、DATABASE_SCHEMA.md 和 AUTHENTICATION_FLOW.md 创建全栈用户管理系统的 Spring Boot 后端实现计划。

**技术栈**: Spring Boot 3.5 + JDK 21 + Spring Data JPA + Spring Security + JWT + Flyway

---

## 零、需求概述与差距分析

### 0.1 功能模块划分

| 模块 | 优先级 | 用例 |
|------|--------|------|
| **用户管理模块** | P0 | 用户 CRUD、部门管理、用户状态管理、个人资料管理、登录历史查看 |
| **角色权限模块** | P0 | 角色 CRUD、权限定义 (四级模型)、用户角色分配、数据权限范围、角色模板、角色继承 |
| **认证授权模块** | P0 | JWT 认证、密码策略、会话管理 |
| **审计日志模块** | P0 | 操作日志记录、登录日志、日志查询 |
| **系统配置模块** | P0/P1 | 安全策略配置 (P0)、邮件服务配置 (P1)、性能配置管理 (P1) |

### 0.2 关键业务规则

| 规则 ID | 描述 | 实现位置 |
|---------|------|----------|
| BR-001 | 邮箱必须全局唯一 | `UserRepository.existsByEmail()` |
| BR-002 | 用户软删除 | `deleted_at` 字段 + `@SQLRestriction` |
| BR-201 | 密码 BCrypt 加密 (强度 12) | `PasswordEncoder` Bean |
| BR-202 | 登录失败 5 次锁定 30 分钟 | `failed_login_attempts`, `locked_until` 字段 |
| BR-203 | JWT Token RSA256 签名 | `JwtService` |
| BR-104 | 权限变更后需重新登录生效 | Token 刷新时重新加载权限 |

### 0.3 需求 - 实现差距表

| 用例 ID | 用例名称 | 实现状态 | 实现文件 | 备注 |
|--------|----------|----------|----------|------|
| UC-01.01 | 用户 CRUD | 未实现 | `UserService`, `UserController` | 核心功能 |
| UC-01.05 | 部门管理 | 未实现 | `DepartmentService`, `DepartmentController` | 五级组织架构 |
| UC-01.04 | 用户状态管理 | 未实现 | `UserService` | 状态变更和锁定 |
| UC-01.06 | 用户个人资料管理 | 未实现 | `UserController` | 个人资料更新 |
| UC-01.07 | 用户登录历史查看 | 未实现 | `AuditLogService` | 登录记录查询 |
| UC-02.01 | 角色 CRUD | 未实现 | `RoleService`, `RoleController` | 角色管理 |
| UC-02.02 | 权限定义与管理 | 未实现 | `PermissionService` | 四级权限模型 |
| UC-02.03 | 用户角色分配 | 未实现 | `UserService.assignRoles()` | 用户 - 角色关联 |
| UC-02.04 | 数据权限范围 | 未实现 | `Role.dataScope` | ALL/DEPT/SELF/CUSTOM |
| UC-02.05 | 角色权限模板 | 未实现 | `PermissionTemplate` 实体 | 权限模板 |
| UC-02.06 | 角色继承管理 | 未实现 | `Role.parentRoleId` | 角色多继承 |
| UC-03.01 | JWT 认证 | 未实现 | `JwtService`, `AuthService` | JWT+Refresh Token |
| UC-03.03 | 密码策略 | 未实现 | `SecurityConfig` | 密码复杂度配置 |
| UC-03.05 | 会话管理 | 未实现 | `UserSession` 实体 | 会话查看和强制下线 |
| UC-04.01 | 操作日志记录 | 未实现 | `AuditLogService` | AOP 拦截记录 |
| UC-04.02 | 登录日志 | 未实现 | `AuditLogService` | 登录成功/失败记录 |
| UC-04.03 | 日志查询 | 未实现 | `AuditLogController` | 日志筛选查询 |
| UC-05.03 | 安全策略配置 | 未实现 | `SecurityConfigService` | 密码和登录策略 |
| UC-05.02 | 邮件服务配置 | 未实现 | `MailConfigService` | SMTP 配置和模板 |
| UC-05.04 | 性能配置管理 | 未实现 | `PerformanceConfigService` | 缓存和连接池配置 |

---

---

## 一、任务分解（按阶段）

### 阶段 1: 项目初始化与配置
**目标**: 建立 Spring Boot 基础项目结构和依赖配置

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 1.1 | 创建 Maven 项目结构 (pom.xml) | P0 | 1小时 |
| 1.2 | 配置 Spring Boot Starter 依赖 | P0 | 1小时 |
| 1.3 | 配置多环境 YAML 配置 (dev/test/prod) | P0 | 1.5小时 |
| 1.4 | 配置 Flyway 数据库迁移 | P0 | 1小时 |
| 1.5 | 配置日志和错误处理 | P1 | 1小时 |
| 1.6 | 配置 Docker 和 docker-compose | P1 | 1.5小时 |

### 阶段 2: 数据库模型与 JPA 实体
**目标**: 实现所有 JPA 实体和初始 Flyway 迁移

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 2.1 | 创建基础实体基类 (AbstractEntity) | P0 | 1小时 |
| 2.2 | 实现 User JPA 实体 | P0 | 1.5小时 |
| 2.3 | 实现 Role JPA 实体 | P0 | 1小时 |
| 2.4 | 实现 Permission JPA 实体 | P0 | 1小时 |
| 2.5 | 实现关联表 (user_roles, role_permissions) | P0 | 1小时 |
| 2.6 | 实现审计实体 (AuditLog, UserSession) | P1 | 2小时 |
| 2.7 | 创建 Flyway 初始迁移脚本 V1 | P0 | 1.5小时 |
| 2.8 | 配置数据库连接和 HikariCP 连接池 | P0 | 1小时 |

### 阶段 3: DTO 和验证定义
**目标**: 定义所有请求/响应数据 DTO (使用 Java Records)

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 3.1 | 创建通用响应 DTO (ApiResponse) | P0 | 1小时 |
| 3.2 | 定义用户相关 DTO (UserCreateRequest, UserResponse) | P0 | 2小时 |
| 3.3 | 定义认证相关 DTO (LoginRequest, TokenResponse) | P0 | 1.5小时 |
| 3.4 | 定义角色相关 DTO (RoleCreateRequest, RoleResponse) | P0 | 1.5小时 |
| 3.5 | 定义分页查询参数 DTO | P1 | 1小时 |
| 3.6 | 配置 Jakarta Validation 约束 | P1 | 1小时 |

### 阶段 4: Spring Data Repository
**目标**: 实现数据访问层

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 4.1 | 创建 UserRepository 接口 | P0 | 1.5小时 |
| 4.2 | 创建 RoleRepository 接口 | P0 | 1小时 |
| 4.3 | 创建 PermissionRepository 接口 | P0 | 1小时 |
| 4.4 | 实现自定义查询方法 | P0 | 2小时 |
| 4.5 | 配置 Repository 审计功能 | P1 | 1小时 |

### 阶段 5: 核心业务服务
**目标**: 实现业务逻辑层

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 5.1 | 实现密码哈希服务 (BCryptPasswordEncoder) | P0 | 1.5小时 |
| 5.2 | 实现 JWT 令牌服务 (NimbusJwtEncoder) | P0 | 2.5小时 |
| 5.3 | 实现 UserService (CRUD + 业务逻辑) | P0 | 3.5小时 |
| 5.4 | 实现 AuthService (登录/登出/刷新) | P0 | 3.5小时 |
| 5.5 | 实现 RoleService | P0 | 2小时 |
| 5.6 | 实现 PermissionService | P0 | 1.5小时 |
| 5.7 | 实现审计日志服务 | P1 | 2小时 |

### 阶段 6: Spring Security 配置
**目标**: 实现 JWT + OAuth2 + RBAC

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 6.1 | 配置 SecurityFilterChain | P0 | 2.5小时 |
| 6.2 | 实现 JwtAuthenticationFilter | P0 | 2.5小时 |
| 6.3 | 实现 JwtTokenProvider | P0 | 2小时 |
| 6.4 | 实现 UserDetailsServiceImpl | P0 | 1.5小时 |
| 6.5 | 配置方法级权限 (@PreAuthorize) | P0 | 2小时 |
| 6.6 | 实现账户锁定机制 (5次失败锁定) | P1 | 2小时 |
| 6.7 | 配置 CORS 和 CSRF 防护 | P0 | 1.5小时 |

### 阶段 7: REST API 控制器
**目标**: 实现 API_SPECIFICATION.md 中定义的所有端点

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 7.1 | 实现 AuthController (/auth/login, /auth/register, /auth/refresh, /auth/me) | P0 | 3.5小时 |
| 7.2 | 实现 UserController (/users, /users/{id}) | P0 | 3.5小时 |
| 7.3 | 实现 RoleController (/roles) | P0 | 2.5小时 |
| 7.4 | 实现 PermissionController (/permissions) | P0 | 1.5小时 |
| 7.5 | 实现全局异常处理 (GlobalExceptionHandler) | P0 | 2小时 |
| 7.6 | 实现健康检查端点 (/actuator/health) | P1 | 30分钟 |

### 阶段 8: 测试实现
**目标**: 实现单元测试和集成测试，覆盖率 > 85%

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 8.1 | 配置测试环境和 Testcontainers | P0 | 2.5小时 |
| 8.2 | 编写 Repository 层测试 | P0 | 2.5小时 |
| 8.3 | 编写 Service 层单元测试 | P0 | 4.5小时 |
| 8.4 | 编写 Security 层单元测试 | P0 | 3.5小时 |
| 8.5 | 编写 Controller 层集成测试 | P0 | 4.5小时 |
| 8.6 | 编写认证授权完整流程测试 | P0 | 3小时 |
| 8.7 | 配置 JaCoCo 覆盖率报告 | P0 | 1.5小时 |

### 阶段 9: 文档与优化
**目标**: 完善文档和性能优化

| 任务ID | 任务描述 | 优先级 | 预计工时 |
|--------|----------|--------|----------|
| 9.1 | 配置 SpringDoc OpenAPI | P1 | 1.5小时 |
| 9.2 | 编写 API 使用文档 | P1 | 2.5小时 |
| 9.3 | 性能优化 (数据库索引、JVM 调优) | P1 | 3.5小时 |
| 9.4 | 代码审查和重构 | P1 | 2.5小时 |

---

## 二、文件清单

### 2.1 项目配置文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/pom.xml` | Maven 依赖配置 | 1.1 |
| `backend/src/main/resources/application.yml` | 主配置 | 1.3 |
| `backend/src/main/resources/application-dev.yml` | 开发环境 | 1.3 |
| `backend/src/main/resources/application-prod.yml` | 生产环境 | 1.3 |
| `backend/Dockerfile` | 容器化配置 | 1.6 |
| `backend/docker-compose.yml` | 本地开发环境 | 1.6 |

### 2.2 数据库相关文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/src/main/java/com/usermanagement/domain/AbstractEntity.java` | JPA 基础实体 | 2.1 |
| `backend/src/main/java/com/usermanagement/domain/user/User.java` | 用户实体 | 2.2 |
| `backend/src/main/java/com/usermanagement/domain/user/Role.java` | 角色实体 | 2.3 |
| `backend/src/main/java/com/usermanagement/domain/user/Permission.java` | 权限实体 | 2.4 |
| `backend/src/main/java/com/usermanagement/repository/UserRepository.java` | 用户仓库 | 4.1 |
| `backend/src/main/java/com/usermanagement/repository/RoleRepository.java` | 角色仓库 | 4.2 |
| `backend/src/main/java/com/usermanagement/repository/PermissionRepository.java` | 权限仓库 | 4.3 |
| `backend/src/main/resources/db/migration/V1__Initial_schema.sql` | 初始迁移 | 2.7 |

### 2.3 DTO 文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/src/main/java/com/usermanagement/web/dto/ApiResponse.java` | 通用响应 | 3.1 |
| `backend/src/main/java/com/usermanagement/web/dto/UserCreateRequest.java` | 用户创建请求 | 3.2 |
| `backend/src/main/java/com/usermanagement/web/dto/UserResponse.java` | 用户响应 | 3.2 |
| `backend/src/main/java/com/usermanagement/web/dto/LoginRequest.java` | 登录请求 | 3.3 |
| `backend/src/main/java/com/usermanagement/web/dto/TokenResponse.java` | 令牌响应 | 3.3 |
| `backend/src/main/java/com/usermanagement/web/dto/PageRequest.java` | 分页请求 | 3.5 |

### 2.4 服务层文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/src/main/java/com/usermanagement/service/UserService.java` | 用户服务接口 | 5.3 |
| `backend/src/main/java/com/usermanagement/service/UserServiceImpl.java` | 用户服务实现 | 5.3 |
| `backend/src/main/java/com/usermanagement/service/AuthService.java` | 认证服务 | 5.4 |
| `backend/src/main/java/com/usermanagement/service/JwtService.java` | JWT 服务 | 5.2 |
| `backend/src/main/java/com/usermanagement/service/RoleService.java` | 角色服务 | 5.5 |
| `backend/src/main/java/com/usermanagement/service/PermissionService.java` | 权限服务 | 5.6 |

### 2.5 安全相关文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/src/main/java/com/usermanagement/config/SecurityConfig.java` | 安全配置 | 6.1 |
| `backend/src/main/java/com/usermanagement/security/JwtAuthenticationFilter.java` | JWT 过滤器 | 6.2 |
| `backend/src/main/java/com/usermanagement/security/JwtTokenProvider.java` | JWT 提供者 | 6.3 |
| `backend/src/main/java/com/usermanagement/security/UserDetailsServiceImpl.java` | 用户详情服务 | 6.4 |
| `backend/src/main/java/com/usermanagement/security/PermissionEvaluator.java` | 权限评估器 | 6.5 |

### 2.6 控制器文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/src/main/java/com/usermanagement/web/controller/AuthController.java` | 认证端点 | 7.1 |
| `backend/src/main/java/com/usermanagement/web/controller/UserController.java` | 用户端点 | 7.2 |
| `backend/src/main/java/com/usermanagement/web/controller/RoleController.java` | 角色端点 | 7.3 |
| `backend/src/main/java/com/usermanagement/web/controller/PermissionController.java` | 权限端点 | 7.4 |
| `backend/src/main/java/com/usermanagement/web/exception/GlobalExceptionHandler.java` | 全局异常处理 | 7.5 |

### 2.7 应用入口文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/src/main/java/com/usermanagement/Application.java` | Spring Boot 入口 | 1.1 |

### 2.8 测试文件

| 文件路径 | 描述 | 所属阶段 |
|----------|------|----------|
| `backend/src/test/resources/application-test.yml` | 测试配置 | 8.1 |
| `backend/src/test/java/com/usermanagement/repository/UserRepositoryTest.java` | 仓库测试 | 8.2 |
| `backend/src/test/java/com/usermanagement/service/UserServiceTest.java` | 服务测试 | 8.3 |
| `backend/src/test/java/com/usermanagement/security/JwtTokenProviderTest.java` | JWT 测试 | 8.4 |
| `backend/src/test/java/com/usermanagement/web/controller/AuthControllerTest.java` | 认证集成测试 | 8.5 |
| `backend/src/test/java/com/usermanagement/web/controller/UserControllerTest.java` | 用户集成测试 | 8.5 |

---

## 三、依赖关系

### 3.1 任务依赖图

```
阶段 1: 项目初始化
    |
    v
阶段 2: JPA 实体  <-- 依赖阶段 1 (配置完成)
    |
    v
阶段 3: DTO 定义 <-- 依赖阶段 2 (模型定义)
    |
    v
阶段 4: Repository <-- 依赖阶段 2 (实体定义)
    |
    v
阶段 5: 服务层 <-- 依赖阶段 3, 4 (DTO + Repository)
    |
    v
阶段 6: 安全配置 <-- 依赖阶段 5 (UserService)
    |
    v
阶段 7: API 控制器 <-- 依赖阶段 3, 5, 6 (DTO + 服务 + 安全)
    |
    v
阶段 8: 测试 <-- 依赖阶段 2-7 (所有实现)
    |
    v
阶段 9: 文档优化 <-- 依赖阶段 6, 7, 8 (安全 + API + 测试)
```

### 3.2 Bean 依赖关系

```
Config
    |
    +--> SecurityConfig
    |       |
    |       +--> JwtTokenProvider
    |       |       |
    |       |       +--> JwtProperties
    |       |
    |       +--> JwtAuthenticationFilter
    |               |
    |               +--> UserDetailsServiceImpl
    |                       |
    |                       +--> UserRepository
    |                               |
    |                               +--> EntityManager
    |
    +--> DatabaseConfig
            |
            +--> DataSource
            |       |
            |       +--> HikariCP
            |
            +--> JpaProperties
                    |
                    +--> EntityManagerFactory
                            |
                            +--> User, Role, Permission (Entities)

Service Layer
    |
    +--> AuthService
    |       |
    |       +--> UserRepository
    |       +--> JwtTokenProvider
    |       +--> PasswordEncoder (BCrypt)
    |
    +--> UserService
    |       |
    |       +--> UserRepository
    |       +--> RoleRepository
    |
    +--> RoleService
            |
            +--> RoleRepository
            +--> PermissionRepository

Web Layer (Controller)
    |
    +--> AuthController
    |       |
    |       +--> AuthService
    |       +--> LoginRequest, TokenResponse (DTO)
    |
    +--> UserController
            |
            +--> UserService
            +--> UserCreateRequest, UserResponse (DTO)
```

### 3.3 关键依赖说明

| 依赖项 | 被依赖项 | 说明 |
|--------|----------|------|
| JPA 实体 | Repository, Service | 数据持久化基础 |
| Repository | Service | 数据访问抽象 |
| DTO | Controller, Service | API 数据契约 |
| JwtTokenProvider | AuthService, JwtAuthenticationFilter | 令牌生成和验证 |
| UserDetailsService | JwtAuthenticationFilter | 用户加载 |
| SecurityConfig | 全局 | 安全配置中心 |
| UserService | AuthService, UserController | 用户业务逻辑 |

---

## 四、风险点

### 4.1 技术风险

| 风险ID | 风险描述 | 影响程度 | 缓解措施 |
|--------|----------|----------|----------|
| R1 | JDK 21 虚拟线程稳定性 | 中 | 使用同步代码，虚拟线程作为可选优化 |
| R2 | JWT 刷新令牌安全存储 | 高 | 使用 HttpOnly Cookie，实现令牌黑名单 |
| R3 | Spring Security 6 配置变化 | 中 | 参考官方迁移指南，使用 Lambda DSL |
| R4 | 分布式会话管理 | 中 | 使用 JWT 无状态认证 |
| R5 | 测试覆盖率达标困难 | 中 | 采用 TDD，使用 @DataJpaTest, @WebMvcTest |

### 4.2 进度风险

| 风险ID | 风险描述 | 影响程度 | 缓解措施 |
|--------|----------|----------|----------|
| R6 | Spring Security 配置复杂度 | 高 | 先实现基础 JWT，逐步添加 RBAC |
| R7 | Flyway 迁移脚本问题 | 中 | 充分测试迁移，保留回滚脚本 |
| R8 | 依赖版本冲突 | 低 | 使用 Spring Boot BOM 管理版本 |

### 4.3 安全风险

| 风险ID | 风险描述 | 影响程度 | 缓解措施 |
|--------|----------|----------|----------|
| R9 | JWT 密钥管理 | 高 | 环境变量存储，生产环境定期轮换 |
| R10 | CORS 配置错误 | 高 | 显式配置允许的来源，不使用通配符 |
| R11 | SQL 注入风险 | 中 | 使用 JPA 参数化查询，避免原生 SQL |
| R12 | 敏感数据泄露 | 高 | DTO 排除敏感字段，日志脱敏处理 |

---

## 五、测试策略

### 5.1 测试分层

```
                    ▲
                   / \
                  / E2E \        10% - 关键用户流程
                 /─────────\
                /  Integration \  20% - API + 数据库
               /─────────────────\
              /       Unit         \ 70% - 函数、类、方法
             /─────────────────────────\
```

### 5.2 单元测试策略

| 测试对象 | 测试内容 | 覆盖率目标 | 注解 |
|----------|----------|------------|------|
| 领域实体 | 验证约束、生命周期 | 90% | `@DataJpaTest` |
| Repository | 查询方法、自定义 | 90% | `@DataJpaTest` |
| 服务层 | 业务逻辑、事务 | 90% | `@ExtendWith(MockitoExtension.class)` |
| 安全模块 | JWT 生成验证 | 100% | 标准单元测试 |
| 工具函数 | 边界条件 | 95% | 标准单元测试 |

### 5.3 集成测试策略

| 测试对象 | 测试内容 | 技术 |
|----------|----------|------|
| API 端点 | 请求/响应、状态码 | `@SpringBootTest` + `TestRestTemplate` |
| 认证流程 | 登录/登出/刷新完整流程 | `@SpringBootTest` + Testcontainers |
| 权限检查 | 不同角色访问控制 | `@WithMockUser` |
| 数据库迁移 | Flyway 脚本 | `@Testcontainers` + PostgreSQL |

### 5.4 测试环境配置

```java
// 关键测试注解配置

// 集成测试
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
}

// Repository 测试
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {UserRepositoryTest.Initializer.class})
class UserRepositoryTest { }

// Web 层测试
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest { }

// 安全测试
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthSecurityTest { }
```

### 5.5 覆盖率检查点

| 阶段 | 检查项 | 目标覆盖率 |
|------|--------|------------|
| 阶段 5 完成 | 服务层单元测试 | 70% |
| 阶段 6 完成 | 安全配置测试 | 80% |
| 阶段 7 完成 | Controller 集成测试 | 85% |
| 阶段 8 完成 | 整体覆盖率 | >= 85% |

### 5.6 测试执行命令

```bash
# 运行所有测试
./mvnw test

# 运行单元测试
./mvnw test -Dtest="*Test"

# 运行集成测试
./mvnw test -Dtest="*IntegrationTest"

# 生成覆盖率报告 (JaCoCo)
./mvnw jacoco:report
# 报告位置: target/site/jacoco/index.html

# 运行特定模块测试
./mvnw test -Dtest=UserServiceTest

# 跳过测试打包
./mvnw package -DskipTests
```

---

## 六、技术选型与依赖

### 6.1 核心依赖 (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.0</version>
</parent>

<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.5.0</spring-boot.version>
    <testcontainers.version>1.19.0</testcontainers.version>
</properties>

<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- JPA + Database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Monitoring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
        </plugin>
    </plugins>
</build>
```

### 6.2 可选依赖

| 依赖 | 用途 | 阶段 |
|------|------|------|
| bucket4j | 速率限制 | 6.7 |
| caffeine | 本地缓存 | 未来 |
| micrometer-registry-prometheus | 监控指标 | 9.1 |
| springdoc-openapi | API 文档 | 9.1 |

---

## 七、实施检查清单

### 7.1 代码质量检查

- [ ] 所有方法有 JavaDoc 注释
- [ ] 所有类和方法有适当的访问修饰符
- [ ] 代码符合 Google Java Style Guide
- [ ] 无硬编码敏感信息
- [ ] 异常处理完善
- [ ] 使用 `final` 修饰不可变引用
- [ ] 使用 Records 定义 DTO (JDK 21)

### 7.2 安全检查

- [ ] 密码使用 BCrypt 哈希 (强度 12)
- [ ] JWT 密钥从环境变量读取
- [ ] 所有端点有适当权限控制
- [ ] 输入数据经过 Jakarta Validation 验证
- [ ] 敏感字段不在响应中暴露
- [ ] CORS 配置明确允许来源
- [ ] CSRF 防护已启用

### 7.3 性能检查

- [ ] 数据库查询使用 EntityGraph 避免 N+1
- [ ] 适当的数据库索引已创建
- [ ] 连接池配置合理 (HikariCP)
- [ ] JVM 内存配置已优化
- [ ] 响应时间 P95 < 200ms

### 7.4 测试检查

- [ ] 单元测试覆盖率 >= 85%
- [ ] 所有 API 端点有集成测试
- [ ] 认证流程完整测试
- [ ] 权限检查完整测试
- [ ] Testcontainers 集成测试通过

---

## 八、总结

本实施计划涵盖从项目初始化到测试完成的完整 Spring Boot 后端开发流程，共 9 个阶段，预计总工时约 90-100 小时。

**关键成功因素**:
1. 严格按照分层架构实现 (Controller → Service → Repository → Entity)
2. 测试驱动开发，确保覆盖率达标 (>= 85%)
3. 安全优先，特别是 Spring Security 配置
4. 文档同步更新
5. 保持与原有 FastAPI API 的兼容性

**里程碑**:
- M1 (阶段 1-2): 基础架构和 JPA 实体完成
- M2 (阶段 3-5): Repository 和服务层完成
- M3 (阶段 6-7): Spring Security 和 API 完成
- M4 (阶段 8-9): 测试通过，文档完善
