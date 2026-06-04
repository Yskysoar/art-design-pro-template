package com.template.social.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 社交私信发送请求。
 */
public record SocialMessageSendRequest(
        @NotNull(message = "接收用户不能为空")
        Long receiverId,

        /**
         * 消息类型，取值：TEXT、IMAGE、FILE。
         */
        String messageType,

        /**
         * 文本内容；图片和附件消息可为空。
         */
        @Size(max = 1000, message = "消息内容不能超过1000字")
        String content,

        /**
         * 图片或附件消息关联的文件 ID。
         */
        Long fileId
) {
}
