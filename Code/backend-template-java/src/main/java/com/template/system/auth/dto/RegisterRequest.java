package com.template.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 前台注册请求参数。
 *
 * @param userName 用户名
 * @param password 密码
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 20) @Pattern(regexp = "^[A-Za-z0-9_]+$") String userName,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank String captchaId,
        @NotBlank @Size(min = 4, max = 4) String captchaCode
) {
}
