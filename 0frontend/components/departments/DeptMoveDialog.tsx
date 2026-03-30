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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import type { Department } from '@/types';

interface DeptMoveDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  department: Department | null;
  departments: Department[];
  onMove: (targetParentId: string | null) => Promise<void>;
}

export function DeptMoveDialog({
  open,
  onOpenChange,
  department,
  departments,
  onMove,
}: DeptMoveDialogProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [selectedParentId, setSelectedParentId] = useState<string>('none');

  useEffect(() => {
    if (open) {
      setSelectedParentId(department?.parentId || 'none');
    }
  }, [open, department]);

  const flattenDepartments = (depts: Department[], level = 0): (Department & { _level: number })[] => {
    let result: (Department & { _level: number })[] = [];
    depts.forEach((dept) => {
      result.push({ ...dept, _level: level });
      if (dept.children) {
        result = [...result, ...flattenDepartments(dept.children, level + 1)];
      }
    });
    return result;
  };

  const flatDepartments = flattenDepartments(departments);

  const isDescendant = (parentId: string, dept: Department | null): boolean => {
    if (!dept) return false;
    if (parentId === dept.id) return true;
    if (dept.parentId) {
      return isDescendant(parentId, flatDepartments.find((d) => d.id === dept.parentId) || null);
    }
    return false;
  };

  const availableParents = flatDepartments.filter(
    (d) => d.id !== department?.id && !isDescendant(d.id, department)
  );

  const handleSubmit = async () => {
    setIsLoading(true);
    try {
      await onMove(selectedParentId === 'none' ? null : selectedParentId);
      onOpenChange(false);
    } catch (error) {
      console.error('移动部门失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  if (!department) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[450px]">
        <DialogHeader>
          <DialogTitle>移动部门</DialogTitle>
          <DialogDescription>
            将 "{department.name}" 移动到新的上级部门下
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="parent">选择上级部门</Label>
            <Select
              value={selectedParentId}
              onValueChange={setSelectedParentId}
            >
              <SelectTrigger>
                <SelectValue placeholder="请选择上级部门" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="none">无（作为顶级部门）</SelectItem>
                {availableParents.map((dept) => (
                  <SelectItem key={dept.id} value={dept.id}>
                    {'  '.repeat(dept._level)}
                    {dept.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              当前层级：第 {department.level} 级（最多 5 级）
            </p>
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
          <Button
            type="button"
            onClick={handleSubmit}
            disabled={isLoading}
          >
            {isLoading ? '移动中...' : '确认移动'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
