-- 系统基础表结构。
-- 适用范围：用户、角色、菜单、权限、组织、配置项和 RBAC 关联关系。

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  user_name VARCHAR(50) NOT NULL COMMENT '用户名',
  password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希',
  nick_name VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  user_gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
  user_phone VARCHAR(30) DEFAULT NULL COMMENT '手机号',
  user_email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '用户状态：NORMAL/DISABLED/LOCKED',
  create_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_user_name (user_name),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  role_level INT NOT NULL DEFAULT 0 COMMENT '角色层级，数值越高管理级别越高',
  role_sort INT NOT NULL DEFAULT 0 COMMENT '展示排序',
  role_type VARCHAR(20) NOT NULL DEFAULT 'BUSINESS' COMMENT '角色类型：SYSTEM/BUSINESS/GUEST',
  access_scope VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '访问端：ADMIN/PORTAL/BOTH',
  data_scope VARCHAR(30) NOT NULL DEFAULT 'SELF' COMMENT '数据权限范围',
  manageable TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许被普通管理员管理',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  create_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_role_code (role_code),
  KEY idx_sys_role_enabled (enabled),
  KEY idx_sys_role_access_scope (access_scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
  permission_code VARCHAR(100) NOT NULL COMMENT '权限标识',
  module_code VARCHAR(50) NOT NULL COMMENT '模块编码',
  access_scope VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '访问端：ADMIN/PORTAL/BOTH',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  create_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (permission_code),
  KEY idx_sys_permission_module (module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限标识表';

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  menu_type VARCHAR(20) NOT NULL DEFAULT 'MENU' COMMENT '菜单类型：DIR/MENU/BUTTON/IFRAME/LINK',
  path VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
  name VARCHAR(100) DEFAULT NULL COMMENT '路由名称',
  component VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
  redirect VARCHAR(255) DEFAULT NULL COMMENT '重定向地址',
  title VARCHAR(100) NOT NULL COMMENT '菜单标题或国际化Key',
  icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
  access_scope VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '访问端：ADMIN/PORTAL/BOTH',
  permission_code VARCHAR(100) DEFAULT NULL COMMENT '按钮或接口权限标识',
  iframe_link VARCHAR(500) DEFAULT NULL COMMENT 'iframe或外链地址',
  keep_alive TINYINT NOT NULL DEFAULT 0 COMMENT '是否缓存',
  fixed_tab TINYINT NOT NULL DEFAULT 0 COMMENT '是否固定标签',
  hidden TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏',
  hidden_tab TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏标签',
  active_path VARCHAR(255) DEFAULT NULL COMMENT '激活菜单路径',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  create_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_menu_name (name),
  KEY idx_sys_menu_parent (parent_id),
  KEY idx_sys_menu_access_scope (access_scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

CREATE TABLE IF NOT EXISTS sys_org (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父组织ID',
  ancestors VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖级列表',
  org_name VARCHAR(100) NOT NULL COMMENT '组织名称',
  org_code VARCHAR(100) NOT NULL COMMENT '组织编码',
  org_type VARCHAR(30) NOT NULL DEFAULT 'GROUP' COMMENT '组织类型：DEPT/CLUB/GROUP/MERCHANT',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  create_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_org_code (org_code),
  KEY idx_sys_org_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织表';

CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  config_value VARCHAR(500) NOT NULL COMMENT '配置值',
  description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  editable TINYINT NOT NULL DEFAULT 1 COMMENT '是否可编辑',
  create_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted BIGINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，删除后写入记录ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_config_key_deleted (config_key, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_role (user_id, role_id),
  KEY idx_sys_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_user_org (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  org_id BIGINT NOT NULL COMMENT '组织ID',
  primary_org TINYINT NOT NULL DEFAULT 0 COMMENT '是否主组织',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_org (user_id, org_id),
  KEY idx_sys_user_org_org (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户组织关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
  KEY idx_sys_role_menu_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
  KEY idx_sys_role_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS sys_role_org (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  org_id BIGINT NOT NULL COMMENT '组织ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_org (role_id, org_id),
  KEY idx_sys_role_org_org (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色自定义数据权限组织关联表';
