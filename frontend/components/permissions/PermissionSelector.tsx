'use client';

import { useState, useCallback, useEffect } from 'react';
import { Search, Check, ChevronsUpDown } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { Badge } from '@/components/ui/badge';
import type { PermissionNode } from '@/lib/api/services/permission';

interface PermissionSelectorProps {
  permissions: PermissionNode[];
  selectedIds: string[];
  onChange: (selectedIds: string[]) => void;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 权限选择器组件
 *
 * 功能：
 * - 树形权限选择
 * - 搜索过滤
 * - 全选/清空
 * - 级联选择（选中父节点自动选中子节点）
 */
export function PermissionSelector({
  permissions,
  selectedIds,
  onChange,
  disabled = false,
  placeholder = '选择权限',
}: PermissionSelectorProps) {
  const [open, setOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set(['user', 'role', 'department']));

  const selectedSet = new Set(selectedIds);

  // 过滤权限列表
  const filterPermissions = useCallback((nodes: PermissionNode[], query: string): PermissionNode[] => {
    if (!query.trim()) return nodes;

    const lowerQuery = query.toLowerCase();
    const result: PermissionNode[] = [];

    const filter = (nodeList: PermissionNode[]): PermissionNode[] => {
      return nodeList.reduce<PermissionNode[]>((acc, node) => {
        const matchesName = node.name.toLowerCase().includes(lowerQuery);
        const matchesCode = node.code.toLowerCase().includes(lowerQuery);
        const matches = matchesName || matchesCode;

        if (node.children) {
          const filteredChildren = filter(node.children);
          if (matches || filteredChildren.length > 0) {
            acc.push({
              ...node,
              children: filteredChildren.length > 0 ? filteredChildren : undefined,
            });
          }
        } else if (matches) {
          acc.push(node);
        }
        return acc;
      }, []);
    };

    return filter(nodes);
  }, []);

  const filteredPermissions = filterPermissions(permissions, searchQuery);

  // 获取所有子节点 ID
  const getAllDescendantIds = useCallback((nodes: PermissionNode[], targetId: string): string[] => {
    for (const node of nodes) {
      if (node.id === targetId) {
        return [node.id, ...getAllChildIds(node.children || [])];
      }
      if (node.children) {
        const result = getAllDescendantIds(node.children, targetId);
        if (result.length > 0) return result;
      }
    }
    return [];
  }, []);

  const getAllChildIds = useCallback((nodes: PermissionNode[]): string[] => {
    let ids: string[] = [];
    nodes.forEach((node) => {
      ids.push(node.id);
      if (node.children) {
        ids = [...ids, ...getAllChildIds(node.children!)];
      }
    });
    return ids;
  }, []);

  // 获取所有权限 ID
  const getAllPermissionIds = useCallback((): string[] => {
    const ids: string[] = [];
    const collect = (nodes: PermissionNode[]) => {
      nodes.forEach((node) => {
        ids.push(node.id);
        if (node.children) collect(node.children!);
      });
    };
    collect(permissions);
    return ids;
  }, [permissions]);

  // 处理选择
  const handleCheck = useCallback((id: string, checked: boolean) => {
    const descendantIds = getAllDescendantIds(permissions, id);

    if (checked) {
      const newIds = new Set(selectedSet);
      descendantIds.forEach((permId) => newIds.add(permId));
      onChange(Array.from(newIds));
    } else {
      const newIds = Array.from(selectedSet).filter(
        (permId) => !descendantIds.includes(permId)
      );
      onChange(newIds);
    }
  }, [permissions, selectedSet, onChange, getAllDescendantIds]);

  // 切换展开/折叠
  const handleToggle = useCallback((id: string) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }, []);

  // 全选/清空
  const handleSelectAll = useCallback(() => {
    onChange(getAllPermissionIds());
  }, [onChange, getAllPermissionIds]);

  const handleClearAll = useCallback(() => {
    onChange([]);
  }, [onChange]);

  // 展开/折叠全部
  const expandAll = useCallback(() => {
    const allIds = getAllPermissionIds();
    setExpandedIds(new Set(allIds));
  }, [getAllPermissionIds]);

  const collapseAll = useCallback(() => {
    setExpandedIds(new Set());
  }, []);

  // 计算选中数量
  const selectedCount = selectedIds.length;
  const totalCount = getAllPermissionIds().length;

  // 渲染权限树节点
  const renderNode = (node: PermissionNode, level: number) => {
    const hasChildren = node.children && node.children.length > 0;
    const isExpanded = expandedIds.has(node.id);
    const isSelected = selectedSet.has(node.id);

    return (
      <div key={node.id} className="select-none">
        <div
          className={cn(
            'flex items-center gap-2 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer',
            isSelected && 'bg-muted'
          )}
          style={{ paddingLeft: `${level * 16 + 8}px` }}
          onClick={(e) => e.stopPropagation()}
        >
          <Checkbox
            checked={isSelected}
            onCheckedChange={(checked) => handleCheck(node.id, checked as boolean)}
            id={`selector-perm-${node.id}`}
          />

          {hasChildren ? (
            <button
              onClick={() => handleToggle(node.id)}
              className="flex items-center justify-center w-4 h-4 hover:bg-muted rounded"
            >
              {isExpanded ? (
                <span className="text-xs">▼</span>
              ) : (
                <span className="text-xs">▶</span>
              )}
            </button>
          ) : (
            <span className="w-4" />
          )}

          <label
            htmlFor={`selector-perm-${node.id}`}
            className="flex-1 text-sm cursor-pointer truncate"
          >
            {node.name}
          </label>

          {node.code && (
            <span className="text-xs text-muted-foreground ml-1">{node.code}</span>
          )}
        </div>

        {hasChildren && isExpanded && (
          <div>
            {node.children!.map((child) => renderNode(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  return (
    <Popover open={open && !disabled} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          className="w-full justify-between"
          disabled={disabled}
        >
          <div className="flex items-center gap-1 flex-wrap">
            {selectedCount === 0 ? (
              <span className="text-muted-foreground">{placeholder}</span>
            ) : (
              <>
                <span className="text-sm">{selectedCount} 个权限</span>
                {selectedCount > 0 && (
                  <Badge variant="secondary" className="text-xs">
                    {selectedCount}/{totalCount}
                  </Badge>
                )}
              </>
            )}
          </div>
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[320px] p-0" align="start">
        <div className="flex flex-col max-h-[400px]">
          {/* 搜索栏 */}
          <div className="p-2 border-b">
            <div className="relative">
              <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索权限..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8"
                autoFocus
              />
            </div>
          </div>

          {/* 工具栏 */}
          <div className="flex items-center justify-between px-2 py-1.5 border-b bg-muted/30">
            <span className="text-xs text-muted-foreground">
              已选 {selectedCount} 个
            </span>
            <div className="flex gap-1">
              <button
                onClick={expandAll}
                className="text-xs text-muted-foreground hover:text-foreground px-1"
              >
                展开
              </button>
              <span className="text-muted-foreground">|</span>
              <button
                onClick={collapseAll}
                className="text-xs text-muted-foreground hover:text-foreground px-1"
              >
                折叠
              </button>
              <span className="text-muted-foreground">|</span>
              <button
                onClick={handleSelectAll}
                className="text-xs text-muted-foreground hover:text-foreground px-1"
              >
                全选
              </button>
              <span className="text-muted-foreground">|</span>
              <button
                onClick={handleClearAll}
                className="text-xs text-muted-foreground hover:text-foreground px-1"
              >
                清空
              </button>
            </div>
          </div>

          {/* 权限树 */}
          <div className="flex-1 overflow-auto py-2">
            {filteredPermissions.length === 0 ? (
              <div className="flex items-center justify-center py-8 text-muted-foreground text-sm">
                暂无权限数据
              </div>
            ) : (
              filteredPermissions.map((node) => renderNode(node, 0))
            )}
          </div>

          {/* 底部确认按钮 */}
          <div className="flex justify-end gap-2 p-2 border-t">
            <Button
              size="sm"
              variant="outline"
              onClick={() => setOpen(false)}
            >
              完成
            </Button>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}
