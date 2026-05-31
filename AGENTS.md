# AGENTS.md

本文件是当前项目的协作与工程维护规则。后续 AI Agent、脚本或人工协作时，应优先遵守本文件。

## 一、项目边界

当前项目根目录必须保持以下目录结构：

| 目录 | 用途 | 操作权限 |
| :--- | :--- | :--- |
| `Code/` | 项目代码 | 可读写 |
| `Docs/` | 项目文档 | 可读写 |
| `Info/` | 用户敏感信息 | 禁止读取、写入、修改、删除、提交 |

禁止为了扫描、搜索、提交或打包而进入 `Info/`。所有命令都应显式排除 `Info/`。

## 二、当前项目状态

- 当前前端项目位于 `Code/art-design-pro/`。
- 当前后端项目位于 `Code/backend-template-java/`。
- 当前文档位于 `Docs/`。
- 根目录已经作为新的 Git 仓库使用，管理 `Code/`、`Docs/` 和根级配置。
- 原 `Code/art-design-pro/.git` 已移除，不再保留原开源仓库历史和远程地址。
- 远程仓库使用 SSH 地址：`git@github.com:Yskysoar/art-design-pro-template.git`。
- 项目已接入 Vite middleware 本地 Mock：`Code/art-design-pro/mock/local-mock.ts`。
- 当前后端基础联调已通过：`/api/health`、`/api/auth/login`、`/api/user/info`、`/api/v3/system/menus`。
- 当前后端已开发并编译通过：`/api/user/list`、`POST /api/user`、`PUT /api/user/{id}`、`PATCH /api/user/{id}/status`、`DELETE /api/user/{id}`、`/api/role/list`。验证真实返回前需要用户手动重启后端进程。
- 当前前端联调账号为 `admin/admin123`，仅用于模板和开发环境。
- `.env`、`.env.development`、`.env.production` 属于本地环境文件，不得提交。

## 三、代码目录规则

`Code/` 下按项目类型组织代码。当前结构为：

```text
Code/
├── art-design-pro/
│   ├── mock/
│   ├── public/
│   ├── scripts/
│   ├── src/
│   ├── vite.config.ts
│   ├── package.json
│   └── pnpm-lock.yaml
└── backend-template-java/
    ├── pom.xml
    ├── README.md
    └── src/
```

代码维护要求：

- 优先遵循现有 Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router 技术栈。
- 新增业务接口优先放入 `src/api/`，类型优先维护到 `src/types/api/api.d.ts`。
- 表格、表单、权限、请求封装优先复用现有组件和 hooks。
- 后端使用 Java 17 + Spring Boot 3.x + Maven + MyBatis-Plus。
- 后端包名当前使用 `com.template`。
- 后端当前覆盖 Tomcat 为 `10.1.55`，用于修复 IDE 扫描到的 Tomcat 传递依赖 CVE。
- 后端已注册 MyBatis-Plus 分页插件，分页列表优先使用 `selectPage` 和 `LambdaQueryWrapper`。
- 前后端认证统一使用 Bearer JWT，请求头格式为 `Authorization: Bearer <token>`。
- 后续接入 Spring 三层架构后端时，controller 层只处理数据形式和规范，真实业务处理放在 service 层。
- 若后端使用 MyBatis-Plus，优先使用框架内置能力；只有复杂查询、复杂统计或复杂函数场景才自定义 SQL。

## 四、文档目录规则

`Docs/` 文档命名规则为：`序号.内容.md`。

当前文档：

```text
Docs/
├── 0.项目文档目录.md
├── 1.项目说明文档.md
├── 2.代码审查总结.md
└── 3.后端开发设计方案.md
```

维护要求：

- `Docs/0.项目文档目录.md` 是文档索引，新增或调整文档后必须同步更新。
- `Docs/1.项目说明文档.md` 只维护项目情况、结构、业务功能、业务逻辑、后端对接、API 设计、新增接口方法和 Mock 方案。
- `Docs/2.代码审查总结.md` 只维护代码审查、安全风险、上线阻断项、修复优先级和整改建议。
- `Docs/3.后端开发设计方案.md` 维护后端技术选型、架构、权限、数据权限、接口、数据库、Mock SQL 和开发阶段规划。
- 修改接口契约、Mock 行为、后端对接方式或安全策略后，必须同步更新相关文档。

## 五、本地 Mock 规则

当前使用 Vite middleware 提供开发环境本地 Mock。

入口文件：

```text
Code/art-design-pro/mock/local-mock.ts
```

启用条件：

```env
VITE_USE_LOCAL_MOCK=true
```

维护要求：

- Mock 只用于开发和调试，不得作为生产逻辑。
- Mock 数据不得包含真实密码、真实 token、真实用户隐私或敏感业务数据。
- 新增 Mock 接口时，应同步更新 `Docs/1.项目说明文档.md`。
- 后端真实接口完成后，应逐步替换对应 Mock。

当前已覆盖：

- `POST /api/auth/login`
- `GET /api/user/info`
- `GET /api/user/list`
- `GET /api/role/list`
- `GET /api/v3/system/menus`
- `GET /api/article/types`
- `GET /api/article/detail`

## 六、Git 与提交规则

根目录是当前唯一 Git 仓库。提交前必须检查：

```powershell
git status --short --ignored
git diff --cached --name-only
```

不得提交：

- `Info/`
- `Code/art-design-pro/node_modules/`
- `Code/art-design-pro/dist/`
- `Code/art-design-pro/.env`
- `Code/art-design-pro/.env.development`
- `Code/art-design-pro/.env.production`
- `Code/art-design-pro/.vite-mock.out`
- `Code/art-design-pro/.vite-mock.err`
- `Code/art-design-pro/.vite-dev.out`
- `Code/art-design-pro/.vite-dev.err`
- `Code/art-design-pro/.frontend-dev.out`
- `Code/art-design-pro/.frontend-dev.err`
- `Code/art-design-pro/.auto-import.json`
- `Code/art-design-pro/src/types/import/auto-imports.d.ts`
- `Code/art-design-pro/src/types/import/components.d.ts`
- `Code/backend-template-java/target/`
- `Code/backend-template-java/uploads/`
- `Code/backend-template-java/logs/`
- `Code/backend-template-java/.boot-*.out`
- `Code/backend-template-java/.boot-*.err`
- `.claude/`

提交信息规则：

- 使用 Conventional Commits 类型前缀 + 中文简要说明。
- 示例：`chore: 初始化后端项目骨架`、`docs: 更新后端设计文档`、`feat: 新增用户管理接口`、`fix: 修复登录鉴权问题`。

如需推送，优先使用 SSH：

```powershell
git push -u origin master
```

含义：把当前本地 `master` 分支推送到远程仓库，并建立本地分支与远程分支的跟踪关系。

如果网络沙箱阻止连接 GitHub，需要请求提升权限后再推送。

## 七、安全与上线要求

当前项目仍处于模板整理和后端接入前阶段，上线前必须优先处理 `Docs/2.代码审查总结.md` 中的 P0 和 P1 问题。

重点要求：

- 生产环境不得包含演示账号和默认弱密码。
- 生产环境不得依赖外部 Mock。
- 所有 `v-html`、富文本、SVG 渲染点必须做可信净化。
- iframe 外链必须做协议和域名白名单，并配置 sandbox。
- token/Cookie 认证方案必须和后端统一，推荐上线前评估 HttpOnly Cookie。
- 上传接口必须校验 MIME、后缀、大小、存储路径和返回 URL。
- 生产环境不得输出敏感 console 日志。

## 八、编码与中文规则

- 新增和修改文件统一使用 UTF-8。
- 中文文档、注释、日志不得出现乱码。
- 命令说明和操作解释使用中文。
- 高风险操作必须先说明影响并获得用户确认。

高风险操作包括：

- 删除文件或目录。
- 修改权限。
- 覆盖配置。
- 强制推送。
- 重写 Git 历史。
- 清理 Git 元数据。

示例：

```powershell
Remove-Item -LiteralPath 'Code\art-design-pro\.git' -Recurse -Force
```

含义：删除 `Code/art-design-pro/.git` 目录及其所有内容，只移除该项目内的 Git 历史和远程关联，不删除代码文件。

## 九、验证规则

前端代码改动后，至少执行：

```powershell
node_modules\.bin\vue-tsc.cmd --noEmit
```

含义：运行 TypeScript 类型检查，不生成构建产物。

涉及构建、依赖、路由、Mock 或生产配置时，建议补充：

```powershell
node_modules\.bin\vite.cmd build
```

含义：执行 Vite 生产构建，验证项目是否能正常打包。

后端代码改动后，至少执行：

```powershell
D:\Coding\Maven\apache-maven-3.9.15\bin\mvn.cmd compile
```

含义：使用本机 Maven 编译 `Code/backend-template-java`，验证 Java 源码和依赖是否正确。

前后端联调时建议验证：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/health -Method Get
```

含义：验证后端健康检查是否正常。

```powershell
Invoke-RestMethod -Uri http://localhost:5173/api/auth/login -Method Post -ContentType 'application/json' -Body '{"userName":"admin","password":"admin123"}'
```

含义：通过 Vite 代理验证前端是否能访问真实后端登录接口。
