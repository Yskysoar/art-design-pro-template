package com.template.notification.vo;

/**
 * 通知列表行数据。
 *
 * @param id          通知 ID
 * @param noticeType  通知类型
 * @param title       标题
 * @param content     内容摘要
 * @param actorId     触发人 ID
 * @param actorName   触发人显示名
 * @param actorAvatar 触发人头像
 * @param targetType  目标类型
 * @param targetId    目标 ID
 * @param targetUrl   目标 URL
 * @param read        是否已读
 * @param createTime  创建时间
 */
public record NotificationItemVo(
        Long id,
        String noticeType,
        String title,
        String content,
        Long actorId,
        String actorName,
        String actorAvatar,
        String targetType,
        Long targetId,
        String targetUrl,
        boolean read,
        String createTime
) {
}
