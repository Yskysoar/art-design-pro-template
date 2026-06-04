-- 文件资源、通知中心与内容治理 Mock 数据。
-- 说明：本文件使用 INSERT IGNORE，便于在已有开发库中重复补充数据。

INSERT IGNORE INTO file_resource (
  id, original_name, storage_name, storage_path, url, content_type,
  extension, size, sha256, storage_type, uploader_id, create_by, create_time
)
VALUES
  (40, 'banner-campaign-preview.jpg', '20260604-banner-campaign-preview.jpg', 'article/2026/06/20260604-banner-campaign-preview.jpg', 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?w=1200&q=80', 'image/jpeg', 'jpg', 482312, '4040404040404040404040404040404040404040404040404040404040404040', 'REMOTE', 4, 'operator', '2026-06-04 09:05:00'),
  (41, 'editorial-cover-alt.png', '20260604-editorial-cover-alt.png', 'article/2026/06/20260604-editorial-cover-alt.png', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=1200&q=80', 'image/png', 'png', 731904, '4141414141414141414141414141414141414141414141414141414141414141', 'REMOTE', 3, 'editor', '2026-06-04 09:18:00'),
  (42, 'richtext-inline-diagram.webp', '20260604-richtext-inline-diagram.webp', 'article/2026/06/20260604-richtext-inline-diagram.webp', '/src/assets/images/cover/img5.webp', 'image/webp', 'webp', 214528, '4242424242424242424242424242424242424242424242424242424242424242', 'LOCAL', 3, 'editor', '2026-06-04 09:42:00'),
  (43, 'article-review-notes.pdf', '20260604-article-review-notes.pdf', 'article/2026/06/20260604-article-review-notes.pdf', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'application/pdf', 'pdf', 132640, '4343434343434343434343434343434343434343434343434343434343434343', 'REMOTE', 2, 'moderator', '2026-06-04 10:03:00'),
  (44, 'release-checklist.xlsx', '20260604-release-checklist.xlsx', 'article/2026/06/20260604-release-checklist.xlsx', 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/file_example_XLSX_10.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'xlsx', 13840, '4444444444444444444444444444444444444444444444444444444444444444', 'REMOTE', 4, 'operator', '2026-06-04 10:16:00'),
  (45, 'visual-guideline.docx', '20260604-visual-guideline.docx', 'article/2026/06/20260604-visual-guideline.docx', 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/file-sample_100kB.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'docx', 102400, '4545454545454545454545454545454545454545454545454545454545454545', 'REMOTE', 5, 'designer', '2026-06-04 10:25:00'),
  (46, 'social-chat-screenshot-a.webp', '20260604-social-chat-screenshot-a.webp', 'social/2026/06/20260604-social-chat-screenshot-a.webp', '/src/assets/images/cover/img6.webp', 'image/webp', 'webp', 368128, '4646464646464646464646464646464646464646464646464646464646464646', 'LOCAL', 9, 'tester', '2026-06-04 11:10:00'),
  (47, 'social-chat-screenshot-b.webp', '20260604-social-chat-screenshot-b.webp', 'social/2026/06/20260604-social-chat-screenshot-b.webp', '/src/assets/images/cover/img7.webp', 'image/webp', 'webp', 392448, '4747474747474747474747474747474747474747474747474747474747474747', 'LOCAL', 7, 'photographer', '2026-06-04 11:28:00'),
  (48, 'privacy-policy-draft.txt', '20260604-privacy-policy-draft.txt', 'system/2026/06/20260604-privacy-policy-draft.txt', 'https://www.w3.org/TR/PNG/iso_8859-1.txt', 'text/plain', 'txt', 6120, '4848484848484848484848484848484848484848484848484848484848484848', 'REMOTE', 1, 'admin', '2026-06-04 11:46:00'),
  (49, 'moderation-evidence.zip', '20260604-moderation-evidence.zip', 'moderation/2026/06/20260604-moderation-evidence.zip', 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/zip_2MB.zip', 'application/zip', 'zip', 2097152, '4949494949494949494949494949494949494949494949494949494949494949', 'REMOTE', 2, 'moderator', '2026-06-04 12:05:00'),
  (50, 'product-demo-video.mp4', '20260604-product-demo-video.mp4', 'article/2026/06/20260604-product-demo-video.mp4', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', 'video/mp4', 'mp4', 1128375, '5050505050505050505050505050505050505050505050505050505050505050', 'REMOTE', 8, 'producter', '2026-06-04 12:36:00'),
  (51, 'meeting-audio-sample.mp3', '20260604-meeting-audio-sample.mp3', 'system/2026/06/20260604-meeting-audio-sample.mp3', 'https://interactive-examples.mdn.mozilla.net/media/cc0-audio/t-rex-roar.mp3', 'audio/mpeg', 'mp3', 54726, '5151515151515151515151515151515151515151515151515151515151515151', 'REMOTE', 4, 'operator', '2026-06-04 13:10:00'),
  (52, 'unused-large-export.xlsx', '20260604-unused-large-export.xlsx', 'system/2026/06/20260604-unused-large-export.xlsx', 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/file_example_XLSX_50.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'xlsx', 28724, '5252525252525252525252525252525252525252525252525252525252525252', 'REMOTE', 1, 'admin', '2026-06-04 13:40:00');

INSERT IGNORE INTO notification_message (
  id, recipient_id, actor_id, notice_type, title, content,
  target_type, target_id, target_url, read_time, create_time, deleted
)
VALUES
  (1, 1, NULL, 'SYSTEM', '系统维护提醒', '今晚 23:30 将进行开发环境索引刷新，预计影响 5 分钟。', 'SYSTEM_NOTICE', NULL, '/dashboard/console', NULL, '2026-06-04 08:00:00', 0),
  (2, 1, 5, 'FOLLOW', '设计访客关注了你', '设计访客关注了你的主页，可以在社交模块查看互关状态。', 'USER', 5, '/social/chat', NULL, '2026-06-04 08:18:00', 0),
  (3, 1, 7, 'PRIVATE_MESSAGE', '视觉摄影师发来图片', '我拍了一组界面素材，可用于文章封面。', 'SOCIAL_CONVERSATION', 3, '/social/chat', NULL, '2026-06-04 08:42:00', 0),
  (4, 1, 3, 'COMMENT_REPLY', '文章编辑回复了你的评论', '这里也可以验证二级评论列表的折叠和展示。', 'ARTICLE', 2, '/article/detail/2', '2026-06-04 09:05:00', '2026-06-04 08:58:00', 0),
  (5, 2, 1, 'PRIVATE_MESSAGE', '系统管理员回复了你', '今天下午一起确认发布计划。', 'SOCIAL_CONVERSATION', 6, '/social/chat', NULL, '2026-06-04 09:26:00', 0),
  (6, 2, NULL, 'SYSTEM', '举报审核队列有新数据', '当前有 3 条待处理举报，请及时查看。', 'MODERATION_REPORT', NULL, '/moderation/reports', NULL, '2026-06-04 09:40:00', 0),
  (7, 3, 10, 'PRIVATE_MESSAGE', '特约作者发送了附件', '投稿说明我整理好了，编辑老师帮忙看一下。', 'SOCIAL_CONVERSATION', 5, '/social/chat', '2026-06-04 10:08:00', '2026-06-04 10:02:00', 0),
  (8, 4, 2, 'COMMENT_REPLY', '内容版主回复了评论', '权限码和菜单按钮保持一致后，前后端排查会简单很多。', 'ARTICLE', 2, '/article/detail/2', NULL, '2026-06-04 10:30:00', 0),
  (9, 5, 1, 'FOLLOW', '系统管理员关注了你', '你们已经互相关注，可以发送图片和附件。', 'USER', 1, '/social/chat', '2026-06-04 11:00:00', '2026-06-04 10:45:00', 0),
  (10, 9, 1, 'SYSTEM', '账号安全提醒', '你的账号近期被加入黑名单测试样例，仅用于开发环境验证。', 'USER', 9, '/system/user', NULL, '2026-06-04 11:12:00', 0),
  (11, 1, NULL, 'SYSTEM', '文件资源台账已更新', '上传资源新增图片、Office、压缩包、音视频等多种样例。', 'FILE_RESOURCE', NULL, '/system/file', NULL, '2026-06-04 13:50:00', 0),
  (12, 2, NULL, 'SYSTEM', '举报处理结果待同步', '部分举报已处理，后续可扩展为向举报人推送结果。', 'MODERATION_REPORT', NULL, '/moderation/reports', '2026-06-04 14:10:00', '2026-06-04 14:00:00', 0);

INSERT IGNORE INTO content_report (
  id, target_type, target_id, reason_type, description, reporter_id, reporter_name,
  status, handler_id, handler_name, handling_remark, handled_time,
  create_time, update_time, deleted
)
VALUES
  (1, 'ARTICLE', 5, 'OUTDATED', '已下线文章仍在外部链接中传播，建议确认是否需要彻底隐藏入口。', 5, 'designer', 'PENDING', NULL, NULL, NULL, NULL, '2026-06-04 08:20:00', '2026-06-04 08:20:00', 0),
  (2, 'COMMENT', 3, 'HARASSMENT', '这条隐藏评论用于验证举报进入待处理队列后的展示状态。', 4, 'operator', 'PROCESSING', 2, 'moderator', '正在核对评论上下文，暂不自动处罚。', NULL, '2026-06-04 08:45:00', '2026-06-04 09:05:00', 0),
  (3, 'PRIVATE_MESSAGE', 9, 'SPAM', '连续三条等待回复私信，用于验证私信额度和举报审核组合场景。', 1, 'admin', 'PENDING', NULL, NULL, NULL, NULL, '2026-06-04 09:12:00', '2026-06-04 09:12:00', 0),
  (4, 'ARTICLE', 1, 'COPYRIGHT', '文章配图疑似未标注来源，先记录为版权核查样例。', 7, 'photographer', 'RESOLVED', 2, 'moderator', '已确认当前为开发环境示例图，生产环境需要补来源和授权。', '2026-06-04 10:18:00', '2026-06-04 09:50:00', '2026-06-04 10:18:00', 0),
  (5, 'COMMENT', 4, 'PERSONAL_INFO', '删除评论样例中包含个人信息风险，验证已删除评论举报处理。', 8, 'producter', 'REJECTED', 1, 'admin', '目标内容已删除且前台不再展示，暂不追加处理。', '2026-06-04 10:45:00', '2026-06-04 10:20:00', '2026-06-04 10:45:00', 0),
  (6, 'PRIVATE_MESSAGE', 18, 'FRAUD', '附件型私信举报样例，用于验证 Office 文件消息的审核台账。', 2, 'moderator', 'PROCESSING', 1, 'admin', '正在确认附件来源，第一版只记录处理状态。', NULL, '2026-06-04 11:05:00', '2026-06-04 11:30:00', 0),
  (7, 'ARTICLE', 3, 'LOW_QUALITY', '评论交互优化方案内容偏短，作为低质量内容举报样例。', 9, 'tester', 'REJECTED', 2, 'moderator', '内容符合开发样例用途，驳回举报。', '2026-06-04 12:05:00', '2026-06-04 11:40:00', '2026-06-04 12:05:00', 0),
  (8, 'COMMENT', 7, 'OFF_TOPIC', '评论内容偏向 UI 走查，与文章主题有轻微偏离。', 10, 'guest_writer', 'PENDING', NULL, NULL, NULL, NULL, '2026-06-04 12:24:00', '2026-06-04 12:24:00', 0),
  (9, 'PRIVATE_MESSAGE', 5, 'FILE_RISK', 'PDF 附件需要验证下载和预览入口，作为文件风险举报样例。', 5, 'designer', 'RESOLVED', 1, 'admin', '已确认文件为公开 PDF 示例，保留消息。', '2026-06-04 13:15:00', '2026-06-04 12:55:00', '2026-06-04 13:15:00', 0),
  (10, 'ARTICLE', 2, 'MISLEADING', '权限联调记录中的说明可能让新用户误以为生产也能用演示账号。', 4, 'operator', 'PENDING', NULL, NULL, NULL, NULL, '2026-06-04 13:30:00', '2026-06-04 13:30:00', 0);
