'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Users, Shield, Building2, FileText } from 'lucide-react';

const stats = [
  {
    title: '总用户数',
    value: '0',
    description: '系统注册用户总数',
    icon: Users,
  },
  {
    title: '角色数量',
    value: '0',
    description: '已定义的角色数量',
    icon: Shield,
  },
  {
    title: '部门数量',
    value: '0',
    description: '组织架构部门数',
    icon: Building2,
  },
  {
    title: '审计日志',
    value: '0',
    description: '今日操作日志数',
    icon: FileText,
  },
];

export default function DashboardPage() {
  const { isAuthenticated, isLoading, user, checkAuth } = useAuthStore();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  // middleware 会处理未认证重定向，这里仅处理加载状态
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <p>加载中...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">仪表板</h1>
        <p className="text-muted-foreground">
          欢迎使用用户管理系统
          {user && <span>，{user.fullName || user.username}</span>}
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <Card key={stat.title}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  {stat.title}
                </CardTitle>
                <Icon className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stat.value}</div>
                <p className="text-xs text-muted-foreground">
                  {stat.description}
                </p>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Quick Actions */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>快速开始</CardTitle>
            <CardDescription>常用功能入口</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <ul className="list-disc list-inside text-sm text-muted-foreground space-y-1">
              <li>用户管理 - 管理系统用户</li>
              <li>角色权限 - 配置角色和权限</li>
              <li>部门管理 - 维护组织架构</li>
              <li>审计日志 - 查看操作记录</li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
