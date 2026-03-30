'use client';

import { ReactNode } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { usePermissionStore, DATA_SCOPE, type DataScope } from '@/stores/permissionStore';

interface PermissionGuardProps {
  children: ReactNode;
  /**
   * 需要至少一个权限（OR 逻辑）
   */
  permissions?: string[];
  /**
   * 需要所有权限（AND 逻辑）
   */
  allPermissions?: string[];
  /**
   * 无权限时的回退内容
   */
  fallback?: ReactNode;
}

/**
 * 权限守卫组件
 *
 * 用途：按钮级权限控制
 * - 用户无权限时隐藏或禁用 UI 元素
 * - 支持 OR 和 AND 两种权限检查模式
 *
 * 使用示例：
 * <PermissionGuard permissions={['user:create']}>
 *   <Button>创建用户</Button>
 * </PermissionGuard>
 *
 * <PermissionGuard allPermissions={['user:view', 'user:edit']}>
 *   <Button>编辑用户</Button>
 * </PermissionGuard>
 */
export function PermissionGuard({
  children,
  permissions,
  allPermissions,
  fallback = null,
}: PermissionGuardProps) {
  const { hasAnyPermission, hasAllPermissions } = useAuthStore();

  // 检查权限
  let hasAccess = true;

  if (permissions && permissions.length > 0) {
    // OR 逻辑：至少需要一个权限
    hasAccess = hasAnyPermission(permissions);
  } else if (allPermissions && allPermissions.length > 0) {
    // AND 逻辑：需要所有权限
    hasAccess = hasAllPermissions(allPermissions);
  }

  if (hasAccess) {
    return <>{children}</>;
  }

  return <>{fallback}</>;
}

/**
 * 数据范围守卫组件
 *
 * 用途：根据用户数据范围控制 UI 元素显示
 *
 * 使用示例：
 * <DataScopeGuard minScope="DEPT">
 *   <Button>查看部门数据</Button>
 * </DataScopeGuard>
 */
export function DataScopeGuard({
  children,
  minScope,
  fallback = null,
}: {
  children: ReactNode;
  minScope: DataScope;
  fallback?: ReactNode;
}) {
  const { dataScope } = usePermissionStore();
  const scopeLevels: Record<DataScope, number> = {
    SELF: 0,
    DEPT_ONLY: 1,
    DEPT: 2,
    ALL: 3,
  };

  const hasAccess = scopeLevels[dataScope as DataScope] >= scopeLevels[minScope];

  if (hasAccess) {
    return <>{children}</>;
  }

  return <>{fallback}</>;
}

/**
 * 权限检查 Hook
 *
 * 使用示例：
 * const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermission();
 * if (hasPermission('user:create')) { ... }
 */
export function usePermission() {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = useAuthStore();

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
  };
}

/**
 * 数据权限范围 Hook
 *
 * 使用示例：
 * const { dataScope, canAccessData, canAccessDeptData } = useDataScope();
 * if (canAccessData('DEPT')) { ... }
 */
export function useDataScope() {
  const { dataScope } = usePermissionStore();

  const scopeLevels: Record<DataScope, number> = {
    SELF: 0,
    DEPT_ONLY: 1,
    DEPT: 2,
    ALL: 3,
  };

  const currentLevel = scopeLevels[dataScope as DataScope] || 0;

  return {
    dataScope,
    /**
     * 检查是否可以访问指定范围的数据
     */
    canAccessData: (scope: DataScope): boolean => {
      return currentLevel >= scopeLevels[scope];
    },
    /**
     * 是否可以访问全部数据
     */
    canAccessAll: (): boolean => currentLevel >= scopeLevels.ALL,
    /**
     * 是否可以访问部门及以下数据
     */
    canAccessDeptData: (): boolean => currentLevel >= scopeLevels.DEPT,
    /**
     * 是否可以访问本部门数据
     */
    canAccessDeptOnlyData: (): boolean => currentLevel >= scopeLevels.DEPT_ONLY,
    /**
     * 是否只能访问本人数据
     */
    canAccessSelfOnly: (): boolean => currentLevel === scopeLevels.SELF,
  };
}
