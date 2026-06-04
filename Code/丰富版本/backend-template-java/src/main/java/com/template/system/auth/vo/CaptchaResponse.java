package com.template.system.auth.vo;

/**
 * 验证码响应。
 *
 * @param captchaId   验证码 ID
 * @param imageBase64 图片 Base64 数据
 * @param expiresIn   有效秒数
 */
public record CaptchaResponse(String captchaId, String imageBase64, long expiresIn) {
}
