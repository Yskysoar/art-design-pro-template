package com.template.system.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 当前登录用户修改密码请求。
 *
 * @param oldPassword 当前密码
 * @param newPassword 新密码
 */
public record UserPasswordChangeRequest(
        @NotBlank @Size(min = 6, max = 100) String oldPassword,
        @NotBlank @Size(min = 6, max = 100) String newPassword
) {
}
