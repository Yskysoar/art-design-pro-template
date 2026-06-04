package com.template.article.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 评论敏感词状态请求。
 *
 * @param enabled 启用状态
 */
public record CommentSensitiveWordStatusRequest(@NotNull Integer enabled) {
}
