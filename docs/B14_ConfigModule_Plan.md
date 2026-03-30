# Implementation Plan: B14 系统配置模块 (System Configuration Module)

## Overview

- **Objective**: 实现系统配置管理模块，支持配置的 CRUD 操作、多级缓存和动态刷新
- **Scope**: 后端实现（Entity, Repository, Service, Controller, DTO, Cache, Tests）
- **Complexity**: Medium
- **Estimated Effort**: 3-4 days

## Requirements

### Functional Requirements
- [ ] 系统配置的增删改查（CRUD）
- [ ] 配置按分类管理（AUTH, SYSTEM, USER, etc.）
- [ ] 支持多种配置类型（STRING, NUMBER, BOOLEAN, JSON）
- [ ] 多级缓存：L1 本地缓存 + L2 Redis 缓存 + L3 数据库
- [ ] 配置动态刷新机制
- [ ] 配置变更历史记录

### Acceptance Criteria
- [ ] API 支持配置的分页查询、按分类查询
- [ ] 缓存命中率 > 80%（读多写少场景）
- [ ] 配置更新后缓存自动刷新
- [ ] 测试覆盖率 ≥ 85%
- [ ] 遵循项目现有代码风格

## Technical Analysis

### Current State

**数据库**: V4__system_configs.sql 已创建
- `system_config` 表：id, config_key, config_value, config_type, category, description, encrypted, status, created_at, updated_at, version
- `config_history` 表：记录配置变更历史
- 初始数据：认证、系统、用户相关配置已预置

**Redis 配置**: RedisConfig.java 已存在
- RedisTemplate<String, Object> 已配置
- JSON 序列化器已设置

**缓存模式**: DepartmentCache.java 提供参考
- 纯 Redis 缓存模式（L2）
- 异常降级处理
- 空值缓存保护

**需要新增**:
- L1 本地缓存（Caffeine）- 热点配置
- Redis Pub/Sub 刷新通知机制
- ConfigService 及多级缓存逻辑

### Proposed Changes

**架构设计**:
```
┌─────────────────────────────────────────────────────────────┐
│                      ConfigController                        │
│                   (REST API Endpoints)                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       ConfigService                          │
│  ┌───────────────────────────────────────────────────────┐   │
│  │              ConfigCacheService (L1 + L2)             │   │
│  │  ┌─────────────────┐    ┌─────────────────────────┐   │   │
│  │  │ Caffeine Cache  │───▶│     Redis Cache         │   │   │
│  │  │ (Hot Configs)   │    │  (Distributed Cache)    │   │   │
│  │  └─────────────────┘    └─────────────────────────┘   │   │
│  └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   SystemConfigRepository                     │
│                    (JPA Repository)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      SystemConfig Entity                     │
│                    (Database Persistence)                    │
└─────────────────────────────────────────────────────────────┘
```

**缓存策略**:
| 层级 | 技术 | 用途 | 过期策略 |
|------|------|------|----------|
| L1 | Caffeine | 热点配置（100 条） | 10 分钟 + 容量驱逐 |
| L2 | Redis | 全量配置 | 30 分钟 |
| L3 | PostgreSQL | 持久化存储 | - |

**刷新机制**:
- 配置更新 → 清除 L1/L2 缓存
- Redis Pub/Sub 通知集群其他实例刷新

### Components to Modify

| Component | Changes | Impact |
|-----------|---------|--------|
| CacheProperties.java | 添加 Config 缓存配置 | Low |
| application.yml | 添加配置缓存属性 | Low |

### New Components

| Component | Purpose | Location |
|-----------|---------|----------|
| SystemConfig.java | 配置实体 | domain/ |
| ConfigHistory.java | 配置历史实体 | domain/ |
| SystemConfigRepository.java | 配置数据访问 | repository/ |
| SystemConfigDTO.java | 配置 DTO | web/dto/ |
| ConfigRequest.java | 配置请求 DTO | web/dto/ |
| ConfigMapper.java | 配置映射器 | web/mapper/ |
| ConfigService.java | 配置服务接口 | service/ |
| ConfigServiceImpl.java | 配置服务实现 | service/ |
| ConfigCacheService.java | 配置缓存服务 | service/cache/ |
| ConfigController.java | 配置 API 控制器 | web/controller/ |
| ConfigControllerTest.java | 控制器测试 | test/ |
| ConfigServiceImplTest.java | 服务层测试 | test/ |
| ConfigCacheServiceTest.java | 缓存服务测试 | test/ |

## Implementation Steps

### Phase 1: Foundation (Entity + Repository + DTO)

1. [ ] 创建 SystemConfig 实体
   - 文件：`backend/src/main/java/com/usermanagement/domain/SystemConfig.java`
   - 继承 BaseEntity
   - 字段：configKey, configValue, configType, category, description, encrypted, status
   - 验证：@NotNull, @Size, @Enumerated

2. [ ] 创建 ConfigHistory 实体（可选，用于审计）
   - 文件：`backend/src/main/java/com/usermanagement/domain/ConfigHistory.java`

3. [ ] 创建 Repository 接口
   - 文件：`backend/src/main/java/com/usermanagement/repository/SystemConfigRepository.java`
   - 方法：findByConfigKey, findByCategory, existsByConfigKey

4. [ ] 创建 DTO 类
   - `SystemConfigDTO.java` - 响应 DTO
   - `ConfigCreateRequest.java` - 创建请求
   - `ConfigUpdateRequest.java` - 更新请求
   - `ConfigListResponse.java` - 列表响应（分页）

5. [ ] 创建 Mapper 接口
   - 文件：`backend/src/main/java/com/usermanagement/web/mapper/ConfigMapper.java`

### Phase 2: Service Layer (Core + Cache)

6. [ ] 创建 ConfigService 接口
   - 文件：`backend/src/main/java/com/usermanagement/service/ConfigService.java`
   - 方法：create, update, delete, getById, getByKey, getByCategory, list

7. [ ] 创建 ConfigCacheService（缓存服务）
   - 文件：`backend/src/main/java/com/usermanagement/service/cache/ConfigCacheService.java`
   - L1: CaffeineCache (@PostConstruct 初始化)
   - L2: RedisCache
   - 方法：get, put, evict, evictAll, refreshFromRedis

8. [ ] 更新 CacheProperties
   - 添加 Config 缓存配置属性

9. [ ] 实现 ConfigServiceImpl
   - 文件：`backend/src/main/java/com/usermanagement/service/ConfigServiceImpl.java`
   - 集成 ConfigCacheService
   - 实现配置变更历史记录（可选）

### Phase 3: Controller + API

10. [ ] 创建 ConfigController
    - 文件：`backend/src/main/java/com/usermanagement/web/controller/ConfigController.java`
    - GET /api/configs - 分页列表
    - GET /api/configs/{id} - 详情
    - GET /api/configs/key/{key} - 按 key 查询
    - GET /api/configs/category/{category} - 按分类查询
    - POST /api/configs - 创建
    - PUT /api/configs/{id} - 更新
    - DELETE /api/configs/{id} - 删除
    - POST /api/configs/{id}/refresh - 刷新缓存

### Phase 4: Testing

11. [ ] ConfigServiceImpl 单元测试
    - 文件：`backend/src/test/java/com/usermanagement/service/ConfigServiceImplTest.java`
    - 测试 CRUD 操作
    - 测试缓存逻辑
    - 测试异常处理

12. [ ] ConfigCacheService 单元测试
    - 文件：`backend/src/test/java/com/usermanagement/service/cache/ConfigCacheServiceTest.java`
    - 测试 L1/L2 缓存命中
    - 测试缓存刷新
    - 测试异常降级

13. [ ] ConfigController 集成测试
    - 文件：`backend/src/test/java/com/usermanagement/web/controller/ConfigControllerTest.java`
    - 测试 API 端点
    - 测试请求验证

### Phase 5: Integration + Polish

14. [ ] Redis Pub/Sub 刷新通知（可选增强）
    - 配置更新时发布消息
    - 订阅消息刷新本地缓存

15. [ ] 更新 PLAN.md 记录进度

## Dependencies

### Prerequisites
- [x] Redis 配置已完成 (RedisConfig.java)
- [x] 数据库表已创建 (V4__system_configs.sql)
- [x] Testcontainers Redis 已配置

### Dependent Tasks
- 前端配置管理界面（待实现）
- 配置热加载到其他模块的集成

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Caffeine 依赖缺失 | Low | Medium | 检查 pom.xml，必要时添加 |
| 缓存一致性问题 | Medium | High | 更新时同时清除 L1+L2 缓存 |
| Redis 连接失败 | Medium | Medium | 异常降级到数据库查询 |
| 配置类型转换错误 | Low | Medium | 严格的类型验证和转换逻辑 |

## Testing Strategy

### Unit Tests
- [ ] ConfigServiceImpl - CRUD 操作测试
- [ ] ConfigServiceImpl - 缓存集成测试
- [ ] ConfigCacheService - L1/L2 缓存测试
- [ ] ConfigMapper - 映射测试

### Integration Tests
- [ ] ConfigController - API 端点测试
- [ ] 配置读写 + 缓存命中验证
- [ ] 配置刷新机制测试

### Test Data
- 使用 @Sql 脚本加载测试数据
- 使用 Testcontainers Redis

## Rollback Plan
- 数据库：删除 V4__system_configs.sql（如未合并）
- 代码：git revert 相关 commits
- 配置：移除 application.yml 中的配置项

## Success Criteria
- [ ] 所有 API 端点正常工作
- [ ] 缓存命中率 > 80%（读多写少场景）
- [ ] 测试覆盖率 ≥ 85%
- [ ] 代码审查通过
- [ ] PLAN.md 已更新

---

## Appendix: Code Patterns

### Entity Pattern (参考 User.java)
```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "system_config")
public class SystemConfig extends BaseEntity {
    @Column(name = "config_key", length = 100, nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT", nullable = false)
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", length = 20, nullable = false)
    private ConfigType configType;

    // ... 其他字段
}
```

### Cache Service Pattern (参考 DepartmentCache.java)
```java
@Service
public class ConfigCacheService {
    private Cache<String, Object> localCache;  // L1
    private final RedisTemplate<String, Object> redisTemplate;  // L2

    // get: L1 → L2 → DB
    // put: L1 + L2
    // evict: L1 + L2 + Pub/Sub
}
```

### Service Pattern (参考 RoleServiceImpl.java)
```java
@Service
public class ConfigServiceImpl implements ConfigService {
    private final SystemConfigRepository repository;
    private final ConfigCacheService cacheService;
    private final ConfigMapper mapper;

    @Override
    @Transactional
    public ConfigDTO createConfig(ConfigCreateRequest request) {
        // 1. 验证
        // 2. 创建
        // 3. 缓存
        // 4. 返回
    }
}
```
