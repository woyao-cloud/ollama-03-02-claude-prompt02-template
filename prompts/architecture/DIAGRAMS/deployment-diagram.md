# 部署图

使用 C4 模型 Level 4: Deployment Diagram

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#e1f5fe', 'primaryTextColor': '#01579b', 'primaryBorderColor': '#0288d1', 'lineColor': '#0288d1', 'secondaryColor': '#fff3e0', 'tertiaryColor': '#e8f5e9'}}}%%
C4Deployment
    title 部署图 - 生产环境

    Deployment_Node(internet, "Internet", "公网") {
        Deployment_Node(cdn, "CDN", "阿里云CDN/CloudFlare") {
            Container(static, "静态资源", "前端JS/CSS/图片")
        }
    }

    Deployment_Node(k8s, "Kubernetes集群", "K8s 1.28+") {
        Deployment_Node(ingress, "Ingress层", "Nginx Ingress Controller") {
            Container(ingressPod, "Ingress Pod", "Nginx", "负载均衡<br/>SSL终止<br/>限流")
        }

        Deployment_Node(app, "应用层", "应用服务") {
            Deployment_Node(auth, "认证服务", "3-10 Pods") {
                Container(authPod, "Auth Pod", "Spring Boot", "认证服务")
            }
            Deployment_Node(user, "用户服务", "3-10 Pods") {
                Container(userPod, "User Pod", "Spring Boot", "用户服务")
            }
            Deployment_Node(role, "角色权限服务", "3-10 Pods") {
                Container(rolePod, "Role Pod", "Spring Boot", "角色权限服务")
            }
            Deployment_Node(dept, "部门服务", "3-10 Pods") {
                Container(deptPod, "Dept Pod", "Spring Boot", "部门服务")
            }
            Deployment_Node(audit, "审计服务", "3-10 Pods") {
                Container(auditPod, "Audit Pod", "Spring Boot", "审计服务")
            }
        }
    }

    Deployment_Node(data, "数据层", "数据存储") {
        Deployment_Node(pg, "PostgreSQL集群", "主从架构") {
            ContainerDb(pgMaster, "PostgreSQL Master", "写操作")
            ContainerDb(pgSlave1, "PostgreSQL Replica 1", "读操作")
            ContainerDb(pgSlave2, "PostgreSQL Replica 2", "读操作")
        }

        Deployment_Node(redis, "Redis集群", "Cluster模式") {
            ContainerDb(redisNode1, "Redis Node 1", "Master")
            ContainerDb(redisNode2, "Redis Node 2", "Master")
            ContainerDb(redisNode3, "Redis Node 3", "Master")
        }

        Deployment_Node(kafkaCluster, "Kafka集群", "3节点") {
            ContainerDb(kafka1, "Kafka Broker 1", "消息队列")
            ContainerDb(kafka2, "Kafka Broker 2", "消息队列")
            ContainerDb(kafka3, "Kafka Broker 3", "消息队列")
        }
    }

    Deployment_Node(monitoring, "监控层", "可观测性") {
        Container(prometheus, "Prometheus", "指标收集")
        Container(grafana, "Grafana", "可视化")
        Container(elk, "ELK Stack", "日志聚合")
    }

    Rel(internet, ingressPod, "HTTPS", "TLS 1.3")
    Rel(ingressPod, authPod, "内部路由", "HTTP")
    Rel(ingressPod, userPod, "内部路由", "HTTP")
    Rel(ingressPod, rolePod, "内部路由", "HTTP")
    Rel(ingressPod, deptPod, "内部路由", "HTTP")
    Rel(ingressPod, auditPod, "内部路由", "HTTP")

    Rel(authPod, redis, "会话/缓存", "SSL")
    Rel(authPod, pgMaster, "读写", "SSL")

    Rel(userPod, redis, "缓存", "SSL")
    Rel(userPod, pgMaster, "写", "SSL")
    Rel(userPod, pgSlave1, "读", "SSL")

    Rel(rolePod, redis, "权限缓存", "SSL")
    Rel(rolePod, pgMaster, "写", "SSL")
    Rel(rolePod, pgSlave1, "读", "SSL")

    Rel(auditPod, kafkaCluster, "发送事件", "SSL")
    Rel(auditPod, pgSlave2, "读审计数据", "SSL")

    UpdateElementStyle(internet, $fontColor="#01579b", $bgColor="#e1f5fe", $borderColor="#0288d1")
    UpdateElementStyle(ingressPod, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(authPod, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(userPod, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(rolePod, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(deptPod, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(auditPod, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(pgMaster, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(pgSlave1, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(pgSlave2, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(redisNode1, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(redisNode2, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(redisNode3, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(kafka1, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(kafka2, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(kafka3, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
```

## 说明

### 部署拓扑

| 层级 | 组件 | 规格 | 数量 | 说明 |
|------|------|------|------|------|
| 接入层 | Nginx Ingress | 2CPU/4GB | 2-3 | 负载均衡，SSL终止 |
| 应用层 | Auth Service | 1CPU/2GB | 3-10 | JWT认证，HPA自动扩缩 |
| 应用层 | User Service | 1CPU/2GB | 3-10 | 用户管理，HPA自动扩缩 |
| 应用层 | Role Service | 1CPU/2GB | 3-10 | 权限管理，HPA自动扩缩 |
| 应用层 | Dept Service | 1CPU/2GB | 3-10 | 部门管理，HPA自动扩缩 |
| 应用层 | Audit Service | 1CPU/2GB | 3-10 | 审计服务，HPA自动扩缩 |
| 数据层 | PostgreSQL Master | 4CPU/16GB | 1 | 写操作 |
| 数据层 | PostgreSQL Replica | 4CPU/16GB | 2 | 读操作 |
| 数据层 | Redis Master | 2CPU/8GB | 3 | Cluster模式 |
| 数据层 | Kafka Broker | 4CPU/8GB | 3 | 消息队列 |

### 环境配置

| 环境 | 命名空间 | 副本数 | 数据库 |
|------|----------|--------|--------|
| Local | local | 1 | H2 |
| Dev | dev | 1 | PostgreSQL单节点 |
| SIT | sit | 2 | PostgreSQL主从 |
| UAT | uat | 3 | PostgreSQL主从 |
| Prod | prod | 5+ | PostgreSQL主从+只读 |

### 高可用设计

| 组件 | 高可用策略 | 故障恢复 |
|------|------------|----------|
| Nginx | 多实例部署 | K8s自动重启 |
| App Pods | HPA+多实例 | 自动扩缩容 |
| PostgreSQL | 主从复制 | 自动故障转移 |
| Redis | Cluster模式 | 自动故障转移 |
| Kafka | 多Broker+副本 | 自动重新选举 |

---

## 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0 | 2026-03-24 | 系统架构师 | 初始版本 |
