'use client';

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { AlertTriangle } from 'lucide-react';
import type { Department } from '@/types';

interface DeleteConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  department: Department | null;
  onConfirm: () => Promise<void>;
}

export function DeleteConfirmDialog({
  open,
  onOpenChange,
  department,
  onConfirm,
}: DeleteConfirmDialogProps) {
  if (!department) return null;

  const hasChildren = department.children && department.children.length > 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[450px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-destructive">
            <AlertTriangle className="w-5 h-5" />
            确认删除
          </DialogTitle>
          <DialogDescription>
            {hasChildren
              ? `无法删除部门 "${department.name}"，因为它包含 ${department.children!.length} 个子部门。请先删除或移动所有子部门。`
              : `确定要删除部门 "${department.name}" 吗？此操作不可恢复。`}
          </DialogDescription>
        </DialogHeader>

        {!hasChildren && (
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              取消
            </Button>
            <Button
              type="button"
              variant="destructive"
              onClick={async () => {
                await onConfirm();
                onOpenChange(false);
              }}
            >
              删除
            </Button>
          </DialogFooter>
        )}

        {hasChildren && (
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              关闭
            </Button>
          </DialogFooter>
        )}
      </DialogContent>
    </Dialog>
  );
}
