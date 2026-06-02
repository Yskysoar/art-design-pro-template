package com.template.article.vo;

/**
 * 评论敏感词响应。
 */
public record CommentSensitiveWordVo(
        Long id,
        String word,
        String matchType,
        Integer enabled,
        String remark,
        String createTime,
        String updateTime
) {
}
