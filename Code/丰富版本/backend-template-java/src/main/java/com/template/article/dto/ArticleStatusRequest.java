package com.template.article.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 文章状态更新请求。
 *
 * @param status 文章状态
 */
public record ArticleStatusRequest(
        @NotBlank(message = "文章状态不能为空")
        String status
) {
}
