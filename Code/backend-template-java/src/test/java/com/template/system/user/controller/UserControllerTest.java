package com.template.system.user.controller;

import com.template.common.exception.BusinessException;
import com.template.common.exception.GlobalExceptionHandler;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.auth.JsonAccessDeniedHandler;
import com.template.security.auth.JsonAuthenticationEntryPoint;
import com.template.security.config.SecurityConfig;
import com.template.security.jwt.JwtAuthenticationFilter;
import com.template.security.jwt.JwtTokenService;
import com.template.security.permission.PermissionService;
import com.template.system.auth.service.AuthService;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.service.UserService;
import com.template.system.user.vo.UserListItemVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理 Controller 权限入口测试。
 */
@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = {
        UserController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("未登录访问用户列表应返回 HTTP 401")
    void listUsersShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("无用户查看权限访问用户列表应返回 HTTP 403")
    void listUsersShouldReturnForbiddenWithoutPermission() throws Exception {
        AppUserPrincipal principal = new AppUserPrincipal(2L, "guest", List.of("R_GUEST"));
        doThrow(new BusinessException(ApiCode.FORBIDDEN, "无权限访问：system:user:view"))
                .when(permissionService)
                .requirePermission(eq(principal), eq("system:user:view"));

        mockMvc.perform(get("/api/user/list").header("Authorization", "Bearer " + token(principal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.msg").value("无权限访问：system:user:view"));
    }

    @Test
    @DisplayName("拥有用户查看权限访问用户列表应返回分页数据")
    void listUsersShouldReturnPageWhenPermissionGranted() throws Exception {
        AppUserPrincipal principal = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));
        when(userService.pageUsers(any(UserListQuery.class))).thenReturn(new PageResult<>(
                List.of(new UserListItemVo(
                        1L,
                        "/avatar.webp",
                        "1",
                        "admin",
                        "女",
                        "admin",
                        "13800000010",
                        "admin@example.com",
                        List.of("R_SUPER"),
                        List.of(1L),
                        "system",
                        "2026-06-01 09:00:00",
                        "system",
                        "2026-06-01 09:00:00"
                )),
                1,
                20,
                1
        ));

        mockMvc.perform(get("/api/user/list").header("Authorization", "Bearer " + token(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].userName").value("admin"));
    }

    private String token(AppUserPrincipal principal) {
        return jwtTokenService.createAccessToken(principal.userId(), principal.userName(), principal.roles());
    }
}
