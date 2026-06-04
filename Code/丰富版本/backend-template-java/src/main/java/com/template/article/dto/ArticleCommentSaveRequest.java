package com.template.article.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 评论保存请求。
 *
 * @param articleId 文章 ID
 * @param parentId  父评论 ID，0 或空表示一级评论
 * @param content   评论内容
 */
public record ArticleCommentSaveRequest(
        @NotNull(message = "文章ID不能为空")
        Long articleId,
        Long parentId,
        String content
) {
}
