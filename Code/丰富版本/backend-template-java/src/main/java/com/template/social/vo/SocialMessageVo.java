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
        Long fileId,
        String fileUrl,
        String fileName,
        Long fileSize,
        String fileContentType,
        boolean mine,
        boolean read,
        String createTime
) {
}
