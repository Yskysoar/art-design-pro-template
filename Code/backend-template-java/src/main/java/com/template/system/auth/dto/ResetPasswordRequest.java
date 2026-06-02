package com.template.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 开发期找回密码请求参数。
 *
 * @param userName    用户名
 * @param newPassword 新密码
 */
public record ResetPasswordRequest(
        @NotBlank @Size(max = 50) String userName,
        @NotBlank @Size(min = 6, max = 100) String newPassword,
        @NotBlank String captchaId,
        @NotBlank @Size(min = 4, max = 4) String captchaCode
) {
}
