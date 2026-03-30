'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function SettingsPage() {
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
        <h1 className="text-3xl font-bold">系统配置</h1>
        <p className="text-muted-foreground">管理系统设置和配置</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>系统设置</CardTitle>
          <CardDescription>配置系统参数</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">系统配置功能开发中...</p>
        </CardContent>
      </Card>
    </div>
  );
}
