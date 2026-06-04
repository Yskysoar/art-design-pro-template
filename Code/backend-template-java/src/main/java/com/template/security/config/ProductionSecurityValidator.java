package com.template.security.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 生产环境安全配置启动校验。
 */
@Component
@Profile("prod")
public class ProductionSecurityValidator implements ApplicationRunner {

    static final String DEFAULT_JWT_SECRET = "change-me-to-a-secure-secret-with-at-least-32-bytes";
    private static final int MIN_JWT_SECRET_LENGTH = 32;

    private final SecurityProperties securityProperties;

    public ProductionSecurityValidator(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateJwtSecret();
    }

    void validateJwtSecret() {
        String jwtSecret = securityProperties.getJwtSecret();
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("生产环境 JWT_SECRET 不能为空");
        }
        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("生产环境必须通过 JWT_SECRET 覆盖默认 JWT 密钥");
        }
        if (jwtSecret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException("生产环境 JWT_SECRET 长度不能少于 32 个字符");
        }
    }
}
