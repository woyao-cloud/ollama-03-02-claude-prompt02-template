# 组件图

使用 C4 模型 Level 3: Component Diagram

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#e1f5fe', 'primaryTextColor': '#01579b', 'primaryBorderColor': '#0288d1', 'lineColor': '#0288d1', 'secondaryColor': '#fff3e0', 'tertiaryColor': '#e8f5e9'}}}%%
C4Component
    title 组件图 - 认证服务

    Container(spa, "单页应用", "Next.js", "前端应用")
    Container_Boundary(authService, "认证服务") {
        Component(authController, "认证控制器", "Spring MVC<br/>REST API", "处理HTTP请求<br/>输入校验")
        Component(authServiceComp, "认证服务", "Spring Service", "业务逻辑<br/>Token管理")
        Component(jwtProvider, "JWT提供者", "JJWT Library", "Token生成<br/>Token验证<br/>RSA签名")
        Component(userDetailsService, "用户详情服务", "Spring Security", "用户信息加载<br/>密码验证")
        Component(auditProducer, "审计生产者", "Spring Kafka", "发送审计事件")
        Component(rateLimiter, "限流器", "RedisRateLimiter", "登录限流<br/>防暴力破解")
    }
    Container(redis, "Redis", "Redis Cluster", "会话/缓存")
    Container(postgres, "PostgreSQL", "PostgreSQL", "用户数据")
    Container(kafka, "Kafka", "Kafka Cluster", "审计日志")

    Rel(spa, authController, "登录/刷新/登出", "HTTPS/JSON")
    Rel(authController, authServiceComp, "调用")
    Rel(authController, rateLimiter, "检查限流")

    Rel(authServiceComp, jwtProvider, "生成/验证Token")
    Rel(authServiceComp, userDetailsService, "加载用户信息")
    Rel(authServiceComp, redis, "管理会话", "RESP3")
    Rel(authServiceComp, auditProducer, "发送审计事件")

    Rel(userDetailsService, postgres, "查询用户", "JDBC")
    Rel(rateLimiter, redis, "检查计数", "RESP3")
    Rel(auditProducer, kafka, "发送事件", "Kafka")

    UpdateElementStyle(spa, $fontColor="#01579b", $bgColor="#e1f5fe", $borderColor="#0288d1")
    UpdateElementStyle(authController, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(authServiceComp, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(jwtProvider, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(userDetailsService, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(auditProducer, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(rateLimiter, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(redis, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(postgres, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(kafka, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
```

---

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#e1f5fe', 'primaryTextColor': '#01579b', 'primaryBorderColor': '#0288d1', 'lineColor': '#0288d1', 'secondaryColor': '#fff3e0', 'tertiaryColor': '#e8f5e9'}}}%%
C4Component
    title 组件图 - 角色权限服务

    Container(spa, "单页应用", "Next.js", "前端应用")
    Container_Boundary(roleService, "角色权限服务") {
        Component(roleController, "角色控制器", "Spring MVC", "角色CRUD API")
        Component(permController, "权限控制器", "Spring MVC", "权限CRUD API")
        Component(roleServiceComp, "角色服务", "Spring Service", "角色业务逻辑")
        Component(permService, "权限服务", "Spring Service", "权限业务逻辑")
        Component(rbacEvaluator, "RBAC评估器", "Custom", "权限检查<br/>数据权限过滤")
        Component(permCache, "权限缓存", "CacheManager", "权限缓存管理")
    }
    Container(redis, "Redis", "Redis Cluster", "权限缓存")
    Container(postgres, "PostgreSQL", "PostgreSQL", "权限数据")
    Container(kafka, "Kafka", "Kafka Cluster", "审计事件")

    Rel(spa, roleController, "角色管理", "HTTPS/JSON")
    Rel(spa, permController, "权限管理", "HTTPS/JSON")

    Rel(roleController, roleServiceComp, "调用")
    Rel(permController, permService, "调用")

    Rel(roleServiceComp, rbacEvaluator, "权限检查")
    Rel(roleServiceComp, permCache, "缓存操作")
    Rel(permService, permCache, "缓存操作")

    Rel(permCache, redis, "读写缓存", "RESP3")
    Rel(roleServiceComp, postgres, "读写数据", "JDBC")
    Rel(permService, postgres, "读写数据", "JDBC")
    Rel(roleServiceComp, kafka, "发送事件", "Kafka")

    UpdateElementStyle(spa, $fontColor="#01579b", $bgColor="#e1f5fe", $borderColor="#0288d1")
    UpdateElementStyle(roleController, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(permController, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(roleServiceComp, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(permService, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(rbacEvaluator, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(permCache, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(redis, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(postgres, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(kafka, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
```

## 说明

### 认证服务组件

| 组件 | 类型 | 职责 |
|------|------|------|
| 认证控制器 | Controller | 接收HTTP请求，参数校验，调用服务 |
| 认证服务 | Service | 业务逻辑，协调各组件 |
| JWT提供者 | Component | Token生成、验证、签名 |
| 用户详情服务 | Service | 集成Spring Security，加载用户信息 |
| 审计生产者 | Component | 发送审计事件到Kafka |
| 限流器 | Component | Redis-based限流，防暴力破解 |

### 角色权限服务组件

| 组件 | 类型 | 职责 |
|------|------|------|
| 角色控制器 | Controller | 角色CRUD API |
| 权限控制器 | Controller | 权限CRUD API |
| 角色服务 | Service | 角色业务逻辑 |
| 权限服务 | Service | 权限业务逻辑 |
| RBAC评估器 | Component | 权限检查，数据权限过滤 |
| 权限缓存 | Component | 权限缓存管理 |

---

## 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0 | 2026-03-24 | 系统架构师 | 初始版本 |
