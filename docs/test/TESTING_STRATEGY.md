# 测试策略文档

## 测试金字塔

| 层级 | 比例 | 工具 |
|------|------|------|
| 单元测试 | 70% | JUnit 5 + Mockito / Jest |
| 集成测试 | 20% | Spring Boot Test + Testcontainers / MSW |
| E2E 测试 | 10% | Playwright |

## 后端测试

- **单元测试**: JPA 实体、服务层逻辑、工具函数
  - 使用 `@ExtendWith(MockitoExtension.class)` 进行服务层测试
  - 使用 `@DataJpaTest` 进行 Repository 层测试
  - 使用 Mockito 进行依赖模拟
- **集成测试**: API 端点、数据库交互、安全认证
  - 使用 `@SpringBootTest` 进行完整上下文测试
  - 使用 `@WebMvcTest` 进行 Controller 层测试
  - 使用 **Testcontainers** 进行真实数据库测试 (PostgreSQL)
- **E2E 测试**: 完整用户流程 (可与前端 E2E 测试结合)

### 测试工具

| 工具 | 用途 |
|------|------|
| JUnit 5 | 测试框架 |
| Mockito | 模拟对象 |
| AssertJ | 流式断言 |
| Spring Boot Test | Spring 上下文测试 |
| Testcontainers | Docker 容器化测试数据库 |
| JaCoCo | 代码覆盖率报告 |

### 覆盖率报告
- 使用 **JaCoCo Maven Plugin** 生成覆盖率报告
- 报告位置: `target/site/jacoco/index.html`
- 配置覆盖率阈值: >= 85%

## 前端测试

- **单元测试**: 组件渲染、Hook 逻辑、工具函数
- **集成测试**: API 集成、页面交互
- **E2E 测试**: 用户流程、跨浏览器

## 覆盖率目标

- 后端: ≥ 85%
- 前端: ≥ 80%

## CI/CD 集成

1. 代码质量检查
2. 单元测试
3. 集成测试
4. 端到端测试
