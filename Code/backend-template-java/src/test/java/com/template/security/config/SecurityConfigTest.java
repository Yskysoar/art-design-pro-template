package com.template.security.config;

import com.template.security.auth.JsonAccessDeniedHandler;
import com.template.security.auth.JsonAuthenticationEntryPoint;
import com.template.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Spring Security 配置测试。
 */
class SecurityConfigTest {

    @Test
    @DisplayName("CORS 配置应从安全配置项生成")
    void corsConfigurationSourceShouldUseSecurityProperties() {
        SecurityProperties properties = new SecurityProperties();
        SecurityProperties.Cors cors = new SecurityProperties.Cors();
        cors.setAllowedOrigins(List.of("https://admin.example.com"));
        cors.setAllowedMethods(List.of("GET", "POST"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowCredentials(false);
        cors.setMaxAgeSeconds(1800);
        properties.setCors(cors);
        SecurityConfig securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(JsonAuthenticationEntryPoint.class),
                mock(JsonAccessDeniedHandler.class),
                properties
        );

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/health");
        CorsConfiguration configuration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("https://admin.example.com");
        assertThat(configuration.getAllowedMethods()).containsExactly("GET", "POST");
        assertThat(configuration.getAllowedHeaders()).containsExactly("Authorization", "Content-Type");
        assertThat(configuration.getAllowCredentials()).isFalse();
        assertThat(configuration.getMaxAge()).isEqualTo(1800);
    }
}
