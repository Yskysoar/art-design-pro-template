package com.template.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 内容举报处理请求。
 *
 * @param status 状态
 * @param remark 处理备注
 */
public record ContentReportHandleRequest(
        @NotBlank @Size(max = 20) String status,
        @Size(max = 500) String remark
) {
}
