import { usePermissionStore } from '@/stores/permissionStore';

/**
 * 前端权限控制 Hook
 *
 * 用于组件内按钮级权限控制
 *
 * @example
 * ```tsx
 * function UserList() {
 *   const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermission();
 *
 *   return (
 *     <div>
 *       {hasPermission('user:create') && <Button>新增用户</Button>}
 *       {hasAnyPermission(['user:edit', 'user:delete']) && <ActionsMenu />}
 *       {hasAllPermissions(['user:view', 'user:export']) && <ExportButton />}
 *     </div>
 *   );
 * }
 * ```
 */
export function usePermission() {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermissionStore();
  return { hasPermission, hasAnyPermission, hasAllPermissions };
}

/**
 * 权限控制组件 Props
 */
export interface PermissionProps {
  /** 需要的权限代码 */
  permission?: string;
  /** 需要的权限代码列表 (任一满足) */
  anyPermissions?: string[];
  /** 需要的权限代码列表 (全部满足) */
  allPermissions?: string[];
  /** 子组件 */
  children: React.ReactNode;
  /** 无权限时的回退组件 */
  fallback?: React.ReactNode;
}

/**
 * 权限控制组件
 *
 * 用于 JSX 中声明式权限控制
 *
 * @example
 * ```tsx
 * <Permission permission="user:create">
 *   <Button>新增用户</Button>
 * </Permission>
 *
 * <Permission anyPermissions={['user:edit', 'user:delete']}>
 *   <ActionsMenu />
 * </Permission>
 *
 * <Permission allPermissions={['user:view', 'user:export']} fallback={<NoAccess />}>
 *   <ExportButton />
 * </Permission>
 * ```
 */
export function Permission({
  permission,
  anyPermissions,
  allPermissions,
  children,
  fallback = null,
}: PermissionProps) {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermission();

  let hasAccess = true;

  if (permission) {
    hasAccess = hasPermission(permission);
  } else if (anyPermissions) {
    hasAccess = hasAnyPermission(anyPermissions);
  } else if (allPermissions) {
    hasAccess = hasAllPermissions(allPermissions);
  }

  return hasAccess ? <>{children}</> : <>{fallback}</>;
}
