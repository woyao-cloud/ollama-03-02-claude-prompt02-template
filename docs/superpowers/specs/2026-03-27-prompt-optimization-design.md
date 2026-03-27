# 提示词优化设计方案

**文档版本**: 1.2
**最后更新**: 2026-03-27 (第二次修订)
**编写人**: Claude Code
**依据**: 用户需求分析、现有提示词结构评估、实际项目状态验证

---

## 1. 概述

### 1.1 项目背景
当前全栈用户管理系统项目存在提示词分散管理问题：
- 项目级提示词（CLAUDE.md, AGENTS.md）位于项目根目录，未纳入统一管理
- 架构和需求文档已部分迁移至 `prompts/` 目录，但配置文件中仍引用旧路径
- Agent 提示词位于全局目录 `~/.claude/agents/`，未纳入项目版本控制
- 缺乏统一的标准和完整整合

### 1.2 优化目标
1. **提高可读性和清晰度** - 使提示词更容易理解，减少歧义
2. **提高可维护性和组织性** - 改善文件结构，便于更新和扩展
3. **提高效率和性能** - 减少冗余，优化 token 使用
4. **统一风格和格式** - 确保所有提示词遵循一致的格式

### 1.3 设计原则
- **统一性**: 所有提示词在同一目录下，便于版本控制和团队协作
- **模块化**: 按功能划分目录，清晰明确
- **可扩展**: 支持新 Agent 和提示词类型的添加
- **向后兼容**: 更新文件引用，保持现有 Agent 工作流

---

## 2. 目录结构设计

### 2.1 当前结构
```
prompts/                    # 现有提示词目录（已部分实现）
├── architecture/          # 架构文档（已存在）
├── requirements/          # 需求文档（已存在）
├── planning/             # 计划文档（已存在）
├── product/              # 产品文档（已存在）
├── deployment/           # 部署文档（已存在）
├── AGENT_GUIDE.md        # Agent协作指南（已存在）
├── DATABASE_SCHEMA.md    # 数据库设计（已存在）
AGENTS.md                   # Agent 配置（项目级，在项目根目录）
CLAUDE.md                   # Claude 配置（项目级，在项目根目录）
~/.claude/agents/          # 全局 Agent 提示词（Unix/Mac）
                           # Windows: C:\Users\laido\.claude\agents\
```

**注意**: 项目已建立prompts目录结构，但Agent提示词仍在全局目录，项目级配置仍在根目录。本次优化需完成统一整合。

### 2.2 新结构设计
```
prompts/                    # 统一提示词目录
├── agents/                # Agent 提示词（从全局目录迁移）
│   ├── business-analyst.md
│   ├── architect.md
│   ├── tdd-guide.md
│   ├── code-reviewer.md
│   ├── security-reviewer.md
│   ├── e2e-runner.md
│   ├── gsd-planner.md
│   └── gsd-executor.md
├── architecture/          # 架构文档（已存在）
├── requirements/          # 需求文档（已存在）
├── project/              # 项目级配置提示词
│   ├── CLAUDE.md         # 从项目根目录移动
│   ├── AGENTS.md         # 从项目根目录移动
│   └── README.md         # 项目提示词说明
├── templates/            # 可复用模板
│   ├── agent-template.md
│   └── project-template.md
└── README.md             # 目录说明和导航
```

### 2.3 目录说明
| 目录 | 内容 | 来源 | 用途 |
|------|------|------|------|
| `agents/` | Agent 提示词文件 | `~/.claude/agents/` (Windows: `C:\Users\laido\.claude\agents\`) | Agent 工作指令和配置 |
| `architecture/` | 系统架构文档 | `prompts/architecture/` (已存在) | 架构决策和设计 |
| `requirements/` | 需求文档 | `prompts/requirements/` (已存在) | 功能和性能需求 |
| `project/` | 项目级配置 | 项目根目录 | 项目特定配置 |
| `templates/` | 可复用模板 | 新建 | 标准化模板 |
| 根目录 | 导航文档 | 新建 | 目录说明和快速访问 |

---

## 3. 文件迁移方案

### 3.1 迁移步骤
**阶段1：准备和备份**（预计30分钟）
1. 创建备份目录
2. 分析所有引用路径
3. 制定引用更新映射表

**阶段2：目录完善**（预计30分钟）
1. 完善 `prompts/` 目录结构（添加缺失的子目录）
2. 验证现有文档位置和完整性
3. 扫描和记录文件引用

**阶段3：Agent 提示词迁移**（预计1小时）
1. 从全局目录复制 Agent 文件
2. 统一格式和结构
3. 更新内部引用路径

**阶段4：项目级提示词优化**（预计45分钟）
1. 移动 CLAUDE.md 和 AGENTS.md
2. 优化内容格式
3. 更新根目录引用

**阶段5：模板和导航**（预计30分钟）
1. 创建标准模板文件
2. 编写导航文档
3. 建立目录索引

**阶段6：验证和测试**（预计30分钟）
1. 完整性检查
2. 功能测试
3. 性能验证

### 3.2 引用更新策略
需要更新的文件路径引用（文件已在prompts目录，但配置文件中仍引用docs路径）：

| 原始路径 | 新路径 | 影响文件 |
|----------|--------|----------|
| `docs/requirements/FUNCTIONAL_REQUIREMENTS.md` | `prompts/requirements/FUNCTIONAL_REQUIREMENTS.md` | CLAUDE.md, architect.md 等 |
| `docs/architecture/SYSTEM_ARCHITECTURE.md` | `prompts/architecture/SYSTEM_ARCHITECTURE.md` | CLAUDE.md, architect.md 等 |
| `docs/prompts/AGENT_COLLABORATION.md` | `prompts/AGENT_COLLABORATION.md` | AGENTS.md |
| `~/.claude/agents/business-analyst.md` (Windows: `C:\Users\laido\.claude\agents\business-analyst.md`) | `prompts/agents/business-analyst.md` | AGENTS.md, 其他引用 |

### 3.3 批量处理脚本
```bash
#!/bin/bash
# 提示词迁移脚本（适配已存在的prompts目录）
set -e  # 出错即停止

echo "=== 阶段1：准备和备份 ==="
mkdir -p prompts-backup
cp -r prompts/ prompts-backup/prompts/
cp AGENTS.md prompts-backup/
cp CLAUDE.md prompts-backup/
cp -r ~/.claude/agents/ prompts-backup/global-agents/ 2>/dev/null || echo "全局Agent目录未找到，跳过备份"

echo "=== 阶段2：目录完善 ==="
mkdir -p prompts/agents prompts/project prompts/templates
# architecture/和requirements/目录已存在，无需创建

echo "=== 阶段3：Agent提示词迁移 ==="
if [ -d "$HOME/.claude/agents" ]; then
    cp -r "$HOME/.claude/agents/"*.md prompts/agents/
    echo "已从全局目录复制Agent文件"
elif [ -d "/c/Users/laido/.claude/agents" ]; then
    cp -r "/c/Users/laido/.claude/agents/"*.md prompts/agents/
    echo "已从Windows全局目录复制Agent文件"
else
    echo "警告：未找到全局Agent目录，Agent文件需手动复制"
fi

echo "=== 阶段4：项目文件迁移 ==="
mv AGENTS.md prompts/project/
mv CLAUDE.md prompts/project/

echo "=== 阶段5：引用更新（手动步骤）==="
echo "请手动更新以下文件中的路径引用："
echo "1. prompts/project/CLAUDE.md - 更新docs/引用为prompts/"
echo "2. prompts/project/AGENTS.md - 更新~/.claude/agents/引用为prompts/agents/"
echo "3. 检查其他可能引用旧路径的文件"

echo "=== 迁移准备完成 ==="
echo "注意：请务必执行阶段5的引用更新，否则功能可能异常"
```

### 3.4 平台兼容性考虑
当前项目运行在 Windows 环境下，迁移脚本需要确保跨平台兼容性：

#### 关键兼容性问题
1. **路径分隔符**：使用正斜杠 `/` 作为跨平台路径分隔符
2. **全局目录路径**：Windows 上的全局 Agent 目录为 `C:\Users\laido\.claude\agents\`
3. **命令差异**：Windows 命令与 Unix 命令的差异

#### Windows 兼容脚本（备选方案）
```batch
@echo off
REM Windows 批处理脚本（适配已存在的prompts目录）

echo === 阶段1：准备和备份 ===
mkdir prompts-backup
xcopy /E /I prompts prompts-backup\prompts
copy AGENTS.md prompts-backup\
copy CLAUDE.md prompts-backup\
xcopy /E /I "%USERPROFILE%\.claude\agents" prompts-backup\global-agents 2>nul || echo 全局Agent目录未找到，跳过备份

echo === 阶段2：目录完善 ===
mkdir prompts\agents 2>nul
mkdir prompts\project 2>nul
mkdir prompts\templates 2>nul
REM architecture/和requirements/目录已存在，无需创建

echo === 阶段3：Agent提示词迁移 ===
if exist "%USERPROFILE%\.claude\agents\*.md" (
    xcopy "%USERPROFILE%\.claude\agents\*.md" prompts\agents\ /Y
    echo 已从全局目录复制Agent文件
) else (
    echo 警告：未找到全局Agent目录，Agent文件需手动复制
)

echo === 阶段4：项目文件迁移 ===
move AGENTS.md prompts\project\
move CLAUDE.md prompts\project\

echo === 阶段5：引用更新（手动步骤）===
echo 请手动更新以下文件中的路径引用：
echo 1. prompts\project\CLAUDE.md - 更新docs/引用为prompts/
echo 2. prompts\project\AGENTS.md - 更新~/.claude/agents/引用为prompts/agents/
echo 3. 检查其他可能引用旧路径的文件

echo === 迁移准备完成 ===
echo 注意：请务必执行阶段5的引用更新，否则功能可能异常
```

#### 跨平台建议
- 对于关键迁移操作，建议使用 Python 或 Node.js 脚本确保跨平台兼容性
- 测试环境：Windows 11 + Git Bash（提供 Unix 兼容命令）
- 验证所有路径引用在迁移后有效

---

## 4. 内容优化标准

### 4.1 格式统一规范
#### 文件前言（Frontmatter）
```yaml
---
name: [agent-name]          # 英文小写，连字符分隔
description: [一句话描述]   # 中英文混合，不超过80字符
tools: [工具列表]          # 逗号分隔，按功能分组
color: [颜色]             # 统一使用语义化颜色
version: 1.0              # 版本控制
---
```

**颜色规范**：
- `blue`: 业务/分析类 Agent
- `purple`: 架构/设计类 Agent
- `orange`: 开发/测试类 Agent
- `cyan`: 审查/质量类 Agent
- `green`: 执行/部署类 Agent
- `red`: 安全/关键类 Agent

#### 标题结构（统一层级）
```
# [Agent名称] Agent

## 角色定义
## 核心职责
## 工作流程
## 输出标准
## 质量检查清单
## Agent 协作
## 启动命令
```

### 4.2 内容结构优化
#### 角色定义（精简清晰）
**优化前**：
```
资深业务分析师，拥有 10 年以上企业级系统需求分析经验。
**核心职责**: ...
```

**优化后**：
```
资深业务分析师（10+年经验），专注于企业级系统需求分析。

**专长领域**：
- 需求发现与澄清
- FRD/NFRD 编写
- 业务流程建模
```

#### 工作流程（表格化）
```
| 阶段 | 工作内容 | 关键动作 | 输出物 | 质量门禁 |
|------|---------|---------|--------|----------|
| 1. 需求发现 | 理解业务目标 | 阅读文档、访谈 | 利益相关者清单 | 清单完整 |
```

#### 质量检查清单（标准化）
```
### 完整性检查
- [ ] 所有用户角色都有对应功能需求
- [ ] 每个需求都有明确验收标准
- [ ] 异常场景100%覆盖

### 一致性检查
- [ ] 术语使用完全一致
- [ ] 需求之间无冲突
- [ ] 与架构文档对齐
```

### 4.3 语言优化标准
#### 指令语言（从描述性到指令性）
**优化前**：
```
TDD 引导者，负责 RED-GREEN-REFACTOR 循环。
```

**优化后**：
```
你是一个 TDD 专家，严格遵循 RED→GREEN→REFACTOR 循环：
1. **RED**: 编写失败测试，验证需求理解
2. **GREEN**: 最小实现通过测试
3. **REFACTOR**: 优化代码结构，保持测试通过
```

#### 示例规范化
```java
// 模式：Given-When-Then + 描述性测试名称
@Test
void createUser_shouldEncodePassword() {
    // Given - 准备测试数据
    when(passwordEncoder.encode("plain-password")).thenReturn("encoded-hash");

    // When - 执行被测方法
    userService.createUser(registrationRequest);

    // Then - 验证结果
    verify(passwordEncoder).encode("plain-password");
    assertThat(savedUser.getPassword()).isEqualTo("encoded-hash");
}
```

### 4.4 性能优化（Token 使用）
| 文件类型 | 当前平均行数 | 优化后预估 | Token 减少 |
|----------|-------------|-----------|-----------|
| Agent 提示词 | 120-150行 | 80-100行 | 25-30% |
| 项目配置 | 80-100行 | 60-70行 | 20-25% |
| 文档文件 | 可变 | 精简15% | 15-20% |

---

## 5. 实施步骤和时间安排

### 5.1 时间估算
**总预计时间**: 7小时（分段执行）

| 阶段 | 任务 | 预计时间 | 关键产出 |
|------|------|---------|----------|
| 阶段1 | 准备和架构 | 1小时 | 备份完成、引用映射表 |
| 阶段2 | 目录重构 | 1.5小时 | prompts/ 目录结构就绪 |
| 阶段3 | Agent 迁移 | 2小时 | Agent 提示词迁移完成 |
| 阶段4 | 项目优化 | 1.5小时 | 项目级提示词优化完成 |
| 阶段5 | 模板导航 | 1小时 | 模板系统和导航文档 |
| 阶段6 | 验证测试 | 1小时 | 验证报告、测试通过 |

### 5.2 关键里程碑
1. **M1**: 目录结构就绪（2小时）
2. **M2**: Agent 迁移完成（4小时）
3. **M3**: 内容优化完成（6小时）
4. **M4**: 验证通过（7小时）

### 5.3 建议执行计划
- **第1天**: 阶段1-2（准备+目录重构）
- **第2天**: 阶段3-4（Agent迁移+项目优化）
- **第3天**: 阶段5-6（模板+验证）

---

## 6. 风险缓解策略

### 6.1 技术风险
| 风险 | 可能性 | 影响 | 缓解措施 |
|------|-------|------|----------|
| 文件丢失 | 低 | 高 | 阶段化备份，每个阶段单独备份 |
| 引用中断 | 中 | 中 | 先扫描后替换，保留原文件备份 |
| 格式错误 | 中 | 低 | 使用模板验证，批量格式化 |
| Agent 失效 | 低 | 高 | 保留全局目录，项目内优先测试 |

### 6.2 回滚方案
#### 完全回滚
```bash
# 如果迁移失败，执行完全回滚
rm -rf prompts/
mv prompts-backup/docs/ docs/
mv prompts-backup/AGENTS.md ./
mv prompts-backup/CLAUDE.md ./
rm -rf prompts-backup/
```

**注意**: 回滚脚本为 Unix/Linux 环境设计。Windows 用户可能需要：
1. 使用 Git Bash 或 WSL 执行
2. 或将命令转换为 Windows 等效命令

#### 部分回滚
```bash
# 如果某个阶段失败，回滚该阶段，保留已完成的工作
```

### 6.3 质量门禁
#### 预检查清单
- [ ] 文件前言格式正确
- [ ] 标题结构符合规范
- [ ] 语言指令清晰无歧义
- [ ] 所有示例可执行
- [ ] 引用路径有效

#### 后验证测试
1. **语法检查**: Markdown 格式正确
2. **链接验证**: 所有引用文件存在
3. **Agent 测试**: 至少启动一个 Agent 验证功能
4. **Token 计算**: 确认优化效果

---

## 7. 验收标准

### 7.1 必须完成（MUST）
- [ ] `prompts/` 目录结构符合设计
- [ ] 所有 Agent 提示词迁移完成
- [ ] 项目级提示词位置正确
- [ ] 所有文件引用更新正确
- [ ] 关键 Agent 可正常启动

### 7.2 应该完成（SHOULD）
- [ ] Token 使用减少15%以上
- [ ] 可读性评分提高（人工评估）
- [ ] 模板系统可用
- [ ] 导航文档完整

### 7.3 最好完成（COULD）
- [ ] 自动化验证脚本
- [ ] 性能基准测试
- [ ] 团队协作文档
- [ ] CI/CD 集成方案

---

## 8. 附录

### 8.1 模板示例
#### agent-template.md
```markdown
---
name: [agent-name]
description: [一句话描述]
tools: [Read, Write, Bash, Glob, Grep]
color: [blue|purple|orange|cyan|green|red]
version: 1.0
---

# [Agent名称] Agent

## 角色定义
[角色描述，强调专业领域和经验]

## 核心职责
1. **职责1** - 详细说明
2. **职责2** - 详细说明
3. **职责3** - 详细说明

## 工作流程
| 阶段 | 工作内容 | 关键动作 | 输出物 | 质量门禁 |
|------|---------|---------|--------|----------|
| 1. 准备 | [内容] | [动作] | [产出] | [检查点] |

## 输出标准
### [文档名称]
- [要求1]
- [要求2]

## 质量检查清单
### 完整性检查
- [ ] [检查项]

### 一致性检查
- [ ] [检查项]

## Agent 协作
| 上游 Agent | 输入 | 下游 Agent | 输出 |
|-----------|------|-----------|------|
| [上游] | [输入文件] | [下游] | [输出文件] |

## 启动命令
```bash
claude agent [agent-name]
```
```

#### project-template.md
```markdown
---
name: [project-name]
description: [项目描述]
version: 1.0
---

# [项目名称] - 项目配置

## 项目概述
[项目背景、目标和范围]

## 技术栈
### 后端
- [技术1]: [版本] - [用途]
- [技术2]: [版本] - [用途]

### 前端
- [技术1]: [版本] - [用途]

## 开发原则
1. **原则1**: 说明
2. **原则2**: 说明

## 项目结构
```
[目录结构树形图]
```

## 关键文档
| 文档 | 路径 | 用途 |
|------|------|------|
| [文档名] | [路径] | [用途] |

## 质量指标
- 后端测试覆盖率: ≥ 85%
- 前端测试覆盖率: ≥ 80%
- API 响应时间 P95: < 200ms
```

### 8.2 术语表
| 术语 | 定义 |
|------|------|
| Frontmatter | Markdown 文件开头的 YAML 元数据 |
| Token | AI 模型处理的基本文本单位 |
| RBAC | Role-Based Access Control，基于角色的访问控制 |
| TDD | Test-Driven Development，测试驱动开发 |

### 8.3 变更记录
| 版本 | 日期 | 变更内容 | 负责人 |
|------|------|----------|--------|
| 1.2 | 2026-03-27 | 修复结构描述不准确问题，更新迁移脚本适配现有prompts目录 | Claude Code |
| 1.1 | 2026-03-27 | 添加平台兼容性说明，修复路径描述 | Claude Code |
| 1.0 | 2026-03-27 | 初始版本 | Claude Code |
```

---

**文档状态**: ✅ 已完成
**下一步**: 设计文档审查 → 实施计划编写 → 迁移执行