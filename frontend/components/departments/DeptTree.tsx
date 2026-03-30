'use client';

import { useState, useCallback } from 'react';
import { ChevronRight, ChevronDown, Folder, FolderOpen } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { Department } from '@/types';
import { useDragDrop } from './useDragDrop';

interface DeptTreeNodeProps {
  dept: Department;
  level: number;
  expandedIds: Set<string>;
  onToggle: (id: string) => void;
  onSelect: (dept: Department) => void;
  selectedId?: string;
  onMove?: (draggedId: string, targetId: string | null, moveType: 'before' | 'after' | 'into') => Promise<void>;
  dragDrop?: ReturnType<typeof useDragDrop>;
}

function DeptTreeNode({
  dept,
  level,
  expandedIds,
  onToggle,
  onSelect,
  selectedId,
  onMove,
  dragDrop,
}: DeptTreeNodeProps) {
  const isExpanded = expandedIds.has(dept.id);
  const isSelected = selectedId === dept.id;
  const hasChildren = dept.children && dept.children.length > 0;
  const isDragDisabled = !onMove;

  const handleDragStart = useCallback((e: React.DragEvent) => {
    if (dragDrop && onMove) {
      dragDrop.handleDragStart(e, dept.id);
    }
  }, [dragDrop, onMove, dept.id]);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    if (dragDrop && onMove) {
      dragDrop.handleDragOver(e, dept.id);
    }
  }, [dragDrop, onMove, dept.id]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    if (dragDrop && onMove) {
      dragDrop.handleDragLeave(e);
    }
  }, [dragDrop, onMove]);

  const handleDrop = useCallback((e: React.DragEvent) => {
    if (dragDrop && onMove) {
      dragDrop.handleDrop(e, dept.id);
    }
  }, [dragDrop, onMove, dept.id]);

  const handleDragEnd = useCallback(() => {
    if (dragDrop) {
      dragDrop.handleDragEnd();
    }
  }, [dragDrop]);

  const dropIndicatorClass = dragDrop ? dragDrop.getDropIndicatorClass(dept.id) : '';

  return (
    <div className="select-none">
      <div
        className={cn(
          'flex items-center gap-1 rounded-md px-2 py-1.5 cursor-pointer transition-colors',
          'hover:bg-muted/50',
          isSelected && 'bg-muted',
          dropIndicatorClass === 'drop-target-before' && 'border-t-2 border-primary',
          dropIndicatorClass === 'drop-target-after' && 'border-b-2 border-primary',
          dropIndicatorClass === 'drop-target-into' && 'bg-muted/80 ring-1 ring-primary',
          isDragDisabled ? 'cursor-not-allowed' : 'drag-handle'
        )}
        style={{ paddingLeft: `${level * 16 + 8}px` }}
        onClick={() => onSelect(dept)}
        draggable={!isDragDisabled}
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onDragEnd={handleDragEnd}
      >
        {hasChildren ? (
          <button
            onClick={(e) => {
              e.stopPropagation();
              onToggle(dept.id);
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

        {isExpanded ? (
          <FolderOpen className="w-4 h-4 text-primary" />
        ) : (
          <Folder className="w-4 h-4 text-muted-foreground" />
        )}

        <span className="flex-1 text-sm truncate">{dept.name}</span>

        {dept.code && (
          <span className="text-xs text-muted-foreground ml-2">{dept.code}</span>
        )}
      </div>

      {hasChildren && isExpanded && (
        <div>
          {dept.children!.map((child) => (
            <DeptTreeNode
              key={child.id}
              dept={child}
              level={level + 1}
              expandedIds={expandedIds}
              onToggle={onToggle}
              onSelect={onSelect}
              selectedId={selectedId}
              onMove={onMove}
              dragDrop={dragDrop}
            />
          ))}
        </div>
      )}
    </div>
  );
}

interface DeptTreeProps {
  departments: Department[];
  selectedDept?: Department | null;
  onSelect?: (dept: Department) => void;
  onMove?: (draggedId: string, targetId: string | null, moveType: 'before' | 'after' | 'into') => Promise<void>;
}

export function DeptTree({
  departments,
  selectedDept,
  onSelect,
  onMove,
}: DeptTreeProps) {
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());

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

  const handleSelect = useCallback((dept: Department) => {
    onSelect?.(dept);
  }, [onSelect]);

  const dragDrop = onMove ? useDragDrop(departments, onMove) : undefined;

  const expandAll = useCallback(() => {
    const allIds = new Set<string>();
    const collectIds = (depts: Department[]) => {
      depts.forEach((dept) => {
        allIds.add(dept.id);
        if (dept.children) {
          collectIds(dept.children);
        }
      });
    };
    collectIds(departments);
    setExpandedIds(allIds);
  }, [departments]);

  const collapseAll = useCallback(() => {
    setExpandedIds(new Set());
  }, []);

  return (
    <div className="border rounded-lg bg-card">
      <div className="flex items-center justify-between px-3 py-2 border-b">
        <span className="text-sm font-medium">部门树</span>
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
        </div>
      </div>

      <div className="max-h-[400px] overflow-auto py-2">
        {departments.length === 0 ? (
          <div className="flex items-center justify-center py-8 text-muted-foreground">
            暂无部门数据
          </div>
        ) : (
          departments.map((dept) => (
            <DeptTreeNode
              key={dept.id}
              dept={dept}
              level={0}
              expandedIds={expandedIds}
              onToggle={handleToggle}
              onSelect={handleSelect}
              selectedId={selectedDept?.id}
              onMove={onMove}
              dragDrop={dragDrop}
            />
          ))
        )}
      </div>
    </div>
  );
}
