package com.template.system.auth.service;

import com.template.system.auth.vo.CaptchaResponse;

/**
 * 图形验证码服务。
 */
public interface CaptchaService {

    /**
     * 生成图形验证码。
     *
     * @return 验证码图片和 ID
     */
    CaptchaResponse generate();

    /**
     * 校验并消费验证码。
     *
     * @param captchaId   验证码 ID
     * @param captchaCode 用户输入验证码
     */
    void validateAndConsume(String captchaId, String captchaCode);
}
