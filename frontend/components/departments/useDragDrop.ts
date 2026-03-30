'use client';

import { useState, useCallback } from 'react';
import type { Department } from '@/types';

export interface DragDropResult {
  draggedDept: Department | null;
  targetDept: Department | null;
  moveType: 'before' | 'after' | 'into';
}

export function useDragDrop(
  departments: Department[],
  onMove: (draggedId: string, targetId: string | null, moveType: 'before' | 'after' | 'into') => Promise<void>
) {
  const [draggedId, setDraggedId] = useState<string | null>(null);
  const [dropTarget, setDropTarget] = useState<{ id: string; type: 'before' | 'after' | 'into' } | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  const handleDragStart = useCallback((e: React.DragEvent, deptId: string) => {
    setDraggedId(deptId);
    setIsDragging(true);
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', deptId);
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent, targetId: string) => {
    e.preventDefault();
    if (!draggedId || draggedId === targetId) return;

    const rect = (e.target as HTMLElement).getBoundingClientRect();
    const relativeY = e.clientY - rect.top;
    const threshold = rect.height / 3;

    if (relativeY < threshold) {
      setDropTarget({ id: targetId, type: 'before' });
    } else if (relativeY > rect.height - threshold) {
      setDropTarget({ id: targetId, type: 'after' });
    } else {
      setDropTarget({ id: targetId, type: 'into' });
    }
  }, [draggedId]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    const rect = (e.target as HTMLElement).getBoundingClientRect();
    const relativeY = e.clientY - rect.top;

    if (relativeY < 0 || relativeY > rect.height) {
      setDropTarget(null);
    }
  }, []);

  const handleDrop = useCallback(async (e: React.DragEvent, targetId: string) => {
    e.preventDefault();

    if (!draggedId || !dropTarget || draggedId === targetId) {
      setDraggedId(null);
      setDropTarget(null);
      setIsDragging(false);
      return;
    }

    try {
      await onMove(draggedId, dropTarget.id, dropTarget.type);
    } catch (error) {
      console.error('移动部门失败:', error);
    } finally {
      setDraggedId(null);
      setDropTarget(null);
      setIsDragging(false);
    }
  }, [draggedId, dropTarget, onMove]);

  const handleDragEnd = useCallback(() => {
    setDraggedId(null);
    setDropTarget(null);
    setIsDragging(false);
  }, []);

  const getDropIndicatorClass = useCallback((deptId: string): string => {
    if (!dropTarget || dropTarget.id !== deptId) return '';

    switch (dropTarget.type) {
      case 'before':
        return 'drop-target-before';
      case 'after':
        return 'drop-target-after';
      case 'into':
        return 'drop-target-into';
      default:
        return '';
    }
  }, [dropTarget]);

  return {
    draggedId,
    dropTarget,
    isDragging,
    handleDragStart,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    handleDragEnd,
    getDropIndicatorClass,
  };
}
