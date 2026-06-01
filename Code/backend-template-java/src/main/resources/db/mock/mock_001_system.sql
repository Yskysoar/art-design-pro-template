-- 系统基础 Mock 数据。
-- 说明：管理员初始密码为 admin123，password_hash 保存 BCrypt 哈希。

INSERT INTO sys_config (id, config_key, config_value, description, editable, create_by, update_by)
VALUES
  (1, 'user_org_relation_mode', 'ONE_TO_MANY', '用户和组织默认一对多', 1, 'system', 'system'),
  (2, 'role_level_enabled', 'true', '启用角色层级管理', 1, 'system', 'system'),
  (3, 'anonymous_portal_access', 'true', '允许匿名访问前台公开内容', 1, 'system', 'system'),
  (4, 'guest_admin_access', 'false', '禁止游客访问后台', 1, 'system', 'system');

INSERT INTO sys_org (id, parent_id, ancestors, org_name, org_code, org_type, sort, enabled, create_by, update_by)
VALUES
  (1, 0, '0', '模板组织', 'ORG_TEMPLATE', 'GROUP', 1, 1, 'system', 'system'),
  (2, 1, '0,1', '内容管理组', 'ORG_CONTENT', 'GROUP', 10, 1, 'system', 'system'),
  (3, 1, '0,1', '运营管理组', 'ORG_OPERATION', 'GROUP', 20, 1, 'system', 'system');

INSERT INTO sys_user (
  id, user_name, password_hash, nick_name, user_gender, user_phone, user_email,
  avatar, status, create_by, update_by
)
VALUES
  (
    1,
    'admin',
    '$2a$10$rfvyC6RCEizuD33WiIVFKeSD4liX0oGZQiICwxVUowzZcov3EJuk2',
    '系统管理员',
    '男',
    '13800000001',
    'admin@example.com',
    '/src/assets/images/avatar/avatar.webp',
    'NORMAL',
    'system',
    'system'
  );

INSERT INTO sys_role (
  id, role_name, role_code, role_level, role_sort, role_type, access_scope,
  data_scope, manageable, enabled, description, create_by, update_by
)
VALUES
  (1, '超级管理员', 'R_SUPER', 100, 1, 'SYSTEM', 'BOTH', 'ALL', 0, 1, '拥有系统全部管理权限', 'system', 'system'),
  (2, '内容版主', 'R_MODERATOR', 60, 2, 'BUSINESS', 'BOTH', 'CURRENT_ORG_AND_SUB', 1, 1, '可管理所属组织内容', 'system', 'system'),
  (3, '普通用户', 'R_USER', 10, 3, 'BUSINESS', 'PORTAL', 'SELF', 1, 1, '可访问前台并进行授权交互', 'system', 'system'),
  (4, '游客', 'R_GUEST', 0, 4, 'GUEST', 'PORTAL', 'SELF', 0, 1, '只能访问前台公开只读内容', 'system', 'system');

INSERT INTO sys_permission (id, permission_name, permission_code, module_code, access_scope, enabled, create_by, update_by)
VALUES
  (1, '新增用户', 'system:user:add', 'system', 'ADMIN', 1, 'system', 'system'),
  (2, '编辑用户', 'system:user:edit', 'system', 'ADMIN', 1, 'system', 'system'),
  (3, '删除用户', 'system:user:delete', 'system', 'ADMIN', 1, 'system', 'system'),
  (4, '新增角色', 'system:role:add', 'system', 'ADMIN', 1, 'system', 'system'),
  (5, '编辑角色', 'system:role:edit', 'system', 'ADMIN', 1, 'system', 'system'),
  (6, '分配角色权限', 'system:role:permission', 'system', 'ADMIN', 1, 'system', 'system'),
  (7, '管理组织', 'system:org:manage', 'system', 'ADMIN', 1, 'system', 'system'),
  (8, '管理配置项', 'system:config:manage', 'system', 'ADMIN', 1, 'system', 'system'),
  (9, '发布文章', 'article:publish:add', 'article', 'BOTH', 1, 'system', 'system'),
  (10, '编辑文章', 'article:publish:edit', 'article', 'BOTH', 1, 'system', 'system'),
  (11, '管理菜单', 'system:menu:manage', 'system', 'ADMIN', 1, 'system', 'system');

INSERT INTO sys_menu (
  id, parent_id, menu_type, path, name, component, redirect, title, icon,
  access_scope, permission_code, keep_alive, fixed_tab, hidden, hidden_tab,
  active_path, sort, enabled, create_by, update_by
)
VALUES
  (1, 0, 'DIR', '/dashboard', 'Dashboard', '/index/index', NULL, 'menus.dashboard.title', 'ri:pie-chart-line', 'ADMIN', NULL, 0, 0, 0, 0, NULL, 1, 1, 'system', 'system'),
  (2, 1, 'MENU', 'console', 'Console', '/dashboard/console', NULL, 'menus.dashboard.console', 'ri:home-smile-2-line', 'ADMIN', NULL, 0, 1, 0, 0, NULL, 1, 1, 'system', 'system'),
  (3, 0, 'DIR', '/system', 'System', '/index/index', NULL, 'menus.system.title', 'ri:user-3-line', 'ADMIN', NULL, 0, 0, 0, 0, NULL, 10, 1, 'system', 'system'),
  (4, 3, 'MENU', 'user', 'User', '/system/user', NULL, 'menus.system.user', 'ri:user-line', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 1, 1, 'system', 'system'),
  (5, 3, 'MENU', 'role', 'Role', '/system/role', NULL, 'menus.system.role', 'ri:user-settings-line', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 2, 1, 'system', 'system'),
  (6, 3, 'MENU', 'menu', 'Menus', '/system/menu', NULL, 'menus.system.menu', 'ri:menu-line', 'ADMIN', NULL, 1, 0, 0, 0, NULL, 3, 1, 'system', 'system'),
  (7, 0, 'DIR', '/article', 'Article', '/index/index', NULL, 'menus.article.title', 'ri:book-2-line', 'BOTH', NULL, 0, 0, 0, 0, NULL, 20, 1, 'system', 'system'),
  (8, 7, 'MENU', 'article-list', 'ArticleList', '/article/list', NULL, 'menus.article.articleList', 'ri:article-line', 'BOTH', NULL, 1, 0, 0, 0, NULL, 1, 1, 'system', 'system'),
  (9, 7, 'MENU', 'publish', 'ArticlePublish', '/article/publish', NULL, 'menus.article.articlePublish', 'ri:telegram-2-line', 'ADMIN', 'article:publish:add', 1, 0, 0, 0, NULL, 2, 1, 'system', 'system'),
  (10, 4, 'BUTTON', NULL, 'UserAdd', NULL, NULL, '新增用户', NULL, 'ADMIN', 'system:user:add', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (11, 4, 'BUTTON', NULL, 'UserEdit', NULL, NULL, '编辑用户', NULL, 'ADMIN', 'system:user:edit', 0, 0, 1, 1, NULL, 2, 1, 'system', 'system'),
  (12, 4, 'BUTTON', NULL, 'UserDelete', NULL, NULL, '删除用户', NULL, 'ADMIN', 'system:user:delete', 0, 0, 1, 1, NULL, 3, 1, 'system', 'system'),
  (13, 5, 'BUTTON', NULL, 'RoleAdd', NULL, NULL, '新增角色', NULL, 'ADMIN', 'system:role:add', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system'),
  (14, 5, 'BUTTON', NULL, 'RoleEdit', NULL, NULL, '编辑角色', NULL, 'ADMIN', 'system:role:edit', 0, 0, 1, 1, NULL, 2, 1, 'system', 'system'),
  (15, 5, 'BUTTON', NULL, 'RolePermission', NULL, NULL, '分配角色权限', NULL, 'ADMIN', 'system:role:permission', 0, 0, 1, 1, NULL, 3, 1, 'system', 'system'),
  (16, 6, 'BUTTON', NULL, 'MenuManage', NULL, NULL, '管理菜单', NULL, 'ADMIN', 'system:menu:manage', 0, 0, 1, 1, NULL, 1, 1, 'system', 'system');

INSERT INTO sys_user_role (user_id, role_id)
VALUES
  (1, 1);

INSERT INTO sys_user_org (user_id, org_id, primary_org)
VALUES
  (1, 1, 1);

INSERT INTO sys_role_org (role_id, org_id)
VALUES
  (2, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
  (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
  (2, 7), (2, 8), (2, 9),
  (3, 7), (3, 8),
  (4, 7), (4, 8);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11),
  (2, 9), (2, 10);
