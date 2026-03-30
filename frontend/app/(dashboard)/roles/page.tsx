'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { roleService } from '@/lib/api/services/role';
import { useToast } from '@/components/ui/use-toast';
import type { Role } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { RoleForm, RoleDetailDialog, DataScopeBadge } from '@/components/roles';
import { Plus, Shield, Pencil, Trash2, Eye } from 'lucide-react';
import type { PermissionNode } from '@/components/roles/PermissionTree';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';

// 模拟权限树数据 - 实际应从 API 获取
const mockPermissions: PermissionNode[] = [
  {
    id: 'user',
    name: '用户管理',
    code: 'user',
    resource: 'user',
    actions: ['create', 'read', 'update', 'delete'],
    children: [
      { id: 'user:create', name: '创建用户', code: 'user:create', resource: 'user', actions: ['create'] },
      { id: 'user:read', name: '查看用户', code: 'user:read', resource: 'user', actions: ['read'] },
      { id: 'user:update', name: '编辑用户', code: 'user:update', resource: 'user', actions: ['update'] },
      { id: 'user:delete', name: '删除用户', code: 'user:delete', resource: 'user', actions: ['delete'] },
    ],
  },
  {
    id: 'role',
    name: '角色管理',
    code: 'role',
    resource: 'role',
    actions: ['create', 'read', 'update', 'delete'],
    children: [
      { id: 'role:create', name: '创建角色', code: 'role:create', resource: 'role', actions: ['create'] },
      { id: 'role:read', name: '查看角色', code: 'role:read', resource: 'role', actions: ['read'] },
      { id: 'role:update', name: '编辑角色', code: 'role:update', resource: 'role', actions: ['update'] },
      { id: 'role:delete', name: '删除角色', code: 'role:delete', resource: 'role', actions: ['delete'] },
    ],
  },
  {
    id: 'department',
    name: '部门管理',
    code: 'department',
    resource: 'department',
    actions: ['create', 'read', 'update', 'delete'],
    children: [
      { id: 'department:create', name: '创建部门', code: 'department:create', resource: 'department', actions: ['create'] },
      { id: 'department:read', name: '查看部门', code: 'department:read', resource: 'department', actions: ['read'] },
      { id: 'department:update', name: '编辑部门', code: 'department:update', resource: 'department', actions: ['update'] },
      { id: 'department:delete', name: '删除部门', code: 'department:delete', resource: 'department', actions: ['delete'] },
    ],
  },
];

export default function RolesPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const { toast } = useToast();

  const [roles, setRoles] = useState<Role[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);

  const [formOpen, setFormOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  useEffect(() => {
    if (isAuthenticated) {
      loadRoles();
    }
  }, [isAuthenticated]);

  const loadRoles = async () => {
    setIsLoading(true);
    try {
      const data = await roleService.getAllRoles();
      setRoles(data);
    } catch (error) {
      toast({
        title: '加载失败',
        description: error instanceof Error ? error.message : '获取角色列表失败',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreate = () => {
    setSelectedRole(null);
    setFormOpen(true);
  };

  const handleEdit = () => {
    if (selectedRole) {
      setFormOpen(true);
    }
  };

  const handleView = () => {
    if (selectedRole) {
      setDetailOpen(true);
    }
  };

  const handleDelete = () => {
    if (selectedRole) {
      setDeleteOpen(true);
    }
  };

  const handleFormSubmit = async (data: {
    name: string;
    code: string;
    description?: string;
    dataScope: string;
    permissionIds: string[];
  }) => {
    try {
      // 将 permissionIds 转换为 Permission 对象
      const permissions = data.permissionIds.map((id) => {
        // 从 mockPermissions 中查找对应的权限信息
        const findPermission = (nodes: PermissionNode[], id: string): any => {
          for (const node of nodes) {
            if (node.id === id) return node;
            if (node.children) {
              const found = findPermission(node.children, id);
              if (found) return found;
            }
          }
          return null;
        };
        return findPermission(mockPermissions, id);
      }).filter(Boolean);

      if (selectedRole) {
        await roleService.updateRole(selectedRole.id, {
          name: data.name,
          code: data.code,
          description: data.description,
          dataScope: data.dataScope,
          permissions,
        });
        toast({
          title: '更新成功',
          description: '角色信息已更新',
          variant: 'success',
        });
      } else {
        await roleService.createRole({
          name: data.name,
          code: data.code,
          description: data.description,
          dataScope: data.dataScope,
          permissions,
        });
        toast({
          title: '创建成功',
          description: '新角色已创建',
          variant: 'success',
        });
      }
      loadRoles();
    } catch (error) {
      toast({
        title: selectedRole ? '更新失败' : '创建失败',
        description: error instanceof Error ? error.message : '操作失败',
        variant: 'destructive',
      });
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedRole) return;

    try {
      await roleService.deleteRole(selectedRole.id);
      toast({
        title: '删除成功',
        description: '角色已被删除',
        variant: 'success',
      });
      setSelectedRole(null);
      loadRoles();
    } catch (error) {
      toast({
        title: '删除失败',
        description: error instanceof Error ? error.message : '删除角色失败',
        variant: 'destructive',
      });
    }
  };

  const hasSelectedRole = !!selectedRole;

  if (authLoading || !isAuthenticated) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">角色管理</h1>
          <p className="text-muted-foreground">管理系统角色和权限配置</p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          新增角色
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader className="py-4">
            <div className="flex items-center gap-2">
              <Shield className="h-5 w-5 text-muted-foreground" />
              <CardTitle className="text-base">角色列表</CardTitle>
            </div>
            <CardDescription>选择角色进行查看、编辑或删除操作</CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <p className="text-muted-foreground">加载中...</p>
              </div>
            ) : (
              <div className="border rounded-lg">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>角色名称</TableHead>
                      <TableHead>角色代码</TableHead>
                      <TableHead>数据范围</TableHead>
                      <TableHead>权限数</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {roles.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4} className="text-center text-muted-foreground py-8">
                          暂无角色数据
                        </TableCell>
                      </TableRow>
                    ) : (
                      roles.map((role) => (
                        <TableRow
                          key={role.id}
                          className={selectedRole?.id === role.id ? 'bg-muted' : ''}
                          onClick={() => setSelectedRole(role)}
                          style={{ cursor: 'pointer' }}
                        >
                          <TableCell className="font-medium">{role.name}</TableCell>
                          <TableCell className="font-mono text-sm">{role.code}</TableCell>
                          <TableCell>
                            <DataScopeBadge value={((role as any).dataScope) || 'SELF'} />
                          </TableCell>
                          <TableCell>
                            <Badge variant="secondary">{role.permissions.length}</Badge>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="py-4">
            <div className="flex items-center gap-2">
              <Shield className="h-5 w-5 text-muted-foreground" />
              <CardTitle className="text-base">角色详情</CardTitle>
            </div>
            <CardDescription>查看和编辑选中角色的信息</CardDescription>
          </CardHeader>
          <CardContent>
            {selectedRole ? (
              <div className="space-y-4">
                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">角色名称</div>
                  <div className="text-lg font-medium">{selectedRole.name}</div>
                </div>

                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">角色代码</div>
                  <div className="text-lg font-medium font-mono">{selectedRole.code}</div>
                </div>

                {selectedRole.description && (
                  <div className="grid gap-2">
                    <div className="text-sm text-muted-foreground">角色描述</div>
                    <div className="text-sm">{selectedRole.description}</div>
                  </div>
                )}

                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">数据范围</div>
                  <div>
                    <DataScopeBadge value={((selectedRole as any).dataScope) || 'SELF'} />
                  </div>
                </div>

                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">权限数量</div>
                  <div className="text-lg font-medium">{selectedRole.permissions.length} 个</div>
                </div>

                <div className="flex flex-wrap gap-1 pt-2">
                  {selectedRole.permissions.slice(0, 6).map((permission) => (
                    <Badge key={permission.id} variant="outline" className="text-xs">
                      {permission.name}
                    </Badge>
                  ))}
                  {selectedRole.permissions.length > 6 && (
                    <Badge variant="secondary" className="text-xs">
                      +{selectedRole.permissions.length - 6} 更多
                    </Badge>
                  )}
                </div>

                <div className="flex gap-2 pt-4 border-t">
                  <Button variant="outline" onClick={handleView}>
                    <Eye className="mr-2 h-4 w-4" />
                    详情
                  </Button>
                  <Button variant="outline" onClick={handleEdit} disabled={!hasSelectedRole}>
                    <Pencil className="mr-2 h-4 w-4" />
                    编辑
                  </Button>
                  <Button
                    variant="outline"
                    onClick={handleDelete}
                    disabled={!hasSelectedRole}
                    className="text-destructive hover:text-destructive"
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    删除
                  </Button>
                </div>
              </div>
            ) : (
              <div className="flex items-center justify-center py-8 text-muted-foreground">
                请从左侧选择一个角色
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <RoleForm
        open={formOpen}
        onOpenChange={setFormOpen}
        role={selectedRole}
        permissions={mockPermissions}
        onSubmit={handleFormSubmit}
      />

      <RoleDetailDialog
        open={detailOpen}
        onOpenChange={setDetailOpen}
        role={selectedRole}
      />

      {deleteOpen && (
        <Dialog open={deleteOpen} onOpenChange={setDeleteOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>确认删除</DialogTitle>
              <DialogDescription>
                确定要删除角色 &quot;{selectedRole?.name}&quot; 吗？此操作不可恢复。
              </DialogDescription>
            </DialogHeader>
            <div className="flex justify-end gap-2 pt-4">
              <Button variant="outline" onClick={() => setDeleteOpen(false)}>
                取消
              </Button>
              <Button variant="destructive" onClick={handleDeleteConfirm}>
                删除
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
