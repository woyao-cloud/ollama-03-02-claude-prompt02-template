# 性能测试指南

## 前置条件

1. 安装 k6: https://k6.io/docs/getting-started/installation/
   ```bash
   # Windows (Chocolatey)
   choco install k6

   # macOS (Homebrew)
   brew install k6

   # Linux (Deb)
   sudo gpg --keyserver hkp://keyserver.ubuntu.com --recv-keys C5AD17C747E3415A3642D57D77C6C49136C10126
   sudo echo "deb [trusted=yes] https://dl.k6.io/deb stable main" > /etc/apt/sources.list.d/k6.list
   sudo apt-get update && sudo apt-get install k6
   ```

2. 确保后端服务运行在 `http://localhost:8080`
3. 准备测试用户数据

## 测试场景

### 1. 基准测试 (3 分钟)
```bash
k6 run --scenario baseline k6-login.js
```

### 2. 压力测试 (10 分钟)
```bash
k6 run --scenario stress k6-login.js
```

### 3. 峰值测试 (1 分钟)
```bash
k6 run --scenario spike k6-login.js
```

### 4. 耐力测试 (10 分钟)
```bash
k6 run --scenario endurance k6-login.js
```

## 性能目标

| 指标 | 目标值 |
|------|--------|
| 登录接口 P95 | < 100ms |
| 登录吞吐量 | >= 10,000 TPS |
| 成功率 | > 99% |

## 结果分析

测试完成后会生成 `summary.json` 文件，包含详细的性能指标。

### 关键指标

- `login_success_rate`: 登录成功率
- `login_p95`: P95 响应时间
- `success_logins`: 成功登录次数
- `failed_logins`: 失败登录次数

## 自定义配置

### 修改目标 URL
```bash
BASE_URL=http://prod-server:8080/api k6 run k6-login.js
```

### 修改 VU 数量
```bash
k6 run --vus 200 --duration 1m k6-login.js
```

## 云压测 (k6 Cloud)

```bash
k6 login  # 登录 k6 Cloud
k6 run --out cloud k6-login.js
```
