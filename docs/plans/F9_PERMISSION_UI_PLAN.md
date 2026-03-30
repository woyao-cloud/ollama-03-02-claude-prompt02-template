# Implementation Plan: F9 权限配置界面开发

## Overview
- **Objective**: 实现前端权限配置界面，包括权限列表展示、搜索过滤、权限管理功能
- **Scope**: 前端权限页面、Hooks、组件、API 调用层、类型定义
- **Complexity**: Medium
- **Estimated Effort**: 3-4 days

## Requirements

### Functional Requirements
- [ ] 权限列表页面 - 展示所有权限（表格形式）
- [ ] 权限搜索功能 - 按名称/代码搜索
- [ ] 权限过滤功能 - 按类型、状态过滤
- [ ] 权限详情查看 - 查看权限完整信息
- [ ] 权限创建功能 - 创建新权限
- [ ] 权限编辑功能 - 修改权限信息
- [ ] 权限删除功能 - 删除权限
- [ ] 权限状态切换 - 激活/禁用权限
- [ ] 前端权限 Hook - usePermission 等工具函数

### Acceptance Criteria
- [ ] 权限列表正确展示所有字段
- [ ] 搜索功能正常工作
- [ ] 过滤功能正常工作（类型、状态）
- [ ] 分页功能正常
- [ ] CRUD 操作正常
- [ ] 权限检查 Hook 正常工作
- [ ] 测试覆盖率 ≥ 80%

## Technical Analysis

### Current State

**后端 API (已完成)**:
- `PermissionService` - 权限服务接口
- `PermissionDTO` - 权限响应 DTO
- `PermissionCreateRequest` / `PermissionUpdateRequest` - 请求 DTO
- `PermissionType` 枚举 - MENU, ACTION, FIELD, DATA
- `PermissionStatus` 枚举 - ACTIVE, INACTIVE

**前端现有代码**:
- `PermissionGuard.tsx` - 已有权限守卫组件和 usePermission Hook
- `PermissionTree.tsx` - 权限树组件（用于角色分配）
- `authStore.ts` - 权限检查方法 (hasPermission, hasAnyPermission, hasAllPermissions)
- 用户列表页面模式 (`users/page.tsx`)
- 角色列表页面模式 (`roles/page.tsx`)
- API 客户端模式 (`lib/api/client.ts`)
- shadcn/ui 组件可用

### Proposed Changes

1. **扩展类型定义** - 完善 Permission 类型，添加后端 DTO 对应字段
2. **创建 Permission API Service** - 权限相关的 API 调用层
3. **创建权限 Hooks** - usePermissionList, usePermissionCheck
4. **创建权限页面** - `/permissions` 路由页面
5. **创建权限组件** - PermissionTable, PermissionFilters, PermissionForm, PermissionBadge
6. **更新 Sidebar** - 添加权限管理菜单项（可选，或合并在角色权限下）

### Components to Modify

| Component | Changes | Impact |
|-----------|---------|--------|
| `types/index.ts` | 扩展 Permission 类型 | Low |
| `lib/api/services/index.ts` | 导出 permissionService | Low |
| `app/(dashboard)/layout.tsx` | 无修改 | None |
| `components/layout/Sidebar.tsx` | 可选：添加权限菜单 | Low |

### New Components

| Component | Purpose | Location |
|-----------|---------|----------|
| `Permission` (extended) | 完整权限类型定义 | `types/index.ts` |
| `permissionService` | 权限 API 调用 | `lib/api/services/permission.ts` |
| `usePermissionList` | 获取权限列表 Hook | `hooks/usePermissionList.ts` |
| `usePermissionCheck` | 权限检查 Hook | `hooks/usePermissionCheck.ts` |
| `PermissionsPage` | 权限列表主页面 | `app/(dashboard)/permissions/page.tsx` |
| `PermissionTable` | 权限表格组件 | `components/permissions/PermissionTable.tsx` |
| `PermissionFilters` | 权限过滤组件 | `components/permissions/PermissionFilters.tsx` |
| `PermissionForm` | 权限表单组件 | `components/permissions/PermissionForm.tsx` |
| `PermissionDetailDialog` | 权限详情弹窗 | `components/permissions/PermissionDetailDialog.tsx` |
| `PermissionBadge` | 权限类型/状态标识 | `components/permissions/PermissionBadge.tsx` |
| `DeleteConfirmDialog` | 删除确认弹窗 | `components/permissions/DeleteConfirmDialog.tsx` |

## Implementation Steps

### Phase 1: 基础架构 (Foundation)

1. [ ] **扩展类型定义**
   - Details: 在 `types/index.ts` 中扩展 Permission 类型，添加 type, status, parentId, icon, route, sortOrder 等字段
   - Files: `frontend/types/index.ts`
   - Verification: TypeScript 编译通过

2. [ ] **创建 Permission API Service**
   - Details: 创建 `lib/api/services/permission.ts`，实现 getAllPermissions, getPermissionById, createPermission, updatePermission, deletePermission, updatePermissionStatus 等方法
   - Files: `frontend/lib/api/services/permission.ts`, `frontend/lib/api/services/index.ts`
   - Verification: API 调用类型正确

3. [ ] **创建权限 Hooks**
   - Details:
     - `hooks/usePermissionList.ts` - 获取权限列表，支持搜索、过滤、分页
     - `hooks/usePermissionCheck.ts` - 高级权限检查逻辑
   - Files: `frontend/hooks/usePermissionList.ts`, `frontend/hooks/usePermissionCheck.ts`
   - Verification: Hook 返回正确数据

### Phase 2: 组件开发 (Core Components)

4. [ ] **创建 PermissionBadge 组件**
   - Details: 显示权限类型 (MENU/ACTION/FIELD/DATA) 和状态 (ACTIVE/INACTIVE) 的 Badge 组件
   - Files: `frontend/components/permissions/PermissionBadge.tsx`
   - Verification: 正确显示不同类型和状态的样式

5. [ ] **创建 PermissionFilters 组件**
   - Details: 搜索框、类型选择器、状态选择器、清除筛选按钮
   - Files: `frontend/components/permissions/PermissionFilters.tsx`
   - Verification: 筛选条件正确触发回调

6. [ ] **创建 PermissionTable 组件**
   - Details: 权限列表表格，支持行点击、操作菜单（查看、编辑、删除、切换状态）
   - Files: `frontend/components/permissions/PermissionTable.tsx`
   - Verification: 表格正确渲染数据，操作按钮正常工作

7. [ ] **创建 PermissionForm 组件**
   - Details: 权限创建/编辑表单，包含名称、代码、类型、资源、操作、父权限、图标、路由、排序号等字段
   - Files: `frontend/components/permissions/PermissionForm.tsx`
   - Verification: 表单验证正确，提交成功

8. [ ] **创建 PermissionDetailDialog 组件**
   - Details: 权限详情查看弹窗
   - Files: `frontend/components/permissions/PermissionDetailDialog.tsx`
   - Verification: 正确显示权限完整信息

9. [ ] **创建 DeleteConfirmDialog 组件**
   - Details: 删除权限确认弹窗
   - Files: `frontend/components/permissions/DeleteConfirmDialog.tsx`
   - Verification: 确认后执行删除

### Phase 3: 页面集成 (Integration)

10. [ ] **创建权限列表页面**
    - Details: 创建 `/permissions` 路由页面，集成所有组件，实现完整 CRUD 流程
    - Files: `frontend/app/(dashboard)/permissions/page.tsx`
    - Verification: 页面正常渲染，所有功能可用

11. [ ] **更新 Sidebar 菜单（可选）**
    - Details: 在 Sidebar 中添加"权限管理"菜单项，或将其作为"角色权限"的子菜单
    - Files: `frontend/components/layout/Sidebar.tsx`
    - Verification: 菜单正确显示并可跳转

### Phase 4: 测试与优化 (Testing & Optimization)

12. [ ] **编写组件测试**
    - Details: 为 PermissionTable, PermissionFilters, PermissionForm 等组件编写单元测试
    - Files: `frontend/components/permissions/*.test.tsx`
    - Verification: 测试通过率 100%

13. [ ] **编写 Hook 测试**
    - Details: 为 usePermissionList, usePermissionCheck 编写测试
    - Files: `frontend/hooks/*.test.ts`
    - Verification: 测试通过率 100%

14. [ ] **E2E 测试**
    - Details: 编写关键用户流的 E2E 测试（查看列表、搜索、创建、编辑、删除）
    - Files: `frontend/e2e/permissions/*.spec.ts`
    - Verification: E2E 测试通过

15. [ ] **更新 PLAN.md**
    - Details: 记录 F9 任务完成状态
    - Files: `PLAN.md`
    - Verification: 文档更新

## Dependencies

### Prerequisites
- [x] 后端 Permission API 已完成
- [x] 基础 UI 组件 (shadcn/ui) 已可用
- [x] API 客户端框架已建立

### Dependent Tasks
- F9 权限配置界面是独立功能，不阻塞其他任务
- 角色管理页面 (F8) 已完成，可参考其实现模式

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| 后端 API 字段与前端类型不匹配 | Low | Medium | 仔细对照后端 DTO 定义前端类型 |
| 权限树形结构展示复杂 | Medium | Medium | 先实现扁平列表，树形结构作为增强功能 |
| 权限类型枚举值不一致 | Low | High | 对照后端 PermissionType 枚举定义前端类型 |
| 测试覆盖率不足 | Medium | Low | 使用 TDD 方式，先写测试再实现 |

## Testing Strategy

### Unit Tests
- [ ] PermissionBadge - 不同类型和状态的样式
- [ ] PermissionFilters - 筛选条件变化回调
- [ ] PermissionTable - 数据渲染和操作回调
- [ ] PermissionForm - 表单验证和提交
- [ ] usePermissionList - 数据加载和状态管理
- [ ] usePermissionCheck - 权限检查逻辑

### Integration Tests
- [ ] 权限列表页面加载
- [ ] 搜索和过滤功能
- [ ] 创建权限流程
- [ ] 编辑权限流程
- [ ] 删除权限流程

### E2E Tests
- [ ] 访问权限列表页面
- [ ] 搜索权限
- [ ] 按类型过滤
- [ ] 按状态过滤
- [ ] 创建新权限
- [ ] 编辑现有权限
- [ ] 删除权限
- [ ] 切换权限状态

## Rollback Plan
如果出现问题：
1. 回滚前端代码变更
2. 后端 API 保持不变（向后兼容）
3. 使用 Git 回退到上一个稳定版本

## Success Criteria
- [ ] 所有功能需求完成
- [ ] 所有验收标准满足
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过
- [ ] E2E 测试通过
- [ ] 代码审查通过
- [ ] 无 TypeScript 类型错误

## File Structure

```
frontend/
├── types/
│   └── index.ts                    # 扩展 Permission 类型
├── lib/
│   └── api/
│       └── services/
│           ├── permission.ts       # 新增
│           └── index.ts            # 导出 permissionService
├── hooks/
│   ├── usePermissionList.ts        # 新增
│   └── usePermissionCheck.ts       # 新增
├── app/
│   └── (dashboard)/
│       └── permissions/
│           └── page.tsx            # 新增
└── components/
    └── permissions/                # 新增目录
        ├── index.ts                # 统一导出
        ├── PermissionTable.tsx
        ├── PermissionFilters.tsx
        ├── PermissionForm.tsx
        ├── PermissionDetailDialog.tsx
        ├── PermissionBadge.tsx
        └── DeleteConfirmDialog.tsx
```

## Code Examples

### Permission Type Extension

```typescript
// types/index.ts
export interface Permission {
  id: string;
  name: string;
  code: string;
  resource: string;
  actions: string[];
  // 新增字段
  type: 'MENU' | 'ACTION' | 'FIELD' | 'DATA';
  status: 'ACTIVE' | 'INACTIVE';
  parentId?: string;
  icon?: string;
  route?: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}
```

### Permission Service

```typescript
// lib/api/services/permission.ts
import apiClient from '../client';
import type { ApiResponse, Permission, PaginatedResponse, QueryParams } from '@/types';

export const permissionService = {
  getAllPermissions: async (): Promise<Permission[]> => {
    const response = await apiClient.get<ApiResponse<Permission[]>>('/permissions/all');
    const { data } = response.data;
    if (!data) throw new Error('获取权限列表失败');
    return data;
  },

  getPermissions: async (params?: QueryParams): Promise<PaginatedResponse<Permission>> => {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<Permission>>>('/permissions', { params });
    const { data } = response.data;
    if (!data) throw new Error('获取权限列表失败');
    return data;
  },

  getPermissionById: async (id: string): Promise<Permission> => {
    const response = await apiClient.get<ApiResponse<Permission>>(`/permissions/${id}`);
    const { data } = response.data;
    if (!data) throw new Error('获取权限详情失败');
    return data;
  },

  createPermission: async (data: Omit<Permission, 'id' | 'createdAt' | 'updatedAt'>): Promise<Permission> => {
    const response = await apiClient.post<ApiResponse<Permission>>('/permissions', data);
    const { data: result } = response.data;
    if (!result) throw new Error('创建权限失败');
    return result;
  },

  updatePermission: async (id: string, data: Partial<Permission>): Promise<Permission> => {
    const response = await apiClient.put<ApiResponse<Permission>>(`/permissions/${id}`, data);
    const { data: result } = response.data;
    if (!result) throw new Error('更新权限失败');
    return result;
  },

  deletePermission: async (id: string): Promise<void> => {
    await apiClient.delete(`/permissions/${id}`);
  },

  updatePermissionStatus: async (id: string, status: 'ACTIVE' | 'INACTIVE'): Promise<Permission> => {
    const response = await apiClient.patch<ApiResponse<Permission>>(`/permissions/${id}/status`, { status });
    const { data: result } = response.data;
    if (!result) throw new Error('更新权限状态失败');
    return result;
  },
};
```

### usePermissionList Hook

```typescript
// hooks/usePermissionList.ts
import { useState, useEffect, useCallback } from 'react';
import { permissionService } from '@/lib/api';
import type { Permission, PaginatedResponse } from '@/types';

interface UsePermissionListOptions {
  search?: string;
  type?: string;
  status?: string;
  page?: number;
  limit?: number;
}

export function usePermissionList(options: UsePermissionListOptions = {}) {
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [pagination, setPagination] = useState<PaginatedResponse<Permission>>({
    items: [],
    total: 0,
    page: 1,
    limit: 10,
    totalPages: 0,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadPermissions = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const params: Record<string, string | number> = {
        page: options.page || 1,
        limit: options.limit || 10,
      };
      if (options.search) params.search = options.search;
      if (options.type) params.type = options.type;
      if (options.status) params.status = options.status;

      const data = await permissionService.getPermissions(params);
      setPermissions(data.items);
      setPagination(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载失败');
    } finally {
      setIsLoading(false);
    }
  }, [options.search, options.type, options.status, options.page, options.limit]);

  useEffect(() => {
    loadPermissions();
  }, [loadPermissions]);

  return {
    permissions,
    pagination,
    isLoading,
    error,
    refresh: loadPermissions,
  };
}
```
