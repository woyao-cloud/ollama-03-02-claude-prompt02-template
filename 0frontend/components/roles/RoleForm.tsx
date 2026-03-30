'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { PermissionTree, type PermissionNode } from '@/components/roles/PermissionTree';
import { DataScopeSelect, type DataScope } from '@/components/roles/DataScopeSelect';
import type { Role } from '@/types';

export interface RoleFormData {
  name: string;
  code: string;
  description?: string;
  dataScope: DataScope;
  permissionIds: string[];
}

interface RoleFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  role?: Role | null;
  permissions: PermissionNode[];
  onSubmit: (data: RoleFormData) => Promise<void>;
}

export function RoleForm({
  open,
  onOpenChange,
  role,
  permissions,
  onSubmit,
}: RoleFormProps) {
  const isEdit = !!role;
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof RoleFormData, string>>>({});

  const [formData, setFormData] = useState<RoleFormData>({
    name: '',
    code: '',
    description: '',
    dataScope: 'SELF',
    permissionIds: [],
  });

  useEffect(() => {
    if (role) {
      setFormData({
        name: role.name,
        code: role.code,
        description: role.description || '',
        dataScope: (role as any).dataScope || 'SELF',
        permissionIds: role.permissions.map((p) => p.id),
      });
    } else {
      setFormData({
        name: '',
        code: '',
        description: '',
        dataScope: 'SELF',
        permissionIds: [],
      });
    }
    setErrors({});
  }, [role, open]);

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof RoleFormData, string>> = {};

    if (!formData.name.trim()) {
      newErrors.name = '角色名称不能为空';
    }

    if (!formData.code.trim()) {
      newErrors.code = '角色代码不能为空';
    } else if (!/^[a-zA-Z0-9_-]+$/.test(formData.code)) {
      newErrors.code = '角色代码只能包含字母、数字、下划线和短横线';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      await onSubmit(formData);
      onOpenChange(false);
    } catch (error) {
      console.error('表单提交失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePermissionChange = (selectedIds: string[]) => {
    setFormData((prev) => ({ ...prev, permissionIds: selectedIds }));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-hidden flex flex-col">
        <form onSubmit={handleSubmit} className="flex flex-col flex-1 overflow-hidden">
          <DialogHeader>
            <DialogTitle>{isEdit ? '编辑角色' : '创建角色'}</DialogTitle>
            <DialogDescription>
              {isEdit ? '修改角色信息和权限配置' : '填写以下信息创建新角色'}
            </DialogDescription>
          </DialogHeader>

          <div className="flex-1 overflow-auto py-4 space-y-4">
            <div className="grid gap-2">
              <Label htmlFor="name">角色名称 *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, name: e.target.value }))
                }
                placeholder="请输入角色名称，如：管理员、普通用户"
                disabled={isLoading}
              />
              {errors.name && (
                <p className="text-sm text-destructive">{errors.name}</p>
              )}
            </div>

            <div className="grid gap-2">
              <Label htmlFor="code">角色代码 *</Label>
              <Input
                id="code"
                value={formData.code}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, code: e.target.value }))
                }
                placeholder="请输入角色代码，如：admin, user"
                disabled={isLoading}
              />
              {errors.code && (
                <p className="text-sm text-destructive">{errors.code}</p>
              )}
            </div>

            <div className="grid gap-2">
              <Label htmlFor="description">角色描述</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, description: e.target.value }))
                }
                placeholder="请输入角色描述"
                disabled={isLoading}
                rows={2}
              />
            </div>

            <div className="grid gap-2">
              <Label>数据范围</Label>
              <DataScopeSelect
                value={formData.dataScope}
                onChange={(value) =>
                  setFormData((prev) => ({ ...prev, dataScope: value }))
                }
                disabled={isLoading}
              />
            </div>

            <div className="grid gap-2">
              <Label>权限配置</Label>
              <PermissionTree
                permissions={permissions}
                selectedPermissionIds={formData.permissionIds}
                onChange={handlePermissionChange}
              />
            </div>
          </div>

          <DialogFooter className="border-t pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              取消
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? '保存中...' : isEdit ? '保存' : '创建'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
