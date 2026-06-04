-- 系统管理 Mock 数据。
-- 说明：所有演示账号初始密码均为 admin123，password_hash 保存 BCrypt 哈希。

INSERT INTO sys_config (id, config_key, config_value, description, editable, create_by, update_by)
VALUES
  (1, 'user_org_relation_mode', 'ONE_TO_MANY', '用户与组织关系模式：允许一个用户归属多个组织', 0, 'system', 'system'),
  (2, 'role_level_enabled', 'true', '启用角色层级管理，低层级角色不能管理高层级角色', 0, 'system', 'system'),
  (3, 'anonymous_portal_access', 'true', '允许匿名用户访问已发布且可见的文章内容', 1, 'system', 'system'),
  (4, 'guest_admin_access', 'false', '禁止游客角色访问后台管理端', 1, 'system', 'system'),
  (5, 'article_comment_hide_enabled', 'true', '开启文章评论隐藏与恢复功能', 1, 'system', 'system'),
  (6, 'article_default_visible', 'true', '新建文章默认对前台可见', 1, 'system', 'system'),
  (7, 'upload_max_size_mb', '10', '本地上传文件大小上限，单位 MB', 1, 'system', 'system'),
  (8, 'system_notice', '欢迎使用 Art Design Pro 开发模板', '后台首页展示的系统提示文案', 1, 'system', 'system');

INSERT INTO sys_org (id, parent_id, ancestors, org_name, org_code, org_type, sort, enabled, create_by, update_by)
VALUES
  (1, 0, '0', '模板组织', 'ORG_TEMPLATE', 'GROUP', 1, 1, 'system', 'system'),
  (2, 1, '0,1', '内容中心', 'ORG_CONTENT', 'DEPT', 10, 1, 'system', 'system'),
  (3, 2, '0,1,2', '编辑组', 'ORG_EDITOR', 'GROUP', 11, 1, 'system', 'system'),
  (4, 2, '0,1,2', '审核组', 'ORG_REVIEW', 'GROUP', 12, 1, 'system', 'system'),
  (5, 1, '0,1', '运营中心', 'ORG_OPERATION', 'DEPT', 20, 1, 'system', 'system'),
  (6, 5, '0,1,5', '活动运营组', 'ORG_CAMPAIGN', 'GROUP', 21, 1, 'system', 'system'),
  (7, 1, '0,1', '设计社群', 'ORG_DESIGN_CLUB', 'CLUB', 30, 1, 'system', 'system'),
  (8, 1, '0,1', '示例商家', 'ORG_DEMO_MERCHANT', 'MERCHANT', 40, 0, 'system', 'system');

INSERT INTO sys_user (
  id, user_name, password_hash, nick_name, user_gender, user_phone, user_email,
  avatar, status, create_by, update_by
)
VALUES
  (1, 'admin', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '系统管理员', '男', '13800000001', 'admin@example.com', '/src/assets/images/avatar/avatar.webp', 'NORMAL', 'system', 'system'),
  (2, 'moderator', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '内容版主', '男', '13800000002', 'moderator@example.com', '/src/assets/images/avatar/avatar1.webp', 'NORMAL', 'system', 'system'),
  (3, 'editor', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '文章编辑', '女', '13800000003', 'editor@example.com', '/src/assets/images/avatar/avatar2.webp', 'NORMAL', 'system', 'system'),
  (4, 'operator', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '运营同学', '女', '13800000004', 'operator@example.com', '/src/assets/images/avatar/avatar3.webp', 'NORMAL', 'system', 'system'),
  (5, 'designer', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '设计访客', '男', '13800000005', 'designer@example.com', '/src/assets/images/avatar/avatar4.webp', 'NORMAL', 'system', 'system'),
  (6, 'disabled_user', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '停用用户', '女', '13800000006', 'disabled@example.com', '/src/assets/images/avatar/avatar5.webp', 'DISABLED', 'system', 'system'),
  (7, 'photographer', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '视觉摄影师', '女', '13800000007', 'photo@example.com', '/src/assets/images/avatar/avatar6.webp', 'NORMAL', 'system', 'system'),
  (8, 'producter', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '产品策划', '男', '13800000008', 'product@example.com', '/src/assets/images/avatar/avatar7.webp', 'NORMAL', 'system', 'system'),
  (9, 'tester', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '体验测试员', '女', '13800000009', 'tester@example.com', '/src/assets/images/avatar/avatar8.webp', 'NORMAL', 'system', 'system'),
  (10, 'guest_writer', '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2', '特约作者', '男', '13800000010', 'writer@example.com', '/src/assets/images/avatar/avatar9.webp', 'NORMAL', 'system', 'system');

INSERT INTO sys_role (
  id, role_name, role_code, role_level, role_sort, role_type, access_scope,
  data_scope, manageable, enabled, description, create_by, update_by
)
VALUES
  (1, '超级管理员', 'R_SUPER', 100, 1, 'SYSTEM', 'BOTH', 'ALL', 0, 1, '拥有系统全部管理权限', 'system', 'system'),
  (2, '内容管理员', 'R_CONTENT_ADMIN', 70, 2, 'BUSINESS', 'BOTH', 'CURRENT_ORG_AND_SUB', 1, 1, '管理文章、评论和内容审核', 'system', 'system'),
  (3, '运营管理员', 'R_OPERATION_ADMIN', 60, 3, 'BUSINESS', 'ADMIN', 'CUSTOM', 1, 1, '管理运营组织内用户与基础配置', 'system', 'system'),
  (4, '文章编辑', 'R_EDITOR', 40, 4, 'BUSINESS', 'BOTH', 'CURRENT_ORG', 1, 1, '创建和编辑文章内容', 'system', 'system'),
  (5, '普通用户', 'R_USER', 10, 5, 'BUSINESS', 'PORTAL', 'SELF', 1, 1, '访问前台并参与评论互动', 'system', 'system'),
  (6, '游客', 'R_GUEST', 0, 6, 'GUEST', 'PORTAL', 'SELF', 0, 1, '只能访问公开只读内容', 'system', 'system');

INSERT INTO sys_permission (id, permission_name, permission_code, module_code, access_scope, enabled, create_by, update_by)
VALUES
  (1, '新增用户', 'system:user:add', 'system', 'ADMIN', 1, 'system', 'system'),
  (2, '编辑用户', 'system:user:edit', 'system', 'ADMIN', 1, 'system', 'system'),
  (3, '删除用户', 'system:user:delete', 'system', 'ADMIN', 1, 'system', 'system'),
  (4, '新增角色', 'system:role:add', 'system', 'ADMIN', 1, 'system', 'system'),
  (5, '编辑角色', 'system:role:edit', 'system', 'ADMIN', 1, 'system', 'system'),
  (6, '分配角色权限', 'system:role:permission', 'system', 'ADMIN', 1, 'system', 'system'),
  (7, '管理组织', 'system:org:manage', 'system', 'ADMIN', 1, 'system', 'system'),
  (8, '管理配置', 'system:config:manage', 'system', 'ADMIN', 1, 'system', 'system'),
  (9, '发布文章', 'article:publish:add', 'article', 'BOTH', 1, 'system', 'system'),
  (10, '编辑文章', 'article:publish:edit', 'article', 'BOTH', 1, 'system', 'system'),
  (11, '管理菜单', 'system:menu:manage', 'system', 'ADMIN', 1, 'system', 'system'),
  (12, '查看用户', 'system:user:view', 'system', 'ADMIN', 1, 'system', 'system'),
  (13, '查看角色', 'system:role:view', 'system', 'ADMIN', 1, 'system', 'system'),
  (14, '上传文章资源', 'article:upload', 'article', 'BOTH', 1, 'system', 'system'),
  (15, '管理文章评论', 'article:comment:manage', 'article', 'BOTH', 1, 'system', 'system'),
  (16, '管理敏感词库', 'system:sensitive-word', 'system', 'ADMIN', 1, 'system', 'system'),
  (17, '管理文件资源', 'system:file:manage', 'system', 'ADMIN', 1, 'system', 'system'),
  (18, '管理举报审核', 'moderation:report:manage', 'moderation', 'ADMIN', 1, 'system', 'system');

INSERT INTO sys_menu (
  id, parent_id, menu_type, path, name, component, redirect, title, icon,
  access_scope, permission_code, keep_alive, fixed_tab, hidden, hidden_tab,
  active_path, sort, enabled, create_by, update_by
)
VALUES
  (1, 0, 'DIR', '/dashboard', 'Dashboard', '/index/index', NULL, 'menus.dashboard.title', 'ri:pie-chart-line', 'ADMIN', NULL, 0, 0, 0, 0, NULL, 1, 1, 'system', 'system'),
  (2, 1, 'MENU', 'console', 'Console', '/dashboard/console', NULL, 'menus.dashboard.console', 'ri:home-smile-2-line', 'ADMIN', NULL, 0, 1, 0, 0, NULL, 1, 1, 'system', 'system'),
  (3, 0, 'DIR', '/article', 'Article', '/index/index', NULL, 'menus.article.title', 'ri:book-2-line', 'BOTH', NULL, 0, 0, 0, 0, NULL, 20, 1, 'system', 'system'),
  (4, 3, 'MENU', 'article-list', 'ArticleList', '/article/list', NULL, 'menus.article.articleList', 'ri:article-line', 'BOTH', NULL, 1, 0, 0, 0, NULL, 1, 1, 'system', 'system'),
  (5, 3, 'MENU', 'publish', 'ArticlePublish', '/article/publish', NULL, 'menus.article.articlePublish', 'ri:telegram-2-line', 'ADMIN', 'article:publish:add', 1, 0, 0, 0, NULL, 2, 1, 'system', 'system'),
  (6, 3, 'MENU', 'detail/:id', 'ArticleDetail', '/article/detail', NULL, 'menus.article.articleDetail', NULL, 'BOTH', NULL, 1, 0, 1, 0, '/article/article-list', 3, 1, 'system', 'system'),
  (7, 0, 'DIR', '/system', 'System', '/index/index', NULL, 'menus.system.title', 'ri:user-3-line', 'ADMIN', NULL, 0, 0, 0, 0, NULL, 30, 1, 'system', 'system'),
  (9, 7, 'MENU', 'user', 'User', '/system/user', NULL, 'menus.system.user', 'ri:user-line', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 1, 1, 'system', 'system'),
  (10, 7, 'MENU', 'role', 'Role', '/system/role', NULL, 'menus.system.role', 'ri:user-settings-line', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 2, 1, 'system', 'system'),
  (11, 7, 'MENU', 'org', 'Org', '/system/org', NULL, 'menus.system.org', 'ri:organization-chart', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 3, 1, 'system', 'system'),
  (12, 7, 'MENU', 'config', 'Config', '/system/config', NULL, 'menus.system.config', 'ri:settings-3-line', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 4, 1, 'system', 'system'),
  (13, 7, 'MENU', 'file', 'FileResource', '/system/file', NULL, 'menus.system.fileResource', 'ri:file-list-3-line', 'ADMIN', 'system:file:manage', 1, 0, 0, 0, NULL, 5, 1, 'system', 'system'),
  (14, 7, 'MENU', 'menu', 'Menus', '/system/menu', NULL, 'menus.system.menu', 'ri:menu-line', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 6, 1, 'system', 'system'),
  (29, 31, 'MENU', 'sensitive-word', 'SensitiveWord', '/article/comment', NULL, 'menus.moderation.sensitiveWord', 'ri:shield-keyhole-line', 'ADMIN', 'system:sensitive-word', 0, 0, 0, 0, NULL, 2, 1, 'system', 'system'),
  (15, 7, 'MENU', 'user-center', 'UserCenter', '/system/user-center', NULL, 'menus.system.userCenter', 'ri:user-line', 'BOTH', NULL, 1, 0, 1, 1, NULL, 99, 1, 'system', 'system'),
  (16, 9, 'BUTTON', NULL, 'UserAdd', NULL, NULL, '新增用户', NULL, 'ADMIN', 'system:user:add', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (17, 9, 'BUTTON', NULL, 'UserEdit', NULL, NULL, '编辑用户', NULL, 'ADMIN', 'system:user:edit', 0, 0, 1, 1, NULL, 2, 1, 'system', 'system'),
  (18, 9, 'BUTTON', NULL, 'UserDelete', NULL, NULL, '删除用户', NULL, 'ADMIN', 'system:user:delete', 0, 0, 1, 1, NULL, 3, 1, 'system', 'system'),
  (19, 10, 'BUTTON', NULL, 'RoleAdd', NULL, NULL, '新增角色', NULL, 'ADMIN', 'system:role:add', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (20, 10, 'BUTTON', NULL, 'RoleEdit', NULL, NULL, '编辑角色', NULL, 'ADMIN', 'system:role:edit', 0, 0, 1, 1, NULL, 2, 1, 'system', 'system'),
  (21, 10, 'BUTTON', NULL, 'RolePermission', NULL, NULL, '分配角色权限', NULL, 'ADMIN', 'system:role:permission', 0, 0, 1, 1, NULL, 3, 1, 'system', 'system'),
  (22, 11, 'BUTTON', NULL, 'OrgManage', NULL, NULL, '管理组织', NULL, 'ADMIN', 'system:org:manage', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (23, 12, 'BUTTON', NULL, 'ConfigManage', NULL, NULL, '管理配置', NULL, 'ADMIN', 'system:config:manage', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (24, 14, 'BUTTON', NULL, 'MenuManage', NULL, NULL, '管理菜单', NULL, 'ADMIN', 'system:menu:manage', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (25, 4, 'BUTTON', NULL, 'ArticleAdd', NULL, NULL, '新增文章', NULL, 'BOTH', 'article:publish:add', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (26, 4, 'BUTTON', NULL, 'ArticleEdit', NULL, NULL, '编辑文章', NULL, 'BOTH', 'article:publish:edit', 0, 0, 1, 1, NULL, 2, 1, 'system', 'system'),
  (27, 5, 'BUTTON', NULL, 'ArticleUpload', NULL, NULL, '上传文章资源', NULL, 'BOTH', 'article:upload', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (28, 29, 'BUTTON', NULL, 'SensitiveWordManage', NULL, NULL, '管理敏感词库', NULL, 'ADMIN', 'system:sensitive-word', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (30, 13, 'BUTTON', NULL, 'FileResourceManage', NULL, NULL, '管理文件资源', NULL, 'ADMIN', 'system:file:manage', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (31, 0, 'DIR', '/moderation', 'Moderation', '/index/index', NULL, 'menus.moderation.title', 'ri:shield-check-line', 'ADMIN', NULL, 0, 0, 0, 0, NULL, 40, 1, 'system', 'system'),
  (32, 31, 'MENU', 'reports', 'ContentReport', '/moderation/reports', NULL, 'menus.moderation.report', 'ri:alarm-warning-line', 'ADMIN', 'moderation:report:manage', 1, 0, 0, 0, NULL, 1, 1, 'system', 'system'),
  (33, 32, 'BUTTON', NULL, 'ContentReportManage', NULL, NULL, '管理举报审核', NULL, 'ADMIN', 'moderation:report:manage', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system');

INSERT INTO sys_user_role (user_id, role_id)
VALUES
  (1, 1),
  (2, 2),
  (3, 4),
  (4, 3),
  (5, 5),
  (6, 5),
  (7, 5),
  (8, 5),
  (9, 5),
  (10, 4);

INSERT INTO sys_user_org (user_id, org_id, primary_org)
VALUES
  (1, 1, 1),
  (2, 2, 1),
  (2, 4, 0),
  (3, 3, 1),
  (4, 5, 1),
  (4, 6, 0),
  (5, 7, 1),
  (6, 8, 1),
  (7, 7, 1),
  (8, 5, 1),
  (9, 6, 1),
  (10, 3, 1);

INSERT INTO sys_role_org (role_id, org_id)
VALUES
  (2, 2),
  (3, 5),
  (3, 6),
  (4, 3);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24), (1, 25), (1, 26), (1, 27), (1, 28), (1, 29), (1, 30), (1, 31), (1, 32), (1, 33),
  (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 14), (2, 15), (2, 25), (2, 26), (2, 27), (2, 28), (2, 29), (2, 31), (2, 32), (2, 33),
  (3, 1), (3, 2), (3, 7), (3, 9), (3, 11), (3, 12), (3, 15), (3, 17), (3, 22), (3, 23),
  (4, 3), (4, 4), (4, 5), (4, 6), (4, 15), (4, 25), (4, 26), (4, 27),
  (5, 3), (5, 4), (5, 6), (5, 15),
  (6, 3), (6, 4), (6, 6);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18),
  (2, 9), (2, 10), (2, 14), (2, 15), (2, 16), (2, 18),
  (3, 2), (3, 7), (3, 8), (3, 12),
  (4, 9), (4, 10), (4, 14);
