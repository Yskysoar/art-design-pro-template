package com.template.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.dto.ArticleListQuery;
import com.template.article.dto.ArticleSaveRequest;
import com.template.article.dto.ArticleStatusRequest;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleAttachment;
import com.template.article.entity.ArticleCategory;
import com.template.article.entity.ArticleComment;
import com.template.article.mapper.ArticleAttachmentMapper;
import com.template.article.mapper.ArticleCategoryMapper;
import com.template.article.mapper.ArticleCommentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.article.service.ArticleService;
import com.template.article.service.HtmlSanitizerService;
import com.template.article.vo.ArticleAttachmentVo;
import com.template.article.vo.ArticleCategoryVo;
import com.template.article.vo.ArticleDetailVo;
import com.template.article.vo.ArticleListItemVo;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.file.entity.FileResource;
import com.template.file.service.FileStorageService;
import com.template.security.auth.AppUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章业务服务实现。
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final int ENABLED = 1;
    private static final int VISIBLE = 1;
    private static final int HIDDEN = 0;
    private static final int NOT_DELETED = 0;
    private static final long ROOT_PARENT_ID = 0L;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> ARTICLE_STATUSES = Set.of("DRAFT", "PUBLISHED", "OFFLINE");

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper categoryMapper;
    private final ArticleAttachmentMapper attachmentMapper;
    private final ArticleCommentMapper commentMapper;
    private final FileStorageService fileStorageService;
    private final HtmlSanitizerService htmlSanitizerService;

    public ArticleServiceImpl(
            ArticleMapper articleMapper,
            ArticleCategoryMapper categoryMapper,
            ArticleAttachmentMapper attachmentMapper,
            ArticleCommentMapper commentMapper,
            FileStorageService fileStorageService,
            HtmlSanitizerService htmlSanitizerService
    ) {
        this.articleMapper = articleMapper;
        this.categoryMapper = categoryMapper;
        this.attachmentMapper = attachmentMapper;
        this.commentMapper = commentMapper;
        this.fileStorageService = fileStorageService;
        this.htmlSanitizerService = htmlSanitizerService;
    }

    @Override
    public List<ArticleCategoryVo> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<ArticleCategory>()
                        .eq(ArticleCategory::getEnabled, ENABLED)
                        .eq(ArticleCategory::getDeleted, NOT_DELETED)
                        .orderByAsc(ArticleCategory::getSort)
                        .orderByAsc(ArticleCategory::getId))
                .stream()
                .map(category -> new ArticleCategoryVo(category.getId(), category.getCategoryName()))
                .toList();
    }

    @Override
    public PageResult<ArticleListItemVo> pageArticles(ArticleListQuery query) {
        long current = query.current() == null || query.current() < 1 ? DEFAULT_CURRENT : query.current();
        long size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);
        IPage<Article> page = articleMapper.selectPage(Page.of(current, size), buildListWrapper(query));
        Map<Long, ArticleCategory> categoryMap = loadCategoryMap(page.getRecords());

        return new PageResult<>(
                page.getRecords().stream()
                        .map(article -> toListVo(article, categoryMap.get(article.getCategoryId())))
                        .toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    public ArticleDetailVo getArticle(Long id) {
        Article article = getExistingArticle(id);
        incrementViewCount(article);
        ArticleCategory category = getExistingCategory(article.getCategoryId());
        return toDetailVo(article, category, loadAttachments(article.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleSaveRequest request, AppUserPrincipal principal) {
        Article article = new Article();
        applyArticleFields(article, request, principal.userName(), true);
        article.setCreateBy(principal.userName());
        article.setViewCount(0L);
        article.setCommentCount(0L);
        article.setDeleted(NOT_DELETED);
        articleMapper.insert(article);
        saveAttachments(article.getId(), request.attachmentIds());
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long id, ArticleSaveRequest request, AppUserPrincipal principal) {
        Article article = getExistingArticle(id);
        applyArticleFields(article, request, principal.userName(), false);
        articleMapper.updateById(article);
        saveAttachments(article.getId(), request.attachmentIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        getExistingArticle(id);
        articleMapper.deleteById(id);
        attachmentMapper.delete(new LambdaQueryWrapper<ArticleAttachment>().eq(ArticleAttachment::getArticleId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, ArticleStatusRequest request, AppUserPrincipal principal) {
        Article article = getExistingArticle(id);
        String status = normalizeStatus(request.status());
        article.setStatus(status);
        article.setUpdateBy(principal.userName());
        if ("PUBLISHED".equals(status) && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.updateById(article);
    }

    private LambdaQueryWrapper<Article> buildListWrapper(ArticleListQuery query) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getDeleted, NOT_DELETED)
                .like(StringUtils.hasText(query.title()), Article::getTitle, query.title())
                .eq(query.categoryId() != null, Article::getCategoryId, query.categoryId())
                .eq(StringUtils.hasText(query.status()), Article::getStatus, query.status())
                .orderByDesc(Article::getCreateTime);
        if (query.year() != null) {
            LocalDateTime start = LocalDateTime.of(query.year(), 1, 1, 0, 0);
            wrapper.ge(Article::getCreateTime, start).lt(Article::getCreateTime, start.plusYears(1));
        }
        return wrapper;
    }

    private void applyArticleFields(
            Article article,
            ArticleSaveRequest request,
            String operator,
            boolean create
    ) {
        ArticleCategory category = getExistingCategory(request.categoryId());
        String status = normalizeStatus(StringUtils.hasText(request.status()) ? request.status() : "PUBLISHED");
        String sanitizedHtml = htmlSanitizerService.sanitize(request.contentHtml());
        if (!StringUtils.hasText(htmlSanitizerService.toPlainText(sanitizedHtml))) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文章内容不能为空");
        }

        article.setTitle(request.title());
        article.setCategoryId(category.getId());
        article.setCoverUrl(request.coverUrl());
        article.setSummary(request.summary());
        article.setContentHtml(sanitizedHtml);
        article.setContentText(htmlSanitizerService.toPlainText(sanitizedHtml));
        article.setVisible(Boolean.FALSE.equals(request.visible()) ? HIDDEN : VISIBLE);
        article.setStatus(status);
        article.setUpdateBy(operator);
        if (create || ("PUBLISHED".equals(status) && article.getPublishTime() == null)) {
            article.setPublishTime("PUBLISHED".equals(status) ? LocalDateTime.now() : null);
        }
    }

    private Article getExistingArticle(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文章ID不能为空");
        }
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getId, id)
                .eq(Article::getDeleted, NOT_DELETED));
        if (article == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "文章不存在");
        }
        return article;
    }

    private ArticleCategory getExistingCategory(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文章分类不能为空");
        }
        ArticleCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<ArticleCategory>()
                .eq(ArticleCategory::getId, id)
                .eq(ArticleCategory::getEnabled, ENABLED)
                .eq(ArticleCategory::getDeleted, NOT_DELETED));
        if (category == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文章分类不存在或已停用");
        }
        return category;
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = status == null ? "" : status.toUpperCase();
        if (!ARTICLE_STATUSES.contains(normalizedStatus)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文章状态不支持：" + status);
        }
        return normalizedStatus;
    }

    private void saveAttachments(Long articleId, List<Long> attachmentIds) {
        attachmentMapper.delete(new LambdaQueryWrapper<ArticleAttachment>().eq(ArticleAttachment::getArticleId, articleId));
        List<Long> distinctIds = attachmentIds == null
                ? List.of()
                : new ArrayList<>(new LinkedHashSet<>(attachmentIds));
        for (int index = 0; index < distinctIds.size(); index++) {
            Long fileId = distinctIds.get(index);
            fileStorageService.getExistingFile(fileId);
            ArticleAttachment attachment = new ArticleAttachment();
            attachment.setArticleId(articleId);
            attachment.setFileId(fileId);
            attachment.setSort(index + 1);
            attachmentMapper.insert(attachment);
        }
    }

    private Map<Long, ArticleCategory> loadCategoryMap(List<Article> articles) {
        List<Long> categoryIds = articles.stream().map(Article::getCategoryId).distinct().toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectList(new LambdaQueryWrapper<ArticleCategory>().in(ArticleCategory::getId, categoryIds))
                .stream()
                .collect(Collectors.toMap(ArticleCategory::getId, Function.identity()));
    }

    private List<ArticleAttachmentVo> loadAttachments(Long articleId) {
        List<ArticleAttachment> attachments = attachmentMapper.selectList(new LambdaQueryWrapper<ArticleAttachment>()
                .eq(ArticleAttachment::getArticleId, articleId)
                .orderByAsc(ArticleAttachment::getSort));
        return attachments.stream().map(attachment -> {
            FileResource file = fileStorageService.getExistingFile(attachment.getFileId());
            return new ArticleAttachmentVo(
                    file.getId(),
                    file.getOriginalName(),
                    file.getUrl(),
                    file.getSize(),
                    file.getContentType()
            );
        }).toList();
    }

    private ArticleListItemVo toListVo(Article article, ArticleCategory category) {
        String categoryName = category == null ? "" : category.getCategoryName();
        String createTime = formatDateTime(article.getCreateTime());
        return new ArticleListItemVo(
                article.getId(),
                article.getTitle(),
                article.getCategoryId(),
                categoryName,
                categoryName,
                article.getCoverUrl(),
                article.getCoverUrl(),
                article.getSummary(),
                article.getStatus(),
                Integer.valueOf(VISIBLE).equals(article.getVisible()),
                article.getViewCount(),
                article.getViewCount(),
                article.getCommentCount(),
                article.getCreateBy(),
                createTime,
                createTime,
                formatDateTime(article.getPublishTime())
        );
    }

    private ArticleDetailVo toDetailVo(Article article, ArticleCategory category, List<ArticleAttachmentVo> attachments) {
        String sanitizedHtml = htmlSanitizerService.sanitize(article.getContentHtml());
        long rootCommentCount = countComments(article.getId(), ROOT_PARENT_ID, true);
        long replyCount = countComments(article.getId(), ROOT_PARENT_ID, false);
        return new ArticleDetailVo(
                article.getId(),
                article.getTitle(),
                article.getCategoryId(),
                category.getCategoryName(),
                String.valueOf(article.getCategoryId()),
                article.getCoverUrl(),
                article.getSummary(),
                sanitizedHtml,
                sanitizedHtml,
                Integer.valueOf(VISIBLE).equals(article.getVisible()),
                article.getStatus(),
                article.getViewCount(),
                article.getCommentCount(),
                rootCommentCount,
                replyCount,
                article.getCreateBy(),
                formatDateTime(article.getCreateTime()),
                formatDateTime(article.getPublishTime()),
                attachments
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private void incrementViewCount(Article article) {
        long current = article.getViewCount() == null ? 0L : article.getViewCount();
        article.setViewCount(current + 1);
        articleMapper.updateById(article);
    }

    private long countComments(Long articleId, long parentId, boolean rootOnly) {
        Long count = commentMapper.selectCount(new LambdaQueryWrapper<ArticleComment>()
                .eq(ArticleComment::getArticleId, articleId)
                .eq(rootOnly, ArticleComment::getParentId, parentId)
                .ne(!rootOnly, ArticleComment::getParentId, parentId)
                .eq(ArticleComment::getDeleted, NOT_DELETED)
                .ne(ArticleComment::getStatus, "DELETED"));
        return count == null ? 0L : count;
    }
}
