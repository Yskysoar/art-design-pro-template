package com.template.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.notification.dto.NotificationListQuery;
import com.template.notification.entity.NotificationMessage;
import com.template.notification.mapper.NotificationMessageMapper;
import com.template.notification.service.NotificationService;
import com.template.notification.vo.NotificationItemVo;
import com.template.notification.vo.NotificationUnreadCountVo;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户通知消息服务实现。
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final long NOT_DELETED = 0L;
    private static final String TYPE_FOLLOW = "FOLLOW";
    private static final String TYPE_PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
    private static final String TYPE_COMMENT_REPLY = "COMMENT_REPLY";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NotificationMessageMapper notificationMapper;
    private final SysUserMapper userMapper;

    public NotificationServiceImpl(NotificationMessageMapper notificationMapper, SysUserMapper userMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<NotificationItemVo> pageNotifications(NotificationListQuery query, AppUserPrincipal principal) {
        long current = query.current() == null || query.current() < 1 ? DEFAULT_CURRENT : query.current();
        long size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);
        String noticeType = normalizeNoticeType(query.noticeType());
        IPage<NotificationMessage> page = notificationMapper.selectPage(Page.of(current, size), baseWrapper(principal.userId())
                .eq(StringUtils.hasText(noticeType), NotificationMessage::getNoticeType, noticeType)
                .isNull(Boolean.TRUE.equals(query.unread()), NotificationMessage::getReadTime)
                .orderByDesc(NotificationMessage::getCreateTime)
                .orderByDesc(NotificationMessage::getId));
        Map<Long, SysUser> actors = loadActors(page.getRecords().stream()
                .map(NotificationMessage::getActorId)
                .collect(Collectors.toSet()));
        return new PageResult<>(
                page.getRecords().stream().map(item -> toVo(item, actors.get(item.getActorId()))).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    public NotificationUnreadCountVo unreadCount(AppUserPrincipal principal) {
        Long count = notificationMapper.selectCount(baseWrapper(principal.userId()).isNull(NotificationMessage::getReadTime));
        return new NotificationUnreadCountVo(count == null ? 0L : count);
    }

    @Override
    public void markRead(Long id, AppUserPrincipal principal) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "通知ID不能为空");
        }
        int updated = notificationMapper.update(null, new LambdaUpdateWrapper<NotificationMessage>()
                .set(NotificationMessage::getReadTime, LocalDateTime.now())
                .eq(NotificationMessage::getId, id)
                .eq(NotificationMessage::getRecipientId, principal.userId())
                .eq(NotificationMessage::getDeleted, NOT_DELETED)
                .isNull(NotificationMessage::getReadTime));
        if (updated == 0 && notificationMapper.selectCount(baseWrapper(principal.userId()).eq(NotificationMessage::getId, id)) == 0) {
            throw new BusinessException(ApiCode.NOT_FOUND, "通知不存在");
        }
    }

    @Override
    public void markAllRead(AppUserPrincipal principal) {
        notificationMapper.update(null, new LambdaUpdateWrapper<NotificationMessage>()
                .set(NotificationMessage::getReadTime, LocalDateTime.now())
                .eq(NotificationMessage::getRecipientId, principal.userId())
                .eq(NotificationMessage::getDeleted, NOT_DELETED)
                .isNull(NotificationMessage::getReadTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFollowNotification(Long actorId, Long recipientId) {
        if (isSelfOrMissing(actorId, recipientId)) {
            return;
        }
        SysUser actor = userMapper.selectById(actorId);
        String actorName = displayName(actor, "有人");
        insert(actorId, recipientId, TYPE_FOLLOW, actorName + "关注了你", null, "USER", actorId, "/social/chat");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPrivateMessageNotification(Long actorId, Long recipientId, Long conversationId, String content) {
        if (isSelfOrMissing(actorId, recipientId)) {
            return;
        }
        SysUser actor = userMapper.selectById(actorId);
        String actorName = displayName(actor, "有人");
        insert(
                actorId,
                recipientId,
                TYPE_PRIVATE_MESSAGE,
                actorName + "给你发来一条私信",
                summarize(content, "图片或附件消息"),
                "CONVERSATION",
                conversationId,
                "/social/chat"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCommentReplyNotification(Long actorId, Long recipientId, Long articleId, Long commentId, String content) {
        if (isSelfOrMissing(actorId, recipientId)) {
            return;
        }
        SysUser actor = userMapper.selectById(actorId);
        String actorName = displayName(actor, "有人");
        insert(
                actorId,
                recipientId,
                TYPE_COMMENT_REPLY,
                actorName + "回复了你的评论",
                summarize(content, null),
                "ARTICLE_COMMENT",
                commentId,
                "/article/detail/" + articleId
        );
    }

    private void insert(
            Long actorId,
            Long recipientId,
            String noticeType,
            String title,
            String content,
            String targetType,
            Long targetId,
            String targetUrl
    ) {
        NotificationMessage notification = new NotificationMessage();
        notification.setRecipientId(recipientId);
        notification.setActorId(actorId);
        notification.setNoticeType(noticeType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setTargetUrl(targetUrl);
        notification.setCreateTime(LocalDateTime.now());
        notification.setDeleted(NOT_DELETED);
        notificationMapper.insert(notification);
    }

    private LambdaQueryWrapper<NotificationMessage> baseWrapper(Long recipientId) {
        return new LambdaQueryWrapper<NotificationMessage>()
                .eq(NotificationMessage::getRecipientId, recipientId)
                .eq(NotificationMessage::getDeleted, NOT_DELETED);
    }

    private NotificationItemVo toVo(NotificationMessage item, SysUser actor) {
        return new NotificationItemVo(
                item.getId(),
                item.getNoticeType(),
                item.getTitle(),
                item.getContent(),
                item.getActorId(),
                actor == null ? null : displayName(actor, null),
                actor == null ? null : actor.getAvatar(),
                item.getTargetType(),
                item.getTargetId(),
                item.getTargetUrl(),
                item.getReadTime() != null,
                item.getCreateTime() == null ? null : DATE_TIME_FORMATTER.format(item.getCreateTime())
        );
    }

    private Map<Long, SysUser> loadActors(Set<Long> actorIds) {
        Set<Long> ids = actorIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, ids))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));
    }

    private boolean isSelfOrMissing(Long actorId, Long recipientId) {
        return actorId == null || recipientId == null || actorId.equals(recipientId);
    }

    private String displayName(SysUser user, String fallback) {
        if (user == null) {
            return fallback;
        }
        return StringUtils.hasText(user.getNickName()) ? user.getNickName() : user.getUserName();
    }

    private String summarize(String content, String fallback) {
        String value = StringUtils.hasText(content) ? content.trim() : fallback;
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.length() > 120 ? value.substring(0, 120) : value;
    }

    private String normalizeNoticeType(String noticeType) {
        return StringUtils.hasText(noticeType) ? noticeType.trim().toUpperCase() : null;
    }
}
