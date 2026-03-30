import { create } from 'zustand';
import { permissionService, type PermissionNode } from '@/lib/api/services/permission';
import type { Permission } from '@/types';

interface PermissionState {
  // 所有可用权限（树形结构）
  allPermissions: PermissionNode[];
  // 当前用户拥有的权限
  userPermissions: Permission[];
  // 用户权限代码集合（用于快速查找）
  permissionCodes: Set<string>;
  // 数据权限范围
  dataScope: string;
  // 加载状态
  isLoading: boolean;
  // 错误信息
  error: string | null;
  // 上次刷新时间
  lastRefreshTime: number | null;

  // Actions
  loadAllPermissions: () => Promise<void>;
  loadUserPermissions: () => Promise<void>;
  refreshPermissions: () => Promise<void>;
  clearPermissions: () => void;
  hasPermission: (code: string) => boolean;
  hasAnyPermission: (codes: string[]) => boolean;
  hasAllPermissions: (codes: string[]) => boolean;
  setDataScope: (scope: string) => void;
}

const CACHE_DURATION = 5 * 60 * 1000; // 5 分钟缓存

export const usePermissionStore = create<PermissionState>((set, get) => ({
  allPermissions: [],
  userPermissions: [],
  permissionCodes: new Set(),
  dataScope: 'SELF',
  isLoading: false,
  error: null,
  lastRefreshTime: null,

  loadAllPermissions: async () => {
    set({ isLoading: true, error: null });
    try {
      const permissions = await permissionService.getAllPermissions();
      set({ allPermissions: permissions, isLoading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : '加载权限失败';
      set({ error: message, isLoading: false });
      throw error;
    }
  },

  loadUserPermissions: async () => {
    set({ isLoading: true, error: null });
    try {
      const permissions = await permissionService.getCurrentUserPermissions();
      const codes = new Set(permissions.map((p) => p.code));
      set({
        userPermissions: permissions,
        permissionCodes: codes,
        isLoading: false,
        lastRefreshTime: Date.now(),
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : '加载用户权限失败';
      set({ error: message, isLoading: false });
      throw error;
    }
  },

  refreshPermissions: async () => {
    // 检查缓存是否有效
    const { lastRefreshTime } = get();
    if (lastRefreshTime && Date.now() - lastRefreshTime < CACHE_DURATION) {
      return; // 缓存有效，跳过刷新
    }
    await get().loadUserPermissions();
  },

  clearPermissions: () => {
    set({
      allPermissions: [],
      userPermissions: [],
      permissionCodes: new Set(),
      dataScope: 'SELF',
      lastRefreshTime: null,
      error: null,
    });
  },

  hasPermission: (code: string): boolean => {
    const { permissionCodes } = get();
    // 如果是超级管理员，拥有所有权限
    if (permissionCodes.has('admin') || permissionCodes.has('*')) return true;
    return permissionCodes.has(code);
  },

  hasAnyPermission: (codes: string[]): boolean => {
    return codes.some((code) => get().hasPermission(code));
  },

  hasAllPermissions: (codes: string[]): boolean => {
    return codes.every((code) => get().hasPermission(code));
  },

  setDataScope: (scope: string) => {
    set({ dataScope: scope });
  },
}));

// 导出数据范围常量
export const DATA_SCOPE = {
  ALL: 'ALL', // 全部数据
  DEPT: 'DEPT', // 本部门及以下
  DEPT_ONLY: 'DEPT_ONLY', // 仅本部门
  SELF: 'SELF', // 仅本人数据
} as const;

export type DataScope = (typeof DATA_SCOPE)[keyof typeof DATA_SCOPE];
