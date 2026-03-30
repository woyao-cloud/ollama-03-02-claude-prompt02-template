'use client';

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import type { User, Role, Department } from '@/types';

interface UserDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
  roles: Role[];
  departments: Department[];
}

export function UserDetailDialog({
  open,
  onOpenChange,
  user,
  roles,
  departments,
}: UserDetailDialogProps) {
  if (!user) return null;

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

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>用户详情</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
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

          <Separator />

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

          <Separator />

          <div className="grid grid-cols-3 gap-4">
            <div className="text-sm text-muted-foreground">状态</div>
            <div className="col-span-2">{getStatusBadge(user.status)}</div>
          </div>

          <Separator />

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
      </DialogContent>
    </Dialog>
  );
}
