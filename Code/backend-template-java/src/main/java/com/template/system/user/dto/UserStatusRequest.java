package com.template.system.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户状态修改请求。
 *
 * @param status 前端状态编码或后端状态值
 */
public record UserStatusRequest(
        @NotBlank(message = "用户状态不能为空")
        String status
) {
}
