// 用户类型
export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  avatar?: string;
  departmentId?: string;
  roleIds?: string[];
  status: 'active' | 'inactive' | 'locked';
  createdAt: string;
  updatedAt: string;
}

// 角色类型
export interface Role {
  id: string;
  name: string;
  code: string;
  description?: string;
  permissions: Permission[];
  createdAt: string;
  updatedAt: string;
}

// 权限类型
export interface Permission {
  id: string;
  name: string;
  code: string;
  resource: string;
  actions: string[];
}

// 部门类型
export interface Department {
  id: string;
  name: string;
  code: string;
  parentId?: string;
  parent?: Department;
  children?: Department[];
  level: number;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

// 认证相关类型
export interface LoginRequest {
  username?: string;
  email?: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: 'Bearer';
  user: User;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: 'Bearer';
}

// API 响应类型
export interface ApiResponse<T = unknown> {
  success: boolean;
  data: T | null;
  error?: string;
  message?: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

// 查询参数类型
export interface QueryParams {
  page?: number;
  limit?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  search?: string;
  [key: string]: unknown;
}

// 数据范围类型
export type DataScope = 'ALL' | 'DEPT' | 'DEPT_ONLY' | 'SELF';
