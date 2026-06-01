package com.template.article.vo;

/**
 * 文章附件响应。
 *
 * @param id           文件 ID
 * @param originalName 原始文件名
 * @param url          访问 URL
 * @param size         文件大小
 * @param contentType  MIME 类型
 */
public record ArticleAttachmentVo(
        Long id,
        String originalName,
        String url,
        Long size,
        String contentType
) {
}
