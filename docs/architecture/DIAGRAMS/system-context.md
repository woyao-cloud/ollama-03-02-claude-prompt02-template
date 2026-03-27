# 系统上下文图

使用 C4 模型 Level 1: System Context Diagram

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#e1f5fe', 'primaryTextColor': '#01579b', 'primaryBorderColor': '#0288d1', 'lineColor': '#0288d1', 'secondaryColor': '#fff3e0', 'tertiaryColor': '#e8f5e9'}}}%%
C4Context
    title 系统上下文图 - 用户角色权限管理系统

    Person(endUser, "终端用户", "普通员工<br/>查看个人信息、申请权限")
    Person(manager, "部门经理", "管理部门成员<br/>审批权限申请")
    Person(admin, "系统管理员", "管理用户、角色、权限<br/>查看审计日志")
    Person(auditor, "审计员", "生成审计报告<br/>合规检查")

    System_Boundary(userManagementSystem, "用户角色权限管理系统") {
        System(userManagement, "用户管理系统", "用户管理、角色权限<br/>审计日志、安全认证")
    }

    System_Ext(emailService, "邮件服务", "SMTP<br/>发送通知邮件")
    System_Ext(smsService, "短信服务", "阿里云/腾讯云<br/>发送验证码")
    System_Ext(ssoProvider, "SSO提供商", "OAuth2.0<br/>企业微信/钉钉")
    System_Ext(hrSystem, "HR系统", "员工信息同步<br/>组织架构")

    Rel(endUser, userManagement, "查看个人信息<br/>申请权限", "HTTPS")
    Rel(manager, userManagement, "管理部门成员<br/>审批申请", "HTTPS")
    Rel(admin, userManagement, "管理用户/角色/权限<br/>配置系统", "HTTPS")
    Rel(auditor, userManagement, "查看审计日志<br/>生成报表", "HTTPS")

    Rel(userManagement, emailService, "发送邮件", "SMTP")
    Rel(userManagement, smsService, "发送验证码", "HTTP API")
    Rel(userManagement, ssoProvider, "OAuth2认证", "HTTPS")
    Rel(userManagement, hrSystem, "同步用户数据", "REST API")

    UpdateElementStyle(endUser, $fontColor="#01579b", $bgColor="#e1f5fe", $borderColor="#0288d1")
    UpdateElementStyle(manager, $fontColor="#01579b", $bgColor="#e1f5fe", $borderColor="#0288d1")
    UpdateElementStyle(admin, $fontColor="#01579b", $bgColor="#e1f5fe", $borderColor="#0288d1")
    UpdateElementStyle(auditor, $fontColor="#01579b", $bgColor="#e1f5fe", $borderColor="#0288d1")
    UpdateElementStyle(userManagement, $fontColor="#e65100", $bgColor="#fff3e0", $borderColor="#ef6c00")
    UpdateElementStyle(emailService, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(smsService, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(ssoProvider, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
    UpdateElementStyle(hrSystem, $fontColor="#2e7d32", $bgColor="#e8f5e9", $borderColor="#43a047")
```

## 说明

### 用户角色

| 角色 | 描述 | 主要职责 |
|------|------|----------|
| 终端用户 | 普通员工 | 查看/修改个人信息，查看权限，申请权限 |
| 部门经理 | 团队负责人 | 查看部门成员，审批权限申请 |
| 系统管理员 | IT运维人员 | 用户CRUD，角色权限配置，系统配置 |
| 审计员 | 合规人员 | 查看审计日志，生成报表，合规检查 |

### 外部系统

| 系统 | 类型 | 集成方式 | 用途 |
|------|------|----------|------|
| 邮件服务 | SMTP | SMTP协议 | 发送激活邮件、通知邮件 |
| 短信服务 | HTTP API | REST API | 发送2FA验证码 |
| SSO提供商 | OAuth2.0 | HTTPS | 企业微信/钉钉单点登录 |
| HR系统 | REST API | HTTPS | 员工信息同步 |

### 技术约束

- 所有外部通信使用 HTTPS/TLS 1.3
- SSO集成遵循 OAuth2.0 标准
- 邮件服务支持 SMTP 协议
- HR系统集成使用 REST API + JWT

---

## 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0 | 2026-03-24 | 系统架构师 | 初始版本 |
