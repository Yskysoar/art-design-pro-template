package com.template.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求参数。
 */
public record LoginRequest(
        @NotBlank @Size(max = 50) String userName,
        @NotBlank @Size(max = 100) String password,
        @NotBlank String captchaId,
        @NotBlank @Size(min = 4, max = 4) String captchaCode
) {
}
