'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { userService } from '@/lib/api';
import { useToast } from '@/components/ui/use-toast';
import type { User, Role, Department } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { DeleteConfirmDialog, UserForm } from '@/components/users';
import { ArrowLeft, MoreVertical, Pencil, Trash2, User as UserIcon } from 'lucide-react';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';

export default function UserDetailPage() {
  const router = useRouter();
  const params = useParams();
  const userId = params.id as string;
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const { toast } = useToast();

  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [roles] = useState<Role[]>([]);
  const [departments] = useState<Department[]>([]);

  // Modal 状态
  const [formOpen, setFormOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  useEffect(() => {
    if (isAuthenticated && userId) {
      loadUser();
    }
  }, [isAuthenticated, userId]);

  const loadUser = async () => {
    setIsLoading(true);
    try {
      const data = await userService.getUserById(userId);
      setUser(data);
    } catch (error) {
      toast({
        title: '加载失败',
        description: error instanceof Error ? error.message : '获取用户信息失败',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusBadge = (status: User['status']) => {
    switch (status) {
      case 'active':
        return <Badge variant="success">活跃</Badge>;
      case 'inactive':
        return <Badge variant="secondary">未激活</Badge>;
      case 'locked':
        return <Badge variant="destructive">已锁定</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const getRoleNames = (roleIds?: string[]) => {
    if (!roleIds?.length) return '无';
    return roleIds
      .map((id) => roles.find((r) => r.id === id)?.name)
      .filter(Boolean)
      .join(', ');
  };

  const getDepartmentName = (departmentId?: string) => {
    if (!departmentId) return '无';
    return departments.find((d) => d.id === departmentId)?.name || departmentId;
  };

  const handleDelete = async () => {
    if (!user) return;
    try {
      await userService.deleteUser(user.id);
      toast({
        title: '删除成功',
        description: '用户已被删除',
        variant: 'success',
      });
      router.push('/users');
    } catch (error) {
      toast({
        title: '删除失败',
        description: error instanceof Error ? error.message : '删除用户失败',
        variant: 'destructive',
      });
    }
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
    if (!user) return;
    try {
      await userService.updateUser(user.id, data);
      toast({
        title: '更新成功',
        description: '用户信息已更新',
        variant: 'success',
      });
      loadUser();
      setFormOpen(false);
    } catch (error) {
      toast({
        title: '更新失败',
        description: error instanceof Error ? error.message : '更新用户失败',
        variant: 'destructive',
      });
    }
  };

  if (authLoading || !isAuthenticated) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-muted-foreground">加载中...</p>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-muted-foreground">用户不存在</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 页面头部 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.push('/users')}
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold">用户详情</h1>
            <p className="text-muted-foreground">查看和编辑用户信息</p>
          </div>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline">
              <MoreVertical className="mr-2 h-4 w-4" />
              更多操作
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => setFormOpen(true)}>
              <Pencil className="mr-2 h-4 w-4" />
              编辑
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={() => setDeleteOpen(true)}
              className="text-destructive focus:text-destructive"
            >
              <Trash2 className="mr-2 h-4 w-4" />
              删除
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* 用户信息卡片 */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Avatar className="h-16 w-16">
              <AvatarFallback className="text-lg">
                {user.fullName?.charAt(0) || user.username.charAt(0)}
              </AvatarFallback>
            </Avatar>
            <div>
              <CardTitle className="text-xl">{user.fullName || user.username}</CardTitle>
              <CardDescription>@{user.username}</CardDescription>
            </div>
            <div className="ml-auto">{getStatusBadge(user.status)}</div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* 基本信息 */}
          <div>
            <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
              <UserIcon className="h-5 w-5 text-muted-foreground" />
              基本信息
            </h3>
            <div className="grid gap-4">
              <div className="grid grid-cols-3 gap-4">
                <div className="text-sm text-muted-foreground">用户名</div>
                <div className="col-span-2 font-medium">{user.username}</div>
              </div>
              <Separator />
              <div className="grid grid-cols-3 gap-4">
                <div className="text-sm text-muted-foreground">邮箱</div>
                <div className="col-span-2 font-medium">{user.email}</div>
              </div>
              <Separator />
              <div className="grid grid-cols-3 gap-4">
                <div className="text-sm text-muted-foreground">姓名</div>
                <div className="col-span-2 font-medium">{user.fullName || '-'}</div>
              </div>
            </div>
          </div>

          <Separator />

          {/* 组织信息 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">组织信息</h3>
            <div className="grid gap-4">
              <div className="grid grid-cols-3 gap-4">
                <div className="text-sm text-muted-foreground">部门</div>
                <div className="col-span-2 font-medium">
                  {getDepartmentName(user.departmentId)}
                </div>
              </div>
              <Separator />
              <div className="grid grid-cols-3 gap-4">
                <div className="text-sm text-muted-foreground">角色</div>
                <div className="col-span-2 font-medium">
                  {getRoleNames(user.roleIds)}
                </div>
              </div>
            </div>
          </div>

          <Separator />

          {/* 时间信息 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">时间信息</h3>
            <div className="grid gap-4">
              <div className="grid grid-cols-3 gap-4">
                <div className="text-sm text-muted-foreground">创建时间</div>
                <div className="col-span-2 font-medium">
                  {new Date(user.createdAt).toLocaleString('zh-CN')}
                </div>
              </div>
              <Separator />
              <div className="grid grid-cols-3 gap-4">
                <div className="text-sm text-muted-foreground">更新时间</div>
                <div className="col-span-2 font-medium">
                  {new Date(user.updatedAt).toLocaleString('zh-CN')}
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 弹窗 */}
      <UserForm
        open={formOpen}
        onOpenChange={setFormOpen}
        user={user}
        onSubmit={handleFormSubmit}
        roles={roles}
        departments={departments}
      />

      <DeleteConfirmDialog
        open={deleteOpen}
        onOpenChange={setDeleteOpen}
        user={user}
        onConfirm={handleDelete}
      />
    </div>
  );
}
