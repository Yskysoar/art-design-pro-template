-- 敏感词库 Mock 数据。
-- 这些数据用于验证文章、评论、摘要、标题、用户信息、组织、角色、配置和菜单等文本入口的统一拦截。

INSERT INTO comment_sensitive_word (
  id, word, match_type, enabled, remark, create_by, update_by
)
VALUES
  (1, '禁用词A', 'CONTAINS', 1, '通用内容拦截示例', 'system', 'system'),
  (2, '违规词B', 'CONTAINS', 1, '评论与文章内容拦截示例', 'system', 'system'),
  (3, '风险昵称', 'CONTAINS', 1, '用户昵称、用户名和邮箱备注类字段拦截示例', 'system', 'system'),
  (4, '测试屏蔽项', 'CONTAINS', 1, '组织、角色、配置和菜单名称拦截示例', 'system', 'system'),
  (5, '停用敏感词', 'CONTAINS', 0, '停用状态不参与拦截，用于验证状态开关', 'system', 'system'),
  (6, '广告引流', 'CONTAINS', 1, '运营内容高频风险词示例', 'system', 'system'),
  (7, '恶意脚本', 'CONTAINS', 1, '富文本内容安全验证示例', 'system', 'system'),
  (8, '虚假承诺', 'CONTAINS', 1, '标题和摘要安全验证示例', 'system', 'system');
