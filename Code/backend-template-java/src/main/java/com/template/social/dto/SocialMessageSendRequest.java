package com.template.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 社交私信发送请求。
 */
public record SocialMessageSendRequest(
        @NotNull(message = "接收用户不能为空")
        Long receiverId,

        @NotBlank(message = "消息内容不能为空")
        @Size(max = 1000, message = "消息内容不能超过1000字")
        String content
) {
}
