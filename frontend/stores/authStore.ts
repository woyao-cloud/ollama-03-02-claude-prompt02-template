import { create } from 'zustand';
import { authService } from '@/lib/api';
import { usePermissionStore } from './permissionStore';
import type { User, Permission } from '@/types';

// Cookie 工具函数
const cookies = {
  get: (name: string): string | undefined => {
    if (typeof document === 'undefined') return undefined;
    const value = document.cookie.match(`(^|;)\\s*${name}\\s*=\\s*([^;]+)`);
    return value ? value.pop() : undefined;
  },
  set: (name: string, value: string, days: number = 7): void => {
    if (typeof document === 'undefined') return;
    const expires = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString();
    document.cookie = `${name}=${value}; expires=${expires}; path=/; SameSite=Strict; Secure`;
  },
  remove: (name: string): void => {
    if (typeof document === 'undefined') return;
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`;
  },
};

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
  clearError: () => void;
  hasPermission: (permissionCode: string) => boolean;
  hasAnyPermission: (permissionCodes: string[]) => boolean;
  hasAllPermissions: (permissionCodes: string[]) => boolean;
  refreshPermissions: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  login: async (identifier: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      // 判断是邮箱还是用户名
      const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(identifier);
      const loginData = {
        username: isEmail ? '' : identifier,
        email: isEmail ? identifier : '',
        password,
      };
      const response = await authService.login(loginData);
      // 存储 Token 到 Cookie
      cookies.set('access_token', response.accessToken, 1);
      cookies.set('refresh_token', response.refreshToken, 7);
      // 设置用户信息
      set({ user: response.user, isAuthenticated: true, isLoading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : '登录失败';
      set({ error: message, isLoading: false });
      throw error;
    }
  },

  logout: async () => {
    set({ isLoading: true });
    try {
      await authService.logout();
    } finally {
      // 清除 Cookie 和状态
      cookies.remove('access_token');
      cookies.remove('refresh_token');
      set({ user: null, isAuthenticated: false, isLoading: false });
    }
  },

  checkAuth: async () => {
    set({ isLoading: true });
    try {
      const user = await authService.getCurrentUser();
      set({ user, isAuthenticated: true, isLoading: false });
    } catch {
      // Token 可能过期，清除 Cookie
      cookies.remove('access_token');
      cookies.remove('refresh_token');
      set({ user: null, isAuthenticated: false, isLoading: false });
    }
  },

  clearError: () => {
    set({ error: null });
  },

  // 权限检查：检查用户是否拥有指定权限
  hasPermission: (permissionCode: string): boolean => {
    const { user } = get();
    if (!user) return false;

    // 如果是超级管理员，拥有所有权限
    if (user.roleIds?.includes('admin')) return true;

    // 使用 permissionStore 检查权限
    return usePermissionStore.getState().hasPermission(permissionCode);
  },

  // 检查是否拥有任意一个权限
  hasAnyPermission: (permissionCodes: string[]): boolean => {
    return permissionCodes.some(code => get().hasPermission(code));
  },

  // 检查是否拥有所有权限
  hasAllPermissions: (permissionCodes: string[]): boolean => {
    return permissionCodes.every(code => get().hasPermission(code));
  },

  // 刷新用户权限
  refreshPermissions: async (): Promise<void> => {
    await usePermissionStore.getState().refreshPermissions();
  },
}));

// 导出 Cookie 工具供 middleware 使用
export { cookies };
