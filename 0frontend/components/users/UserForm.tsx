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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import type { User, Role, Department } from '@/types';

interface UserFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user?: User | null;
  onSubmit: (data: UserFormData) => Promise<void>;
  roles: Role[];
  departments: Department[];
}

export interface UserFormData {
  username: string;
  email: string;
  password?: string;
  fullName?: string;
  departmentId?: string;
  roleIds: string[];
  status: 'active' | 'inactive' | 'locked';
}

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;

export function UserForm({
  open,
  onOpenChange,
  user,
  onSubmit,
  roles,
  departments,
}: UserFormProps) {
  const isEdit = !!user;
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof UserFormData, string>>>({});

  const [formData, setFormData] = useState<UserFormData>({
    username: '',
    email: '',
    password: '',
    fullName: '',
    departmentId: '',
    roleIds: [],
    status: 'active',
  });

  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username,
        email: user.email,
        password: '',
        fullName: user.fullName || '',
        departmentId: user.departmentId || '',
        roleIds: user.roleIds || [],
        status: user.status,
      });
    } else {
      setFormData({
        username: '',
        email: '',
        password: '',
        fullName: '',
        departmentId: '',
        roleIds: [],
        status: 'active',
      });
    }
    setErrors({});
  }, [user, open]);

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof UserFormData, string>> = {};

    if (!formData.username.trim()) {
      newErrors.username = '用户名不能为空';
    }

    if (!formData.email.trim()) {
      newErrors.email = '邮箱不能为空';
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = '邮箱格式不正确';
    }

    if (!isEdit && !formData.password) {
      newErrors.password = '密码不能为空';
    } else if (formData.password && !passwordRegex.test(formData.password)) {
      newErrors.password = '密码必须包含大小写字母和数字，且长度不少于 8 位';
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
      const submitData = { ...formData };
      if (isEdit && !submitData.password) {
        delete submitData.password;
      }
      await onSubmit(submitData);
      onOpenChange(false);
    } catch (error) {
      console.error('表单提交失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRoleToggle = (roleId: string) => {
    setFormData((prev) => ({
      ...prev,
      roleIds: prev.roleIds.includes(roleId)
        ? prev.roleIds.filter((id) => id !== roleId)
        : [...prev.roleIds, roleId],
    }));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{isEdit ? '编辑用户' : '创建用户'}</DialogTitle>
            <DialogDescription>
              {isEdit ? '修改用户信息' : '填写以下信息创建新用户'}
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="username">用户名 *</Label>
              <Input
                id="username"
                value={formData.username}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, username: e.target.value }))
                }
                placeholder="请输入用户名"
                disabled={isLoading}
              />
              {errors.username && (
                <p className="text-sm text-destructive">{errors.username}</p>
              )}
            </div>

            <div className="grid gap-2">
              <Label htmlFor="email">邮箱 *</Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, email: e.target.value }))
                }
                placeholder="请输入邮箱"
                disabled={isLoading}
              />
              {errors.email && (
                <p className="text-sm text-destructive">{errors.email}</p>
              )}
            </div>

            <div className="grid gap-2">
              <Label htmlFor="fullName">姓名</Label>
              <Input
                id="fullName"
                value={formData.fullName}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, fullName: e.target.value }))
                }
                placeholder="请输入姓名"
                disabled={isLoading}
              />
            </div>

            <div className="grid gap-2">
              <Label htmlFor="password">
                {isEdit ? '密码 (留空则不修改)' : '密码 *'}
              </Label>
              <Input
                id="password"
                type="password"
                value={formData.password}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, password: e.target.value }))
                }
                placeholder={isEdit ? '留空则不修改密码' : '请输入密码'}
                disabled={isLoading}
              />
              {errors.password && (
                <p className="text-sm text-destructive">{errors.password}</p>
              )}
            </div>

            <div className="grid gap-2">
              <Label htmlFor="department">部门</Label>
              <Select
                value={formData.departmentId || 'none'}
                onValueChange={(value) =>
                  setFormData((prev) => ({
                    ...prev,
                    departmentId: value === 'none' ? '' : value,
                  }))
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="请选择部门" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">无</SelectItem>
                  {departments.map((dept) => (
                    <SelectItem key={dept.id} value={dept.id}>
                      {dept.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="grid gap-2">
              <Label>角色</Label>
              <div className="grid grid-cols-2 gap-2 rounded-md border p-3">
                {roles.map((role) => (
                  <div
                    key={role.id}
                    className="flex items-center space-x-2"
                  >
                    <Checkbox
                      id={`role-${role.id}`}
                      checked={formData.roleIds.includes(role.id)}
                      onCheckedChange={() => handleRoleToggle(role.id)}
                      disabled={isLoading}
                    />
                    <Label
                      htmlFor={`role-${role.id}`}
                      className="text-sm font-normal cursor-pointer"
                    >
                      {role.name}
                    </Label>
                  </div>
                ))}
              </div>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="status">状态</Label>
              <Select
                value={formData.status}
                onValueChange={(value: 'active' | 'inactive' | 'locked') =>
                  setFormData((prev) => ({ ...prev, status: value }))
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="active">活跃</SelectItem>
                  <SelectItem value="inactive">未激活</SelectItem>
                  <SelectItem value="locked">已锁定</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
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
