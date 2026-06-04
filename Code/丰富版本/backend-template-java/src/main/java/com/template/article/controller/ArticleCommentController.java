package com.template.article.controller;

import com.template.article.dto.ArticleCommentListQuery;
import com.template.article.dto.ArticleCommentSaveRequest;
import com.template.article.dto.ArticleCommentStatusRequest;
import com.template.article.service.ArticleCommentService;
import com.template.article.vo.ArticleCommentVo;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章评论接口。
 */
@RestController
@RequestMapping("/api/article/comment")
public class ArticleCommentController {

    private final ArticleCommentService commentService;

    public ArticleCommentController(ArticleCommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 公开查询文章评论。
     *
     * @param query 查询参数
     * @return 评论分页
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<ArticleCommentVo>> listComments(
            @ModelAttribute ArticleCommentListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(commentService.pageComments(query, principal));
    }

    /**
     * 新增评论或回复。
     *
     * @param request   评论请求
     * @param principal 当前用户
     * @return 评论 ID
     */
    @PostMapping
    public ApiResponse<Long> createComment(
            @Valid @RequestBody ArticleCommentSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(commentService.createComment(request, principal));
    }

    /**
     * 更新评论状态。
     *
     * @param id        评论 ID
     * @param request   状态请求
     * @param principal 当前用户
     * @return 空响应
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ArticleCommentStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        commentService.updateStatus(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 删除评论。
     *
     * @param id        评论 ID
     * @param principal 当前用户
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        commentService.deleteComment(id, principal);
        return ApiResponse.success(null);
    }
}
