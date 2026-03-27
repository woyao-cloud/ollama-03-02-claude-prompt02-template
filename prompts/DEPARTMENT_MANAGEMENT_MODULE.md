# 部门管理模块设计文档

## 1. 概述

### 1.1 模块定位
部门管理模块是用户角色权限管理系统的核心组件，负责管理企业组织架构的树形结构，支持多层级部门管理、部门成员管理、部门权限继承等功能。

### 1.2 设计目标
1. **组织架构可视化**: 提供直观的部门树形结构管理界面
2. **高效管理**: 支持批量操作和快速部门结构调整
3. **权限集成**: 与角色权限模块深度集成，支持基于部门的权限控制
4. **高可用性**: 支持大型企业复杂组织架构（最多5级部门层级）
5. **审计完整**: 所有部门变更操作都有完整的审计日志

### 1.3 核心功能
- 部门树形结构管理（创建、编辑、删除、移动）
- 部门成员管理（查看、调整、负责人设置）
- 部门权限继承与覆盖
- 部门数据权限控制
- 部门结构调整历史记录
- 部门统计与报表

---

## 2. 架构设计

### 2.1 技术架构

```
部门管理模块架构
├── 前端层 (Next.js + TypeScript)
│   ├── 部门树形组件 (可拖拽树形视图)
│   ├── 部门成员管理界面
│   ├── 部门权限配置界面
│   └── 部门统计报表界面
├── 业务逻辑层 (Spring Boot)
│   ├── DepartmentService (部门业务逻辑)
│   ├── DepartmentMemberService (部门成员逻辑)
│   ├── DepartmentPermissionService (部门权限逻辑)
│   └── DepartmentAuditService (部门审计逻辑)
├── 数据访问层 (Spring Data JPA)
│   ├── DepartmentRepository (部门数据访问)
│   ├── DepartmentMemberRepository (部门成员关系)
│   └── DepartmentHistoryRepository (部门变更历史)
└── 数据存储层 (PostgreSQL + Redis)
    ├── PostgreSQL (持久化存储)
    └── Redis (部门树缓存、热点数据)
```

### 2.2 数据库设计

#### 2.2.1 部门表 (departments)
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY, AUTO_INCREMENT |
| code | VARCHAR(50) | 部门代码 | UNIQUE, NOT NULL |
| name | VARCHAR(100) | 部门名称 | NOT NULL |
| description | TEXT | 部门描述 | NULLABLE |
| parent_id | BIGINT | 上级部门ID | FOREIGN KEY, NULLABLE |
| path | VARCHAR(500) | 材质化路径 | NOT NULL, INDEXED |
| level | INT | 部门层级 | NOT NULL, CHECK(level <= 5) |
| manager_id | BIGINT | 部门负责人ID | FOREIGN KEY to users |
| order_index | INT | 同级排序索引 | DEFAULT 0 |
| status | VARCHAR(20) | 状态 | DEFAULT 'ACTIVE' |
| created_at | TIMESTAMP | 创建时间 | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | 更新时间 | DEFAULT CURRENT_TIMESTAMP |
| created_by | BIGINT | 创建人 | FOREIGN KEY to users |
| updated_by | BIGINT | 更新人 | FOREIGN KEY to users |

#### 2.2.2 部门成员表 (department_members)
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY, AUTO_INCREMENT |
| department_id | BIGINT | 部门ID | FOREIGN KEY, NOT NULL |
| user_id | BIGINT | 用户ID | FOREIGN KEY, NOT NULL |
| is_manager | BOOLEAN | 是否为负责人 | DEFAULT FALSE |
| start_date | DATE | 加入日期 | DEFAULT CURRENT_DATE |
| end_date | DATE | 离开日期 | NULLABLE |
| created_at | TIMESTAMP | 创建时间 | DEFAULT CURRENT_TIMESTAMP |
| created_by | BIGINT | 创建人 | FOREIGN KEY to users |

#### 2.2.3 部门变更历史表 (department_changes)
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY, AUTO_INCREMENT |
| department_id | BIGINT | 部门ID | FOREIGN KEY, NOT NULL |
| change_type | VARCHAR(50) | 变更类型 | NOT NULL |
| old_data | JSONB | 变更前数据 | NULLABLE |
| new_data | JSONB | 变更后数据 | NULLABLE |
| changed_by | BIGINT | 变更人 | FOREIGN KEY to users |
| changed_at | TIMESTAMP | 变更时间 | DEFAULT CURRENT_TIMESTAMP |
| ip_address | VARCHAR(45) | IP地址 | NULLABLE |

### 2.3 缓存设计

#### 2.3.1 Redis缓存策略
- **部门树缓存**: 完整的部门树形结构，缓存时间15分钟
- **部门信息缓存**: 单个部门信息，缓存时间5分钟
- **部门成员缓存**: 部门成员列表，缓存时间10分钟
- **部门路径缓存**: 部门路径映射，缓存时间30分钟

#### 2.3.2 缓存键设计
```
department:tree:{tenantId}           # 完整部门树
department:info:{departmentId}       # 部门详细信息
department:members:{departmentId}    # 部门成员列表
department:path:{departmentId}       # 部门路径信息
department:children:{departmentId}   # 子部门列表
```

---

## 3. 核心功能设计

### 3.1 部门树形结构管理

#### 3.1.1 部门创建
**接口**: `POST /api/departments`
**请求参数**:
```json
{
  "code": "IT",
  "name": "信息技术部",
  "description": "负责公司信息技术基础设施",
  "parentId": 1,
  "managerId": 1001,
  "orderIndex": 0
}
```

**业务规则**:
1. 部门代码必须唯一
2. 部门层级不能超过5级
3. 创建后自动生成path字段（格式: .1.2.3.）
4. 自动设置level字段
5. 记录创建审计日志

#### 3.1.2 部门更新
**接口**: `PUT /api/departments/{id}`
**特殊处理**:
1. 更新parentId时，需要递归更新所有子部门的path和level
2. 部门负责人变更时，需要同步更新权限
3. 记录完整的变更历史

#### 3.1.3 部门删除
**接口**: `DELETE /api/departments/{id}`
**删除策略**:
1. **软删除**: 标记为INACTIVE状态
2. **级联处理**: 下级部门需要同时处理
3. **成员迁移**: 部门成员需要重新分配到其他部门
4. **权限清理**: 清理相关的权限配置

#### 3.1.4 部门移动（调整层级）
**接口**: `POST /api/departments/{id}/move`
**请求参数**:
```json
{
  "newParentId": 2,
  "orderIndex": 0
}
```

**业务逻辑**:
1. 验证新父部门存在且层级有效
2. 递归更新所有子部门的path和level
3. 更新部门树缓存
4. 记录结构调整审计日志

### 3.2 部门成员管理

#### 3.2.1 添加部门成员
**接口**: `POST /api/departments/{id}/members`
**请求参数**:
```json
{
  "userId": 1001,
  "isManager": false
}
```

**业务规则**:
1. 用户不能重复加入同一部门
2. 设置部门负责人时，自动取消原负责人
3. 支持批量添加成员

#### 3.2.2 移除部门成员
**接口**: `DELETE /api/departments/{id}/members/{userId}`
**业务规则**:
1. 部门负责人不能被直接移除，需要先更换负责人
2. 记录成员变动审计日志
3. 清理相关的部门数据权限

#### 3.2.3 调整部门成员
**接口**: `POST /api/users/{userId}/department`
**请求参数**:
```json
{
  "newDepartmentId": 2,
  "effectiveDate": "2026-03-27"
}
```

**业务规则**:
1. 自动从原部门移除
2. 添加到新部门
3. 更新用户的数据权限范围
4. 发送通知给相关部门负责人

### 3.3 部门权限管理

#### 3.3.1 部门权限继承
**设计原则**:
1. 下级部门默认继承上级部门的所有权限
2. 下级部门可以覆盖上级部门的权限设置
3. 权限继承关系可视化展示

#### 3.3.2 部门数据权限
**数据范围类型**:
1. **全部数据**: 可访问所有部门数据
2. **本部门数据**: 只能访问本部门数据
3. **本部门及下级部门**: 可访问本部门及所有下级部门数据
4. **个人数据**: 只能访问个人创建的数据

**实现方式**:
1. 在数据查询时自动添加部门过滤条件
2. 基于用户所在部门动态构建数据范围
3. 支持复杂的多部门数据权限组合

#### 3.3.3 部门角色关联
**关联关系**:
1. 部门可以关联多个角色
2. 部门成员自动获得部门关联的角色
3. 部门角色优先级高于个人角色

---

## 4. 接口设计

### 4.1 RESTful API 列表

#### 4.1.1 部门管理接口
| 方法 | 端点 | 描述 | 权限要求 |
|------|------|------|----------|
| GET | `/api/departments` | 获取部门列表（支持树形结构） | 部门查看权限 |
| GET | `/api/departments/{id}` | 获取部门详情 | 部门查看权限 |
| POST | `/api/departments` | 创建部门 | 部门管理权限 |
| PUT | `/api/departments/{id}` | 更新部门信息 | 部门管理权限 |
| DELETE | `/api/departments/{id}` | 删除部门 | 部门管理权限 |
| POST | `/api/departments/{id}/move` | 移动部门位置 | 部门管理权限 |
| GET | `/api/departments/{id}/tree` | 获取部门子树 | 部门查看权限 |

#### 4.1.2 部门成员接口
| 方法 | 端点 | 描述 | 权限要求 |
|------|------|------|----------|
| GET | `/api/departments/{id}/members` | 获取部门成员列表 | 部门成员查看权限 |
| POST | `/api/departments/{id}/members` | 添加部门成员 | 部门成员管理权限 |
| DELETE | `/api/departments/{id}/members/{userId}` | 移除部门成员 | 部门成员管理权限 |
| PUT | `/api/departments/{id}/manager` | 设置部门负责人 | 部门管理权限 |
| GET | `/api/users/{userId}/departments` | 获取用户所在部门 | 用户查看权限 |

#### 4.1.3 部门权限接口
| 方法 | 端点 | 描述 | 权限要求 |
|------|------|------|----------|
| GET | `/api/departments/{id}/permissions` | 获取部门权限配置 | 权限查看权限 |
| POST | `/api/departments/{id}/permissions` | 配置部门权限 | 权限管理权限 |
| GET | `/api/departments/{id}/inherited-permissions` | 获取继承的权限 | 权限查看权限 |
| POST | `/api/departments/{id}/override-permission` | 覆盖上级权限 | 权限管理权限 |

### 4.2 接口响应格式

#### 4.2.1 成功响应
```json
{
  "success": true,
  "data": {
    // 业务数据
  },
  "message": "操作成功",
  "timestamp": "2026-03-27T10:30:00Z"
}
```

#### 4.2.2 错误响应
```json
{
  "success": false,
  "error": {
    "code": "DEPARTMENT_NOT_FOUND",
    "message": "部门不存在",
    "details": "部门ID 100 不存在"
  },
  "timestamp": "2026-03-27T10:30:00Z"
}
```

### 4.3 分页与筛选

#### 4.3.1 部门列表分页
```json
{
  "page": 1,
  "size": 20,
  "total": 150,
  "items": [
    // 部门列表
  ],
  "filters": {
    "name": "技术",
    "status": "ACTIVE",
    "level": 2
  }
}
```

---

## 5. 前端组件设计

### 5.1 部门树形组件 (DepartmentTree)

#### 5.1.1 组件特性
- 可拖拽调整部门位置
- 展开/折叠部门子树
- 右键菜单操作（创建、编辑、删除）
- 实时搜索过滤
- 懒加载子部门

#### 5.1.2 组件接口
```typescript
interface DepartmentTreeProps {
  data: DepartmentNode[];
  onSelect: (node: DepartmentNode) => void;
  onDragEnd: (sourceId: number, targetId: number) => void;
  onContextMenu: (node: DepartmentNode, event: React.MouseEvent) => void;
  searchKeyword?: string;
  expandAll?: boolean;
}

interface DepartmentNode {
  id: number;
  code: string;
  name: string;
  level: number;
  children?: DepartmentNode[];
  isLeaf: boolean;
  manager?: UserInfo;
}
```

### 5.2 部门成员表格组件 (DepartmentMemberTable)

#### 5.2.1 组件特性
- 分页显示部门成员
- 支持按姓名、职位搜索
- 批量操作（添加、移除）
- 导出成员列表
- 设置部门负责人

#### 5.2.2 组件接口
```typescript
interface DepartmentMemberTableProps {
  departmentId: number;
  members: DepartmentMember[];
  onAddMember: (userId: number) => Promise<void>;
  onRemoveMember: (userId: number) => Promise<void>;
  onSetManager: (userId: number) => Promise<void>;
  onExport: () => void;
}

interface DepartmentMember {
  userId: number;
  userName: string;
  email: string;
  position: string;
  isManager: boolean;
  joinDate: string;
}
```

### 5.3 部门权限配置组件 (DepartmentPermissionConfig)

#### 5.3.1 组件特性
- 可视化权限继承关系
- 权限开关控制
- 权限覆盖提示
- 批量权限配置
- 权限测试功能

---

## 6. 性能优化

### 6.1 数据库优化

#### 6.1.1 索引设计
```sql
-- 部门路径索引（快速查询子树）
CREATE INDEX idx_departments_path ON departments(path);

-- 部门层级索引（按层级查询）
CREATE INDEX idx_departments_level ON departments(level);

-- 部门父ID索引（查询子部门）
CREATE INDEX idx_departments_parent_id ON departments(parent_id);

-- 部门成员复合索引
CREATE INDEX idx_department_members_department_user ON department_members(department_id, user_id);
```

#### 6.1.2 查询优化
1. **子树查询优化**: 使用path LIKE查询替代递归查询
2. **懒加载策略**: 前端按需加载子部门
3. **批量操作**: 支持批量部门结构调整
4. **异步处理**: 复杂的部门结构调整使用异步任务

### 6.2 缓存优化

#### 6.2.1 多级缓存策略
1. **一级缓存**: Redis缓存热点部门数据
2. **二级缓存**: 应用内缓存部门树结构
3. **三级缓存**: 前端缓存用户相关部门信息

#### 6.2.2 缓存更新策略
1. **主动更新**: 部门变更时主动刷新相关缓存
2. **被动失效**: 缓存设置合理TTL，自动失效
3. **批量刷新**: 批量操作后批量刷新缓存

### 6.3 并发处理

#### 6.3.1 乐观锁机制
```java
@Entity
public class Department {
    @Version
    private Long version;

    // 其他字段
}
```

#### 6.3.2 分布式锁
部门结构调整等敏感操作使用分布式锁，防止并发修改导致数据不一致。

---

## 7. 安全设计

### 7.1 权限控制

#### 7.1.1 操作权限
| 操作 | 所需权限 | 说明 |
|------|----------|------|
| 查看部门 | `department:read` | 查看部门信息 |
| 创建部门 | `department:create` | 创建新部门 |
| 编辑部门 | `department:update` | 编辑部门信息 |
| 删除部门 | `department:delete` | 删除部门 |
| 移动部门 | `department:move` | 调整部门位置 |
| 管理成员 | `department:member:manage` | 管理部门成员 |
| 配置权限 | `department:permission:manage` | 配置部门权限 |

#### 7.1.2 数据权限
1. **部门数据隔离**: 用户只能访问自己所在部门及下级部门数据
2. **跨部门限制**: 默认不能访问其他平行部门数据
3. **敏感部门保护**: 财务、HR等敏感部门额外权限控制

### 7.2 审计日志

#### 7.2.1 审计内容
1. **部门结构变更**: 创建、更新、删除、移动部门
2. **部门成员变更**: 添加、移除、调整部门成员
3. **部门权限变更**: 权限配置、覆盖、继承关系变更
4. **部门负责人变更**: 设置、更换部门负责人

#### 7.2.2 审计要求
1. **完整性**: 所有敏感操作必须有审计记录
2. **不可篡改**: 审计日志一旦记录不能修改
3. **可追溯**: 能够追溯到操作人、时间、IP等信息
4. **长期保存**: 审计日志至少保存3年

### 7.3 输入验证

#### 7.3.1 部门信息验证
```java
public class DepartmentCreateRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$")
    private String code;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Max(value = 5, message = "部门层级不能超过5级")
    private Integer level;

    // 其他字段验证
}
```

#### 7.3.2 业务规则验证
1. **层级限制**: 部门层级不能超过5级
2. **循环引用**: 不能设置子部门为父部门
3. **唯一性约束**: 部门代码必须唯一
4. **状态检查**: 只能对活跃部门进行操作

---

## 8. 测试策略

### 8.1 单元测试

#### 8.1.1 测试范围
1. **DepartmentService**: 部门业务逻辑测试
2. **DepartmentValidator**: 部门数据验证测试
3. **DepartmentCacheService**: 部门缓存逻辑测试
4. **DepartmentPermissionService**: 部门权限逻辑测试

#### 8.1.2 测试用例
```java
@Test
void testCreateDepartment_withValidData_shouldSuccess() {
    // 测试创建部门的成功场景
}

@Test
void testCreateDepartment_exceedMaxLevel_shouldFail() {
    // 测试超过最大层级的失败场景
}

@Test
void testMoveDepartment_withCircularReference_shouldFail() {
    // 测试循环引用的失败场景
}
```

### 8.2 集成测试

#### 8.2.1 API测试
1. **部门管理API**: 测试RESTful接口的正确性
2. **权限控制测试**: 测试接口的权限验证
3. **并发操作测试**: 测试并发下的数据一致性
4. **错误处理测试**: 测试异常场景的错误响应

#### 8.2.2 数据库测试
1. **数据一致性测试**: 测试部门树形结构的数据一致性
2. **事务测试**: 测试复杂操作的事务完整性
3. **性能测试**: 测试大数据量下的查询性能

### 8.3 端到端测试

#### 8.3.1 用户流程测试
1. **部门创建流程**: 从创建到显示的全流程测试
2. **部门结构调整流程**: 测试部门移动和层级调整
3. **部门成员管理流程**: 测试成员添加、移除、调整
4. **部门权限配置流程**: 测试权限配置和继承

#### 8.3.2 性能测试
1. **压力测试**: 测试高并发下的部门操作性能
2. **负载测试**: 测试大数据量下的部门查询性能
3. **稳定性测试**: 测试长时间运行的稳定性

---

## 9. 部署与运维

### 9.1 部署要求

#### 9.1.1 硬件要求
| 组件 | 最低配置 | 推荐配置 | 说明 |
|------|----------|----------|------|
| 应用服务器 | 2核4G | 4核8G | 处理业务逻辑 |
| 数据库服务器 | 2核8G | 4核16G | 存储部门数据 |
| Redis服务器 | 1核2G | 2核4G | 缓存热点数据 |

#### 9.1.2 软件要求
- **JDK**: 21+
- **PostgreSQL**: 15+
- **Redis**: 7+
- **Spring Boot**: 3.5+
- **Node.js**: 18+ (前端)

### 9.2 监控与告警

#### 9.2.1 监控指标
1. **部门操作成功率**: 部门创建、更新、删除的成功率
2. **部门查询响应时间**: 部门树查询、部门详情查询的响应时间
3. **部门缓存命中率**: Redis缓存的命中率
4. **部门数据一致性**: 部门树形结构的数据一致性检查

#### 9.2.2 告警规则
1. **部门操作失败率**: 连续10次操作失败率 > 5%
2. **部门查询超时**: 部门树查询响应时间 > 1s
3. **部门数据不一致**: 检测到部门树数据不一致
4. **部门缓存异常**: 缓存命中率 < 80%

### 9.3 备份与恢复

#### 9.3.1 数据备份策略
1. **全量备份**: 每天凌晨进行全量备份
2. **增量备份**: 每小时进行增量备份
3. **事务日志备份**: 每15分钟备份事务日志

#### 9.3.2 恢复流程
1. **数据恢复**: 从备份恢复部门数据
2. **缓存重建**: 重新构建部门缓存
3. **数据一致性校验**: 验证部门树形结构的完整性
4. **业务验证**: 验证部门管理功能正常

---

## 10. 附录

### 10.1 错误码定义

| 错误码 | 描述 | HTTP状态码 | 处理建议 |
|--------|------|-------------|----------|
| DEP_001 | 部门不存在 | 404 | 检查部门ID是否正确 |
| DEP_002 | 部门代码已存在 | 409 | 使用其他部门代码 |
| DEP_003 | 部门层级超过限制 | 400 | 调整部门层级 |
| DEP_004 | 循环引用错误 | 400 | 检查部门父子关系 |
| DEP_005 | 部门状态无效 | 400 | 检查部门状态 |
| DEP_006 | 权限不足 | 403 | 检查用户权限 |
| DEP_007 | 并发修改冲突 | 409 | 重试操作 |
| DEP_008 | 部门成员已存在 | 409 | 用户已在该部门 |
| DEP_009 | 部门负责人不能移除 | 400 | 先更换负责人 |

### 10.2 数据迁移脚本示例

#### 10.2.1 初始化部门表
```sql
-- 创建根部门
INSERT INTO departments (code, name, path, level, status)
VALUES ('ROOT', '根部门', '.', 0, 'ACTIVE');

-- 创建示例部门结构
INSERT INTO departments (code, name, parent_id, path, level, status)
VALUES
  ('IT', '信息技术部', 1, '.1.', 1, 'ACTIVE'),
  ('HR', '人力资源部', 1, '.2.', 1, 'ACTIVE'),
  ('DEV', '开发组', 2, '.1.2.', 2, 'ACTIVE');
```

#### 10.2.2 部门结构调整脚本
```sql
-- 移动部门并更新所有子部门的路径
WITH RECURSIVE department_tree AS (
  SELECT id, parent_id, path, level
  FROM departments
  WHERE path LIKE '%.1.2.%'  -- 要移动的部门及其子部门

  UNION ALL

  SELECT d.id, d.parent_id, d.path, d.level
  FROM departments d
  INNER JOIN department_tree dt ON d.parent_id = dt.id
)
-- 更新部门路径和层级
UPDATE departments d
SET
  parent_id = CASE WHEN d.id = 2 THEN 3 ELSE d.parent_id END,  -- 移动目标部门
  path = REPLACE(d.path, '.1.2.', '.3.2.'),  -- 更新路径
  level = LENGTH(REPLACE(d.path, '.1.2.', '.3.2.')) - LENGTH(REPLACE(REPLACE(d.path, '.1.2.', '.3.2.'), '.', '')) - 1
FROM department_tree dt
WHERE d.id = dt.id;
```

### 10.3 性能测试报告模板

#### 10.3.1 测试环境
- 服务器配置: 4核8G
- 数据库: PostgreSQL 15，配置16G内存
- 缓存: Redis 7，配置4G内存
- 网络: 内网千兆网络

#### 10.3.2 测试结果
| 测试场景 | 并发用户数 | 平均响应时间 | 成功率 | TPS |
|----------|------------|--------------|--------|-----|
| 部门树查询 | 100 | 85ms | 100% | 1176 |
| 部门创建 | 50 | 120ms | 100% | 417 |
| 部门移动 | 20 | 350ms | 100% | 57 |
| 部门成员查询 | 200 | 95ms | 100% | 2105 |

### 10.4 版本历史

| 版本 | 日期 | 描述 | 作者 |
|------|------|------|------|
| 1.0.0 | 2026-03-27 | 初始版本，包含基础部门管理功能 | 产品团队 |
| 1.1.0 | 2026-04-30 | 增加部门权限继承功能 | 开发团队 |
| 1.2.0 | 2026-05-31 | 优化部门树形结构性能 | 性能优化团队 |
| 1.3.0 | 2026-06-30 | 增加部门统计报表功能 | 数据分析团队 |

---

**文档维护**: 产品部 & 技术部
**最后更新**: 2026-03-27
**下一版本计划**: 2026-04-30（部门权限继承功能）