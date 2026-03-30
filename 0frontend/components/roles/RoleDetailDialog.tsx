'use client';

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { DataScopeBadge, type DataScope } from '@/components/roles/DataScopeSelect';
import type { Role } from '@/types';

interface RoleDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  role: Role | null;
}

export function RoleDetailDialog({
  open,
  onOpenChange,
  role,
}: RoleDetailDialogProps) {
  if (!role) return null;

  const dataScope = ((role as any).dataScope as DataScope) || 'SELF';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>角色详情</DialogTitle>
          <DialogDescription>
            查看角色的详细信息和权限配置
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="grid gap-2">
            <div className="text-sm text-muted-foreground">角色名称</div>
            <div className="text-lg font-medium">{role.name}</div>
          </div>

          <div className="grid gap-2">
            <div className="text-sm text-muted-foreground">角色代码</div>
            <div className="text-lg font-medium font-mono">{role.code}</div>
          </div>

          {role.description && (
            <div className="grid gap-2">
              <div className="text-sm text-muted-foreground">角色描述</div>
              <div className="text-sm">{role.description}</div>
            </div>
          )}

          <div className="grid gap-2">
            <div className="text-sm text-muted-foreground">数据范围</div>
            <div>
              <DataScopeBadge value={dataScope} />
            </div>
          </div>

          <Separator />

          <div className="grid gap-2">
            <div className="text-sm text-muted-foreground">
              权限配置 ({role.permissions.length} 个)
            </div>
            <div className="flex flex-wrap gap-1 max-h-[200px] overflow-auto">
              {role.permissions.length === 0 ? (
                <span className="text-sm text-muted-foreground">暂无权限</span>
              ) : (
                role.permissions.map((permission) => (
                  <Badge key={permission.id} variant="outline" className="text-xs">
                    {permission.name}
                  </Badge>
                ))
              )}
            </div>
          </div>

          <Separator />

          <div className="grid grid-cols-2 gap-4 text-xs text-muted-foreground">
            <div>
              <div>创建时间</div>
              <div className="font-medium">
                {new Date(role.createdAt).toLocaleString('zh-CN')}
              </div>
            </div>
            <div>
              <div>更新时间</div>
              <div className="font-medium">
                {new Date(role.updatedAt).toLocaleString('zh-CN')}
              </div>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
