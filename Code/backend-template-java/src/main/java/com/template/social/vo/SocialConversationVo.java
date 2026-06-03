package com.template.social.vo;

/**
 * 社交会话响应。
 */
public record SocialConversationVo(
        Long id,
        SocialUserVo targetUser,
        SocialMessageVo lastMessage,
        long unreadCount,
        int remainingQuota,
        boolean unlimited,
        String lastMessageTime
) {
}
