package com.template.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全配置项。
 */
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /**
     * JWT 签名密钥。开发环境可使用默认值，生产环境必须通过环境变量覆盖。
     */
    private String jwtSecret = "change-me-to-a-secure-secret-with-at-least-32-bytes";

    /**
     * accessToken 有效期，单位分钟。
     */
    private long accessTokenMinutes = 30;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }
}
