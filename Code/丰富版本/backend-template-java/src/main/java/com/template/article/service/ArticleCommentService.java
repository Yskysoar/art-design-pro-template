package com.template.article.service;

import com.template.article.dto.ArticleCommentListQuery;
import com.template.article.dto.ArticleCommentSaveRequest;
import com.template.article.dto.ArticleCommentStatusRequest;
import com.template.article.vo.ArticleCommentVo;
import com.template.common.pagination.PageResult;
import com.template.security.auth.AppUserPrincipal;

/**
 * 文章评论业务服务。
 */
public interface ArticleCommentService {

    /**
     * 分页查询文章一级评论，并挂载子回复。
     *
     * @param query 查询参数
     * @return 评论分页
     */
    PageResult<ArticleCommentVo> pageComments(ArticleCommentListQuery query, AppUserPrincipal principal);

    /**
     * 新增一级评论或回复。
     *
     * @param request   评论请求
     * @param principal 当前用户
     * @return 新评论 ID
     */
    Long createComment(ArticleCommentSaveRequest request, AppUserPrincipal principal);

    /**
     * 管理端更新评论状态。
     *
     * @param id        评论 ID
     * @param request   状态请求
     * @param principal 当前用户
     */
    void updateStatus(Long id, ArticleCommentStatusRequest request, AppUserPrincipal principal);

    /**
     * 管理端删除评论。
     *
     * @param id        评论 ID
     * @param principal 当前用户
     */
    void deleteComment(Long id, AppUserPrincipal principal);
}
