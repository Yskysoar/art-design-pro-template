package com.template.article.vo;

/**
 * 文章分类响应。
 *
 * @param id   分类 ID
 * @param name 分类名称，兼容当前前端
 */
public record ArticleCategoryVo(Long id, String name) {
}
