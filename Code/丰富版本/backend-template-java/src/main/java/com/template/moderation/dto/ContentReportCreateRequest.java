package com.template.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 内容举报创建请求。
 *
 * @param targetType  目标类型
 * @param targetId    目标 ID
 * @param reasonType  原因类型
 * @param description 补充说明
 */
public record ContentReportCreateRequest(
        @NotBlank @Size(max = 30) String targetType,
        @NotNull Long targetId,
        @NotBlank @Size(max = 50) String reasonType,
        @Size(max = 500) String description
) {
}
