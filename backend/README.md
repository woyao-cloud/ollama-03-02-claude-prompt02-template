# 用户管理系统 - Spring Boot 后端

## 快速开始

### 前置条件
- JDK 21+
- Maven 3.8+
- PostgreSQL 15+ (生产环境)

### 开发环境启动

```bash
cd backend

# 使用 H2 内存数据库启动 (无需外部数据库)
mvn spring-boot:run

# 或指定环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

访问：
- API: http://localhost:8080/api
- H2 控制台：http://localhost:8080/h2-console

### 构建

```bash
# 编译 + 测试
mvn clean verify

# 打包
mvn clean package

# 运行
java -jar target/usermanagement-1.0.0-SNAPSHOT.jar
```

### 测试

```bash
# 运行所有测试
mvn test

# 运行集成测试
mvn verify

# 生成覆盖率报告
mvn clean test jacoco:report
```

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.0 | 应用框架 |
| JDK | 21 | 虚拟线程支持 |
| Spring Security | 6.x | 安全认证 |
| Spring Data JPA | 3.5 | 数据访问 |
| Flyway | 10.x | 数据库迁移 |
| PostgreSQL | 15 | 生产数据库 |
| H2 | - | 开发/测试数据库 |
| JWT (jjwt) | 0.12.6 | Token 认证 |
| MapStruct | 1.6.3 | 对象映射 |
| Lombok | 1.18.36 | 代码简化 |

## 项目结构

```
backend/
├── src/main/java/com/usermanagement/
│   ├── UserManagementApplication.java  # 应用入口
│   ├── config/                         # 配置类
│   │   ├── AppProperties.java
│   │   ├── CorsConfig.java
│   │   └── JacksonConfig.java
│   ├── domain/                         # JPA 实体
│   │   ├── BaseEntity.java
│   │   ├── UserStatus.java
│   │   └── DataScope.java
│   ├── repository/                     # Repository 接口
│   ├── service/                        # 业务服务
│   ├── web/                            # Controller + DTO
│   │   └── GlobalExceptionHandler.java
│   └── security/                       # 安全配置
├── src/main/resources/
│   ├── application.yml                 # 主配置
│   ├── application-dev.yml             # 开发环境
│   ├── application-test.yml            # 测试环境
│   ├── application-prod.yml            # 生产环境
│   └── db/migration/                   # Flyway 迁移脚本
└── pom.xml
```

## 配置说明

### 环境配置

| 环境 | 数据库 | 端口 | 说明 |
|------|--------|------|------|
| dev | H2 (内存) | 8080 | 本地开发，H2 控制台可用 |
| test | Testcontainers PG | 随机 | 集成测试 |
| prod | PostgreSQL | 8080 | 生产环境 |

### 关键配置项

```yaml
# JWT 配置
app.jwt.secret: ${JWT_SECRET}
app.jwt.expiration: 86400000  # 24 小时

# 密码策略
app.password.min-length: 8|12 (dev|prod)
app.password.require-uppercase: true

# 账户锁定
app.account.lockout.threshold: 5
app.account.lockout.duration: 900  # 15 分钟
```

## API 端点

| 端点 | 说明 |
|------|------|
| `/api/auth/login` | 登录 |
| `/api/auth/refresh` | 刷新 Token |
| `/api/auth/register` | 用户注册 |
| `/api/users` | 用户管理 |
| `/api/departments` | 部门管理 |
| `/api/roles` | 角色管理 |
| `/api/permissions` | 权限管理 |
| `/api/audit-logs` | 审计日志 |
| `/api/configs` | 系统配置 |

## 开发指南

### 添加新实体

1. 在 `domain/` 创建实体类，继承 `BaseEntity`
2. 在 `repository/` 创建 Repository 接口
3. 在 `service/` 创建业务服务
4. 在 `web/` 创建 Controller 和 DTO
5. 创建 Flyway 迁移脚本

### 添加新 API

1. 创建 DTO 类 (record)
2. 在 Controller 中添加端点方法
3. 在 Service 中实现业务逻辑
4. 添加单元测试

## 数据库迁移

```bash
# 查看迁移状态
mvn flyway:info

# 执行迁移
mvn flyway:migrate

# 清理数据库 (开发环境)
mvn flyway:clean

# 修复迁移
mvn flyway:repair
```

## 监控端点

| 端点 | 说明 |
|------|------|
| `/api/actuator/health` | 健康检查 |
| `/api/actuator/info` | 应用信息 |
| `/api/actuator/metrics` | 性能指标 |
| `/api/actuator/prometheus` | Prometheus 指标 |

## 日志

开发环境日志输出到控制台，生产环境日志路径：
```
/var/log/usermanagement/application.log
```

## 安全

- 所有密码使用 BCrypt 加密 (强度因子 12)
- JWT Token 认证
- CORS 跨域配置
- 账户锁定保护 (5 次失败锁定 15 分钟)
