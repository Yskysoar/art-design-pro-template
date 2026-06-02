package com.template.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 评论敏感词保存请求。
 *
 * @param word    敏感词内容
 * @param enabled 启用状态
 * @param remark  备注
 */
public record CommentSensitiveWordSaveRequest(
        @NotBlank @Size(max = 100) String word,
        Integer enabled,
        @Size(max = 255) String remark
) {
}
