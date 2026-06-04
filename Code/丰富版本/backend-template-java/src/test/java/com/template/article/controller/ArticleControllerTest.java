package com.template.article.controller;

import com.template.article.dto.ArticleListQuery;
import com.template.article.service.ArticleService;
import com.template.article.vo.ArticleCategoryVo;
import com.template.article.vo.ArticleListItemVo;
import com.template.common.exception.GlobalExceptionHandler;
import com.template.common.pagination.PageResult;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 文章 Controller 权限入口测试。
 */
@WebMvcTest(controllers = ArticleController.class)
@ContextConfiguration(classes = {
        ArticleController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ControllerSecurityTestSupport.SecurityTestConfig.class
})
class ArticleControllerTest extends ControllerSecurityTestSupport {

    private static final String ARTICLE_EDIT = "article:publish:edit";
    private static final String ARTICLE_ADD = "article:publish:add";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("文章分类接口登录后应返回分类")
    void listCategoriesShouldReturnCategories() throws Exception {
        when(articleService.listCategories()).thenReturn(List.of(new ArticleCategoryVo(1L, "技术文章")));

        mockMvc.perform(get("/api/article/types").header("Authorization", "Bearer " + token(jwtTokenService, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("技术文章"));
    }

    @Test
    @DisplayName("无 token 访问文章列表应返回 HTTP 401")
    void listArticlesShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/article/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("无文章编辑权限访问文章列表应返回 HTTP 403")
    void listArticlesShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ARTICLE_EDIT);

        mockMvc.perform(get("/api/article/list").header("Authorization", "Bearer " + token(jwtTokenService, GUEST)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("有文章编辑权限访问文章列表应返回分页数据")
    void listArticlesShouldReturnPageWhenPermissionGranted() throws Exception {
        when(articleService.pageArticles(any(ArticleListQuery.class))).thenReturn(new PageResult<>(
                List.of(new ArticleListItemVo(
                        1L,
                        "后端模板文章模块接入说明",
                        2L,
                        "技术文章",
                        "技术文章",
                        "/cover.png",
                        "/cover.png",
                        "摘要",
                        "PUBLISHED",
                        true,
                        12L,
                        12L,
                        3L,
                        "admin",
                        "2026-06-01 10:00:00",
                        "2026-06-01 10:00:00",
                        "2026-06-01 10:00:00"
                )),
                1,
                20,
                1
        ));

        mockMvc.perform(get("/api/article/list").header("Authorization", "Bearer " + token(jwtTokenService, ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].title").value("后端模板文章模块接入说明"));
    }

    @Test
    @DisplayName("文章写接口应按权限码校验")
    void articleWriteApisShouldCheckPermissionCode() throws Exception {
        String token = token(jwtTokenService, ADMIN);
        String body = "{\"title\":\"文章标题\",\"categoryId\":1,\"contentHtml\":\"<p>正文</p>\",\"visible\":true,\"status\":\"PUBLISHED\",\"attachmentIds\":[]}";

        when(articleService.createArticle(any(), any())).thenReturn(2L);

        mockMvc.perform(post("/api/article")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/article/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/article/1/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"status\":\"OFFLINE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/article/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新增文章无权限应返回 HTTP 403")
    void createArticleShouldReturnForbiddenWithoutPermission() throws Exception {
        deny(permissionService, GUEST, ARTICLE_ADD);

        mockMvc.perform(post("/api/article")
                        .header("Authorization", "Bearer " + token(jwtTokenService, GUEST))
                        .contentType("application/json")
                        .content("{\"title\":\"文章标题\",\"categoryId\":1,\"contentHtml\":\"<p>正文</p>\"}"))
                .andExpect(status().isForbidden());
    }
}
