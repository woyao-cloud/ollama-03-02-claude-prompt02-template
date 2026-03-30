'use client';

import { ReactNode } from 'react';
import { useAuthStore } from '@/stores/authStore';

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
 * 权限检查 Hook
 *
 * 使用示例：
 * const { hasPermission } = usePermission();
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
