package com.template.notification.service;

import com.template.common.pagination.PageResult;
import com.template.notification.dto.NotificationListQuery;
import com.template.notification.vo.NotificationItemVo;
import com.template.notification.vo.NotificationUnreadCountVo;
import com.template.security.auth.AppUserPrincipal;

/**
 * 用户通知消息服务。
 */
public interface NotificationService {

    PageResult<NotificationItemVo> pageNotifications(NotificationListQuery query, AppUserPrincipal principal);

    NotificationUnreadCountVo unreadCount(AppUserPrincipal principal);

    void markRead(Long id, AppUserPrincipal principal);

    void markAllRead(AppUserPrincipal principal);

    void createFollowNotification(Long actorId, Long recipientId);

    void createPrivateMessageNotification(Long actorId, Long recipientId, Long conversationId, String content);

    void createCommentReplyNotification(Long actorId, Long recipientId, Long articleId, Long commentId, String content);
}
