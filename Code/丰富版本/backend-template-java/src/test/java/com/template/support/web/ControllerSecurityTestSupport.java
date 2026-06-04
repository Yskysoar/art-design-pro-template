package com.template.support.web;

import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.config.SecurityProperties;
import com.template.security.jwt.JwtTokenService;
import com.template.security.permission.PermissionService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.mockito.Mockito.doThrow;

/**
 * Controller 权限入口测试通用配置和断言辅助。
 */
public abstract class ControllerSecurityTestSupport {

    protected static final AppUserPrincipal ADMIN =
            new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));
    protected static final AppUserPrincipal GUEST =
            new AppUserPrincipal(2L, "guest", List.of("R_GUEST"));

    @TestConfiguration
    public static class SecurityTestConfig {

        @Bean
        SecurityProperties securityProperties() {
            SecurityProperties properties = new SecurityProperties();
            properties.setJwtSecret("change-me-to-a-secure-secret-with-at-least-32-bytes");
            properties.setAccessTokenMinutes(30);
            return properties;
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    protected String token(JwtTokenService jwtTokenService, AppUserPrincipal principal) {
        return jwtTokenService.createAccessToken(principal.userId(), principal.userName(), principal.roles());
    }

    protected void deny(PermissionService permissionService, AppUserPrincipal principal, String permissionCode) {
        doThrow(new BusinessException(ApiCode.FORBIDDEN, "无权限访问：" + permissionCode))
                .when(permissionService)
                .requirePermission(principal, permissionCode);
    }
}
