'use client';

import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Search, X } from 'lucide-react';

interface UserFiltersProps {
  search: string;
  onSearchChange: (value: string) => void;
  status: string;
  onStatusChange: (value: string) => void;
  departmentId: string;
  onDepartmentIdChange: (value: string) => void;
  departments?: { id: string; name: string }[];
  onClearFilters: () => void;
}

export function UserFilters({
  search,
  onSearchChange,
  status,
  onStatusChange,
  departmentId,
  onDepartmentIdChange,
  departments = [],
  onClearFilters,
}: UserFiltersProps) {
  const hasActiveFilters = search || status || departmentId;

  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="搜索用户名、邮箱或姓名..."
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          className="pl-9"
        />
      </div>

      <Select value={status} onValueChange={onStatusChange}>
        <SelectTrigger className="w-[150px]">
          <SelectValue placeholder="全部状态" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">全部状态</SelectItem>
          <SelectItem value="active">活跃</SelectItem>
          <SelectItem value="inactive">未激活</SelectItem>
          <SelectItem value="locked">已锁定</SelectItem>
        </SelectContent>
      </Select>

      <Select
        value={departmentId}
        onValueChange={onDepartmentIdChange}
      >
        <SelectTrigger className="w-[150px]">
          <SelectValue placeholder="全部部门" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">全部部门</SelectItem>
          {departments.map((dept) => (
            <SelectItem key={dept.id} value={dept.id}>
              {dept.name}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {hasActiveFilters && (
        <Button
          variant="ghost"
          size="sm"
          onClick={onClearFilters}
          className="gap-1"
        >
          <X className="h-4 w-4" />
          清除筛选
        </Button>
      )}
    </div>
  );
}
