package com.template.system.menu.controller;

import com.template.common.exception.GlobalExceptionHandler;
import com.template.security.auth.JsonAccessDeniedHandler;
import com.template.security.auth.JsonAuthenticationEntryPoint;
import com.template.security.config.SecurityConfig;
import com.template.security.jwt.JwtAuthenticationFilter;
import com.template.security.jwt.JwtTokenService;
import com.template.security.permission.PermissionService;
import com.template.support.web.ControllerSecurityTestSupport;
import com.template.system.menu.service.MenuService;
import com.template.system.menu.vo.AppRouteMetaVo;
import com.template.system.menu.vo.AppRouteVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 菜单管理 Controller 权限入口测试。
 */
@WebMvcTest(controllers = MenuController.class)
@ContextConfiguration(classes = {
        MenuController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ControllerSecurityTestSupport.SecurityTestConfig.class
})
class MenuControllerTest extends ControllerSecurityTestSupport {

    private static final String MENU_MANAGE = "system:menu:manage";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private MenuService menuService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("未登录访问菜单管理树应返回 HTTP 401")
    void manageMenuTreeShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v3/system/menus/manage"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("无菜单管理权限访问菜单管理树应返回 HTTP 403")
    void manageMenuTreeShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, MENU_MANAGE);

        mockMvc.perform(get("/api/v3/system/menus/manage")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("有菜单管理权限访问菜单管理树应返回菜单树")
    void manageMenuTreeShouldReturnRoutesWhenPermissionGranted() throws Exception {
        when(menuService.getManageMenuTree()).thenReturn(List.of(route()));

        mockMvc.perform(get("/api/v3/system/menus/manage")
                        .header("Authorization", "Bearer " + token(jwtTokenService, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("System"));
    }

    @Test
    @DisplayName("菜单写接口应按权限码校验")
    void menuWriteApisShouldCheckPermissionCode() throws Exception {
        String token = token(jwtTokenService, ADMIN);
        String body = "{\"parentId\":0,\"menuType\":\"MENU\",\"path\":\"/system/demo\",\"name\":\"SystemDemo\",\"component\":\"/system/demo\",\"title\":\"演示菜单\",\"accessScope\":\"ADMIN\",\"sort\":1,\"enabled\":true}";

        mockMvc.perform(post("/api/v3/system/menus")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v3/system/menus/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v3/system/menus/2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("菜单新增无权限应返回 HTTP 403")
    void createMenuShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, MENU_MANAGE);

        mockMvc.perform(post("/api/v3/system/menus")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST))
                        .contentType("application/json")
                        .content("{\"parentId\":0,\"menuType\":\"MENU\",\"path\":\"/system/demo\",\"name\":\"SystemDemo\",\"component\":\"/system/demo\",\"title\":\"演示菜单\",\"accessScope\":\"ADMIN\",\"sort\":1,\"enabled\":true}"))
                .andExpect(status().isForbidden());
    }

    private AppRouteVo route() {
        return new AppRouteVo(
                1L,
                0L,
                "/system",
                "System",
                "/system/index",
                null,
                new AppRouteMetaVo("系统管理", "Setting", List.of("R_SUPER"), List.of(), true, false, false, false, null, false, null),
                List.of()
        );
    }
}
