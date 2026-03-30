# 用户管理系统 - 前端

基于 Next.js 16+ 的全栈用户管理系统前端。

## 技术栈

- **框架**: Next.js 16+ (App Router)
- **语言**: TypeScript 5+
- **UI 组件**: shadcn/ui + Tailwind CSS 4
- **状态管理**: Zustand
- **HTTP 客户端**: Axios
- **表单**: React Hook Form + Zod

## 项目结构

```
frontend/
├── app/                      # 页面路由
│   ├── (auth)/              # 认证路由组 (登录/注册)
│   │   ├── login/
│   │   └── register/
│   ├── (dashboard)/         # 仪表板路由组
│   │   ├── dashboard/
│   │   └── users/
│   ├── layout.tsx           # 根布局
│   └── globals.css          # 全局样式
├── components/
│   ├── ui/                  # shadcn UI 组件
│   └── layout/              # 布局组件 (Sidebar, Header, DashboardLayout)
├── lib/
│   ├── api/                 # API 客户端
│   │   ├── client.ts        # Axios 实例和拦截器
│   │   └── services/        # API 服务 (auth, user, role, department)
│   └── utils.ts             # 工具函数
├── stores/                  # Zustand 状态管理
│   └── authStore.ts         # 认证状态
└── types/                   # TypeScript 类型定义
    └── index.ts
```

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 配置环境变量

复制 `.env.example` 到 `.env.local`:

```bash
cp .env.example .env.local
```

编辑 `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

### 3. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

## 可用命令

```bash
npm run dev      # 启动开发服务器
npm run build    # 构建生产版本
npm run start    # 启动生产服务器
npm run lint     # 运行 ESLint
```

## API 客户端

API 客户端已配置 JWT Token 自动拦截器:

- 请求时自动添加 `Authorization: Bearer <token>`
- 401 错误时自动刷新 Token
- 刷新失败时自动跳转到登录页

## 状态管理

使用 Zustand 管理全局状态:

- `useAuthStore` - 认证状态 (用户信息、登录状态)

## 默认登录账号

- 用户名：`admin`
- 密码：`admin123`
