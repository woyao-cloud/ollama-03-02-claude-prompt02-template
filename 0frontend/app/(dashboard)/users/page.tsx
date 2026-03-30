'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { userService } from '@/lib/api';
import { useToast } from '@/components/ui/use-toast';
import type { User, Role, Department, PaginatedResponse } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { UserTable, UserFilters, UserForm, ImportModal, UserDetailDialog, DeleteConfirmDialog } from '@/components/users';
import { Plus, Upload, Users } from 'lucide-react';

export default function UsersPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const { toast } = useToast();

  // 用户数据
  const [users, setUsers] = useState<User[]>([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 10, total: 0, totalPages: 0 });
  const [isLoading, setIsLoading] = useState(true);

  // 筛选条件
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('all');
  const [departmentId, setDepartmentId] = useState('all');

  // Modal 状态
  const [formOpen, setFormOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  // 角色和部门数据（用于表单）
  const [roles] = useState<Role[]>([]);
  const [departments] = useState<Department[]>([]);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  useEffect(() => {
    if (isAuthenticated) {
      loadUsers();
    }
  }, [isAuthenticated, pagination.page, search, status, departmentId]);

  const loadUsers = async () => {
    setIsLoading(true);
    try {
      const params: Record<string, string | number> = {
        page: pagination.page,
        limit: pagination.limit,
      };
      if (search) params.search = search;
      if (status !== 'all') params.status = status;
      if (departmentId !== 'all') params.departmentId = departmentId;

      const data = await userService.getUsers(params);
      setUsers(data.items);
      setPagination({
        page: data.page,
        limit: data.limit,
        total: data.total,
        totalPages: data.totalPages,
      });
    } catch (error) {
      toast({
        title: '加载失败',
        description: error instanceof Error ? error.message : '获取用户列表失败',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = (user: User) => {
    setSelectedUser(user);
    setFormOpen(true);
  };

  const handleView = (user: User) => {
    setSelectedUser(user);
    setDetailOpen(true);
  };

  const handleDelete = (user: User) => {
    setSelectedUser(user);
    setDeleteOpen(true);
  };

  const confirmDelete = async () => {
    if (!selectedUser) return;
    try {
      await userService.deleteUser(selectedUser.id);
      toast({
        title: '删除成功',
        description: '用户已被删除',
        variant: 'success',
      });
      loadUsers();
    } catch (error) {
      toast({
        title: '删除失败',
        description: error instanceof Error ? error.message : '删除用户失败',
        variant: 'destructive',
      });
    }
  };

  const handleCreate = () => {
    setSelectedUser(null);
    setFormOpen(true);
  };

  const handleFormSubmit = async (data: {
    username: string;
    email: string;
    password?: string;
    fullName?: string;
    departmentId?: string;
    roleIds: string[];
    status: 'active' | 'inactive' | 'locked';
  }) => {
    try {
      if (selectedUser) {
        await userService.updateUser(selectedUser.id, data);
        toast({
          title: '更新成功',
          description: '用户信息已更新',
          variant: 'success',
        });
      } else {
        await userService.createUser(data);
        toast({
          title: '创建成功',
          description: '新用户已创建',
          variant: 'success',
        });
      }
      loadUsers();
    } catch (error) {
      toast({
        title: selectedUser ? '更新失败' : '创建失败',
        description: error instanceof Error ? error.message : '操作失败',
        variant: 'destructive',
      });
    }
  };

  const handleImport = async (file: File) => {
    // TODO: 实现文件上传 API
    console.log('导入文件:', file);
    // 模拟 API 调用
    await new Promise((resolve) => setTimeout(resolve, 2000));
  };

  const handlePageChange = (newPage: number) => {
    setPagination((prev) => ({ ...prev, page: newPage }));
  };

  const clearFilters = () => {
    setSearch('');
    setStatus('all');
    setDepartmentId('all');
  };

  const hasActiveFilters = search || status !== 'all' || departmentId !== 'all';

  if (authLoading || !isAuthenticated) {
    return null;
  }

  return (
    <div className="space-y-6">
      {/* 页面头部 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">用户管理</h1>
          <p className="text-muted-foreground">管理系统用户和账号</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setImportOpen(true)}>
            <Upload className="mr-2 h-4 w-4" />
            批量导入
          </Button>
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            新增用户
          </Button>
        </div>
      </div>

      {/* 筛选器 */}
      <Card>
        <CardHeader className="py-4">
          <div className="flex items-center gap-2">
            <Users className="h-5 w-5 text-muted-foreground" />
            <CardTitle className="text-base">筛选</CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <UserFilters
            search={search}
            onSearchChange={setSearch}
            status={status}
            onStatusChange={setStatus}
            departmentId={departmentId}
            onDepartmentIdChange={setDepartmentId}
            departments={departments.map((d) => ({ id: d.id, name: d.name }))}
            onClearFilters={clearFilters}
          />
        </CardContent>
      </Card>

      {/* 用户列表 */}
      <Card>
        <CardHeader className="py-4">
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>用户列表</CardTitle>
              <CardDescription>
                共 {pagination.total} 个用户
                {hasActiveFilters && '（已筛选）'}
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <p className="text-muted-foreground">加载中...</p>
            </div>
          ) : users.length === 0 ? (
            <div className="flex items-center justify-center py-8">
              <p className="text-muted-foreground">暂无用户数据</p>
            </div>
          ) : (
            <>
              <UserTable
                users={users}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onView={handleView}
              />

              {/* 分页 */}
              {pagination.totalPages > 1 && (
                <Pagination className="mt-4">
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          if (pagination.page > 1) handlePageChange(pagination.page - 1);
                        }}
                        className={pagination.page === 1 ? 'pointer-events-none opacity-50' : ''}
                      />
                    </PaginationItem>
                    {Array.from({ length: pagination.totalPages }, (_, i) => i + 1).map((page) => (
                      <PaginationItem key={page}>
                        <PaginationLink
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            handlePageChange(page);
                          }}
                          isActive={page === pagination.page}
                        >
                          {page}
                        </PaginationLink>
                      </PaginationItem>
                    ))}
                    <PaginationItem>
                      <PaginationNext
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          if (pagination.page < pagination.totalPages) handlePageChange(pagination.page + 1);
                        }}
                        className={pagination.page === pagination.totalPages ? 'pointer-events-none opacity-50' : ''}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {/* 弹窗 */}
      <UserForm
        open={formOpen}
        onOpenChange={setFormOpen}
        user={selectedUser}
        onSubmit={handleFormSubmit}
        roles={roles}
        departments={departments}
      />

      <ImportModal
        open={importOpen}
        onOpenChange={setImportOpen}
        onImport={handleImport}
      />

      <UserDetailDialog
        open={detailOpen}
        onOpenChange={setDetailOpen}
        user={selectedUser}
        roles={roles}
        departments={departments}
      />

      <DeleteConfirmDialog
        open={deleteOpen}
        onOpenChange={setDeleteOpen}
        user={selectedUser}
        onConfirm={confirmDelete}
      />
    </div>
  );
}
