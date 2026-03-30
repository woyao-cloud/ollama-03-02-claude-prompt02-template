'use client';

import { useRouter } from 'next/navigation';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type { User } from '@/types';
import { MoreHorizontal, Pencil, Trash2, Eye } from 'lucide-react';

interface UserTableProps {
  users: User[];
  onEdit: (user: User) => void;
  onDelete: (user: User) => void;
  onView: (user: User) => void;
}

export function UserTable({ users, onEdit, onDelete, onView }: UserTableProps) {
  const router = useRouter();

  const getStatusBadge = (status: User['status']) => {
    switch (status) {
      case 'active':
        return <Badge variant="success">活跃</Badge>;
      case 'inactive':
        return <Badge variant="secondary">未激活</Badge>;
      case 'locked':
        return <Badge variant="destructive">已锁定</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const handleClickRow = (userId: string) => {
    router.push(`/users/${userId}`);
  };

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>用户名</TableHead>
          <TableHead>邮箱</TableHead>
          <TableHead>姓名</TableHead>
          <TableHead>部门</TableHead>
          <TableHead>角色</TableHead>
          <TableHead>状态</TableHead>
          <TableHead>创建时间</TableHead>
          <TableHead className="w-[70px]">操作</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {users.map((user) => (
          <TableRow key={user.id} className="cursor-pointer hover:bg-muted/50">
            <TableCell
              className="font-medium"
              onClick={() => handleClickRow(user.id)}
            >
              {user.username}
            </TableCell>
            <TableCell onClick={() => handleClickRow(user.id)}>
              {user.email}
            </TableCell>
            <TableCell onClick={() => handleClickRow(user.id)}>
              {user.fullName || '-'}
            </TableCell>
            <TableCell onClick={() => handleClickRow(user.id)}>
              {user.departmentId || '-'}
            </TableCell>
            <TableCell onClick={() => handleClickRow(user.id)}>
              {user.roleIds?.length ? `${user.roleIds.length} 个角色` : '-'}
            </TableCell>
            <TableCell onClick={() => handleClickRow(user.id)}>
              {getStatusBadge(user.status)}
            </TableCell>
            <TableCell onClick={() => handleClickRow(user.id)}>
              {new Date(user.createdAt).toLocaleDateString('zh-CN')}
            </TableCell>
            <TableCell onClick={(e) => e.stopPropagation()}>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon">
                    <MoreHorizontal className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuLabel>操作</DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => onView(user)}>
                    <Eye className="mr-2 h-4 w-4" />
                    详情
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => onEdit(user)}>
                    <Pencil className="mr-2 h-4 w-4" />
                    编辑
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => onDelete(user)}
                    className="text-destructive focus:text-destructive"
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    删除
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
