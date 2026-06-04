# Mock SQL

该目录保存开发和模板环境使用的数据库 Mock SQL。

维护规则：

- 每开发一个业务模块，同步新增或更新对应 SQL，可按业务域拆分文件。
- Mock SQL 必须符合业务逻辑，方便前端本地 Mock 数据逐步迁移到数据库。
- 初始化管理员账号为 `admin`，初始密码为 `admin123`。
- 生产环境不得误导入 Mock SQL。
- 表结构 SQL 位于 `src/main/resources/db/schema/`。

当前文件：

- `mock_001_system.sql`：系统配置、组织、用户、角色、权限、菜单和 RBAC 关联关系。
- `mock_002_article.sql`：文章分类、文件资源、文章、附件和评论数据。
- `mock_003_sensitive_word.sql`：敏感词库数据，用于全站文本入口拦截验证。
- `mock_004_social.sql`：关注、粉丝、拉黑、会话、消息、未读数和私信额度用例。
- `mock_005_governance.sql`：文件资源台账、通知中心和举报审核的多状态、多类型调试数据。
