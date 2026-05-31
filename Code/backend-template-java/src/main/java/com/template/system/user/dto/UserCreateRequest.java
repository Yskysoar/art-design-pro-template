package com.template.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 新增用户请求。
 *
 * @param userName   用户名
 * @param password   初始密码，未传时使用开发期默认值
 * @param nickName   昵称
 * @param userGender 性别
 * @param userPhone  手机号
 * @param userEmail  邮箱
 * @param avatar     头像地址
 * @param status     前端状态编码或后端状态值
 * @param roleCodes  角色编码集合
 */
public record UserCreateRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 50, message = "用户名长度必须在 2 到 50 个字符之间")
        String userName,

        @Size(min = 6, max = 72, message = "密码长度必须在 6 到 72 个字符之间")
        String password,

        @Size(max = 50, message = "昵称不能超过 50 个字符")
        String nickName,

        String userGender,

        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String userPhone,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过 100 个字符")
        String userEmail,

        @Size(max = 255, message = "头像地址不能超过 255 个字符")
        String avatar,

        String status,

        List<String> roleCodes
) {
}
