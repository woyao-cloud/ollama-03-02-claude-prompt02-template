# 前端开发任务清单 (Frontend Task List)

## 概述
基于全栈用户角色权限管理系统的需求文档，整理出的前端开发任务清单。涵盖用户管理、部门管理、角色权限管理、审计日志、系统配置等六大模块。

**技术栈**: Next.js 16 + TypeScript + Tailwind CSS + shadcn/ui + Zustand + Axios
**兼容性**: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+，响应式设计（≥320px）
**性能要求**: 页面加载 < 2s，API集成符合后端性能指标

---

## 1. 用户管理模块 (User Management)

### 1.1 用户列表页面 (`/users`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| USR-001 | 用户列表展示（分页、筛选、排序） | P0 | 待实现 | DataTable组件，筛选表单 | GET /api/users | userStore | 单元测试，E2E测试 |
| USR-002 | 用户详情查看弹窗/页面 | P0 | 待实现 | UserDetailDialog | GET /api/users/{id} | userStore | 单元测试 |
| USR-003 | 创建用户表单 | P0 | 待实现 | UserForm（模态框） | POST /api/users | userStore | 表单验证测试 |
| USR-004 | 编辑用户表单 | P0 | 待实现 | UserForm（模态框） | PUT /api/users/{id} | userStore | 表单验证测试 |
| USR-005 | 删除用户（软删除）确认 | P0 | 待实现 | ConfirmationDialog | DELETE /api/users/{id} | userStore | 交互测试 |

### 1.2 用户批量操作 (`/users/batch`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| USR-006 | 批量导入用户（Excel/CSV上传） | P1 | 待实现 | FileUpload组件，导入进度显示 | POST /api/users/import | userStore | 文件上传测试 |
| USR-007 | 批量导出用户（Excel/CSV下载） | P1 | 待实现 | ExportButton，导出配置 | GET /api/users/export | userStore | 导出功能测试 |
| USR-008 | 批量状态更新（启用/禁用） | P1 | 待实现 | BatchActions工具栏 | PUT /api/users/batch-status | userStore | 批量操作测试 |

### 1.3 用户个人中心 (`/profile`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| USR-009 | 个人资料查看/编辑 | P1 | 待实现 | ProfileForm，头像上传 | GET/PUT /api/profile | authStore, profileStore | 表单验证测试 |
| USR-010 | 登录历史查看 | P1 | 待实现 | LoginHistoryTable，时间筛选 | GET /api/profile/login-history | profileStore | 列表测试 |
| USR-011 | 修改密码 | P0 | 待实现 | ChangePasswordForm | PUT /api/profile/password | authStore | 密码策略测试 |
| USR-012 | 安全设置（会话管理） | P1 | 待实现 | SessionList，强制下线 | GET/DELETE /api/profile/sessions | authStore | 安全功能测试 |

### 1.4 用户自助注册 (`/register`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| USR-013 | 用户注册表单 | P1 | 待实现 | RegistrationForm，验证码 | POST /api/auth/register | authStore | 注册流程测试 |
| USR-014 | 邮箱激活页面 | P1 | 待实现 | ActivationPage | GET /api/auth/activate | authStore | 激活流程测试 |

---

## 2. 部门管理模块 (Department Management)

### 2.1 组织架构页面 (`/departments`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| DEPT-001 | 部门树形结构展示 | P0 | 待实现 | TreeView组件，展开/折叠 | GET /api/departments/tree | departmentStore | 树形组件测试 |
| DEPT-002 | 部门详情面板 | P0 | 待实现 | DepartmentDetailPanel | GET /api/departments/{id} | departmentStore | 详情展示测试 |
| DEPT-003 | 创建部门表单 | P0 | 待实现 | DepartmentForm（模态框） | POST /api/departments | departmentStore | 表单验证测试 |
| DEPT-004 | 编辑部门表单 | P0 | 待实现 | DepartmentForm（模态框） | PUT /api/departments/{id} | departmentStore | 表单验证测试 |
| DEPT-005 | 删除部门确认（检查约束） | P0 | 待实现 | DeleteDepartmentDialog | DELETE /api/departments/{id} | departmentStore | 约束检查测试 |
| DEPT-006 | 部门拖拽排序/层级调整 | P1 | 待实现 | DragDropTree，层级调整UI | PUT /api/departments/reorder | departmentStore | 拖拽交互测试 |

### 2.2 部门成员管理 (`/departments/{id}/members`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| DEPT-007 | 部门成员列表 | P1 | 待实现 | MemberList，分页筛选 | GET /api/departments/{id}/members | departmentStore | 列表测试 |
| DEPT-008 | 添加成员到部门 | P1 | 待实现 | AddMemberDialog，用户选择 | POST /api/departments/{id}/members | departmentStore, userStore | 用户选择测试 |
| DEPT-009 | 从部门移除成员 | P1 | 待实现 | RemoveMemberDialog | DELETE /api/departments/{id}/members/{userId} | departmentStore | 移除操作测试 |

---

## 3. 角色权限管理模块 (Role & Permission Management)

### 3.1 角色管理页面 (`/roles`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| ROLE-001 | 角色列表展示 | P0 | 待实现 | RoleTable，筛选排序 | GET /api/roles | roleStore | 列表测试 |
| ROLE-002 | 创建角色表单 | P0 | 待实现 | RoleForm（包含基本信息） | POST /api/roles | roleStore | 表单验证测试 |
| ROLE-003 | 编辑角色表单 | P0 | 待实现 | RoleForm（模态框） | PUT /api/roles/{id} | roleStore | 表单验证测试 |
| ROLE-004 | 删除角色确认 | P0 | 待实现 | DeleteRoleDialog | DELETE /api/roles/{id} | roleStore | 删除确认测试 |

### 3.2 权限管理页面 (`/permissions`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| PERM-001 | 权限树形结构展示 | P0 | 待实现 | PermissionTree（四级权限） | GET /api/permissions/tree | permissionStore | 树形结构测试 |
| PERM-002 | 权限详情查看 | P0 | 待实现 | PermissionDetailPanel | GET /api/permissions/{id} | permissionStore | 详情测试 |
| PERM-003 | 创建权限项 | P0 | 待实现 | PermissionForm（选择类型） | POST /api/permissions | permissionStore | 表单验证测试 |
| PERM-004 | 编辑权限项 | P0 | 待实现 | PermissionForm（模态框） | PUT /api/permissions/{id} | permissionStore | 表单验证测试 |

### 3.3 角色权限分配 (`/roles/{id}/permissions`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| ROLE-005 | 角色权限分配界面 | P0 | 待实现 | RolePermissionAssignment（树形选择） | GET/PUT /api/roles/{id}/permissions | roleStore, permissionStore | 分配逻辑测试 |
| ROLE-006 | 数据权限范围配置 | P1 | 待实现 | DataScopeSelector（ALL/DEPT/SELF/CUSTOM） | PUT /api/roles/{id}/data-scope | roleStore | 数据范围测试 |
| ROLE-007 | 字段权限配置 | P1 | 待实现 | FieldPermissionMatrix（可读/可写） | PUT /api/roles/{id}/field-permissions | roleStore | 字段权限测试 |

### 3.4 用户角色分配 (`/users/{id}/roles`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| USR-015 | 用户角色分配界面 | P0 | 待实现 | UserRoleAssignment（多选角色） | GET/PUT /api/users/{id}/roles | userStore, roleStore | 分配逻辑测试 |
| USR-016 | 批量用户角色分配 | P1 | 待实现 | BatchRoleAssignment（选择用户+角色） | PUT /api/users/batch-roles | userStore, roleStore | 批量分配测试 |

### 3.5 权限模板管理 (`/permission-templates`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| TEMP-001 | 权限模板列表 | P1 | 待实现 | TemplateList，搜索筛选 | GET /api/permission-templates | templateStore | 列表测试 |
| TEMP-002 | 创建权限模板 | P1 | 待实现 | TemplateForm（选择权限集） | POST /api/permission-templates | templateStore | 模板创建测试 |
| TEMP-003 | 应用模板到角色 | P1 | 待实现 | ApplyTemplateDialog | POST /api/roles/{id}/apply-template | roleStore, templateStore | 模板应用测试 |

### 3.6 角色继承管理 (`/roles/{id}/inheritance`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| ROLE-008 | 角色继承关系配置 | P1 | 待实现 | InheritanceGraph（防止循环） | GET/PUT /api/roles/{id}/parents | roleStore | 继承关系测试 |
| ROLE-009 | 继承权限预览 | P1 | 待实现 | InheritedPermissionsView | GET /api/roles/{id}/effective-permissions | roleStore | 权限计算测试 |

---

## 4. 审计日志模块 (Audit Log)

### 4.1 操作日志页面 (`/audit-logs`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| AUDIT-001 | 审计日志列表（高级筛选） | P0 | 待实现 | AuditLogTable（时间范围、用户、操作类型） | GET /api/audit-logs | auditStore | 筛选功能测试 |
| AUDIT-002 | 日志详情查看 | P0 | 待实现 | AuditLogDetail（前后数据对比） | GET /api/audit-logs/{id} | auditStore | 详情展示测试 |
| AUDIT-003 | 批量日志导出（Excel/PDF/CSV） | P1 | 待实现 | ExportAuditLog（异步任务，进度显示） | POST /api/audit-logs/export | auditStore | 导出功能测试 |
| AUDIT-004 | 登录日志查看 | P0 | 待实现 | LoginLogTable（IP、设备、地点） | GET /api/audit-logs/login | auditStore | 登录日志测试 |

### 4.2 实时告警面板 (`/audit-alerts`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| AUDIT-005 | 实时告警列表（WebSocket） | P1 | 待实现 | AlertFeed（实时更新） | WebSocket /api/alerts | alertStore | 实时更新测试 |
| AUDIT-006 | 告警配置页面 | P1 | 待实现 | AlertConfigForm（阈值、通知方式） | GET/PUT /api/alert-config | alertStore | 配置测试 |

---

## 5. 系统配置模块 (System Configuration)

### 5.1 安全策略配置 (`/config/security`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| CONFIG-001 | 密码策略配置 | P0 | 待实现 | PasswordPolicyForm（长度、复杂度、有效期） | GET/PUT /api/config/password-policy | configStore | 策略配置测试 |
| CONFIG-002 | 登录策略配置 | P0 | 待实现 | LoginPolicyForm（失败锁定、会话超时） | GET/PUT /api/config/login-policy | configStore | 登录策略测试 |
| CONFIG-003 | 网络安全配置 | P0 | 待实现 | NetworkSecurityForm（HTTPS、CSRF、限流） | GET/PUT /api/config/network-security | configStore | 网络安全测试 |

### 5.2 邮件服务配置 (`/config/email`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| CONFIG-004 | SMTP服务器配置 | P1 | 待实现 | EmailConfigForm（测试连接功能） | GET/PUT /api/config/email | configStore | 邮件配置测试 |
| CONFIG-005 | 邮件模板管理 | P1 | 待实现 | EmailTemplateEditor（变量替换） | GET/PUT /api/config/email-templates | configStore | 模板编辑测试 |

### 5.3 性能配置管理 (`/config/performance`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| CONFIG-006 | 缓存配置管理 | P1 | 待实现 | CacheConfigForm（Redis有效期） | GET/PUT /api/config/cache | configStore | 缓存配置测试 |
| CONFIG-007 | 数据库连接池配置 | P1 | 待实现 | DatabaseConfigForm（连接数、超时） | GET/PUT /api/config/database | configStore | 数据库配置测试 |
| CONFIG-008 | 性能监控仪表板 | P1 | 待实现 | PerformanceDashboard（图表） | GET /api/metrics | configStore | 监控图表测试 |

### 5.4 系统信息页面 (`/system/info`)
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| SYS-001 | 系统状态概览 | P0 | 待实现 | SystemStatusCard（健康检查） | GET /api/system/health | systemStore | 状态监控测试 |
| SYS-002 | 服务版本信息 | P0 | 待实现 | VersionInfoPanel | GET /api/system/version | systemStore | 版本信息测试 |

---

## 6. 认证授权模块 (Authentication & Authorization)

### 6.1 登录页面 (`/login`) - 已有基础，需增强
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| AUTH-001 | 增强登录表单（记住我、验证码） | P0 | 待实现 | EnhancedLoginForm | POST /api/auth/login | authStore | 登录流程测试 |
| AUTH-002 | 忘记密码流程 | P0 | 待实现 | ForgotPasswordFlow（邮箱验证） | POST /api/auth/forgot-password | authStore | 密码重置测试 |
| AUTH-003 | 双因素认证页面（TOTP/短信） | P1 | 待实现 | TwoFactorAuthForm | POST /api/auth/2fa | authStore | 2FA流程测试 |
| AUTH-004 | OAuth2第三方登录 | P1 | 待实现 | OAuthButtons（Google/GitHub） | GET /api/auth/oauth2/{provider} | authStore | OAuth集成测试 |

### 6.2 Token管理
| 任务ID | 任务描述 | 优先级 | 状态 | 组件需求 | API集成 | 状态管理 | 测试需求 |
|--------|----------|--------|------|----------|----------|----------|----------|
| AUTH-005 | Token自动刷新机制 | P0 | 待实现 | TokenRefreshInterceptor | POST /api/auth/refresh | authStore | Token刷新测试 |
| AUTH-006 | 会话管理界面 | P1 | 待实现 | SessionManagement（查看/强制下线） | GET/DELETE /api/auth/sessions | authStore | 会话管理测试 |

---

## 7. 通用组件库 (Shared Components)

### 7.1 基础UI组件
| 任务ID | 任务描述 | 优先级 | 状态 | 依赖 | 用途 |
|--------|----------|--------|------|------|------|
| COMP-001 | 增强DataTable（排序、筛选、分页） | P0 | 待实现 | shadcn/ui | 所有列表页面 |
| COMP-002 | TreeView组件（展开/折叠、选择） | P0 | 待实现 | shadcn/ui | 部门树、权限树 |
| COMP-003 | 文件上传组件（进度显示、格式验证） | P1 | 待实现 | shadcn/ui | 批量导入、头像上传 |
| COMP-004 | 图表组件（性能监控） | P1 | 待实现 | Recharts | 监控仪表板 |
| COMP-005 | 富文本编辑器（邮件模板） | P1 | 待实现 | TipTap | 邮件模板编辑 |

### 7.2 业务组件
| 任务ID | 任务描述 | 优先级 | 状态 | 依赖 | 用途 |
|--------|----------|--------|------|------|------|
| COMP-006 | 用户选择器（搜索、多选） | P0 | 待实现 | shadcn/ui + API | 分配用户到部门/角色 |
| COMP-007 | 部门选择器（树形选择） | P0 | 待实现 | TreeView + API | 用户创建/编辑 |
| COMP-008 | 角色选择器（多选、搜索） | P0 | 待实现 | shadcn/ui + API | 用户角色分配 |
| COMP-009 | 权限选择器（树形多选） | P0 | 待实现 | TreeView + API | 角色权限分配 |

---

## 8. 状态管理 (State Management)

### 8.1 Store模块
| 任务ID | 任务描述 | 优先级 | 状态 | 功能 | 相关页面 |
|--------|----------|--------|------|------|----------|
| STORE-001 | authStore增强（Token管理、会话） | P0 | 待实现 | 登录状态、Token刷新、会话管理 | 所有需要认证的页面 |
| STORE-002 | userStore（用户CRUD、批量操作） | P0 | 待实现 | 用户列表、详情、创建、更新、删除 | /users, /profile |
| STORE-003 | departmentStore（部门树、成员） | P0 | 待实现 | 部门树、详情、成员管理 | /departments |
| STORE-004 | roleStore（角色、权限分配） | P0 | 待实现 | 角色列表、权限分配、数据范围 | /roles, /permissions |
| STORE-005 | permissionStore（权限树） | P0 | 待实现 | 权限树形结构、详情 | /permissions |
| STORE-006 | auditStore（日志查询、导出） | P0 | 待实现 | 日志列表、筛选、导出 | /audit-logs |
| STORE-007 | configStore（系统配置） | P0 | 待实现 | 各种配置项的读取和更新 | /config/* |
| STORE-008 | templateStore（权限模板） | P1 | 待实现 | 模板管理、应用 | /permission-templates |

### 8.2 API集成层
| 任务ID | 任务描述 | 优先级 | 状态 | 功能 | 相关API |
|--------|----------|--------|------|------|----------|
| API-001 | Axios实例配置（拦截器、错误处理） | P0 | 待实现 | Token自动添加、错误统一处理 | 所有API调用 |
| API-002 | API服务模块（按业务域组织） | P0 | 待实现 | UserService, DepartmentService等 | 对应后端API |
| API-003 | WebSocket连接管理（实时告警） | P1 | 待实现 | 告警实时推送 | /api/alerts |

---

## 9. 路由与导航 (Routing & Navigation)

### 9.1 页面路由
| 任务ID | 任务描述 | 优先级 | 状态 | 路径 | 权限要求 |
|--------|----------|--------|------|------|----------|
| ROUTE-001 | 定义所有页面路由 | P0 | 待实现 | 见上述各页面路径 | 基于角色权限 |
| ROUTE-002 | 路由守卫（权限验证） | P0 | 待实现 | 中间件验证用户权限 | 所有受保护路由 |
| ROUTE-003 | 动态菜单生成 | P0 | 待实现 | 基于用户权限生成侧边栏菜单 | 布局组件 |

### 9.2 权限控制
| 任务ID | 任务描述 | 优先级 | 状态 | 实现方式 | 应用场景 |
|--------|----------|--------|------|----------|----------|
| PERM-CTRL-001 | 菜单权限控制 | P0 | 待实现 | 基于用户权限隐藏菜单项 | 侧边栏导航 |
| PERM-CTRL-002 | 操作权限控制 | P0 | 待实现 | 按钮禁用/隐藏 | 列表操作按钮 |
| PERM-CTRL-003 | 字段权限控制 | P1 | 待实现 | 表单字段禁用/隐藏 | 用户/部门表单 |
| PERM-CTRL-004 | 数据权限控制 | P1 | 待实现 | 查询条件过滤 | 列表数据筛选 |

---

## 10. 测试需求 (Testing Requirements)

### 10.1 单元测试
| 任务ID | 测试范围 | 覆盖率目标 | 工具 | 重点测试项 |
|--------|----------|------------|------|------------|
| TEST-001 | 工具函数、工具类 | 100% | Jest + React Testing Library | 表单验证、工具函数 |
| TEST-002 | React组件（无状态） | 90% | Jest + React Testing Library | 渲染、Props、事件 |
| TEST-003 | React组件（有状态） | 85% | Jest + React Testing Library | 状态变化、副作用 |
| TEST-004 | Zustand Store | 90% | Jest | 状态更新、Actions |

### 10.2 集成测试
| 任务ID | 测试范围 | 覆盖率目标 | 工具 | 重点测试项 |
|--------|----------|------------|------|------------|
| TEST-005 | API集成测试 | 80% | Jest + MSW | 请求/响应、错误处理 |
| TEST-006 | 表单提交流程 | 85% | Jest + React Testing Library | 表单验证、提交、反馈 |
| TEST-007 | 页面导航测试 | 80% | Jest + React Testing Library | 路由跳转、权限守卫 |

### 10.3 E2E测试（关键用户流）
| 任务ID | 用户流程 | 优先级 | 工具 | 测试场景 |
|--------|----------|--------|------|----------|
| E2E-001 | 用户登录流程 | P0 | Playwright | 正常登录、失败锁定、记住我 |
| E2E-002 | 用户管理流程 | P0 | Playwright | 创建用户、编辑、删除、批量操作 |
| E2E-003 | 角色权限分配流程 | P0 | Playwright | 创建角色、分配权限、用户分配 |
| E2E-004 | 部门管理流程 | P0 | Playwright | 创建部门、调整层级、成员管理 |
| E2E-005 | 审计日志查询导出 | P1 | Playwright | 日志筛选、详情查看、导出 |
| E2E-006 | 系统配置流程 | P1 | Playwright | 安全策略配置、邮件配置 |

---

## 11. 性能优化需求 (Performance Optimization)

### 11.1 加载性能
| 任务ID | 优化措施 | 目标 | 监控指标 |
|--------|----------|------|----------|
| PERF-001 | 代码分割（按路由） | 首屏加载 < 2s | Lighthouse评分 |
| PERF-002 | 图片/资源优化 | 资源大小最小化 | 页面加载时间 |
| PERF-003 | API响应缓存 | 减少重复请求 | API调用次数 |
| PERF-004 | 虚拟滚动（长列表） | 平滑滚动体验 | 内存使用量 |

### 11.2 运行时性能
| 任务ID | 优化措施 | 目标 | 监控指标 |
|--------|----------|------|----------|
| PERF-005 | 组件渲染优化 | 减少不必要的重渲染 | React渲染次数 |
| PERF-006 | 状态更新优化 | 批量状态更新 | 状态更新频率 |
| PERF-007 | 内存泄漏预防 | 稳定的内存使用 | 内存使用趋势 |

---

## 12. 安全需求 (Security Requirements)

### 12.1 前端安全
| 任务ID | 安全措施 | 实现方式 | 验证方法 |
|--------|----------|----------|----------|
| SEC-001 | XSS防护 | 输入过滤、输出编码 | 安全扫描 |
| SEC-002 | CSRF防护 | Token验证、SameSite Cookie | 渗透测试 |
| SEC-003 | 敏感数据保护 | LocalStorage加密、内存清理 | 代码审查 |
| SEC-004 | 安全头设置 | CSP、HSTS等 | 安全扫描 |

### 12.2 认证安全
| 任务ID | 安全措施 | 实现方式 | 验证方法 |
|--------|----------|----------|----------|
| SEC-005 | Token安全存储 | HttpOnly Cookie + 内存存储 | 安全测试 |
| SEC-006 | 自动登出机制 | Token过期处理 | 功能测试 |
| SEC-007 | 会话固定防护 | 登录后更新会话ID | 安全测试 |

---

## 13. 可访问性需求 (Accessibility Requirements)

### 13.1 WCAG 2.1 AA合规
| 任务ID | 可访问性要求 | 实现方式 | 验证工具 |
|--------|--------------|----------|----------|
| ACC-001 | 键盘导航支持 | Tab顺序、快捷键 | 键盘测试 |
| ACC-002 | 屏幕阅读器兼容 | ARIA标签、语义HTML | NVDA/JAWS测试 |
| ACC-003 | 色彩对比度 | 4.5:1以上 | 对比度检查工具 |
| ACC-004 | 焦点管理 | 焦点可见、逻辑顺序 | 焦点测试 |

---

## 14. 实施优先级与阶段划分

### Phase 1: 核心功能 (P0优先级)
- 用户管理基本功能（列表、CRUD）
- 角色权限基本功能（角色CRUD、权限分配）
- 认证增强（登录、Token管理）
- 基础组件库（DataTable、TreeView）
- 路由与权限守卫

**时间估计**: 4-6周
**交付物**: 可用的用户角色权限管理系统核心

### Phase 2: 高级功能 (P1优先级)
- 部门管理（树形结构、成员管理）
- 批量操作（导入导出）
- 个人中心（资料、登录历史）
- 审计日志基础功能
- 系统配置基础功能

**时间估计**: 3-4周
**交付物**: 完整的企业级用户管理系统

### Phase 3: 增强功能 (P1/P2优先级)
- 权限模板、角色继承
- 实时告警、性能监控
- 双因素认证、OAuth2
- 高级导出功能（PDF、异步）
- 移动端适配优化

**时间估计**: 3-4周
**交付物**: 功能完善的用户权限管理平台

### Phase 4: 优化与扩展 (P2优先级)
- 国际化支持
- 主题切换
- 高级性能优化
- 扩展功能（工作流、审批）

**时间估计**: 2-3周
**交付物**: 企业级产品化系统

---

## 15. 依赖关系与风险

### 后端API依赖
1. 所有前端功能依赖对应的后端API实现
2. API响应格式需符合约定（成功/错误统一格式）
3. 权限验证接口需提前实现

### 技术风险
1. 复杂的树形组件性能（部门树、权限树）
2. 实时功能（WebSocket连接稳定性）
3. 大数据量导出性能（异步任务处理）

### 缓解措施
1. 与后端团队密切协作，定义清晰的API契约
2. 性能关键组件进行原型验证
3. 分阶段实施，优先实现核心功能

---

## 总结

本任务清单基于完整的需求文档分析，涵盖了用户角色权限管理系统的所有前端开发需求。总计包含：

- **页面模块**: 6大模块，23+个页面
- **组件需求**: 50+个业务组件
- **状态管理**: 8+个Zustand Store
- **测试需求**: 单元、集成、E2E三层测试
- **任务总数**: 150+个具体开发任务

建议采用分阶段实施策略，优先完成核心功能，逐步完善高级功能。开发过程中需密切关注与后端API的集成，确保前后端协同工作。

**文档版本**: 1.0
**最后更新**: 2026-03-26
**依据文档**:
- `.planning/REQUIREMENTS.md` (v1需求清单)
- `docs/requirements/FUNCTIONAL_REQUIREMENTS.md` (功能需求说明书)
- `docs/requirements/NON_FUNCTIONAL_REQUIREMENTS.md` (非功能需求说明书)


# 前端任务规划文档

## 项目概述
全栈用户管理系统前端部分，基于 Next.js 16 (App Router)、TypeScript 5+、shadcn/ui、Tailwind CSS 和 Zustand 状态管理。

## 需求分析总结

根据 `.planning/REQUIREMENTS.md` 分析，需要前端实现的需求类别：

| 类别 | 需求数量 | 需要前端页面 | 优先级 |
|------|----------|--------------|--------|
| USER | 9 | ✅ 是 | P0 |
| DEPT | 7 | ✅ 是 | P0 |
| ROLE | 7 | ✅ 是 | P0 |
| PERM | 7 | ✅ 是 | P0 |
| AUTH | 10 | ✅ 是 | P0 |
| AUDIT | 7 | ✅ 是 | P0 |
| CONFIG | 5 | ✅ 是 | P0 |
| PERF | 7 | ❌ 否 (后端) | P0 |
| SEC | 8 | ❌ 否 (后端) | P0 |

## 前端页面规划

### 1. 认证与用户相关页面

#### 1.1 登录页面 (`/login`)
- **功能**: 用户登录、记住我、忘记密码
- **对应需求**: AUTH-01, AUTH-03, AUTH-04, AUTH-10
- **组件**:
  - 登录表单 (邮箱/密码)
  - 验证码/双因素认证 (AUTH-08, AUTH-09)
  - 第三方登录 (AUTH-07)
  - 错误提示和锁定状态显示

#### 1.2 注册页面 (`/register`)
- **功能**: 用户自助注册
- **对应需求**: USER-07
- **组件**: 注册表单、邮箱验证、密码强度检查

#### 1.3 个人资料页面 (`/profile`)
- **功能**: 用户个人资料管理
- **对应需求**: USER-08
- **组件**: 个人信息表单、头像上传、密码修改

#### 1.4 用户管理页面 (`/users`)
- **功能**: 用户CRUD、状态管理、批量操作
- **对应需求**: USER-01, USER-02, USER-03, USER-05, USER-06
- **子页面**:
  - 用户列表 (`/users`)
  - 用户详情 (`/users/[id]`)
  - 创建用户 (`/users/new`)
  - 编辑用户 (`/users/[id]/edit`)
- **组件**:
  - 用户表格 (分页、筛选、排序)
  - 批量导入/导出 (Excel/CSV)
  - 用户状态切换 (ACTIVE/INACTIVE/PENDING/LOCKED)
  - 用户角色分配

#### 1.5 用户登录历史 (`/users/[id]/login-history`)
- **功能**: 查看用户登录历史
- **对应需求**: USER-09
- **组件**: 登录历史表格、IP地址、时间、设备信息

### 2. 角色管理页面

#### 2.1 角色列表页面 (`/roles`)
- **功能**: 角色CRUD、权限分配
- **对应需求**: ROLE-01, ROLE-02, ROLE-03, ROLE-05, ROLE-06
- **子页面**:
  - 角色列表 (`/roles`)
  - 角色详情 (`/roles/[id]`)
  - 创建角色 (`/roles/new`)
  - 编辑角色 (`/roles/[id]/edit`)
- **组件**:
  - 角色表格
  - 权限分配树形组件
  - 数据权限范围配置 (ALL/DEPT/SELF)
  - 用户角色分配弹窗

#### 2.2 角色权限模板 (`/roles/templates`)
- **功能**: 预定义权限模板
- **对应需求**: ROLE-05
- **组件**: 模板列表、模板应用

### 3. 部门管理页面

#### 3.1 部门管理页面 (`/departments`)
- **功能**: 部门树形结构管理
- **对应需求**: DEPT-01, DEPT-02, DEPT-03, DEPT-04, DEPT-05, DEPT-06
- **组件**:
  - 部门树形视图 (Materialized Path)
  - 部门CRUD操作
  - 部门成员列表
  - 部门负责人关联
  - 部门编码唯一性验证

### 4. 权限管理页面

#### 4.1 权限管理页面 (`/permissions`)
- **功能**: 权限配置管理
- **对应需求**: PERM-01, PERM-02, PERM-03, PERM-04, PERM-05
- **组件**:
  - 权限树形结构 (点号分隔格式)
  - 菜单权限配置
  - 操作权限配置 (CRUD)
  - 字段权限配置 (可读/可写)
  - 数据权限范围配置

### 5. 审计日志页面

#### 5.1 审计日志页面 (`/audit-logs`)
- **功能**: 操作日志查询、筛选、导出
- **对应需求**: AUDIT-01, AUDIT-02, AUDIT-04, AUDIT-05
- **组件**:
  - 日志表格 (分页、筛选)
  - 多维度查询 (用户、操作类型、时间范围)
  - 日志详情查看
  - 导出功能 (Excel/PDF)
  - 实时告警显示 (AUDIT-07)

### 6. 系统配置页面

#### 6.1 系统配置页面 (`/settings`)
- **功能**: 系统参数配置
- **对应需求**: CONFIG-01, CONFIG-02, CONFIG-03, CONFIG-04, CONFIG-05
- **标签页**:
  - 密码策略配置 (复杂度、过期时间、历史密码)
  - 会话配置 (超时时间、最大会话数)
  - 邮件服务配置
  - 安全策略配置 (登录失败锁定、限流阈值)
  - 系统参数配置

### 7. 仪表板页面

#### 7.1 主仪表板 (`/dashboard`)
- **功能**: 系统概览、统计信息
- **组件**:
  - 用户统计卡片 (活跃用户、新增用户)
  - 角色权限分布
  - 登录活动图表
  - 系统健康状态
  - 最近操作日志

## 前端技术栈实现规划

### 1. 项目结构
```
frontend/
├── app/                    # Next.js App Router
│   ├── (auth)/            # 认证相关路由组
│   │   ├── login/
│   │   ├── register/
│   │   └── forgot-password/
│   ├── (dashboard)/       # 仪表板路由组
│   │   ├── dashboard/
│   │   ├── users/
│   │   ├── roles/
│   │   ├── departments/
│   │   ├── permissions/
│   │   ├── audit-logs/
│   │   └── settings/
│   ├── api/               # API路由
│   │   ├── auth/
│   │   └── proxy/         # 后端API代理
│   ├── layout.tsx         # 根布局
│   └── page.tsx           # 首页
├── components/
│   ├── ui/                # shadcn/ui组件
│   ├── forms/             # 表单组件
│   │   ├── UserForm/
│   │   ├── RoleForm/
│   │   ├── DepartmentForm/
│   │   └── PermissionForm/
│   ├── layout/            # 布局组件
│   │   ├── Header/
│   │   ├── Sidebar/
│   │   ├── MainLayout/
│   │   └── Breadcrumb/
│   ├── data-display/      # 数据展示组件
│   │   ├── DataTable/
│   │   ├── TreeView/
│   │   ├── PermissionTree/
│   │   └── AuditLogTable/
│   └── features/          # 功能组件
│       ├── UserManagement/
│       ├── RoleManagement/
│       └── ImportExport/
├── lib/
│   ├── api/               # API客户端
│   │   ├── client.ts      # Axios配置
│   │   ├── auth.ts        # 认证API
│   │   ├── users.ts       # 用户API
│   │   ├── roles.ts       # 角色API
│   │   └── departments.ts # 部门API
│   ├── utils/             # 工具函数
│   │   ├── validation.ts  # 验证工具
│   │   ├── formatters.ts  # 格式化工具
│   │   └── permissions.ts # 权限检查工具
│   ├── schemas/           # Zod验证模式
│   │   ├── user.schema.ts
│   │   ├── role.schema.ts
│   │   └── department.schema.ts
│   └── constants/         # 常量定义
│       ├── routes.ts      # 路由常量
│       └── permissions.ts # 权限常量
├── stores/                # Zustand状态管理
│   ├── auth.store.ts      # 认证状态
│   ├── user.store.ts      # 用户状态
│   ├── ui.store.ts        # UI状态
│   └── notification.store.ts # 通知状态
├── types/                 # TypeScript类型定义
│   ├── api.ts             # API响应类型
│   ├── user.ts            # 用户类型
│   ├── role.ts            # 角色类型
│   └── department.ts      # 部门类型
├── hooks/                 # 自定义Hook
│   ├── useAuth.ts         # 认证Hook
│   ├── usePermissions.ts  # 权限Hook
│   └── useApi.ts          # API Hook
└── styles/                # 样式文件
    └── globals.css        # 全局样式
```

### 2. 核心组件规划

#### 2.1 认证相关组件
- `AuthGuard`: 路由守卫组件
- `LoginForm`: 登录表单组件
- `RegisterForm`: 注册表单组件
- `ForgotPasswordForm`: 忘记密码表单

#### 2.2 数据表格组件
- `DataTable`: 通用数据表格 (分页、筛选、排序)
- `UserTable`: 用户表格 (带状态管理)
- `RoleTable`: 角色表格 (带权限分配)
- `AuditLogTable`: 审计日志表格 (多维度筛选)

#### 2.3 表单组件
- `UserForm`: 用户创建/编辑表单
- `RoleForm`: 角色创建/编辑表单
- `DepartmentForm`: 部门创建/编辑表单
- `PermissionForm`: 权限配置表单

#### 2.4 树形组件
- `DepartmentTree`: 部门树形结构
- `PermissionTree`: 权限树形结构
- `RolePermissionTree`: 角色权限分配树

#### 2.5 布局组件
- `MainLayout`: 主布局 (Header + Sidebar + Content)
- `Sidebar`: 侧边栏导航 (基于权限动态渲染)
- `Header`: 顶部导航 (用户信息、通知、设置)

### 3. 状态管理规划

#### 3.1 认证状态 (`auth.store.ts`)
```typescript
interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
  setUser: (user: User) => void;
}
```

#### 3.2 用户状态 (`user.store.ts`)
```typescript
interface UserState {
  users: User[];
  selectedUser: User | null;
  pagination: Pagination;
  filters: UserFilters;
  isLoading: boolean;

  // Actions
  fetchUsers: (params?: FetchParams) => Promise<void>;
  createUser: (userData: CreateUserData) => Promise<void>;
  updateUser: (id: string, userData: UpdateUserData) => Promise<void>;
  deleteUser: (id: string) => Promise<void>;
  importUsers: (file: File) => Promise<void>;
  exportUsers: () => Promise<void>;
}
```

#### 3.3 角色状态 (`role.store.ts`)
```typescript
interface RoleState {
  roles: Role[];
  selectedRole: Role | null;
  permissions: Permission[];
  rolePermissions: Record<string, string[]>;

  // Actions
  fetchRoles: () => Promise<void>;
  fetchPermissions: () => Promise<void>;
  assignPermissions: (roleId: string, permissionIds: string[]) => Promise<void>;
  assignUsersToRole: (roleId: string, userIds: string[]) => Promise<void>;
}
```

### 4. API客户端规划

#### 4.1 Axios配置 (`lib/api/client.ts`)
```typescript
const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 添加Token
apiClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器 - Token刷新、错误处理
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Token过期处理
    if (error.response?.status === 401) {
      await refreshToken();
      return apiClient(error.config);
    }
    return Promise.reject(error);
  }
);
```

#### 4.2 API模块划分
- `auth.ts`: 登录、注册、刷新Token、登出
- `users.ts`: 用户CRUD、批量操作、状态管理
- `roles.ts`: 角色CRUD、权限分配
- `departments.ts`: 部门CRUD、树形结构
- `permissions.ts`: 权限管理
- `audit-logs.ts`: 审计日志查询
- `settings.ts`: 系统配置

### 5. 路由与权限控制

#### 5.1 路由结构
```typescript
const routes = [
  // 公开路由
  { path: '/login', component: LoginPage, public: true },
  { path: '/register', component: RegisterPage, public: true },

  // 受保护路由
  { path: '/dashboard', component: DashboardPage, requiredPermissions: ['dashboard.view'] },
  { path: '/users', component: UsersPage, requiredPermissions: ['users.list'] },
  { path: '/users/:id', component: UserDetailPage, requiredPermissions: ['users.read'] },
  { path: '/roles', component: RolesPage, requiredPermissions: ['roles.list'] },
  { path: '/departments', component: DepartmentsPage, requiredPermissions: ['departments.list'] },
  { path: '/permissions', component: PermissionsPage, requiredPermissions: ['permissions.list'] },
  { path: '/audit-logs', component: AuditLogsPage, requiredPermissions: ['audit_logs.list'] },
  { path: '/settings', component: SettingsPage, requiredPermissions: ['settings.view'] },
];
```

#### 5.2 权限检查中间件
```typescript
// app/middleware.ts
export function middleware(request: NextRequest) {
  const token = request.cookies.get('token')?.value;
  const pathname = request.nextUrl.pathname;

  // 检查是否登录
  if (!token && !publicRoutes.includes(pathname)) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  // 检查权限
  if (token && !checkPermissions(pathname, token)) {
    return NextResponse.redirect(new URL('/unauthorized', request.url));
  }

  return NextResponse.next();
}
```

### 6. 测试规划

#### 6.1 测试覆盖率目标
- 单元测试: ≥ 90%
- 组件测试: ≥ 85%
- E2E测试: 关键用户流程100%覆盖

#### 6.2 测试工具
- **单元测试**: Jest + React Testing Library
- **组件测试**: Storybook + Chromatic
- **E2E测试**: Playwright
- **API测试**: MSW (Mock Service Worker)

#### 6.3 测试重点
1. 认证流程测试 (登录、注册、Token刷新)
2. 用户管理测试 (CRUD、批量操作)
3. 权限控制测试 (路由守卫、按钮权限)
4. 表单验证测试 (Zod schema验证)
5. 错误处理测试 (API错误、网络异常)

## 开发阶段规划

### Phase 1: 基础框架搭建 (Week 1-2)
1. 项目初始化 (Next.js + TypeScript + Tailwind)
2. 安装配置 shadcn/ui 组件库
3. 设置 Zustand 状态管理
4. 配置 API 客户端 (Axios + 拦截器)
5. 实现基础布局组件 (Header, Sidebar, MainLayout)
6. 设置路由结构和权限中间件

### Phase 2: 认证模块 (Week 3-4)
1. 登录页面实现
2. 注册页面实现
3. Token管理和刷新机制
4. 路由守卫实现
5. 个人资料页面

### Phase 3: 用户管理模块 (Week 5-6)
1. 用户列表页面 (表格、分页、筛选)
2. 用户详情页面
3. 用户创建/编辑表单
4. 用户状态管理
5. 批量导入/导出功能

### Phase 4: 角色权限模块 (Week 7-8)
1. 角色列表页面
2. 角色创建/编辑表单
3. 权限树形组件
4. 角色权限分配
5. 用户角色分配

### Phase 5: 部门管理模块 (Week 9-10)
1. 部门树形结构页面
2. 部门CRUD操作
3. 部门成员管理
4. 部门负责人关联

### Phase 6: 审计日志与配置 (Week 11-12)
1. 审计日志页面 (表格、筛选、导出)
2. 系统配置页面
3. 仪表板页面
4. 实时通知功能

### Phase 7: 测试与优化 (Week 13-14)
1. 单元测试编写
2. 组件测试编写
3. E2E测试编写
4. 性能优化
5. 代码审查和重构

## 技术挑战与解决方案

### 1. 权限动态渲染
**挑战**: 根据用户权限动态渲染菜单和按钮
**解决方案**:
- 使用权限检查Hook (`usePermissions`)
- 实现高阶组件 (`withPermission`)
- 侧边栏菜单基于权限动态生成

### 2. 树形结构管理
**挑战**: 部门树形结构、权限树形结构
**解决方案**:
- 使用递归组件实现树形视图
- 实现拖拽排序功能
- 使用虚拟滚动优化性能

### 3. 批量操作性能
**挑战**: 批量导入/导出大量数据
**解决方案**:
- 使用Web Worker处理大数据
- 分片上传/下载
- 进度条显示和取消功能

### 4. 实时数据更新
**挑战**: 审计日志实时更新、通知实时推送
**解决方案**:
- 使用WebSocket或Server-Sent Events
- 实现乐观更新 (Optimistic Updates)
- 使用SWR或React Query进行数据缓存

### 5. 多环境配置
**挑战**: 不同环境API地址、配置参数
**解决方案**:
- 使用环境变量管理配置
- 实现配置管理Hook
- 开发环境Mock数据

## 质量保证措施

### 1. 代码质量
- ESLint + Prettier 代码规范
- TypeScript 严格模式
- Husky + lint-staged 提交前检查
- 代码审查流程

### 2. 性能优化
- 图片懒加载和优化
- 代码分割和懒加载
- 服务端渲染优化
- 缓存策略优化

### 3. 安全性
- XSS防护 (DOMPurify)
- CSRF防护
- 输入验证 (Zod)
- 敏感信息脱敏

### 4. 可访问性
- ARIA标签支持
- 键盘导航支持
- 屏幕阅读器兼容
- 颜色对比度检查

## 交付物清单

### 文档
- [ ] 前端架构设计文档
- [ ] 组件API文档 (Storybook)
- [ ] 开发指南文档
- [ ] 部署指南文档

### 代码
- [ ] 完整的Next.js应用
- [ ] 所有规划页面的实现
- [ ] 组件库和工具函数
- [ ] 测试套件 (单元测试、组件测试、E2E测试)

### 配置
- [ ] CI/CD流水线配置
- [ ] 多环境部署配置
- [ ] 监控和告警配置
- [ ] 性能监控配置

## 风险评估与缓解

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| 权限系统复杂度高 | 高 | 中 | 分阶段实现，先实现基础RBAC，再扩展 |
| 树形结构性能问题 | 中 | 高 | 使用虚拟滚动，分页加载，后端优化 |
| 批量操作超时 | 中 | 中 | 分片处理，进度反馈，后台任务 |
| 浏览器兼容性问题 | 低 | 低 | 使用现代浏览器特性，提供降级方案 |
| API接口变更 | 高 | 中 | 使用TypeScript严格类型，API版本管理 |

## 成功标准

1. **功能完整性**: 所有规划页面和功能100%实现
2. **代码质量**: 测试覆盖率≥80%，无严重bug
3. **性能指标**: 页面加载时间<3秒，API响应时间<200ms
4. **用户体验**: 用户满意度调查≥4.5/5分
5. **安全性**: 通过安全扫描，无高危漏洞

---

