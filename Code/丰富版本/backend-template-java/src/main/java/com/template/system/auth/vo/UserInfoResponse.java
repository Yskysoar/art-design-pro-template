package com.template.system.auth.vo;

import java.util.List;

/**
 * 当前用户信息响应。
 */
public record UserInfoResponse(
        Long userId,
        String userName,
        String email,
        String avatar,
        List<String> roles,
        List<String> buttons
) {
}
