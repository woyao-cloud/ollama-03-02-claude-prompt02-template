'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { authService } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { LayoutDashboard } from 'lucide-react';

// 注册步骤
type Step = 1 | 2 | 3;

export default function RegisterPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [currentStep, setCurrentStep] = useState<Step>(1);
  const [countdown, setCountdown] = useState(0);
  const [verificationCode, setVerificationCode] = useState('');

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    fullName: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.id]: e.target.value,
    });
  };

  // 步骤 1: 填写基本信息
  const handleStep1 = () => {
    // 验证用户名
    if (formData.username.length < 3) {
      setError('用户名长度至少为 3 位');
      return;
    }
    // 验证邮箱
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('请输入有效的邮箱地址');
      return;
    }
    // 验证密码
    if (formData.password.length < 6) {
      setError('密码长度至少为 6 位');
      return;
    }
    if (formData.password !== formData.confirmPassword) {
      setError('两次输入的密码不一致');
      return;
    }
    setError('');
    setCurrentStep(2);
  };

  // 步骤 2: 验证邮箱
  const handleSendCode = async () => {
    setIsLoading(true);
    setError('');
    try {
      await authService.sendVerificationCode(formData.email);
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err) {
      setError(err instanceof Error ? err.message : '发送验证码失败');
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyCode = async () => {
    if (!verificationCode) {
      setError('请输入验证码');
      return;
    }
    setIsLoading(true);
    setError('');
    try {
      await authService.verifyCode({ email: formData.email, code: verificationCode });
      setCurrentStep(3);
    } catch (err) {
      setError(err instanceof Error ? err.message : '验证码错误');
    } finally {
      setIsLoading(false);
    }
  };

  // 步骤 3: 完成注册
  const handleRegister = async () => {
    setIsLoading(true);
    setError('');
    try {
      await authService.register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName,
      });
      router.push('/login');
    } catch (err) {
      setError(err instanceof Error ? err.message : '注册失败');
    } finally {
      setIsLoading(false);
    }
  };

  const renderStepIndicator = () => (
    <div className="flex justify-center mb-4">
      <div className="flex items-center gap-2">
        {[1, 2, 3].map((step) => (
          <div key={step} className="flex items-center">
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                currentStep >= step
                  ? 'bg-primary text-primary-foreground'
                  : 'bg-muted text-muted-foreground'
              }`}
            >
              {step}
            </div>
            {step < 3 && (
              <div
                className={`w-12 h-0.5 mx-2 ${
                  currentStep > step ? 'bg-primary' : 'bg-muted'
                }`}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <div className="flex items-center gap-2 mb-2">
            <LayoutDashboard className="h-8 w-8 text-primary" />
            <span className="text-xl font-bold">用户管理系统</span>
          </div>
          <CardTitle className="text-2xl">注册</CardTitle>
          <CardDescription>
            创建新账号
          </CardDescription>
        </CardHeader>
        {renderStepIndicator()}
        <CardContent className="space-y-4">
          {error && (
            <div className="p-3 text-sm text-destructive bg-destructive/10 rounded-md">
              {error}
            </div>
          )}

          {/* 步骤 1: 基本信息 */}
          {currentStep === 1 && (
            <>
              <div className="space-y-2">
                <Label htmlFor="fullName">姓名</Label>
                <Input
                  id="fullName"
                  type="text"
                  placeholder="请输入姓名"
                  value={formData.fullName}
                  onChange={handleChange}
                  disabled={isLoading}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="username">用户名</Label>
                <Input
                  id="username"
                  type="text"
                  placeholder="请输入用户名"
                  value={formData.username}
                  onChange={handleChange}
                  disabled={isLoading}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">邮箱</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="请输入邮箱"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={isLoading}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">密码</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="请输入密码"
                  value={formData.password}
                  onChange={handleChange}
                  disabled={isLoading}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="confirmPassword">确认密码</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  disabled={isLoading}
                  required
                />
              </div>
            </>
          )}

          {/* 步骤 2: 邮箱验证 */}
          {currentStep === 2 && (
            <>
              <div className="text-sm text-muted-foreground mb-4">
                已向 <span className="text-primary font-medium">{formData.email}</span> 发送验证码
              </div>
              <div className="space-y-2">
                <Label htmlFor="code">验证码</Label>
                <div className="flex gap-2">
                  <Input
                    id="code"
                    type="text"
                    placeholder="请输入 6 位验证码"
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value)}
                    disabled={isLoading}
                    maxLength={6}
                    className="flex-1"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleSendCode}
                    disabled={isLoading || countdown > 0}
                  >
                    {countdown > 0 ? `${countdown}s` : '重发'}
                  </Button>
                </div>
              </div>
            </>
          )}

          {/* 步骤 3: 完成 */}
          {currentStep === 3 && (
            <div className="text-center py-4">
              <p className="text-muted-foreground mb-2">信息确认</p>
              <div className="text-sm space-y-2 text-left bg-muted p-4 rounded-md">
                <p><span className="font-medium">用户名:</span> {formData.username}</p>
                <p><span className="font-medium">邮箱:</span> {formData.email}</p>
                <p><span className="font-medium">姓名:</span> {formData.fullName}</p>
              </div>
            </div>
          )}
        </CardContent>
        <CardFooter className="flex flex-col space-y-4">
          {currentStep === 1 && (
            <Button
              type="button"
              className="w-full"
              onClick={handleStep1}
              disabled={isLoading}
            >
              下一步
            </Button>
          )}
          {currentStep === 2 && (
            <Button
              type="button"
              className="w-full"
              onClick={handleVerifyCode}
              disabled={isLoading || !verificationCode}
            >
              验证并继续
            </Button>
          )}
          {currentStep === 3 && (
            <Button
              type="button"
              className="w-full"
              onClick={handleRegister}
              disabled={isLoading}
            >
              {isLoading ? '注册中...' : '完成注册'}
            </Button>
          )}
          <p className="text-sm text-muted-foreground text-center">
            已有账号？{' '}
            <Link href="/login" className="text-primary hover:underline">
              立即登录
            </Link>
          </p>
        </CardFooter>
      </Card>
    </div>
  );
}
