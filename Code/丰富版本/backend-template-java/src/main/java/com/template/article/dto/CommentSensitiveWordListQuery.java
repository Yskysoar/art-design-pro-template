package com.template.article.dto;

/**
 * 评论敏感词查询参数。
 *
 * @param current 当前页
 * @param size    每页数量
 * @param word    敏感词关键词
 * @param enabled 启用状态
 */
public record CommentSensitiveWordListQuery(Long current, Long size, String word, Integer enabled) {
}
