'use client';

import * as React from 'react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

export type DataScope = 'ALL' | 'DEPT' | 'SELF' | 'CUSTOM';

export interface DataScopeOption {
  value: DataScope;
  label: string;
  description: string;
}

const dataScopeOptions: DataScopeOption[] = [
  {
    value: 'ALL',
    label: '全部数据',
    description: '可访问系统中的所有数据',
  },
  {
    value: 'DEPT',
    label: '部门数据',
    description: '仅可访问本部门及下级部门的数据',
  },
  {
    value: 'SELF',
    label: '个人数据',
    description: '仅可访问自己创建的数据',
  },
  {
    value: 'CUSTOM',
    label: '自定义数据',
    description: '根据自定义规则确定数据范围',
  },
];

interface DataScopeSelectProps {
  value?: DataScope;
  onChange?: (value: DataScope) => void;
  disabled?: boolean;
  className?: string;
}

export function DataScopeSelect({
  value,
  onChange,
  disabled,
  className,
}: DataScopeSelectProps) {
  const handleValueChange = (newValue: string) => {
    onChange?.(newValue as DataScope);
  };

  const currentValue = value || 'SELF';
  const currentOption = dataScopeOptions.find((opt) => opt.value === currentValue);

  return (
    <div className={cn('space-y-2', className)}>
      <Select value={currentValue} onValueChange={handleValueChange} disabled={disabled}>
        <SelectTrigger className="w-full">
          <SelectValue placeholder="请选择数据范围" />
        </SelectTrigger>
        <SelectContent>
          {dataScopeOptions.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              <div className="flex items-center gap-2">
                <span>{option.label}</span>
                <Badge variant="secondary" className="text-xs">
                  {option.value}
                </Badge>
              </div>
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {currentOption && (
        <p className="text-xs text-muted-foreground">
          {currentOption.description}
        </p>
      )}
    </div>
  );
}

// 数据范围标签组件
interface DataScopeBadgeProps {
  value: DataScope;
}

export function DataScopeBadge({ value }: DataScopeBadgeProps) {
  const option = dataScopeOptions.find((opt) => opt.value === value);

  const variantMap: Record<DataScope, 'default' | 'secondary' | 'destructive' | 'outline'> = {
    ALL: 'destructive',
    DEPT: 'default',
    SELF: 'secondary',
    CUSTOM: 'outline',
  };

  return (
    <Badge variant={variantMap[value] || 'default'}>
      {option?.label || value}
    </Badge>
  );
}
