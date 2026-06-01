package com.template.article.vo;

/**
 * 文章列表项响应。
 */
public record ArticleListItemVo(
        Long id,
        String title,
        Long categoryId,
        String categoryName,
        String type_name,
        String coverUrl,
        String home_img,
        String summary,
        String status,
        Boolean visible,
        Long viewCount,
        Long count,
        String createBy,
        String createTime,
        String create_time,
        String publishTime
) {
}
