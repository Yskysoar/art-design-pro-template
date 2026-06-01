package com.template.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.dto.ArticleCommentListQuery;
import com.template.article.dto.ArticleCommentSaveRequest;
import com.template.article.dto.ArticleCommentStatusRequest;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleComment;
import com.template.article.mapper.ArticleCommentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.article.service.ArticleCommentService;
import com.template.article.vo.ArticleCommentVo;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.config.entity.SysConfig;
import com.template.system.config.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文章评论业务服务实现。
 */
@Service
public class ArticleCommentServiceImpl implements ArticleCommentService {

    private static final int NOT_DELETED = 0;
    private static final long ROOT_PARENT_ID = 0L;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final int MAX_CONTENT_LENGTH = 500;
    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_HIDDEN = "HIDDEN";
    private static final String STATUS_DELETED = "DELETED";
    private static final String COMMENT_MANAGE_PERMISSION = "article:comment:manage";
    private static final String COMMENT_HIDE_ENABLED_KEY = "article_comment_hide_enabled";
    private static final String SUPER_ROLE_CODE = "R_SUPER";
    private static final Set<String> COMMENT_STATUSES = Set.of(STATUS_NORMAL, STATUS_HIDDEN, STATUS_DELETED);
    private static final String DELETED_CONTENT = "该评论已删除";
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ArticleCommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final PermissionService permissionService;
    private final SysConfigMapper configMapper;

    public ArticleCommentServiceImpl(
            ArticleCommentMapper commentMapper,
            ArticleMapper articleMapper,
            PermissionService permissionService,
            SysConfigMapper configMapper
    ) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.permissionService = permissionService;
        this.configMapper = configMapper;
    }

    @Override
    public PageResult<ArticleCommentVo> pageComments(ArticleCommentListQuery query, AppUserPrincipal principal) {
        Article article = getExistingArticle(query.articleId());
        long current = query.current() == null || query.current() < 1 ? DEFAULT_CURRENT : query.current();
        long size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);

        IPage<ArticleComment> page = commentMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<ArticleComment>()
                .eq(ArticleComment::getArticleId, article.getId())
                .eq(ArticleComment::getParentId, ROOT_PARENT_ID)
                .eq(ArticleComment::getDeleted, NOT_DELETED)
                .orderByDesc(ArticleComment::getCreateTime)
                .orderByDesc(ArticleComment::getId));

        Map<Long, List<ArticleComment>> replies = loadReplies(page.getRecords());
        boolean hideEnabled = isCommentHideEnabled();
        boolean superAdmin = isSuperAdmin(principal);
        boolean commentManager = hasCommentManagePermission(principal);
        return new PageResult<>(
                page.getRecords().stream()
                        .map(comment -> toVo(
                                comment,
                                replies.getOrDefault(comment.getId(), List.of()),
                                Map.of(),
                                article,
                                principal,
                                hideEnabled,
                                superAdmin,
                                commentManager
                        ))
                        .toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(ArticleCommentSaveRequest request, AppUserPrincipal principal) {
        Article article = getExistingArticle(request.articleId());
        String content = normalizeContent(request.content());
        Long parentId = request.parentId() == null ? ROOT_PARENT_ID : request.parentId();

        ArticleComment parent = null;
        Long rootId = ROOT_PARENT_ID;
        if (parentId != ROOT_PARENT_ID) {
            parent = getExistingComment(parentId);
            validateReplyTarget(article.getId(), parent);
            rootId = parent.getParentId() == ROOT_PARENT_ID ? parent.getId() : parent.getRootId();
        }

        ArticleComment comment = new ArticleComment();
        comment.setArticleId(article.getId());
        comment.setParentId(parentId);
        comment.setRootId(rootId);
        comment.setContent(content);
        comment.setStatus(STATUS_NORMAL);
        comment.setUserId(principal.userId());
        comment.setUserName(principal.userName());
        comment.setUserAvatar(null);
        comment.setDeleted(NOT_DELETED);
        commentMapper.insert(comment);
        if (parentId == ROOT_PARENT_ID) {
            comment.setRootId(comment.getId());
            commentMapper.updateById(comment);
        }
        changeCommentCount(article, 1L);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, ArticleCommentStatusRequest request, AppUserPrincipal principal) {
        ArticleComment comment = getExistingComment(id);
        String status = normalizeStatus(request.status());
        if (status.equals(comment.getStatus())) {
            return;
        }

        Article article = getExistingArticle(comment.getArticleId());
        requireStatusPermission(comment, article, status, principal);
        String oldStatus = comment.getStatus();
        comment.setStatus(status);
        if (STATUS_DELETED.equals(status)) {
            comment.setContent(DELETED_CONTENT);
        }
        commentMapper.updateById(comment);
        updateCountForStatusChange(article, oldStatus, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, AppUserPrincipal principal) {
        updateStatus(id, new ArticleCommentStatusRequest(STATUS_DELETED), principal);
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

    private ArticleComment getExistingComment(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "评论ID不能为空");
        }
        ArticleComment comment = commentMapper.selectOne(new LambdaQueryWrapper<ArticleComment>()
                .eq(ArticleComment::getId, id)
                .eq(ArticleComment::getDeleted, NOT_DELETED));
        if (comment == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "评论不存在");
        }
        return comment;
    }

    private void validateReplyTarget(Long articleId, ArticleComment parent) {
        if (!articleId.equals(parent.getArticleId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "父评论不属于当前文章");
        }
        if (STATUS_DELETED.equals(parent.getStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "已删除评论不可回复");
        }
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "评论内容不能为空");
        }
        String normalized = CONTROL_CHAR_PATTERN.matcher(content.trim()).replaceAll("");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "评论内容不能为空");
        }
        if (normalized.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "评论内容不能超过500字");
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.toUpperCase();
        if (!COMMENT_STATUSES.contains(normalized)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "评论状态不支持：" + status);
        }
        return normalized;
    }

    private void updateCountForStatusChange(Article article, String oldStatus, String newStatus) {
        boolean oldCounted = isCountedStatus(oldStatus);
        boolean newCounted = isCountedStatus(newStatus);
        if (oldCounted && !newCounted) {
            changeCommentCount(article, -1L);
        } else if (!oldCounted && newCounted) {
            changeCommentCount(article, 1L);
        }
    }

    private boolean isCountedStatus(String status) {
        return STATUS_NORMAL.equals(status) || STATUS_HIDDEN.equals(status);
    }

    private void changeCommentCount(Article article, long delta) {
        long current = article.getCommentCount() == null ? 0L : article.getCommentCount();
        article.setCommentCount(Math.max(0L, current + delta));
        articleMapper.updateById(article);
    }

    private Map<Long, List<ArticleComment>> loadReplies(List<ArticleComment> rootComments) {
        List<Long> rootIds = rootComments.stream().map(ArticleComment::getId).toList();
        if (rootIds.isEmpty()) {
            return Map.of();
        }
        List<ArticleComment> replies = commentMapper.selectList(new LambdaQueryWrapper<ArticleComment>()
                .in(ArticleComment::getRootId, rootIds)
                .ne(ArticleComment::getParentId, ROOT_PARENT_ID)
                .eq(ArticleComment::getDeleted, NOT_DELETED)
                .orderByAsc(ArticleComment::getCreateTime)
                .orderByAsc(ArticleComment::getId));
        return replies.stream()
                .sorted(Comparator.comparing(ArticleComment::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ArticleComment::getId))
                .collect(Collectors.groupingBy(
                        ArticleComment::getRootId,
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)
                ));
    }

    private ArticleCommentVo toVo(
            ArticleComment comment,
            List<ArticleComment> replies,
            Map<Long, ArticleComment> parentMap,
            Article article,
            AppUserPrincipal principal,
            boolean hideEnabled,
            boolean superAdmin,
            boolean commentManager
    ) {
        String content = STATUS_DELETED.equals(comment.getStatus()) ? DELETED_CONTENT : comment.getContent();
        Map<Long, ArticleComment> replyParentMap = replies.stream()
                .collect(Collectors.toMap(
                        ArticleComment::getId,
                        reply -> reply,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        replyParentMap.put(comment.getId(), comment);
        String replyToUserName = comment.getParentId() == null || comment.getParentId() == ROOT_PARENT_ID
                ? null
                : parentMap.get(comment.getParentId()) == null ? null : parentMap.get(comment.getParentId()).getUserName();
        boolean canHide = canHideComment(comment, article, principal, hideEnabled, superAdmin, commentManager);
        boolean canRestore = canRestoreComment(comment, article, principal, hideEnabled, superAdmin, commentManager);
        boolean canDelete = canDeleteComment(comment, principal, superAdmin, commentManager);
        return new ArticleCommentVo(
                comment.getId(),
                comment.getArticleId(),
                comment.getParentId(),
                comment.getRootId(),
                content,
                comment.getStatus(),
                comment.getUserId(),
                comment.getUserName(),
                comment.getUserAvatar(),
                replyToUserName,
                formatDateTime(comment.getCreateTime()),
                canHide,
                canRestore,
                canDelete,
                replies.stream().map(reply -> toVo(
                        reply,
                        List.of(),
                        replyParentMap,
                        article,
                        principal,
                        hideEnabled,
                        superAdmin,
                        commentManager
                )).toList()
        );
    }

    private void requireStatusPermission(
            ArticleComment comment,
            Article article,
            String targetStatus,
            AppUserPrincipal principal
    ) {
        boolean hideEnabled = isCommentHideEnabled();
        boolean superAdmin = isSuperAdmin(principal);
        boolean commentManager = hasCommentManagePermission(principal);
        if (STATUS_DELETED.equals(targetStatus)) {
            if (canDeleteComment(comment, principal, superAdmin, commentManager)) {
                return;
            }
            throw new BusinessException(ApiCode.FORBIDDEN, "无权限删除该评论");
        }
        if (STATUS_HIDDEN.equals(targetStatus) && canHideComment(comment, article, principal, hideEnabled, superAdmin, commentManager)) {
            return;
        }
        if (STATUS_NORMAL.equals(targetStatus) && canRestoreComment(comment, article, principal, hideEnabled, superAdmin, commentManager)) {
            return;
        }
        throw new BusinessException(ApiCode.FORBIDDEN, "无权限变更该评论状态");
    }

    private boolean canHideComment(
            ArticleComment comment,
            Article article,
            AppUserPrincipal principal,
            boolean hideEnabled,
            boolean superAdmin,
            boolean commentManager
    ) {
        if (comment == null || STATUS_HIDDEN.equals(comment.getStatus()) || STATUS_DELETED.equals(comment.getStatus())) {
            return false;
        }
        if (superAdmin) {
            return true;
        }
        return hideEnabled && principal != null && (commentManager || isArticlePublisher(article, principal));
    }

    private boolean canRestoreComment(
            ArticleComment comment,
            Article article,
            AppUserPrincipal principal,
            boolean hideEnabled,
            boolean superAdmin,
            boolean commentManager
    ) {
        if (comment == null || !STATUS_HIDDEN.equals(comment.getStatus())) {
            return false;
        }
        if (superAdmin) {
            return true;
        }
        return hideEnabled && principal != null && (commentManager || isArticlePublisher(article, principal));
    }

    private boolean canDeleteComment(
            ArticleComment comment,
            AppUserPrincipal principal,
            boolean superAdmin,
            boolean commentManager
    ) {
        if (comment == null || STATUS_DELETED.equals(comment.getStatus())) {
            return false;
        }
        return superAdmin || commentManager || isCommentOwner(comment, principal);
    }

    private boolean isCommentHideEnabled() {
        SysConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, COMMENT_HIDE_ENABLED_KEY)
                .eq(SysConfig::getDeleted, 0L));
        return config == null || Boolean.parseBoolean(config.getConfigValue());
    }

    private boolean hasCommentManagePermission(AppUserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        try {
            permissionService.requirePermission(principal, COMMENT_MANAGE_PERMISSION);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private boolean isSuperAdmin(AppUserPrincipal principal) {
        return principal != null
                && principal.roles() != null
                && principal.roles().contains(SUPER_ROLE_CODE)
                && permissionService.isSuperAdmin(principal);
    }

    private boolean isArticlePublisher(Article article, AppUserPrincipal principal) {
        return article != null
                && principal != null
                && StringUtils.hasText(article.getCreateBy())
                && article.getCreateBy().equals(principal.userName());
    }

    private boolean isCommentOwner(ArticleComment comment, AppUserPrincipal principal) {
        return comment != null
                && principal != null
                && comment.getUserId() != null
                && comment.getUserId().equals(principal.userId());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }
}
