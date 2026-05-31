-- 系统基础 Mock 数据。
-- 说明：密码字段应保存 BCrypt 哈希。当前 SQL 先保留占位，开发用户模块时再生成真实哈希。

-- 初始化配置项
INSERT INTO sys_config (config_key, config_value, description)
VALUES
  ('user_org_relation_mode', 'ONE_TO_MANY', '用户和组织默认一对多'),
  ('role_level_enabled', 'true', '启用角色层级管理'),
  ('anonymous_portal_access', 'true', '允许匿名访问前台公开内容'),
  ('guest_admin_access', 'false', '禁止游客访问后台');

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_code, role_level, role_type, access_scope, data_scope, enabled)
VALUES
  (1, '超级管理员', 'R_SUPER', 100, 'SYSTEM', 'BOTH', 'ALL', 1),
  (2, '普通用户', 'R_USER', 10, 'BUSINESS', 'PORTAL', 'SELF', 1),
  (3, '游客', 'R_GUEST', 0, 'GUEST', 'PORTAL', 'SELF', 1);
