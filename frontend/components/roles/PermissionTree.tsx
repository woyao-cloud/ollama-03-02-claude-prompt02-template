'use client';

import { useState, useCallback } from 'react';
import { ChevronRight, ChevronDown, Shield, ShieldCheck } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Checkbox } from '@/components/ui/checkbox';
import type { Permission } from '@/types';

export interface PermissionNode extends Permission {
  children?: PermissionNode[];
}

interface PermissionTreeNodeProps {
  node: PermissionNode;
  level: number;
  expandedIds: Set<string>;
  onToggle: (id: string) => void;
  selectedIds: Set<string>;
  onCheck: (id: string, checked: boolean) => void;
  indeterminateIds: Set<string>;
}

function PermissionTreeNode({
  node,
  level,
  expandedIds,
  onToggle,
  selectedIds,
  onCheck,
  indeterminateIds,
}: PermissionTreeNodeProps) {
  const isExpanded = expandedIds.has(node.id);
  const hasChildren = node.children && node.children.length > 0;
  const isSelected = selectedIds.has(node.id);
  const isIndeterminate = indeterminateIds.has(node.id);

  const handleCheck = (checked: boolean) => {
    onCheck(node.id, checked);
  };

  return (
    <div className="select-none">
      <div
        className={cn(
          'flex items-center gap-2 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer',
          isSelected && 'bg-muted'
        )}
        style={{ paddingLeft: `${level * 16 + 8}px` }}
      >
        <Checkbox
          checked={isSelected}
          onCheckedChange={handleCheck}
          id={`perm-${node.id}`}
        />

        {hasChildren ? (
          <button
            onClick={(e) => {
              e.stopPropagation();
              onToggle(node.id);
            }}
            className="flex items-center justify-center w-4 h-4 hover:bg-muted rounded"
          >
            {isExpanded ? (
              <ChevronDown className="w-3 h-3" />
            ) : (
              <ChevronRight className="w-3 h-3" />
            )}
          </button>
        ) : (
          <span className="w-4" />
        )}

        {isSelected ? (
          <ShieldCheck className="w-4 h-4 text-primary" />
        ) : (
          <Shield className="w-4 h-4 text-muted-foreground" />
        )}

        <label
          htmlFor={`perm-${node.id}`}
          className="flex-1 text-sm cursor-pointer truncate"
        >
          {node.name}
        </label>

        {node.code && (
          <span className="text-xs text-muted-foreground ml-2">{node.code}</span>
        )}
      </div>

      {hasChildren && isExpanded && (
        <div>
          {node.children!.map((child) => (
            <PermissionTreeNode
              key={child.id}
              node={child}
              level={level + 1}
              expandedIds={expandedIds}
              onToggle={onToggle}
              selectedIds={selectedIds}
              onCheck={onCheck}
              indeterminateIds={indeterminateIds}
            />
          ))}
        </div>
      )}
    </div>
  );
}

interface PermissionTreeProps {
  permissions: PermissionNode[];
  selectedPermissionIds: string[];
  onChange: (selectedIds: string[]) => void;
}

export function PermissionTree({
  permissions,
  selectedPermissionIds,
  onChange,
}: PermissionTreeProps) {
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());

  const selectedIds = new Set(selectedPermissionIds);

  // 计算不确定状态的节点（部分子节点被选中）
  const indeterminateIds = new Set<string>();
  const calculateIndeterminate = (nodes: PermissionNode[]) => {
    nodes.forEach((node) => {
      if (node.children && node.children.length > 0) {
        const childIds = getAllChildIds(node.children);
        const allSelected = childIds.every((id) => selectedIds.has(id));
        const someSelected = childIds.some((id) => selectedIds.has(id));

        if (someSelected && !allSelected) {
          indeterminateIds.add(node.id);
        }
        calculateIndeterminate(node.children!);
      }
    });
  };
  calculateIndeterminate(permissions);

  const getAllChildIds = (nodes: PermissionNode[]): string[] => {
    let ids: string[] = [];
    nodes.forEach((node) => {
      ids.push(node.id);
      if (node.children) {
        ids = [...ids, ...getAllChildIds(node.children!)];
      }
    });
    return ids;
  };

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

  const handleCheck = useCallback((id: string, checked: boolean) => {
    // 递归获取所有子节点 ID
    const getAllDescendantIds = (nodes: PermissionNode[], targetId: string): string[] => {
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
    };

    const descendantIds = getAllDescendantIds(permissions, id);

    if (checked) {
      // 选中：添加当前节点和所有子节点
      const newIds = new Set(selectedIds);
      descendantIds.forEach((permId) => newIds.add(permId));
      onChange(Array.from(newIds));
    } else {
      // 取消选中：移除当前节点和所有子节点
      const newIds = Array.from(selectedIds).filter(
        (permId) => !descendantIds.includes(permId)
      );
      onChange(newIds);
    }
  }, [permissions, selectedIds, onChange]);

  const expandAll = useCallback(() => {
    const allIds = new Set<string>();
    const collectIds = (nodes: PermissionNode[]) => {
      nodes.forEach((node) => {
        allIds.add(node.id);
        if (node.children) {
          collectIds(node.children!);
        }
      });
    };
    collectIds(permissions);
    setExpandedIds(allIds);
  }, [permissions]);

  const collapseAll = useCallback(() => {
    setExpandedIds(new Set());
  }, []);

  const selectAll = useCallback(() => {
    const allIds = getAllChildIds(permissions);
    onChange(allIds);
  }, [permissions, onChange]);

  const clearAll = useCallback(() => {
    onChange([]);
  }, [onChange]);

  return (
    <div className="border rounded-lg bg-card">
      <div className="flex items-center justify-between px-3 py-2 border-b">
        <span className="text-sm font-medium">权限列表</span>
        <div className="flex gap-1">
          <button
            onClick={expandAll}
            className="text-xs text-muted-foreground hover:text-foreground px-1"
          >
            展开全部
          </button>
          <span className="text-muted-foreground">|</span>
          <button
            onClick={collapseAll}
            className="text-xs text-muted-foreground hover:text-foreground px-1"
          >
            折叠全部
          </button>
          <span className="text-muted-foreground">|</span>
          <button
            onClick={selectAll}
            className="text-xs text-muted-foreground hover:text-foreground px-1"
          >
            全选
          </button>
          <span className="text-muted-foreground">|</span>
          <button
            onClick={clearAll}
            className="text-xs text-muted-foreground hover:text-foreground px-1"
          >
            清空
          </button>
        </div>
      </div>

      <div className="max-h-[300px] overflow-auto py-2">
        {permissions.length === 0 ? (
          <div className="flex items-center justify-center py-8 text-muted-foreground">
            暂无权限数据
          </div>
        ) : (
          permissions.map((node) => (
            <PermissionTreeNode
              key={node.id}
              node={node}
              level={0}
              expandedIds={expandedIds}
              onToggle={handleToggle}
              selectedIds={selectedIds}
              onCheck={handleCheck}
              indeterminateIds={indeterminateIds}
            />
          ))
        )}
      </div>

      <div className="px-3 py-2 border-t bg-muted/30">
        <div className="text-xs text-muted-foreground">
          已选择 {selectedIds.size} 个权限
        </div>
      </div>
    </div>
  );
}
