package com.template.system.auth.vo;

/**
 * 登录响应。
 *
 * @param token        accessToken
 * @param refreshToken 刷新令牌（暂与 accessToken 相同，后续可分配不同有效期）
 */
public record LoginResponse(String token, String refreshToken) {
}