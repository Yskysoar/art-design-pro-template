package com.template.file.controller;

import com.template.common.exception.GlobalExceptionHandler;
import com.template.file.service.FileStorageService;
import com.template.file.vo.UploadResponse;
import com.template.security.auth.JsonAccessDeniedHandler;
import com.template.security.auth.JsonAuthenticationEntryPoint;
import com.template.security.config.SecurityConfig;
import com.template.security.jwt.JwtAuthenticationFilter;
import com.template.security.jwt.JwtTokenService;
import com.template.security.permission.PermissionService;
import com.template.support.web.ControllerSecurityTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 文件上传 Controller 权限入口测试。
 */
@WebMvcTest(controllers = FileController.class)
@ContextConfiguration(classes = {
        FileController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ControllerSecurityTestSupport.SecurityTestConfig.class
})
class FileControllerTest extends ControllerSecurityTestSupport {

    private static final String ARTICLE_UPLOAD = "article:upload";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("无 token 上传应返回 HTTP 401")
    void uploadShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(multipart("/api/common/upload").file(imageFile()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("无上传权限应返回 HTTP 403")
    void uploadShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ARTICLE_UPLOAD);

        mockMvc.perform(multipart("/api/common/upload")
                        .file(imageFile())
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("有上传权限应返回文件URL")
    void uploadShouldReturnUrlWhenPermissionGranted() throws Exception {
        when(fileStorageService.uploadImage(any(), any())).thenReturn(new UploadResponse(
                1L,
                "/api/common/files/2026/06/01/demo.png",
                "demo.png",
                6L,
                "image/png"
        ));

        mockMvc.perform(multipart("/api/common/upload")
                        .file(imageFile())
                        .header("Authorization", "Bearer " + token(jwtTokenService, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("/api/common/files/2026/06/01/demo.png"));
    }

    private MockMultipartFile imageFile() {
        return new MockMultipartFile("file", "demo.png", "image/png", "image".getBytes());
    }
}
