package com.template.security.auth;

import java.util.List;

/**
 * 当前登录用户身份。
 *
 * @param userId   用户ID
 * @param userName 用户名
 * @param roles    角色编码
 */
public record AppUserPrincipal(Long userId, String userName, List<String> roles) {
}
