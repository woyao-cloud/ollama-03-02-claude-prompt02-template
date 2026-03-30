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
import type { Department } from '@/types';

interface DeptFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  department?: Department | null;
  departments: Department[];
  onSubmit: (data: DeptFormData) => Promise<void>;
}

export interface DeptFormData {
  name: string;
  code: string;
  parentId?: string;
  level: number;
  sortOrder: number;
}

export function DeptForm({
  open,
  onOpenChange,
  department,
  departments,
  onSubmit,
}: DeptFormProps) {
  const isEdit = !!department;
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof DeptFormData, string>>>({});

  const [formData, setFormData] = useState<DeptFormData>({
    name: '',
    code: '',
    parentId: '',
    level: 1,
    sortOrder: 0,
  });

  useEffect(() => {
    if (department) {
      setFormData({
        name: department.name,
        code: department.code,
        parentId: department.parentId || '',
        level: department.level,
        sortOrder: department.sortOrder,
      });
    } else {
      setFormData({
        name: '',
        code: '',
        parentId: '',
        level: 1,
        sortOrder: 0,
      });
    }
    setErrors({});
  }, [department, open]);

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof DeptFormData, string>> = {};

    if (!formData.name.trim()) {
      newErrors.name = '部门名称不能为空';
    }

    if (!formData.code.trim()) {
      newErrors.code = '部门代码不能为空';
    } else if (!/^[a-zA-Z0-9_-]+$/.test(formData.code)) {
      newErrors.code = '部门代码只能包含字母、数字、下划线和短横线';
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
      await onSubmit({
        ...formData,
        parentId: formData.parentId || undefined,
      });
      onOpenChange(false);
    } catch (error) {
      console.error('表单提交失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const flattenDepartments = (depts: Department[], level = 0): Department[] => {
    let result: Department[] = [];
    depts.forEach((dept) => {
      result.push({ ...dept, _level: level });
      if (dept.children) {
        result = [...result, ...flattenDepartments(dept.children, level + 1)];
      }
    });
    return result;
  };

  const flatDepartments = flattenDepartments(departments);

  const getParentLevel = (parentId?: string): number => {
    if (!parentId) return 0;
    const parent = flatDepartments.find((d) => d.id === parentId);
    return parent ? (parent as any)._level + 1 : 1;
  };

  const currentMaxLevel = getParentLevel(formData.parentId);

  const handleParentChange = (value: string) => {
    setFormData((prev) => ({
      ...prev,
      parentId: value === 'none' ? '' : value,
      level: value === 'none' ? 1 : getParentLevel(value),
    }));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{isEdit ? '编辑部门' : '创建部门'}</DialogTitle>
            <DialogDescription>
              {isEdit ? '修改部门信息' : '填写以下信息创建新部门'}
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="name">部门名称 *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, name: e.target.value }))
                }
                placeholder="请输入部门名称"
                disabled={isLoading}
              />
              {errors.name && (
                <p className="text-sm text-destructive">{errors.name}</p>
              )}
            </div>

            <div className="grid gap-2">
              <Label htmlFor="code">部门代码 *</Label>
              <Input
                id="code"
                value={formData.code}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, code: e.target.value }))
                }
                placeholder="请输入部门代码，如：IT, HR, FIN"
                disabled={isLoading}
              />
              {errors.code && (
                <p className="text-sm text-destructive">{errors.code}</p>
              )}
            </div>

            <div className="grid gap-2">
              <Label htmlFor="parent">上级部门</Label>
              <Select
                value={formData.parentId || 'none'}
                onValueChange={handleParentChange}
              >
                <SelectTrigger>
                  <SelectValue placeholder="请选择上级部门" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">无（作为顶级部门）</SelectItem>
                  {flatDepartments
                    .filter((d) => !isEdit || d.id !== department!.id)
                    .map((dept) => (
                      <SelectItem key={dept.id} value={dept.id}>
                        {'  '.repeat((dept as any)._level || 0)}
                        {dept.name}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">
                当前层级：第 {currentMaxLevel + 1} 级（最多 5 级）
              </p>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="sortOrder">排序</Label>
              <Input
                id="sortOrder"
                type="number"
                min="0"
                value={formData.sortOrder}
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    sortOrder: parseInt(e.target.value) || 0,
                  }))
                }
                placeholder="数字越小越靠前"
                disabled={isLoading}
              />
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
