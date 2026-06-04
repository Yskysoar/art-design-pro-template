-- 社交关注、聊天、图片和附件 Mock 数据。

INSERT INTO file_resource (
  id, original_name, storage_name, storage_path, url, content_type,
  extension, size, sha256, storage_type, uploader_id, create_by
)
VALUES
  (20, '设计稿-首屏卡片.webp', 'social-hero-card.webp', 'social/2026/06/04/social-hero-card.webp', '/src/assets/images/cover/img1.webp', 'image/webp', 'webp', 286000, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'LOCAL', 5, 'designer'),
  (21, '交互走查清单.pdf', 'social-interaction-checklist.pdf', 'social/2026/06/04/social-interaction-checklist.pdf', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'application/pdf', 'pdf', 132640, 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb', 'LOCAL', 1, 'admin'),
  (22, '摄影棚灯光参考.webp', 'social-lighting-reference.webp', 'social/2026/06/04/social-lighting-reference.webp', '/src/assets/images/cover/img2.webp', 'image/webp', 'webp', 312000, 'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc', 'LOCAL', 7, 'photographer'),
  (23, '素材命名规范.txt', 'social-asset-naming.txt', 'social/2026/06/04/social-asset-naming.txt', 'https://www.w3.org/TR/PNG/iso_8859-1.txt', 'text/plain', 'txt', 6120, 'dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd', 'LOCAL', 7, 'photographer'),
  (24, '产品页信息架构.webp', 'social-product-ia.webp', 'social/2026/06/04/social-product-ia.webp', '/src/assets/images/cover/img3.webp', 'image/webp', 'webp', 198000, 'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee', 'LOCAL', 8, 'producter'),
  (25, '作者投稿说明.docx', 'social-writer-guide.docx', 'social/2026/06/04/social-writer-guide.docx', 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/file-sample_100kB.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'docx', 102400, 'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff', 'LOCAL', 10, 'guest_writer'),
  (26, '运营排期表.xlsx', 'social-operation-calendar.xlsx', 'social/2026/06/04/social-operation-calendar.xlsx', 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/file_example_XLSX_10.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'xlsx', 13840, '1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef', 'LOCAL', 4, 'operator'),
  (27, '移动端验收截图.webp', 'social-mobile-check.webp', 'social/2026/06/04/social-mobile-check.webp', '/src/assets/images/cover/img4.webp', 'image/webp', 'webp', 225000, 'abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd', 'LOCAL', 9, 'tester');

INSERT INTO social_follow (id, follower_id, following_id, create_time, deleted)
VALUES
  (1, 1, 5, '2026-06-01 09:00:00', 0),
  (2, 5, 1, '2026-06-01 09:05:00', 0),
  (3, 1, 7, '2026-06-01 09:30:00', 0),
  (4, 7, 1, '2026-06-01 09:35:00', 0),
  (5, 3, 1, '2026-06-01 10:00:00', 0),
  (6, 1, 8, '2026-06-01 10:40:00', 0),
  (7, 10, 3, '2026-06-01 13:00:00', 0),
  (8, 3, 10, '2026-06-01 13:05:00', 0),
  (9, 4, 2, '2026-06-01 15:00:00', 0);

INSERT INTO social_block (id, blocker_id, blocked_id, create_time, deleted)
VALUES
  (1, 1, 9, '2026-06-01 12:00:00', 0),
  (2, 2, 6, '2026-06-01 12:30:00', 0);

INSERT INTO social_conversation (
  id, user_a_id, user_b_id, last_message_id, last_message_time, create_time, update_time, deleted
)
VALUES
  (1, 1, 5, 6, '2026-06-01 09:28:00', '2026-06-01 09:10:00', '2026-06-01 09:28:00', 0),
  (2, 1, 3, 9, '2026-06-01 10:12:00', '2026-06-01 10:00:00', '2026-06-01 10:12:00', 0),
  (3, 1, 7, 13, '2026-06-01 10:02:00', '2026-06-01 09:40:00', '2026-06-01 10:02:00', 0),
  (4, 1, 8, 15, '2026-06-01 10:55:00', '2026-06-01 10:45:00', '2026-06-01 10:55:00', 0),
  (5, 3, 10, 17, '2026-06-01 13:20:00', '2026-06-01 13:10:00', '2026-06-01 13:20:00', 0),
  (6, 2, 4, 19, '2026-06-01 15:20:00', '2026-06-01 15:05:00', '2026-06-01 15:20:00', 0);

INSERT INTO social_message (
  id, conversation_id, sender_id, receiver_id, content, message_type,
  file_id, file_url, file_name, file_size, file_content_type,
  read_time, create_time, deleted
)
VALUES
  (1, 1, 1, 5, '欢迎加入设计社群，这里可以直接发送图片、附件和 emoji。', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 09:12:00', '2026-06-01 09:10:00', 0),
  (2, 1, 5, 1, '收到，我先发一张首屏卡片草图给你看。😀', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 09:14:00', '2026-06-01 09:13:00', 0),
  (3, 1, 5, 1, NULL, 'IMAGE', 20, '/src/assets/images/cover/img1.webp', '设计稿-首屏卡片.webp', 286000, 'image/webp', '2026-06-01 09:18:00', '2026-06-01 09:16:00', 0),
  (4, 1, 1, 5, '图片风格可以，注意按钮和卡片边距统一。', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 09:24:00', '2026-06-01 09:20:00', 0),
  (5, 1, 1, 5, NULL, 'FILE', 21, 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', '交互走查清单.pdf', 132640, 'application/pdf', '2026-06-01 09:30:00', '2026-06-01 09:25:00', 0),
  (6, 1, 5, 1, '清单我下载了，下午按这个顺序走查。', 'TEXT', NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-01 09:28:00', 0),

  (7, 2, 3, 1, '管理员你好，我提交了第一篇文章。', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:03:00', '2026-06-01 10:00:00', 0),
  (8, 2, 3, 1, '想确认一下审核节奏。', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:08:00', '2026-06-01 10:05:00', 0),
  (9, 2, 3, 1, '如果需要修改我可以马上处理。', 'TEXT', NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:12:00', 0),

  (10, 3, 7, 1, '我拍了一组界面素材，可用于文章封面。', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 09:45:00', '2026-06-01 09:40:00', 0),
  (11, 3, 7, 1, NULL, 'IMAGE', 22, '/src/assets/images/cover/img2.webp', '摄影棚灯光参考.webp', 312000, 'image/webp', '2026-06-01 09:52:00', '2026-06-01 09:48:00', 0),
  (12, 3, 7, 1, NULL, 'FILE', 23, 'https://www.w3.org/TR/PNG/iso_8859-1.txt', '素材命名规范.txt', 6120, 'text/plain', '2026-06-01 10:01:00', '2026-06-01 09:55:00', 0),
  (13, 3, 1, 7, '很好，素材名保持英文短横线，后续上传更稳。', 'TEXT', NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:02:00', 0),

  (14, 4, 8, 1, '产品信息架构已经调整，这里先发文本说明。', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:50:00', '2026-06-01 10:45:00', 0),
  (15, 4, 8, 1, '我们还不是互关，图片按钮应该禁用，只能继续文字沟通。', 'TEXT', NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:55:00', 0),

  (16, 5, 10, 3, '投稿说明我整理好了，编辑老师帮忙看一下。', 'TEXT', NULL, NULL, NULL, NULL, NULL, '2026-06-01 13:18:00', '2026-06-01 13:10:00', 0),
  (17, 5, 10, 3, NULL, 'FILE', 25, 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/file-sample_100kB.docx', '作者投稿说明.docx', 102400, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', NULL, '2026-06-01 13:20:00', 0),

  (18, 6, 4, 2, NULL, 'FILE', 26, 'https://file-examples.com/storage/fe9b2df2d66640a7b0f53e8/2017/02/file_example_XLSX_10.xlsx', '运营排期表.xlsx', 13840, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', '2026-06-01 15:15:00', '2026-06-01 15:05:00', 0),
  (19, 6, 2, 4, '收到，今天下午一起确认发布计划。', 'TEXT', NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-01 15:20:00', 0);
