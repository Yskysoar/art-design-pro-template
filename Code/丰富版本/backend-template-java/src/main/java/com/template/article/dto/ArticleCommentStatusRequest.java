package com.template.article.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 评论状态更新请求。
 *
 * @param status 评论状态：NORMAL/HIDDEN/DELETED
 */
public record ArticleCommentStatusRequest(
        @NotBlank(message = "评论状态不能为空")
        String status
) {
}
