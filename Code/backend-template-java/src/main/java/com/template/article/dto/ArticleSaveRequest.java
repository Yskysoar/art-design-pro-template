package com.template.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 文章保存请求。
 *
 * @param title         文章标题
 * @param categoryId    分类 ID
 * @param coverUrl      封面 URL
 * @param summary       摘要
 * @param contentHtml   富文本正文
 * @param visible       是否可见
 * @param status        文章状态
 * @param attachmentIds 附件文件 ID 集合
 */
public record ArticleSaveRequest(
        @NotBlank(message = "文章标题不能为空")
        @Size(max = 120, message = "文章标题不能超过 120 个字符")
        String title,

        @NotNull(message = "文章分类不能为空")
        Long categoryId,

        @Size(max = 600, message = "封面地址不能超过 600 个字符")
        String coverUrl,

        @Size(max = 300, message = "摘要不能超过 300 个字符")
        String summary,

        @NotBlank(message = "文章内容不能为空")
        String contentHtml,

        Boolean visible,

        String status,

        List<Long> attachmentIds
) {
}
