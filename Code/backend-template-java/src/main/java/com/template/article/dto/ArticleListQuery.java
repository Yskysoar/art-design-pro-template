package com.template.article.dto;

/**
 * 文章分页查询参数。
 *
 * @param current    当前页码
 * @param size       每页条数
 * @param title      标题关键字
 * @param categoryId 分类 ID
 * @param status     文章状态
 * @param year       创建年份
 */
public record ArticleListQuery(
        Long current,
        Long size,
        String title,
        Long categoryId,
        String status,
        Integer year
) {
}
