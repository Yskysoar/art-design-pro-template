-- 文章、上传与评论 Mock 数据。

INSERT INTO article_category (id, category_name, category_code, sort, enabled, create_by, update_by)
VALUES
  (1, '产品动态', 'PRODUCT_NEWS', 1, 1, 'system', 'system'),
  (2, '技术文章', 'TECH_ARTICLE', 2, 1, 'system', 'system'),
  (3, '运营公告', 'OPERATION_NOTICE', 3, 1, 'system', 'system'),
  (4, '设计案例', 'DESIGN_CASE', 4, 1, 'system', 'system'),
  (5, '归档分类', 'ARCHIVED_CATEGORY', 99, 0, 'system', 'system');

INSERT INTO file_resource (
  id, original_name, storage_name, storage_path, url, content_type,
  extension, size, sha256, storage_type, uploader_id, create_by
)
VALUES
  (1, 'cover-product.webp', 'cover-product.webp', 'article/mock/cover-product.webp', '/src/assets/images/cover/img1.webp', 'image/webp', 'webp', 128000, '1111111111111111111111111111111111111111111111111111111111111111', 'REMOTE', 1, 'admin'),
  (2, 'cover-tech.webp', 'cover-tech.webp', 'article/mock/cover-tech.webp', '/src/assets/images/cover/img2.webp', 'image/webp', 'webp', 142000, '2222222222222222222222222222222222222222222222222222222222222222', 'REMOTE', 2, 'moderator'),
  (3, 'design-case.webp', 'design-case.webp', 'article/mock/design-case.webp', '/src/assets/images/cover/img3.webp', 'image/webp', 'webp', 166000, '3333333333333333333333333333333333333333333333333333333333333333', 'REMOTE', 3, 'editor');

INSERT INTO article (
  id, title, category_id, cover_url, summary, content_html, content_text,
  visible, status, view_count, comment_count, publish_time, create_by, update_by
)
VALUES
  (
    1,
    '后端模板文章模块接入说明',
    2,
    '/src/assets/images/cover/img1.webp',
    '记录文章、上传和评论模块的真实接口接入范围。',
    '<h2>文章模块</h2><p>当前文章模块已接入分类、列表、详情、发布、编辑、上传和评论基础能力。</p>',
    '文章模块 当前文章模块已接入分类、列表、详情、发布、编辑、上传和评论基础能力。',
    1,
    'PUBLISHED',
    328,
    4,
    '2026-06-01 10:00:00',
    'admin',
    'admin'
  ),
  (
    2,
    '系统管理菜单与权限联调记录',
    2,
    '/src/assets/images/cover/img2.webp',
    '说明菜单、角色权限和后端权限码的联调关系。',
    '<h2>系统管理</h2><p>菜单、角色、权限码和按钮授权需要保持同源。</p>',
    '系统管理 菜单、角色、权限码和按钮授权需要保持同源。',
    1,
    'PUBLISHED',
    210,
    2,
    '2026-06-01 14:30:00',
    'moderator',
    'moderator'
  ),
  (
    3,
    '评论区回复交互优化方案',
    4,
    '/src/assets/images/cover/img3.webp',
    '用于验证评论头像、回复排版和隐藏状态展示。',
    '<h2>评论交互</h2><p>回复区需要头像、用户名、内容和操作按钮保持清晰对齐。</p>',
    '评论交互 回复区需要头像、用户名、内容和操作按钮保持清晰对齐。',
    1,
    'PUBLISHED',
    96,
    3,
    '2026-06-02 09:15:00',
    'editor',
    'editor'
  ),
  (
    4,
    '后台配置管理草稿',
    3,
    NULL,
    '一篇未发布草稿，用于验证后台草稿状态。',
    '<p>配置管理草稿内容。</p>',
    '配置管理草稿内容。',
    0,
    'DRAFT',
    0,
    0,
    NULL,
    'operator',
    'operator'
  ),
  (
    5,
    '已下线文章示例',
    1,
    '/src/assets/images/cover/img4.webp',
    '用于验证下线状态和前台不可见边界。',
    '<p>该文章已下线，仅后台可见。</p>',
    '该文章已下线，仅后台可见。',
    0,
    'OFFLINE',
    18,
    0,
    '2026-05-28 16:00:00',
    'admin',
    'admin'
  );

INSERT INTO article_attachment (id, article_id, file_id, sort)
VALUES
  (1, 1, 1, 1),
  (2, 2, 2, 1),
  (3, 3, 3, 1);

INSERT INTO article_comment (
  id, article_id, parent_id, root_id, content, status,
  user_id, user_name, user_avatar, ip_address, user_agent, create_time
)
VALUES
  (1, 1, 0, 1, '文章模块说明很清楚，后续评论区可以直接作为真实联调样例。', 'NORMAL', 2, 'moderator', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-01 10:20:00'),
  (2, 1, 1, 1, '收到，这条回复用于验证子评论挂载和头像展示。', 'NORMAL', 1, 'admin', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-01 10:25:00'),
  (3, 1, 0, 3, '隐藏评论示例，前台应展示脱敏占位内容。', 'HIDDEN', 5, 'designer', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-01 11:00:00'),
  (4, 1, 0, 4, '删除评论示例，计数不应继续统计。', 'DELETED', 6, 'disabled_user', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-01 11:10:00'),
  (5, 2, 0, 5, '权限码和菜单按钮保持一致后，前后端排查会简单很多。', 'NORMAL', 4, 'operator', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-01 15:10:00'),
  (6, 2, 5, 5, '这里也可以验证二级评论列表的折叠和展示。', 'NORMAL', 2, 'moderator', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-01 15:20:00'),
  (7, 3, 0, 7, '头像和用户名需要并排展示，这条用于浏览器回归。', 'NORMAL', 5, 'designer', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-02 09:40:00'),
  (8, 3, 7, 7, '回复样式现在应保持横向对齐。', 'NORMAL', 3, 'editor', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-02 09:45:00'),
  (9, 3, 7, 7, '移动端也需要保持头像不挤压。', 'NORMAL', 4, 'operator', '/src/assets/images/avatar/avatar.webp', '127.0.0.1', 'Mock Browser', '2026-06-02 09:50:00');
