package com.template.article.vo;

import java.util.List;

/**
 * 文章详情响应。
 */
public record ArticleDetailVo(
        Long id,
        String title,
        Long categoryId,
        String categoryName,
        String blog_class,
        String coverUrl,
        String summary,
        String contentHtml,
        String html_content,
        Boolean visible,
        String status,
        Long viewCount,
        Long commentCount,
        Long rootCommentCount,
        Long replyCount,
        String createBy,
        String createTime,
        String publishTime,
        List<ArticleAttachmentVo> attachments
) {
}
