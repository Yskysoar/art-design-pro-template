package com.template.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 当前用户更新个人资料请求。
 */
public record ProfileUpdateRequest(

        @Size(max = 50, message = "昵称不能超过 50 个字符")
        String nickName,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过 100 个字符")
        String userEmail,

        @Size(max = 255, message = "头像地址不能超过 255 个字符")
        String avatar
) {
}
