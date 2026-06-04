package com.template.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.common.security.SensitiveWordGuard;
import com.template.file.entity.FileResource;
import com.template.file.service.FileStorageService;
import com.template.notification.service.NotificationService;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.convert.SocialStructMapper;
import com.template.social.dto.SocialMessageQuery;
import com.template.social.dto.SocialMessageSendRequest;
import com.template.social.dto.SocialPageQuery;
import com.template.social.entity.SocialBlock;
import com.template.social.entity.SocialConversation;
import com.template.social.entity.SocialFollow;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialBlockMapper;
import com.template.social.mapper.SocialConversationMapper;
import com.template.social.mapper.SocialFollowMapper;
import com.template.social.mapper.SocialMessageMapper;
import com.template.social.service.SocialService;
import com.template.social.vo.SocialConversationVo;
import com.template.social.vo.SocialMessageVo;
import com.template.social.vo.SocialQuotaVo;
import com.template.social.vo.SocialUnreadCountVo;
import com.template.social.vo.SocialUserVo;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 社交关注与聊天业务服务实现。
 */
@Service
public class SocialServiceImpl implements SocialService {

    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long DEFAULT_MESSAGE_SIZE = 30L;
    private static final long MAX_SIZE = 100L;
    private static final long NOT_DELETED = 0L;
    private static final String USER_NORMAL = "NORMAL";
    private static final String MESSAGE_TYPE_TEXT = "TEXT";
    private static final String MESSAGE_TYPE_IMAGE = "IMAGE";
    private static final String MESSAGE_TYPE_FILE = "FILE";
    private static final long SOCIAL_IMAGE_MAX_SIZE = 5L * 1024 * 1024;
    private static final long SOCIAL_FILE_MAX_SIZE = 50L * 1024 * 1024;
    private static final int WAIT_REPLY_QUOTA = 3;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper userMapper;
    private final SocialFollowMapper followMapper;
    private final SocialBlockMapper blockMapper;
    private final SocialConversationMapper conversationMapper;
    private final SocialMessageMapper messageMapper;
    private final SocialStructMapper socialStructMapper;
    private final SensitiveWordGuard sensitiveWordGuard;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public SocialServiceImpl(
            SysUserMapper userMapper,
            SocialFollowMapper followMapper,
            SocialBlockMapper blockMapper,
            SocialConversationMapper conversationMapper,
            SocialMessageMapper messageMapper,
            SocialStructMapper socialStructMapper,
            SensitiveWordGuard sensitiveWordGuard,
            FileStorageService fileStorageService,
            NotificationService notificationService
    ) {
        this.userMapper = userMapper;
        this.followMapper = followMapper;
        this.blockMapper = blockMapper;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.socialStructMapper = socialStructMapper;
        this.sensitiveWordGuard = sensitiveWordGuard;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Override
    public PageResult<SocialUserVo> searchUsers(SocialPageQuery query, AppUserPrincipal principal) {
        long current = normalizeCurrent(query.getCurrent());
        long size = normalizeSize(query.getSize(), DEFAULT_SIZE);
        LambdaQueryWrapper<SysUser> wrapper = activeUserWrapper()
                .ne(SysUser::getId, principal.userId())
                .orderByDesc(SysUser::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(SysUser::getUserName, keyword)
                    .or()
                    .like(SysUser::getNickName, keyword)
                    .or()
                    .like(SysUser::getUserEmail, keyword));
        }
        IPage<SysUser> page = userMapper.selectPage(Page.of(current, size), wrapper);
        return new PageResult<>(
                page.getRecords().stream().map(user -> toUserVo(user, principal.userId())).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    public SocialUserVo getUserProfile(Long targetUserId, AppUserPrincipal principal) {
        return toUserVo(getActiveUser(targetUserId), principal.userId());
    }

    @Override
    public PageResult<SocialUserVo> pageFollowing(SocialPageQuery query, AppUserPrincipal principal) {
        long current = normalizeCurrent(query.getCurrent());
        long size = normalizeSize(query.getSize(), DEFAULT_SIZE);
        IPage<SocialFollow> page = followMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<SocialFollow>()
                .eq(SocialFollow::getFollowerId, principal.userId())
                .eq(SocialFollow::getDeleted, NOT_DELETED)
                .orderByDesc(SocialFollow::getCreateTime)
                .orderByDesc(SocialFollow::getId));
        return socialUserPage(page, page.getRecords().stream().map(SocialFollow::getFollowingId).toList(), principal.userId());
    }

    @Override
    public PageResult<SocialUserVo> pageFollowers(SocialPageQuery query, AppUserPrincipal principal) {
        long current = normalizeCurrent(query.getCurrent());
        long size = normalizeSize(query.getSize(), DEFAULT_SIZE);
        IPage<SocialFollow> page = followMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<SocialFollow>()
                .eq(SocialFollow::getFollowingId, principal.userId())
                .eq(SocialFollow::getDeleted, NOT_DELETED)
                .orderByDesc(SocialFollow::getCreateTime)
                .orderByDesc(SocialFollow::getId));
        return socialUserPage(page, page.getRecords().stream().map(SocialFollow::getFollowerId).toList(), principal.userId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long targetUserId, AppUserPrincipal principal) {
        Long currentUserId = principal.userId();
        assertNotSelf(currentUserId, targetUserId, "不能关注自己");
        getActiveUser(targetUserId);
        if (isBlockedEither(currentUserId, targetUserId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "已拉黑或被对方拉黑，不能关注");
        }
        if (isFollowing(currentUserId, targetUserId)) {
            return;
        }
        SocialFollow follow = new SocialFollow();
        follow.setFollowerId(currentUserId);
        follow.setFollowingId(targetUserId);
        follow.setDeleted(NOT_DELETED);
        followMapper.insert(follow);
        notificationService.createFollowNotification(currentUserId, targetUserId);
    }

    @Override
    public void unfollow(Long targetUserId, AppUserPrincipal principal) {
        deleteFollow(principal.userId(), targetUserId);
    }

    @Override
    public PageResult<SocialUserVo> pageBlocks(SocialPageQuery query, AppUserPrincipal principal) {
        long current = normalizeCurrent(query.getCurrent());
        long size = normalizeSize(query.getSize(), DEFAULT_SIZE);
        IPage<SocialBlock> page = blockMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<SocialBlock>()
                .eq(SocialBlock::getBlockerId, principal.userId())
                .eq(SocialBlock::getDeleted, NOT_DELETED)
                .orderByDesc(SocialBlock::getCreateTime)
                .orderByDesc(SocialBlock::getId));
        return socialUserPage(page, page.getRecords().stream().map(SocialBlock::getBlockedId).toList(), principal.userId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void block(Long targetUserId, AppUserPrincipal principal) {
        Long currentUserId = principal.userId();
        assertNotSelf(currentUserId, targetUserId, "不能拉黑自己");
        getActiveUser(targetUserId);
        if (!isBlockedBy(currentUserId, targetUserId)) {
            SocialBlock block = new SocialBlock();
            block.setBlockerId(currentUserId);
            block.setBlockedId(targetUserId);
            block.setDeleted(NOT_DELETED);
            blockMapper.insert(block);
        }
        deleteFollow(currentUserId, targetUserId);
        deleteFollow(targetUserId, currentUserId);
    }

    @Override
    public void unblock(Long targetUserId, AppUserPrincipal principal) {
        blockMapper.update(null, new LambdaUpdateWrapper<SocialBlock>()
                .set(SocialBlock::getDeleted, System.currentTimeMillis())
                .eq(SocialBlock::getBlockerId, principal.userId())
                .eq(SocialBlock::getBlockedId, targetUserId)
                .eq(SocialBlock::getDeleted, NOT_DELETED));
    }

    @Override
    public PageResult<SocialConversationVo> pageConversations(SocialPageQuery query, AppUserPrincipal principal) {
        long current = normalizeCurrent(query.getCurrent());
        long size = normalizeSize(query.getSize(), DEFAULT_SIZE);
        Long currentUserId = principal.userId();
        IPage<SocialConversation> page = conversationMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<SocialConversation>()
                .eq(SocialConversation::getDeleted, NOT_DELETED)
                .and(w -> w.eq(SocialConversation::getUserAId, currentUserId)
                        .or()
                        .eq(SocialConversation::getUserBId, currentUserId))
                .orderByDesc(SocialConversation::getLastMessageTime)
                .orderByDesc(SocialConversation::getId));
        return new PageResult<>(
                page.getRecords().stream().map(conversation -> toConversationVo(conversation, currentUserId)).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    public PageResult<SocialMessageVo> pageMessages(Long conversationId, SocialMessageQuery query, AppUserPrincipal principal) {
        SocialConversation conversation = getConversationForUser(conversationId, principal.userId());
        long current = normalizeCurrent(query.getCurrent());
        long size = normalizeSize(query.getSize(), DEFAULT_MESSAGE_SIZE);
        IPage<SocialMessage> page = messageMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getConversationId, conversation.getId())
                .eq(SocialMessage::getDeleted, NOT_DELETED)
                .orderByDesc(SocialMessage::getCreateTime)
                .orderByDesc(SocialMessage::getId));
        Map<Long, SysUser> users = loadUsers(page.getRecords().stream()
                .map(SocialMessage::getSenderId)
                .collect(Collectors.toSet()));
        return new PageResult<>(
                page.getRecords().stream().map(message -> toMessageVo(message, users.get(message.getSenderId()), principal.userId())).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SocialMessageVo sendMessage(SocialMessageSendRequest request, AppUserPrincipal principal) {
        Long senderId = principal.userId();
        Long receiverId = request.receiverId();
        assertNotSelf(senderId, receiverId, "不能给自己发送私信");
        getActiveUser(receiverId);
        assertMessageAllowed(senderId, receiverId);
        String messageType = normalizeMessageType(request.messageType());
        String content = null;
        FileResource file = null;
        if (MESSAGE_TYPE_TEXT.equals(messageType)) {
            content = normalizeContent(request.content());
            sensitiveWordGuard.validate("私信内容", content);
        } else {
            if (!isMutualFollow(senderId, receiverId)) {
                throw new BusinessException(ApiCode.BAD_REQUEST, "非互关只能发送文本消息");
            }
            file = validateMessageFile(request.fileId(), senderId, messageType);
            content = normalizeOptionalContent(request.content());
        }
        int remainingQuota = remainingQuota(senderId, receiverId);
        if (remainingQuota <= 0) {
            throw new BusinessException(
                    ApiCode.BAD_REQUEST,
                    "对方回复前最多发送3条私信",
                    new SocialQuotaVo(0)
            );
        }

        SocialConversation conversation = getOrCreateConversation(senderId, receiverId);
        LocalDateTime now = LocalDateTime.now();
        SocialMessage message = new SocialMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType);
        if (file != null) {
            message.setFileId(file.getId());
            message.setFileUrl(file.getUrl());
            message.setFileName(file.getOriginalName());
            message.setFileSize(file.getSize());
            message.setFileContentType(file.getContentType());
        }
        message.setCreateTime(now);
        message.setDeleted(NOT_DELETED);
        messageMapper.insert(message);

        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(now);
        conversation.setUpdateTime(now);
        conversationMapper.updateById(conversation);
        notificationService.createPrivateMessageNotification(senderId, receiverId, conversation.getId(), content);
        return toMessageVo(message, getActiveUser(senderId), senderId);
    }

    @Override
    public void markConversationRead(Long conversationId, AppUserPrincipal principal) {
        getConversationForUser(conversationId, principal.userId());
        messageMapper.update(null, new LambdaUpdateWrapper<SocialMessage>()
                .set(SocialMessage::getReadTime, LocalDateTime.now())
                .eq(SocialMessage::getConversationId, conversationId)
                .eq(SocialMessage::getReceiverId, principal.userId())
                .isNull(SocialMessage::getReadTime)
                .eq(SocialMessage::getDeleted, NOT_DELETED));
    }

    @Override
    public SocialUnreadCountVo unreadCount(AppUserPrincipal principal) {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getReceiverId, principal.userId())
                .isNull(SocialMessage::getReadTime)
                .eq(SocialMessage::getDeleted, NOT_DELETED));
        return new SocialUnreadCountVo(count == null ? 0 : count);
    }

    private PageResult<SocialUserVo> socialUserPage(IPage<?> page, List<Long> userIds, Long currentUserId) {
        Map<Long, SysUser> userMap = loadUsers(Set.copyOf(userIds));
        return new PageResult<>(
                userIds.stream()
                        .map(userMap::get)
                        .filter(Objects::nonNull)
                        .map(user -> toUserVo(user, currentUserId))
                        .toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    private SocialUserVo toUserVo(SysUser user, Long currentUserId) {
        boolean following = isFollowing(currentUserId, user.getId());
        boolean followedBy = isFollowing(user.getId(), currentUserId);
        return socialStructMapper.toUserVo(
                user,
                displayName(user),
                following,
                followedBy,
                following && followedBy,
                isBlockedBy(currentUserId, user.getId()),
                isBlockedBy(user.getId(), currentUserId)
        );
    }

    private SocialConversationVo toConversationVo(SocialConversation conversation, Long currentUserId) {
        Long targetUserId = conversation.getUserAId().equals(currentUserId) ? conversation.getUserBId() : conversation.getUserAId();
        SysUser targetUser = getActiveUser(targetUserId);
        SocialMessage lastMessage = conversation.getLastMessageId() == null
                ? null
                : messageMapper.selectById(conversation.getLastMessageId());
        Long unread = messageMapper.selectCount(new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getConversationId, conversation.getId())
                .eq(SocialMessage::getReceiverId, currentUserId)
                .isNull(SocialMessage::getReadTime)
                .eq(SocialMessage::getDeleted, NOT_DELETED));
        boolean unlimited = isMutualFollow(currentUserId, targetUserId);
        return new SocialConversationVo(
                conversation.getId(),
                toUserVo(targetUser, currentUserId),
                lastMessage == null ? null : toMessageVo(lastMessage, loadUser(lastMessage.getSenderId()), currentUserId),
                unread == null ? 0 : unread,
                unlimited ? WAIT_REPLY_QUOTA : remainingQuota(currentUserId, targetUserId),
                unlimited,
                formatTime(conversation.getLastMessageTime())
        );
    }

    private SocialMessageVo toMessageVo(SocialMessage message, SysUser sender, Long currentUserId) {
        return socialStructMapper.toMessageVo(
                message,
                sender == null ? "" : displayName(sender),
                sender == null ? null : sender.getAvatar(),
                message.getSenderId().equals(currentUserId),
                message.getReadTime() != null,
                formatTime(message.getCreateTime())
        );
    }

    private SocialConversation getOrCreateConversation(Long userId, Long targetUserId) {
        long userA = Math.min(userId, targetUserId);
        long userB = Math.max(userId, targetUserId);
        SocialConversation conversation = conversationMapper.selectOne(new LambdaQueryWrapper<SocialConversation>()
                .eq(SocialConversation::getUserAId, userA)
                .eq(SocialConversation::getUserBId, userB)
                .eq(SocialConversation::getDeleted, NOT_DELETED));
        if (conversation != null) {
            return conversation;
        }
        conversation = new SocialConversation();
        LocalDateTime now = LocalDateTime.now();
        conversation.setUserAId(userA);
        conversation.setUserBId(userB);
        conversation.setCreateTime(now);
        conversation.setUpdateTime(now);
        conversation.setDeleted(NOT_DELETED);
        conversationMapper.insert(conversation);
        return conversation;
    }

    private SocialConversation getConversationForUser(Long conversationId, Long userId) {
        if (conversationId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "会话ID不能为空");
        }
        SocialConversation conversation = conversationMapper.selectOne(new LambdaQueryWrapper<SocialConversation>()
                .eq(SocialConversation::getId, conversationId)
                .eq(SocialConversation::getDeleted, NOT_DELETED)
                .and(w -> w.eq(SocialConversation::getUserAId, userId)
                        .or()
                        .eq(SocialConversation::getUserBId, userId)));
        if (conversation == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "会话不存在");
        }
        return conversation;
    }

    private int remainingQuota(Long senderId, Long receiverId) {
        if (isMutualFollow(senderId, receiverId)) {
            return WAIT_REPLY_QUOTA;
        }
        SocialMessage lastReceiverReply = messageMapper.selectOne(new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getSenderId, receiverId)
                .eq(SocialMessage::getReceiverId, senderId)
                .eq(SocialMessage::getDeleted, NOT_DELETED)
                .orderByDesc(SocialMessage::getCreateTime)
                .orderByDesc(SocialMessage::getId)
                .last("LIMIT 1"));

        LambdaQueryWrapper<SocialMessage> wrapper = new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getSenderId, senderId)
                .eq(SocialMessage::getReceiverId, receiverId)
                .eq(SocialMessage::getDeleted, NOT_DELETED);
        if (lastReceiverReply != null && lastReceiverReply.getCreateTime() != null) {
            wrapper.gt(SocialMessage::getCreateTime, lastReceiverReply.getCreateTime());
        }
        Long sentAfterReply = messageMapper.selectCount(wrapper);
        int used = sentAfterReply == null ? 0 : sentAfterReply.intValue();
        return Math.max(0, WAIT_REPLY_QUOTA - used);
    }

    private void assertMessageAllowed(Long senderId, Long receiverId) {
        if (isBlockedBy(senderId, receiverId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "你已拉黑对方，不能发送私信");
        }
        if (isBlockedBy(receiverId, senderId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "对方已拉黑你，不能发送私信");
        }
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "消息内容不能为空");
        }
        String normalized = content.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "消息内容不能为空");
        }
        if (normalized.length() > 1000) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "消息内容不能超过1000字");
        }
        return normalized;
    }

    private String normalizeOptionalContent(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String normalized = content.trim();
        if (normalized.length() > 1000) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "消息内容不能超过1000字");
        }
        return normalized;
    }

    private String normalizeMessageType(String messageType) {
        if (!StringUtils.hasText(messageType)) {
            return MESSAGE_TYPE_TEXT;
        }
        String normalized = messageType.trim().toUpperCase();
        if (MESSAGE_TYPE_TEXT.equals(normalized) || MESSAGE_TYPE_IMAGE.equals(normalized) || MESSAGE_TYPE_FILE.equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(ApiCode.BAD_REQUEST, "不支持的消息类型");
    }

    private FileResource validateMessageFile(Long fileId, Long senderId, String messageType) {
        FileResource file = fileStorageService.getExistingFile(fileId);
        if (!senderId.equals(file.getUploaderId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "不能引用他人上传的文件");
        }
        String contentType = file.getContentType();
        if (MESSAGE_TYPE_IMAGE.equals(messageType)) {
            if (file.getSize() == null || file.getSize() > SOCIAL_IMAGE_MAX_SIZE) {
                throw new BusinessException(ApiCode.BAD_REQUEST, "图片大小不能超过5MB");
            }
            if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
                throw new BusinessException(ApiCode.BAD_REQUEST, "图片消息只能引用图片文件");
            }
            return file;
        }
        if (file.getSize() == null || file.getSize() > SOCIAL_FILE_MAX_SIZE) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "附件大小不能超过50MB");
        }
        return file;
    }

    private boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null || followerId.equals(followingId)) {
            return false;
        }
        Long count = followMapper.selectCount(new LambdaQueryWrapper<SocialFollow>()
                .eq(SocialFollow::getFollowerId, followerId)
                .eq(SocialFollow::getFollowingId, followingId)
                .eq(SocialFollow::getDeleted, NOT_DELETED));
        return count != null && count > 0;
    }

    private boolean isMutualFollow(Long userId, Long targetUserId) {
        return isFollowing(userId, targetUserId) && isFollowing(targetUserId, userId);
    }

    private boolean isBlockedBy(Long blockerId, Long blockedId) {
        if (blockerId == null || blockedId == null || blockerId.equals(blockedId)) {
            return false;
        }
        Long count = blockMapper.selectCount(new LambdaQueryWrapper<SocialBlock>()
                .eq(SocialBlock::getBlockerId, blockerId)
                .eq(SocialBlock::getBlockedId, blockedId)
                .eq(SocialBlock::getDeleted, NOT_DELETED));
        return count != null && count > 0;
    }

    private boolean isBlockedEither(Long userId, Long targetUserId) {
        return isBlockedBy(userId, targetUserId) || isBlockedBy(targetUserId, userId);
    }

    private void deleteFollow(Long followerId, Long followingId) {
        followMapper.update(null, new LambdaUpdateWrapper<SocialFollow>()
                .set(SocialFollow::getDeleted, System.currentTimeMillis())
                .eq(SocialFollow::getFollowerId, followerId)
                .eq(SocialFollow::getFollowingId, followingId)
                .eq(SocialFollow::getDeleted, NOT_DELETED));
    }

    private void assertNotSelf(Long currentUserId, Long targetUserId, String message) {
        if (targetUserId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "目标用户不能为空");
        }
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, message);
        }
    }

    private SysUser getActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "用户ID不能为空");
        }
        SysUser user = userMapper.selectOne(activeUserWrapper().eq(SysUser::getId, userId));
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "用户不存在或已禁用");
        }
        return user;
    }

    private SysUser loadUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }

    private Map<Long, SysUser> loadUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUser> users = userMapper.selectList(activeUserWrapper().in(SysUser::getId, userIds));
        Map<Long, SysUser> map = new HashMap<>();
        for (SysUser user : users) {
            map.put(user.getId(), user);
        }
        return map;
    }

    private LambdaQueryWrapper<SysUser> activeUserWrapper() {
        return new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, USER_NORMAL)
                .eq(SysUser::getDeleted, 0);
    }

    private long normalizeCurrent(long current) {
        return current < 1 ? DEFAULT_CURRENT : current;
    }

    private long normalizeSize(long size, long defaultSize) {
        if (size < 1) {
            return defaultSize;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : DATE_TIME_FORMATTER.format(time);
    }

    private String displayName(SysUser user) {
        return StringUtils.hasText(user.getNickName()) ? user.getNickName() : user.getUserName();
    }
}
