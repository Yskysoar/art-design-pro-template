package com.template.article.controller;

import com.template.article.dto.ArticleListQuery;
import com.template.article.dto.ArticleSaveRequest;
import com.template.article.dto.ArticleStatusRequest;
import com.template.article.service.ArticleService;
import com.template.article.vo.ArticleCategoryVo;
import com.template.article.vo.ArticleDetailVo;
import com.template.article.vo.ArticleListItemVo;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章管理接口。
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private static final String ARTICLE_ADD_PERMISSION = "article:publish:add";
    private static final String ARTICLE_EDIT_PERMISSION = "article:publish:edit";

    private final ArticleService articleService;
    private final PermissionService permissionService;

    public ArticleController(ArticleService articleService, PermissionService permissionService) {
        this.articleService = articleService;
        this.permissionService = permissionService;
    }

    /**
     * 查询启用文章分类。
     *
     * @return 分类列表
     */
    @GetMapping("/types")
    public ApiResponse<List<ArticleCategoryVo>> listCategories() {
        return ApiResponse.success(articleService.listCategories());
    }

    /**
     * 分页查询文章。
     *
     * @param query     查询参数
     * @param principal 当前用户
     * @return 文章分页
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<ArticleListItemVo>> listArticles(
            @ModelAttribute ArticleListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_EDIT_PERMISSION);
        return ApiResponse.success(articleService.pageArticles(query));
    }

    /**
     * 查询文章详情。
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @GetMapping("/detail")
    public ApiResponse<ArticleDetailVo> getArticle(Long id) {
        return ApiResponse.success(articleService.getArticle(id));
    }

    /**
     * 新增文章。
     *
     * @param request   保存请求
     * @param principal 当前用户
     * @return 新文章 ID
     */
    @PostMapping
    public ApiResponse<Long> createArticle(
            @Valid @RequestBody ArticleSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_ADD_PERMISSION);
        return ApiResponse.success(articleService.createArticle(request, principal));
    }

    /**
     * 更新文章。
     *
     * @param id        文章 ID
     * @param request   保存请求
     * @param principal 当前用户
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_EDIT_PERMISSION);
        articleService.updateArticle(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 删除文章。
     *
     * @param id        文章 ID
     * @param principal 当前用户
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_EDIT_PERMISSION);
        articleService.deleteArticle(id);
        return ApiResponse.success(null);
    }

    /**
     * 更新文章状态。
     *
     * @param id        文章 ID
     * @param request   状态请求
     * @param principal 当前用户
     * @return 空响应
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ArticleStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_EDIT_PERMISSION);
        articleService.updateStatus(id, request, principal);
        return ApiResponse.success(null);
    }
}
