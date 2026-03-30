'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import {
  LayoutDashboard,
  Users,
  Shield,
  Building2,
  Settings,
  FileText,
} from 'lucide-react';

const menuItems = [
  {
    title: '仪表板',
    href: '/dashboard',
    icon: LayoutDashboard,
  },
  {
    title: '用户管理',
    href: '/users',
    icon: Users,
  },
  {
    title: '角色管理',
    href: '/roles',
    icon: Shield,
  },
  {
    title: '权限配置',
    href: '/permissions',
    icon: Shield,
  },
  {
    title: '部门管理',
    href: '/departments',
    icon: Building2,
  },
  {
    title: '审计日志',
    href: '/audit-logs',
    icon: FileText,
  },
  {
    title: '系统配置',
    href: '/settings',
    icon: Settings,
  },
];

interface SidebarProps {
  className?: string;
}

export function Sidebar({ className }: SidebarProps) {
  const pathname = usePathname();

  return (
    <aside
      className={cn(
        'flex h-full w-64 flex-col border-r bg-card',
        className
      )}
    >
      {/* Logo */}
      <div className="flex h-16 items-center border-b px-6">
        <Link href="/dashboard" className="flex items-center gap-2 font-semibold">
          <LayoutDashboard className="h-6 w-6" />
          <span>用户管理系统</span>
        </Link>
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-1 overflow-y-auto p-4">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href || pathname?.startsWith(`${item.href}/`);

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
              )}
            >
              <Icon className="h-5 w-5" />
              <span>{item.title}</span>
            </Link>
          );
        })}
      </nav>

      {/* Footer */}
      <div className="border-t p-4 text-xs text-muted-foreground">
        <p>© 2026 用户管理系统</p>
        <p className="mt-1">版本 1.0.0</p>
      </div>
    </aside>
  );
}
