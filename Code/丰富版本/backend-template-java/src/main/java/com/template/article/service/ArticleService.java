package com.template.article.service;

import com.template.article.dto.ArticleListQuery;
import com.template.article.dto.ArticleSaveRequest;
import com.template.article.dto.ArticleStatusRequest;
import com.template.article.vo.ArticleCategoryVo;
import com.template.article.vo.ArticleDetailVo;
import com.template.article.vo.ArticleListItemVo;
import com.template.common.pagination.PageResult;
import com.template.security.auth.AppUserPrincipal;

import java.util.List;

/**
 * 文章业务服务。
 */
public interface ArticleService {

    /**
     * 查询启用的文章分类。
     *
     * @return 分类列表
     */
    List<ArticleCategoryVo> listCategories();

    /**
     * 分页查询文章。
     *
     * @param query 查询参数
     * @return 文章分页
     */
    PageResult<ArticleListItemVo> pageArticles(ArticleListQuery query);

    /**
     * 查询文章详情。
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    ArticleDetailVo getArticle(Long id);

    /**
     * 新增文章。
     *
     * @param request   保存请求
     * @param principal 当前用户
     * @return 新文章 ID
     */
    Long createArticle(ArticleSaveRequest request, AppUserPrincipal principal);

    /**
     * 更新文章。
     *
     * @param id        文章 ID
     * @param request   保存请求
     * @param principal 当前用户
     */
    void updateArticle(Long id, ArticleSaveRequest request, AppUserPrincipal principal);

    /**
     * 删除文章。
     *
     * @param id 文章 ID
     */
    void deleteArticle(Long id);

    /**
     * 更新文章状态。
     *
     * @param id        文章 ID
     * @param request   状态请求
     * @param principal 当前用户
     */
    void updateStatus(Long id, ArticleStatusRequest request, AppUserPrincipal principal);
}
