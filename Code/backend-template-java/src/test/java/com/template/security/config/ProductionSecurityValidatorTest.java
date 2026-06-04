package com.template.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 生产环境安全配置启动校验测试。
 */
class ProductionSecurityValidatorTest {

    @Test
    @DisplayName("生产环境应拒绝默认 JWT 密钥")
    void validateJwtSecretShouldRejectDefaultSecret() {
        SecurityProperties properties = new SecurityProperties();
        ProductionSecurityValidator validator = new ProductionSecurityValidator(properties);

        assertThatThrownBy(validator::validateJwtSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("生产环境必须通过 JWT_SECRET 覆盖默认 JWT 密钥");
    }

    @Test
    @DisplayName("生产环境应拒绝过短 JWT 密钥")
    void validateJwtSecretShouldRejectShortSecret() {
        SecurityProperties properties = new SecurityProperties();
        properties.setJwtSecret("short-secret");
        ProductionSecurityValidator validator = new ProductionSecurityValidator(properties);

        assertThatThrownBy(validator::validateJwtSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("生产环境 JWT_SECRET 长度不能少于 32 个字符");
    }

    @Test
    @DisplayName("生产环境应允许合规 JWT 密钥")
    void validateJwtSecretShouldAcceptCustomStrongSecret() {
        SecurityProperties properties = new SecurityProperties();
        properties.setJwtSecret("replace-with-a-real-production-secret-2026");
        ProductionSecurityValidator validator = new ProductionSecurityValidator(properties);

        assertThatCode(validator::validateJwtSecret).doesNotThrowAnyException();
    }
}
