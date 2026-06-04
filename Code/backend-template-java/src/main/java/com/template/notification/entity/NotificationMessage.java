package com.template.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户通知消息实体。
 */
@Getter
@Setter
@TableName("notification_message")
public class NotificationMessage {

    /** 通知主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收人用户 ID。 */
    private Long recipientId;

    /** 触发人用户 ID，系统通知可为空。 */
    private Long actorId;

    /** 通知类型：SYSTEM/FOLLOW/PRIVATE_MESSAGE/COMMENT_REPLY。 */
    private String noticeType;

    /** 通知标题。 */
    private String title;

    /** 通知正文摘要。 */
    private String content;

    /** 跳转目标类型。 */
    private String targetType;

    /** 跳转目标 ID。 */
    private Long targetId;

    /** 跳转 URL。 */
    private String targetUrl;

    /** 已读时间，空表示未读。 */
    private LocalDateTime readTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 逻辑删除标记。 */
    private Long deleted;
}
