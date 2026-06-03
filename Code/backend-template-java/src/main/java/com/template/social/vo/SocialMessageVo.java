package com.template.social.vo;

/**
 * 社交消息响应。
 */
public record SocialMessageVo(
        Long id,
        Long conversationId,
        Long senderId,
        Long receiverId,
        String senderName,
        String senderAvatar,
        String content,
        String messageType,
        boolean mine,
        boolean read,
        String createTime
) {
}
