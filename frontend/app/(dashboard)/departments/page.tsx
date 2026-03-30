'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { departmentService } from '@/lib/api/services/department';
import { useToast } from '@/components/ui/use-toast';
import type { Department } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { DeptTree, DeptForm, DeptMoveDialog, DeleteConfirmDialog } from '@/components/departments';
import { Plus, FolderTree, Pencil, Move, Trash2, Building2 } from 'lucide-react';

export default function DepartmentsPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const { toast } = useToast();

  const [departments, setDepartments] = useState<Department[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedDept, setSelectedDept] = useState<Department | null>(null);

  const [formOpen, setFormOpen] = useState(false);
  const [moveDialogOpen, setMoveDialogOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  useEffect(() => {
    if (isAuthenticated) {
      loadDepartments();
    }
  }, [isAuthenticated]);

  const loadDepartments = async () => {
    setIsLoading(true);
    try {
      const data = await departmentService.getDepartmentTree();
      setDepartments(data);
    } catch (error) {
      toast({
        title: '加载失败',
        description: error instanceof Error ? error.message : '获取部门树失败',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreate = () => {
    setSelectedDept(null);
    setFormOpen(true);
  };

  const handleEdit = () => {
    if (selectedDept) {
      setFormOpen(true);
    }
  };

  const handleMove = () => {
    if (selectedDept) {
      setMoveDialogOpen(true);
    }
  };

  const handleDelete = () => {
    if (selectedDept) {
      setDeleteOpen(true);
    }
  };

  const handleFormSubmit = async (data: {
    name: string;
    code: string;
    parentId?: string;
    level: number;
    sortOrder: number;
  }) => {
    try {
      if (selectedDept) {
        await departmentService.updateDepartment(selectedDept.id, data);
        toast({
          title: '更新成功',
          description: '部门信息已更新',
          variant: 'success',
        });
      } else {
        await departmentService.createDepartment(data);
        toast({
          title: '创建成功',
          description: '新部门已创建',
          variant: 'success',
        });
      }
      loadDepartments();
    } catch (error) {
      toast({
        title: selectedDept ? '更新失败' : '创建失败',
        description: error instanceof Error ? error.message : '操作失败',
        variant: 'destructive',
      });
    }
  };

  const handleMoveSubmit = async (targetParentId: string | null) => {
    if (!selectedDept) return;

    try {
      await departmentService.updateDepartment(selectedDept.id, {
        parentId: targetParentId || undefined,
      });
      toast({
        title: '移动成功',
        description: '部门已移动到新位置',
        variant: 'success',
      });
      loadDepartments();
    } catch (error) {
      toast({
        title: '移动失败',
        description: error instanceof Error ? error.message : '移动部门失败',
        variant: 'destructive',
      });
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedDept) return;

    try {
      await departmentService.deleteDepartment(selectedDept.id);
      toast({
        title: '删除成功',
        description: '部门已被删除',
        variant: 'success',
      });
      setSelectedDept(null);
      loadDepartments();
    } catch (error) {
      toast({
        title: '删除失败',
        description: error instanceof Error ? error.message : '删除部门失败',
        variant: 'destructive',
      });
    }
  };

  const handleDragDropMove = async (
    draggedId: string,
    targetId: string | null,
    moveType: 'before' | 'after' | 'into'
  ) => {
    try {
      let newParentId: string | null = null;

      if (moveType === 'into') {
        newParentId = targetId;
      } else if (targetId) {
        const findParent = (depts: Department[], targetId: string): string | null => {
          for (const dept of depts) {
            if (dept.id === targetId) {
              return dept.parentId || null;
            }
            if (dept.children) {
              const result = findParent(dept.children, targetId);
              if (result !== undefined) return result;
            }
          }
          return null;
        };
        newParentId = findParent(departments, targetId);
      }

      await departmentService.updateDepartment(draggedId, {
        parentId: newParentId || undefined,
      });

      toast({
        title: '移动成功',
        description: '部门层级已调整',
        variant: 'success',
      });
      loadDepartments();
    } catch (error) {
      toast({
        title: '移动失败',
        description: error instanceof Error ? error.message : '调整部门层级失败',
        variant: 'destructive',
      });
    }
  };

  const hasSelectedDept = !!selectedDept;

  if (authLoading || !isAuthenticated) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">部门管理</h1>
          <p className="text-muted-foreground">管理企业组织架构的部门树形结构</p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="mr-2 h-4 w-4" />
          新增部门
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader className="py-4">
            <div className="flex items-center gap-2">
              <FolderTree className="h-5 w-5 text-muted-foreground" />
              <CardTitle className="text-base">部门树</CardTitle>
            </div>
            <CardDescription>拖拽调整部门层级关系</CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <p className="text-muted-foreground">加载中...</p>
              </div>
            ) : (
              <DeptTree
                departments={departments}
                selectedDept={selectedDept}
                onSelect={setSelectedDept}
                onMove={handleDragDropMove}
              />
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="py-4">
            <div className="flex items-center gap-2">
              <Building2 className="h-5 w-5 text-muted-foreground" />
              <CardTitle className="text-base">部门详情</CardTitle>
            </div>
            <CardDescription>查看和编辑选中部门的信息</CardDescription>
          </CardHeader>
          <CardContent>
            {selectedDept ? (
              <div className="space-y-4">
                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">部门名称</div>
                  <div className="text-lg font-medium">{selectedDept.name}</div>
                </div>

                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">部门代码</div>
                  <div className="text-lg font-medium">{selectedDept.code}</div>
                </div>

                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">层级</div>
                  <div className="text-lg font-medium">第 {selectedDept.level} 级</div>
                </div>

                <div className="grid gap-2">
                  <div className="text-sm text-muted-foreground">排序</div>
                  <div className="text-lg font-medium">{selectedDept.sortOrder}</div>
                </div>

                {selectedDept.parent && (
                  <div className="grid gap-2">
                    <div className="text-sm text-muted-foreground">上级部门</div>
                    <div className="text-lg font-medium">{selectedDept.parent.name}</div>
                  </div>
                )}

                <div className="flex gap-2 pt-4 border-t">
                  <Button variant="outline" onClick={handleEdit} disabled={!hasSelectedDept}>
                    <Pencil className="mr-2 h-4 w-4" />
                    编辑
                  </Button>
                  <Button variant="outline" onClick={handleMove} disabled={!hasSelectedDept}>
                    <Move className="mr-2 h-4 w-4" />
                    移动
                  </Button>
                  <Button
                    variant="outline"
                    onClick={handleDelete}
                    disabled={!hasSelectedDept}
                    className="text-destructive hover:text-destructive"
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    删除
                  </Button>
                </div>
              </div>
            ) : (
              <div className="flex items-center justify-center py-8 text-muted-foreground">
                请从左侧选择一个部门
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <DeptForm
        open={formOpen}
        onOpenChange={setFormOpen}
        department={selectedDept}
        departments={departments}
        onSubmit={handleFormSubmit}
      />

      <DeptMoveDialog
        open={moveDialogOpen}
        onOpenChange={setMoveDialogOpen}
        department={selectedDept}
        departments={departments}
        onMove={handleMoveSubmit}
      />

      <DeleteConfirmDialog
        open={deleteOpen}
        onOpenChange={setDeleteOpen}
        department={selectedDept}
        onConfirm={handleDeleteConfirm}
      />
    </div>
  );
}
