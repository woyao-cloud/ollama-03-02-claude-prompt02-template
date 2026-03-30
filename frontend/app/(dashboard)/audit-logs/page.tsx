'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function AuditLogsPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  if (authLoading || !isAuthenticated) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">审计日志</h1>
        <p className="text-muted-foreground">查看系统操作日志</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>操作日志</CardTitle>
          <CardDescription>记录所有用户操作</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">审计日志功能开发中...</p>
        </CardContent>
      </Card>
    </div>
  );
}
