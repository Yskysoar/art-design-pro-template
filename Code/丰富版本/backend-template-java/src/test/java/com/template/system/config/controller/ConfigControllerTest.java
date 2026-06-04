package com.template.system.config.controller;

import com.template.common.exception.GlobalExceptionHandler;
import com.template.common.pagination.PageResult;
import com.template.security.auth.JsonAccessDeniedHandler;
import com.template.security.auth.JsonAuthenticationEntryPoint;
import com.template.security.config.SecurityConfig;
import com.template.security.jwt.JwtAuthenticationFilter;
import com.template.security.jwt.JwtTokenService;
import com.template.security.permission.PermissionService;
import com.template.support.web.ControllerSecurityTestSupport;
import com.template.system.config.dto.ConfigListQuery;
import com.template.system.config.service.ConfigService;
import com.template.system.config.vo.ConfigItemVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 配置项管理 Controller 权限入口测试。
 */
@WebMvcTest(controllers = ConfigController.class)
@ContextConfiguration(classes = {
        ConfigController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ControllerSecurityTestSupport.SecurityTestConfig.class
})
class ConfigControllerTest extends ControllerSecurityTestSupport {

    private static final String CONFIG_MANAGE = "system:config:manage";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private ConfigService configService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("未登录访问配置项列表应返回 HTTP 401")
    void listConfigsShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/config/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("无配置项管理权限访问配置项列表应返回 HTTP 403")
    void listConfigsShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, CONFIG_MANAGE);

        mockMvc.perform(get("/api/config/list").header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("有配置项管理权限访问配置项列表应返回分页数据")
    void listConfigsShouldReturnPageWhenPermissionGranted() throws Exception {
        when(configService.pageConfigs(any(ConfigListQuery.class))).thenReturn(new PageResult<>(
                List.of(new ConfigItemVo(
                        1L,
                        "role_level_enabled",
                        "true",
                        "启用角色层级管理",
                        true,
                        "system",
                        "2026-06-01 09:00:00",
                        "system",
                        "2026-06-01 09:00:00"
                )),
                1,
                20,
                1
        ));

        mockMvc.perform(get("/api/config/list").header("Authorization", "Bearer " + token(jwtTokenService, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].configKey").value("role_level_enabled"));
    }

    @Test
    @DisplayName("配置项写接口应按权限码校验")
    void configWriteApisShouldCheckPermissionCode() throws Exception {
        String token = token(jwtTokenService, ADMIN);
        String body = "{\"configKey\":\"dev_feature_enabled\",\"configValue\":\"true\",\"description\":\"开发功能开关\",\"editable\":true}";

        mockMvc.perform(post("/api/config")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/config/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/config/2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("配置项新增无权限应返回 HTTP 403")
    void createConfigShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, CONFIG_MANAGE);

        mockMvc.perform(post("/api/config")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST))
                        .contentType("application/json")
                        .content("{\"configKey\":\"dev_feature_enabled\",\"configValue\":\"true\",\"description\":\"开发功能开关\",\"editable\":true}"))
                .andExpect(status().isForbidden());
    }
}
