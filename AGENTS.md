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
- `Code/art-design-pro-main/` 和 `Code/art-design-pro-main.zip` 是原始前端项目参考快照，只用于当前模板出现巨大混乱时对照修复；不得作为当前项目源码开发、修改、提交或打包对象。
- 当前文档位于 `Docs/`。
- 根目录已经作为新的 Git 仓库使用，管理 `Code/`、`Docs/` 和根级配置。
- 原 `Code/art-design-pro/.git` 已移除，不再保留原开源仓库历史和远程地址。
- 远程仓库使用 SSH 地址：`git@github.com:Yskysoar/art-design-pro-template.git`。
- 项目已接入 Vite middleware 本地 Mock：`Code/art-design-pro/mock/local-mock.ts`。
- 当前后端基础联调已通过：`/api/health`、`/api/auth/login`、`/api/user/info`、`/api/v3/system/menus`。
- 当前后端已开发并编译通过：`/api/user/list`、`POST /api/user`、`PUT /api/user/{id}`、`PATCH /api/user/{id}/status`、`DELETE /api/user/{id}`、`GET /api/user/{id}/orgs`、`PUT /api/user/{id}/orgs`、`/api/role/list`、`POST /api/role`、`PUT /api/role/{id}`、`DELETE /api/role/{id}`、`GET /api/role/{id}/permissions`、`PUT /api/role/{id}/permissions`、`GET /api/role/{id}/data-scope`、`PUT /api/role/{id}/data-scope`、`GET /api/v3/system/menus/manage`、`POST /api/v3/system/menus`、`PUT /api/v3/system/menus/{id}`、`DELETE /api/v3/system/menus/{id}`、`GET /api/org/tree`、`POST /api/org`、`PUT /api/org/{id}`、`DELETE /api/org/{id}`、`GET /api/config/list`、`POST /api/config`、`PUT /api/config/{id}`、`DELETE /api/config/{id}`。其中配置项、组织、角色数据权限和用户组织关系已通过真实接口回归。
- 当前前端系统管理页已接入真实后端接口：用户、角色、菜单、配置项和组织管理 CRUD。
- 当前后端系统管理主要接口已接入权限码校验；超级管理员 `R_SUPER` 可跳过权限码校验，普通角色必须拥有对应权限码。
- 当前配置项已增加内置配置值校验、禁止修改配置键、禁止删除系统内置配置等基础保护。
- 当前后端文章与上传模块已完成第一轮开发和真实 HTTP 回归：文章分类、列表、详情、新增、编辑、删除、状态变更、本地上传、wangEditor 上传和 `/api/common/files/**` 受控文件访问。
- 当前前端文章列表、发布、详情页已接入真实后端接口，并保留接近原模板的卡片列表、发布设置和 Markdown 阅读风格。
- 当前文章与上传模块后端测试已通过：`Tests run: 75, Failures: 0, Errors: 0, Skipped: 0`。
- 当前文章评论模块已完成第一轮开发、二轮安全加固和敏感词管理：文章详情底部评论区、后端评论表、Service、Controller、权限码、Mock SQL、敏感词 CRUD、评论发布频率限制、重复内容限制和敏感词拦截已接入；评论读取跟随文章可访问性，游客只能读取已发布且可见文章评论；隐藏和删除评论后端返回脱敏内容；评论数使用数据库原子更新；已删除评论不可恢复；公开评论响应不返回内部 `userId`，改用 `mine` 标志。
- 当前文章评论模块剩余优先事项：将独立评论演示页改造成评论管理页、补充更多真实浏览器联调、按业务需要扩展审核队列。
- 当前验证码和认证流程已完成开发：图形验证码获取、登录验证码校验、注册、重置密码、修改密码、用户资料展示、头像和顶部用户菜单真实数据渲染已接入；后端业务异常支持携带 `data` 返回，用于前端精确展示登录和验证码错误。
- 当前前端动态路由已完成修复：登录后跳转、刷新动态路由加载、菜单路径转换、路由权限校验和路由初始化失败恢复逻辑已修复；已通过内置浏览器验证登录跳转和冷刷新。
- 当前顶部栏用户菜单已改为真实头像渲染，保留个人中心、修改密码、锁屏和退出登录；使用文档和 GitHub 不再放在右上角用户菜单。修改密码调用 `PUT /api/user/profile/password`，校验旧密码，成功后强制重新登录。
- 所有和当前登录用户对接的前端展示点必须优先使用 `userStore.getUserInfo` / 后端 `/api/user/info` 的真实数据渲染，例如头像、用户名、邮箱、角色、个人中心、锁屏弹窗和右上角用户菜单；只有后端未返回字段时才使用本地默认头像或空状态文案兜底。
- 当前用户管理表格已优化：手机号单行展示，组织列通过 `/api/org/tree` 将 `orgIds` 映射为组织名称；用户状态筛选和表格状态统一使用“启用/禁用”语义；`PATCH /api/user/{id}/status` 用于临时启用或禁用账号，`DELETE /api/user/{id}` 用于逻辑删除用户，不再用“注销”表达临时停用。
- 最近本地提交：`252a619 fix(frontend): 调整系统菜单与表格样式`、`741045d feat(backend): 增加全局敏感词校验`、`d09eab3 chore(db): 重写开发 Mock 数据`。提交前验证已通过：前端 `vue-tsc --noEmit`，后端 `mvn test` 结果 `Tests run: 100, Failures: 0, Errors: 0, Skipped: 0`。
- `Docs/5.开发审查问题记录.md` 已调整为模块内“待修复/部分修复/持续关注在前，已修复在后”的问题台账结构；新增问题和修复记录应继续按该结构维护。
- 当前前端联调账号为 `admin/admin123`，仅用于模板和开发环境。
- `.env`、`.env.development`、`.env.production` 属于本地环境文件，不得提交。
- `Code/backend-template-java/src/main/resources/application-local.yml` 属于本机后端敏感配置文件，不得提交；如需本地固定数据库密码，可复制 `application-local.example.yml` 后填写真实值。
- 用户已授予 Agent 完全访问权限用于项目开发、前后端启动、联调测试和代码审查；后续项目开发由 Agent 自行启动前后端并完成本地联调。该授权不包含高风险操作，高风险操作仍必须先说明影响并获得用户确认。
- 数据库密码等敏感配置只通过本机环境变量传入，例如 `DB_PASSWORD`，不得写入 Git 跟踪文件、Docs 或提交信息。

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
├── art-design-pro-main/        # 原始前端参考快照，禁止提交和日常开发修改
├── art-design-pro-main.zip     # 原始前端参考压缩包，禁止提交
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
- 后端系统管理接口权限通过 `PermissionService` 校验，新增管理接口时必须同步设计权限码、Mock SQL 权限数据和 Controller 入口校验。
- 每完成一个后端业务模块，都要同步维护 `src/main/resources/db/mock/` 下真实数据库 Mock SQL，确保菜单、权限、角色、用户等关联数据自洽。
- 文章与上传模块权限码为 `article:publish:add`、`article:publish:edit`、`article:upload`、`article:comment:manage`；前端按钮权限与后端接口权限应使用同一组权限码。
- 评论接口维护时应保持公开读取安全边界：`GET /api/article/comment/list` 可匿名访问，但必须跟随文章可访问性；公开响应不得返回内部用户 ID；隐藏或删除内容必须在后端脱敏；评论计数更新应保持数据库原子增减。
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
├── 3.后端开发设计方案.md
├── 4.后端测试设计.md
├── 5.开发审查问题记录.md
├── 6.验证码和认证流程.md
└── 临时.下一步工作清单.md
```

维护要求：

- `Docs/0.项目文档目录.md` 是文档索引，新增或调整文档后必须同步更新。
- `Docs/1.项目说明文档.md` 只维护项目情况、结构、业务功能、业务逻辑、后端对接、API 设计、新增接口方法和 Mock 方案。
- `Docs/2.代码审查总结.md` 只维护代码审查、安全风险、上线阻断项、修复优先级和整改建议。
- `Docs/3.后端开发设计方案.md` 维护后端技术选型、架构、权限、数据权限、接口、数据库、Mock SQL 和开发阶段规划。
- `Docs/4.后端测试设计.md` 维护后端测试分层、测试用例、测试命令和回归验证方案。
- `Docs/5.开发审查问题记录.md` 维护开发过程中的模块化问题台账、问题原因、修复状态、修复方式和验证建议。
- `Docs/6.验证码和认证流程.md` 维护验证码、登录、注册、重置密码、修改密码、用户资料和认证错误返回约定。
- `Docs/临时.下一步工作清单.md` 维护阶段性待办，完成或迁移后应删除或并入正式文档。
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
- `Code/art-design-pro-main/`
- `Code/art-design-pro-main.zip`
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
- `Code/backend-template-java/src/main/resources/application-local.yml`
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
- 当前允许 Agent 自行启动前端、启动后端、运行测试、执行只读或常规开发联调命令。高风险操作必须先说明影响并获得用户确认。

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

后端测试改动后，执行：

```powershell
D:\Coding\Maven\apache-maven-3.9.15\bin\mvn.cmd test
```

含义：使用本机 Maven 执行后端测试，验证单元测试和测试依赖是否正确。

数据库测试规则：

- 必要时 Agent 可以操作 SQL 文件并连接数据库执行测试，但只能操作本项目的 `backend-template` 数据库。
- 禁止对其他数据库执行写入、删除、重建、初始化或测试操作。
- 可以在 `backend-template` 数据库中执行接口联调、写接口回归和 SQL 验证。
- 数据库测试完成后必须重新初始化 `backend-template` 数据库，执行 `Code/backend-template-java/src/main/resources/db/` 下的结构 SQL 和 Mock SQL，恢复项目标准开发数据。
- 数据库密码等敏感信息仍只能来自环境变量或本机未追踪配置，不得写入 Git 跟踪文件、Docs 或提交信息。
- Windows 下导入包含中文的 UTF-8 SQL 时，不要使用 PowerShell `Get-Content | mysql` 管道，容易把中文写成 `????`；应使用 `cmd /c` 原始文件重定向，并为 mysql 指定 `--default-character-set=utf8mb4`。

如需启动后端，Agent 可自行执行：

```powershell
$env:DB_PASSWORD="本机数据库密码"
cd "D:\Coding\Project\资源——Art Design Pro开发模板\Code\backend-template-java"
D:\Coding\Maven\apache-maven-3.9.15\bin\mvn.cmd spring-boot:run
```

含义：只在当前 PowerShell 窗口设置数据库密码环境变量，然后启动后端服务。不要把真实密码写入项目配置或文档。

如果使用本机未追踪的 `application-local.yml` 保存数据库密码，可直接在 `Code/backend-template-java` 下执行：

```powershell
D:\Coding\Maven\apache-maven-3.9.15\bin\mvn.cmd spring-boot:run
```

含义：使用 Maven 启动后端服务，数据库密码从本机未追踪配置或环境变量读取。

如需启动前端，Agent 可在 `Code/art-design-pro` 下自行执行：

```powershell
pnpm dev
```

含义：启动 Vite 前端开发服务器，用于本地页面联调和浏览器验证。

前后端联调时建议验证：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/health -Method Get
```

含义：验证后端健康检查是否正常。

```powershell
Invoke-RestMethod -Uri http://localhost:5173/api/auth/login -Method Post -ContentType 'application/json' -Body '{"userName":"admin","password":"admin123"}'
```

含义：通过 Vite 代理验证前端是否能访问真实后端登录接口。
