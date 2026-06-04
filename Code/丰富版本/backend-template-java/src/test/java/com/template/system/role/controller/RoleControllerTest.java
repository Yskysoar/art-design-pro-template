package com.template.system.role.controller;

import com.template.common.exception.GlobalExceptionHandler;
import com.template.security.auth.JsonAccessDeniedHandler;
import com.template.security.auth.JsonAuthenticationEntryPoint;
import com.template.security.config.SecurityConfig;
import com.template.security.jwt.JwtAuthenticationFilter;
import com.template.common.pagination.PageResult;
import com.template.security.jwt.JwtTokenService;
import com.template.security.permission.PermissionService;
import com.template.support.web.ControllerSecurityTestSupport;
import com.template.system.role.dto.RoleListQuery;
import com.template.system.role.service.RoleService;
import com.template.system.role.vo.RoleDataScopeVo;
import com.template.system.role.vo.RoleListItemVo;
import com.template.system.role.vo.RolePermissionVo;
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
 * 角色管理 Controller 权限入口测试。
 */
@WebMvcTest(controllers = RoleController.class)
@ContextConfiguration(classes = {
        RoleController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ControllerSecurityTestSupport.SecurityTestConfig.class
})
class RoleControllerTest extends ControllerSecurityTestSupport {

    private static final String ROLE_VIEW = "system:role:view";
    private static final String ROLE_ADD = "system:role:add";
    private static final String ROLE_EDIT = "system:role:edit";
    private static final String ROLE_PERMISSION = "system:role:permission";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("未登录访问角色列表应返回 HTTP 401")
    void listRolesShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/role/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("无角色查看权限访问角色列表应返回 HTTP 403")
    void listRolesShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ROLE_VIEW);

        mockMvc.perform(get("/api/role/list").header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("有角色查看权限访问角色列表应返回分页数据")
    void listRolesShouldReturnPageWhenPermissionGranted() throws Exception {
        when(roleService.pageRoles(any(RoleListQuery.class))).thenReturn(new PageResult<>(
                List.of(new RoleListItemVo(1L, "超级管理员", "R_SUPER", "全部权限", true, "2026-06-01 09:00:00")),
                1,
                20,
                1
        ));

        mockMvc.perform(get("/api/role/list").header("Authorization", "Bearer " + token(jwtTokenService, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].roleCode").value("R_SUPER"));
    }

    @Test
    @DisplayName("角色写接口应按权限码校验")
    void roleWriteApisShouldCheckPermissionCodes() throws Exception {
        String token = token(jwtTokenService, ADMIN);

        mockMvc.perform(post("/api/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"roleName\":\"业务管理员\",\"roleCode\":\"R_BIZ\",\"description\":\"业务角色\",\"enabled\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/role/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"roleName\":\"业务管理员\",\"roleCode\":\"R_BIZ\",\"description\":\"业务角色\",\"enabled\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/role/2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("角色授权接口应按权限码校验")
    void rolePermissionApisShouldCheckPermissionCode() throws Exception {
        when(roleService.getRolePermissions(1L)).thenReturn(new RolePermissionVo(1L, List.of(1L, 2L)));
        when(roleService.getRoleDataScope(1L)).thenReturn(new RoleDataScopeVo(1L, "ALL", List.of()));
        String token = token(jwtTokenService, ADMIN);

        mockMvc.perform(get("/api/role/1/permissions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuIds[0]").value(1));

        mockMvc.perform(put("/api/role/1/permissions")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"menuIds\":[1,2]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/role/1/data-scope").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataScope").value("ALL"));

        mockMvc.perform(put("/api/role/1/data-scope")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"dataScope\":\"ALL\",\"orgIds\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("角色新增无权限应返回 HTTP 403")
    void createRoleShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ROLE_ADD);

        mockMvc.perform(post("/api/role")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST))
                        .contentType("application/json")
                        .content("{\"roleName\":\"业务管理员\",\"roleCode\":\"R_BIZ\",\"description\":\"业务角色\",\"enabled\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("角色删除无权限应返回 HTTP 403")
    void deleteRoleShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ROLE_EDIT);

        mockMvc.perform(delete("/api/role/2").header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("角色授权无权限应返回 HTTP 403")
    void saveRolePermissionsShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ROLE_PERMISSION);

        mockMvc.perform(put("/api/role/1/permissions")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST))
                        .contentType("application/json")
                        .content("{\"menuIds\":[1]}"))
                .andExpect(status().isForbidden());
    }
}
