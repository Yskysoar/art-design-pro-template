package com.template.article.controller;

import com.template.article.service.ArticleCommentService;
import com.template.article.vo.ArticleCommentVo;
import com.template.common.exception.GlobalExceptionHandler;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 文章评论 Controller 测试。
 */
@WebMvcTest(controllers = ArticleCommentController.class)
@ContextConfiguration(classes = {
        ArticleCommentController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ControllerSecurityTestSupport.SecurityTestConfig.class
})
class ArticleCommentControllerTest extends ControllerSecurityTestSupport {

    private static final String COMMENT_MANAGE = "article:comment:manage";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private ArticleCommentService commentService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("评论列表未登录也可公开读取")
    void listCommentsShouldBePublic() throws Exception {
        when(commentService.pageComments(any(), any())).thenReturn(new PageResult<>(
                List.of(new ArticleCommentVo(
                        1L,
                        1L,
                        0L,
                        1L,
                        "公开评论",
                        "NORMAL",
                        2L,
                        "moderator",
                        null,
                        null,
                        "2026-06-01 10:00:00",
                        false,
                        false,
                        false,
                        List.of()
                )),
                1,
                20,
                1
        ));

        mockMvc.perform(get("/api/article/comment/list?articleId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].content").value("公开评论"));
    }

    @Test
    @DisplayName("新增评论无 token 应返回 HTTP 401")
    void createCommentShouldRequireLogin() throws Exception {
        mockMvc.perform(post("/api/article/comment")
                        .contentType("application/json")
                        .content("{\"articleId\":1,\"parentId\":0,\"content\":\"评论\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("登录后可新增评论")
    void createCommentShouldReturnIdWhenLoggedIn() throws Exception {
        when(commentService.createComment(any(), any())).thenReturn(9L);

        mockMvc.perform(post("/api/article/comment")
                        .header("Authorization", "Bearer " + token(jwtTokenService, ADMIN))
                        .contentType("application/json")
                        .content("{\"articleId\":1,\"parentId\":0,\"content\":\"评论\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(9));
    }

    @Test
    @DisplayName("评论管理无权限应返回 HTTP 403")
    void manageCommentShouldReturnForbiddenWithoutPermission() throws Exception {
        doThrow(new BusinessException(ApiCode.FORBIDDEN, "无权限删除该评论"))
                .when(commentService)
                .deleteComment(any(), any());

        mockMvc.perform(delete("/api/article/comment/1")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("评论管理有权限应返回成功")
    void manageCommentShouldReturnOkWithPermission() throws Exception {
        String token = token(jwtTokenService, ADMIN);

        mockMvc.perform(patch("/api/article/comment/1/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"status\":\"HIDDEN\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/article/comment/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
