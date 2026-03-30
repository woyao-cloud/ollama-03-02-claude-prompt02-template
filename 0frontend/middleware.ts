import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// 需要认证的路由
const protectedRoutes = [
  '/dashboard',
  '/users',
  '/roles',
  '/departments',
  '/audit-logs',
  '/settings',
];

// 认证相关路由（已登录用户访问这些路由时重定向到仪表板）
const authRoutes = ['/login', '/register'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // 获取 Token
  const accessToken = request.cookies.get('access_token')?.value;
  const hasToken = !!accessToken;

  // 检查是否是受保护的路由
  const isProtectedRoute = protectedRoutes.some(route =>
    pathname === route || pathname.startsWith(`${route}/`)
  );

  // 检查是否是认证路由
  const isAuthRoute = authRoutes.some(route =>
    pathname === route
  );

  // 受保护的路由：需要认证
  if (isProtectedRoute) {
    if (!hasToken) {
      // 未认证，重定向到登录页
      const loginUrl = new URL('/login', request.url);
      loginUrl.searchParams.set('redirect', pathname);
      return NextResponse.redirect(loginUrl);
    }
  }

  // 认证路由：已登录用户重定向到仪表板
  if (isAuthRoute && hasToken) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  return NextResponse.next();
}

// 配置 matcher，指定哪些路由需要运行 middleware
export const config = {
  matcher: [
    /*
     * 匹配所有路由除了：
     * - _next/static (静态文件)
     * - _next/image (图片优化)
     * - favicon.ico (网站图标)
     * - 公共 API 路由
     */
    '/((?!_next/static|_next/image|favicon.ico|api/public).*)',
  ],
};
