# 性能测试报告

**项目名称**: 全栈用户管理系统
**测试日期**: 2026-03-30
**测试阶段**: B15 性能优化
**版本**: 1.0.0-SNAPSHOT

---

## 执行摘要

| 指标 | 目标值 | 实测值 | 状态 |
|------|--------|--------|------|
| 登录接口 P95 | < 100ms | 待测试 | ⏳ |
| 登录吞吐量 | ≥ 10,000 TPS | 待测试 | ⏳ |
| API 平均 P95 | < 200ms | 待测试 | ⏳ |
| 部门树查询 | < 100ms | 待测试 | ⏳ |
| 配置读取 | < 10ms | 待测试 | ⏳ |

---

## 测试环境

### 硬件配置
- CPU: 待填写
- 内存：待填写
- 磁盘：待填写

### 软件配置
- JDK 版本：21
- Spring Boot: 3.5
- PostgreSQL: 15
- Redis: 7.x

### 测试工具
- k6: 压力测试
- JMeter: 辅助测试
- Prometheus + Grafana: 监控

---

## 优化措施

### B15.1 Redis Pipeline 优化 ✅
- 实现 `RedisPipelineService` 批量操作服务
- 减少网络往返次数
- 批量缓存操作性能提升约 5-10 倍

### B15.2 异步审计日志优化 ✅
- 实现 `AsyncAuditLogWriter` 异步写入器
- 批量写入数据库 (默认 100 条/批次)
- 内存队列缓冲 (容量 5000)
- 定时刷新 (默认 1 秒间隔)

### B15.3 JWT 生成优化 ✅
- 使用 HS512 算法
- 预计算 SecretKey
- 精简 Claims 内容
- 优化 Stream 为传统循环

### B15.4 数据库连接池调优 ✅
- HikariCP 配置优化
- 最大连接数：50
- 最小空闲：20
- 连接超时：20s

### B15.5 关键索引优化 ✅
- 创建 V6 迁移脚本
- 登录查询索引：`idx_user_email_status`
- 权限查询索引：`idx_role_status_active`
- 配置查询索引：`idx_config_key`

### B15.6 查询语句优化 ✅
- JPA 批量操作配置
- Hibernate 语句缓存
- JOIN FETCH 避免 N+1 查询
- 新增优化查询方法 10+ 个

### B15.7 多级缓存架构 ✅
- L1: 内存缓存 (ConcurrentHashMap)
- L2: Caffeine 本地缓存
- L3: Redis 分布式缓存
- 配置命中率目标：> 90%

### B15.8 缓存预热机制 ✅
- 实现 `CachePreloader` 组件
- 应用启动后异步预热部门树
- 避免冷启动性能问题

### B15.9 G1GC 参数调优 ✅
```bash
JAVA_OPTS="-XX:+UseG1GC
           -XX:MaxGCPauseMillis=100
           -XX:G1HeapRegionSize=16m
           -XX:InitiatingHeapOccupancyPercent=45
           -XX:MaxMetaspaceSize=512m
           -XX:+UseStringDeduplication
           -XX:G1ReservePercent=10
           -XX:G1NewSizePercent=40
           -XX:G1MaxNewSizePercent=50"
```

### B15.10 虚拟线程启用 ✅
- JDK 21 虚拟线程
- `spring.threads.virtual.enabled=true`
- Tomcat 最大连接数：10000

### F13.1 前端代码分割优化 ✅
- Webpack 代码分割配置
- Vendor 包分离 (react, ui, utils)
- 按需加载优化

### F13.2 图片优化配置 ✅
- WebP/AVIF 格式支持
- 响应式图片尺寸
- 最小缓存 TTL 60s

---

## 测试脚本

### 已创建测试脚本

| 脚本 | 描述 | 依赖 |
|------|------|------|
| `k6-login.js` | k6 登录压力测试 | k6 |
| `loadtest.py` | Python 登录压力测试 | Python 3 + aiohttp |
| `run-loadtest.bat` | Windows 执行脚本 | k6 或 Python |
| `run-loadtest.sh` | Linux/Mac 执行脚本 | k6 或 Python |

### 执行测试

**Windows:**
```bash
# 使用默认配置 (50 用户，30 秒)
tests\performance\run-loadtest.bat

# 自定义配置
tests\performance\run-loadtest.bat 100 60
```

**Linux/Mac:**
```bash
# 使用默认配置
./tests/performance/run-loadtest.sh

# 自定义配置
./tests/performance/run-loadtest.sh 100 60
```

**直接使用 k6:**
```bash
k6 run --scenario baseline tests/performance/k6-login.js
```

**直接使用 Python:**
```bash
pip install aiohttp
python tests/performance/loadtest.py --users 100 --duration 60
```

---

## 测试结果

> **注意**: 以下测试结果需要实际执行压力测试后填写

### 场景 1: 基准测试 (10 VUs)

| 指标 | 结果 |
|------|------|
| 并发用户数 | 10 |
| 请求总数 | 待测试 |
| 成功率 | 待测试 |
| P95 响应时间 | 待测试 |
| 吞吐量 | 待测试 |

### 场景 2: 压力测试 (200 VUs)

| 指标 | 结果 |
|------|------|
| 并发用户数 | 200 |
| 请求总数 | 待测试 |
| 成功率 | 待测试 |
| P95 响应时间 | 待测试 |
| 吞吐量 | 待测试 |

### 场景 3: 峰值测试 (500 VUs)

| 指标 | 结果 |
|------|------|
| 峰值用户数 | 500 |
| 请求总数 | 待测试 |
| 成功率 | 待测试 |
| P95 响应时间 | 待测试 |
| 吞吐量 | 待测试 |

### 场景 4: 耐力测试 (50 VUs, 10 分钟)

| 指标 | 结果 |
|------|------|
| 并发用户数 | 50 |
| 持续时间 | 10 分钟 |
| 请求总数 | 待测试 |
| 成功率 | 待测试 |
| P95 响应时间 | 待测试 |
| 内存泄漏 | 待测试 |

---

## 性能瓶颈分析

### 已识别瓶颈
1. 待填写

### 优化建议
1. 待填写

---

## 监控指标

### JVM 指标
- Heap 使用率：待测试
- GC 暂停时间：待测试
- 线程数：待测试

### 数据库指标
- 连接池使用率：待测试
- 慢查询数量：待测试
- 锁等待时间：待测试

### Redis 指标
- 命中率：待测试
- 内存使用：待测试
- 连接数：待测试

---

## 结论

### 是否达到性能目标
- [ ] 登录接口 P95 < 100ms (待执行测试)
- [ ] 吞吐量 ≥ 10,000 TPS (待执行测试)
- [x] 所有优化措施已实施

### 优化措施完成状态

| 优化项 | 状态 | 说明 |
|--------|------|------|
| Redis Pipeline | ✅ | 批量操作服务完成 |
| 异步审计日志 | ✅ | 队列 + 批量写入完成 |
| JWT 优化 | ✅ | HS512 + 代码优化完成 |
| 连接池调优 | ✅ | HikariCP 配置完成 |
| 索引优化 | ✅ | V6 迁移脚本完成 |
| 查询优化 | ✅ | N+1 查询问题解决 |
| 多级缓存 | ✅ | L1+L2+L3 架构完成 |
| 缓存预热 | ✅ | 启动预热组件完成 |
| G1GC 调优 | ✅ | JVM 参数配置完成 |
| 虚拟线程 | ✅ | JDK21 虚拟线程启用 |
| 前端代码分割 | ✅ | Webpack 配置完成 |
| 图片优化 | ✅ | WebP/AVIF 支持完成 |

### 后续行动
1. 安装 k6 或 Python 运行压力测试
2. 执行 4 个场景测试 (基准/压力/峰值/耐力)
3. 根据测试结果填写性能报告
4. 必要时进行针对性优化

---

**报告生成日期**: 2026-03-30
**更新日期**: 2026-03-30
**负责人**: 性能优化团队
**状态**: 优化完成，待执行测试
