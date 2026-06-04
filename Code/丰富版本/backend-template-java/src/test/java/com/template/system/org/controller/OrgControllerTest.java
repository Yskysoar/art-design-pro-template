package com.template.system.org.controller;

import com.template.common.exception.GlobalExceptionHandler;
import com.template.security.auth.JsonAccessDeniedHandler;
import com.template.security.auth.JsonAuthenticationEntryPoint;
import com.template.security.config.SecurityConfig;
import com.template.security.jwt.JwtAuthenticationFilter;
import com.template.security.jwt.JwtTokenService;
import com.template.security.permission.PermissionService;
import com.template.support.web.ControllerSecurityTestSupport;
import com.template.system.org.service.OrgService;
import com.template.system.org.vo.OrgTreeVo;
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
 * 组织管理 Controller 权限入口测试。
 */
@WebMvcTest(controllers = OrgController.class)
@ContextConfiguration(classes = {
        OrgController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ControllerSecurityTestSupport.SecurityTestConfig.class
})
class OrgControllerTest extends ControllerSecurityTestSupport {

    private static final String ORG_MANAGE = "system:org:manage";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private OrgService orgService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("未登录访问组织树应返回 HTTP 401")
    void orgTreeShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/org/tree"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("无组织管理权限访问组织树应返回 HTTP 403")
    void orgTreeShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ORG_MANAGE);

        mockMvc.perform(get("/api/org/tree").header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("有组织管理权限访问组织树应返回组织节点")
    void orgTreeShouldReturnTreeWhenPermissionGranted() throws Exception {
        when(orgService.getOrgTree(ADMIN)).thenReturn(List.of(new OrgTreeVo(1L, 0L, "0", "总部", "ORG_ROOT", "DEPT", 1, true, List.of())));

        mockMvc.perform(get("/api/org/tree").header("Authorization", "Bearer " + token(jwtTokenService, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].orgCode").value("ORG_ROOT"));
    }

    @Test
    @DisplayName("组织写接口应按权限码校验")
    void orgWriteApisShouldCheckPermissionCode() throws Exception {
        String token = token(jwtTokenService, ADMIN);
        String body = "{\"parentId\":0,\"orgName\":\"研发部\",\"orgCode\":\"ORG_RD\",\"orgType\":\"DEPT\",\"sort\":1,\"enabled\":true}";

        mockMvc.perform(post("/api/org")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/org/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/org/2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("组织新增无权限应返回 HTTP 403")
    void createOrgShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ORG_MANAGE);

        mockMvc.perform(post("/api/org")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST))
                        .contentType("application/json")
                        .content("{\"parentId\":0,\"orgName\":\"研发部\",\"orgCode\":\"ORG_RD\",\"orgType\":\"DEPT\",\"sort\":1,\"enabled\":true}"))
                .andExpect(status().isForbidden());
    }
}
