package com.template.article.dto;

/**
 * 文章评论列表查询参数。
 *
 * @param articleId 文章 ID
 * @param current   当前页
 * @param size      每页数量
 */
public record ArticleCommentListQuery(Long articleId, Long current, Long size) {
}
