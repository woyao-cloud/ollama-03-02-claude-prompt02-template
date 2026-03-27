# 后端架构设计

## 技术栈

- **Spring Boot 3.5** + **JDK 21** (虚拟线程、Records、模式匹配)
- **Spring Data JPA** (Hibernate) + **Flyway** 数据库迁移
- **Spring Security** + **JWT** + **OAuth2 Resource Server**
- **PostgreSQL** / **H2** (开发和测试)
- **Maven** 构建工具
- **JUnit 5** + **Testcontainers** 测试框架

### 技术选择理由

| 技术 | 选型理由 |
|------|----------|
| Spring Boot 3.5 | 企业级生态，自动配置，生产就绪功能 |
| JDK 21 | 虚拟线程提升并发性能，Records 简化 DTO |
| Spring Data JPA | 简化数据访问，支持复杂查询和分页 |
| Flyway | 版本化数据库迁移，与 Spring Boot 无缝集成 |
| Spring Security | 全面的安全框架，支持 JWT 和 RBAC |

---

## 项目结构

```
backend/
├── src/main/java/com/usermanagement/
│   ├── Application.java              # Spring Boot 入口
│   ├── config/                       # 配置类
│   │   ├── SecurityConfig.java       # 安全配置
│   │   ├── JwtConfig.java            # JWT 配置
│   │   ├── DatabaseConfig.java       # 数据库配置
│   │   └── WebConfig.java            # Web/CORS 配置
│   ├── domain/                       # 领域模型 (JPA 实体)
│   │   ├── user/
│   │   │   ├── User.java             # 用户实体
│   │   │   ├── Role.java             # 角色实体
│   │   │   └── Permission.java       # 权限实体
│   │   └── audit/
│   │       ├── AuditLog.java         # 审计日志
│   │       └── UserSession.java      # 用户会话
│   ├── repository/                   # Spring Data Repository
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   └── PermissionRepository.java
│   ├── service/                      # 业务服务层
│   │   ├── UserService.java
│   │   ├── AuthService.java
│   │   ├── RoleService.java
│   │   └── JwtService.java
│   ├── security/                     # 安全相关
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   ├── UserDetailsServiceImpl.java
│   │   └── PermissionEvaluator.java
│   ├── web/                          # Web 层
│   │   ├── controller/               # REST 控制器
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   └── RoleController.java
│   │   ├── dto/                      # DTO/Records
│   │   │   ├── UserDTO.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── TokenResponse.java
│   │   │   └── ApiResponse.java
│   │   └── exception/                # 全局异常处理
│   │       ├── GlobalExceptionHandler.java
│   │       └── BusinessException.java
│   └── infrastructure/               # 基础设施
│       ├── audit/
│       └── cache/
├── src/main/resources/
│   ├── application.yml               # 主配置
│   ├── application-dev.yml           # 开发环境
│   ├── application-test.yml          # 测试环境
│   ├── application-prod.yml          # 生产环境
│   └── db/migration/                 # Flyway 迁移脚本
│       ├── V1__Initial_schema.sql
│       ├── V2__Add_roles_permissions.sql
│       └── V3__Add_audit_tables.sql
├── src/test/java/com/usermanagement/
│   ├── unit/                         # 单元测试
│   └── integration/                  # 集成测试
├── pom.xml                           # Maven 配置
├── Dockerfile                        # 容器化
└── docker-compose.yml                # 本地开发环境
```

---

## 分层架构

### 1. Web 层 (Controller)
- **职责**: HTTP 请求/响应处理，参数验证，DTO 转换
- **技术**: Spring MVC, Jakarta Validation
- **特点**:
  - 无业务逻辑，只负责协调
  - 使用 Records 定义 DTO
  - 全局异常统一处理

### 2. 服务层 (Service)
- **职责**: 业务逻辑编排，事务管理
- **技术**: Spring Transaction, Spring Security Context
- **特点**:
  - 声明式事务 `@Transactional`
  - 业务规则验证
  - 领域事件发布

### 3. 领域层 (Domain)
- **职责**: 核心业务实体，业务规则
- **技术**: JPA/Hibernate, Bean Validation
- **特点**:
  - JPA 实体定义
  - 实体生命周期回调
  - 值对象封装

### 4. 数据访问层 (Repository)
- **职责**: 数据持久化抽象
- **技术**: Spring Data JPA, QueryDSL
- **特点**:
  - 接口继承 `JpaRepository`
  - 方法名派生查询
  - 复杂查询使用 `@Query`

### 5. 基础设施层
- **职责**: 技术细节实现，外部集成
- **技术**: Spring Security, JWT, Flyway
- **特点**:
  - 配置类集中管理
  - 安全过滤器链
  - 数据库迁移脚本

---

## 安全设计

### JWT 认证流程

```
Client                Spring Security              JWT Provider
  |                         |                            |
  |-- Login Request ------->|                            |
  |                         |-- Validate Credentials ----|
  |                         |<-- UserDetails ------------|
  |                         |-- Generate Tokens ---------|
  |<-- Access + Refresh ----|                            |
  |                                                      |
  |-- API Request (Bearer) ->|                           |
  |                         |-- Validate JWT ------------|
  |                         |<-- Claims -----------------|
  |                         |-- Set Security Context ----|
  |<-- Protected Resource ---|                            |
```

### 安全组件

| 组件 | 实现 | 说明 |
|------|------|------|
| 密码哈希 | `BCryptPasswordEncoder` | Spring Security 默认，强度因子 12 |
| JWT 生成 | `NimbusJwtEncoder` | RSA 密钥对签名 |
| JWT 验证 | `NimbusJwtDecoder` | 签名验证和过期检查 |
| 认证过滤器 | `JwtAuthenticationFilter` | 自定义过滤器，提取 Bearer Token |
| 权限控制 | `@PreAuthorize` + SpEL | 方法级权限表达式 |
| 账户锁定 | `AccountLockoutService` | 5 次失败锁定 15 分钟 |
| 速率限制 | `RateLimitFilter` | Bucket4j 基于令牌桶算法 |

### RBAC 权限模型

```java
// 权限表达式示例
@PreAuthorize("hasAuthority('users:read')")
public UserDTO getUser(Long id) { ... }

@PreAuthorize("hasRole('ADMIN') or hasAuthority('users:update')")
public UserDTO updateUser(Long id, UserUpdateRequest request) { ... }

@PreAuthorize("@permissionEvaluator.hasPermission(#id, 'users', 'delete')")
public void deleteUser(Long id) { ... }
```

---

## 性能优化

### 1. 数据库优化
- **连接池**: HikariCP (Spring Boot 默认)
  - 最大连接数: 20
  - 最小空闲连接: 5
  - 连接超时: 30 秒
- **查询优化**:
  - 使用 `EntityGraph` 解决 N+1 问题
  - 分页查询使用 `Pageable`
  - 适当添加数据库索引

### 2. JVM 优化 (JDK 21)
- **虚拟线程**: 启用 `spring.threads.virtual.enabled=true`
- **G1GC**: 默认垃圾收集器，适合中等堆内存
- **内存配置**:
  ```bash
  -Xms512m -Xmx1024m
  -XX:+UseContainerSupport
  -XX:MaxRAMPercentage=75.0
  ```

### 3. 缓存策略
- **JPA 二级缓存**: 使用 Caffeine
- **查询缓存**: 对热点只读数据启用
- **Spring Cache**: 方法级缓存抽象

### 4. HTTP 优化
- **压缩**: 启用 Gzip 压缩响应
- **Keep-Alive**: 长连接复用
- **HTTP/2**: 支持多路复用

---

## 配置管理

### 多环境配置 (application-*.yml)

```yaml
# application.yml (公共配置)
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  application:
    name: usermanagement

server:
  port: 8080
  compression:
    enabled: true

---
# application-dev.yml (开发环境)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/usermanagement
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true

---
# application-prod.yml (生产环境)
spring:
  datasource:
    url: ${DATABASE_URL}
    hikari:
      maximum-pool-size: 20
  jpa:
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: true

server:
  shutdown: graceful
```

---

## 关键依赖 (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.0</version>
</parent>

<properties>
    <java.version>21</java.version>
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

    <!-- Data -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
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
```

---

## 监控与可观测性

### Actuator 端点
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标
- `/actuator/prometheus` - Prometheus 格式指标

### 日志配置
- 结构化日志 (JSON 格式)
- 日志级别动态调整
- 分布式追踪 (Micrometer Tracing)
