package com.template.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.dto.ArticleCommentListQuery;
import com.template.article.dto.ArticleCommentSaveRequest;
import com.template.article.dto.ArticleCommentStatusRequest;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleComment;
import com.template.article.mapper.ArticleCommentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.common.security.SensitiveWordGuard;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.config.entity.SysConfig;
import com.template.system.config.mapper.SysConfigMapper;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文章评论业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceImplTest {

    private static final AppUserPrincipal ADMIN = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));

    @Mock
    private ArticleCommentMapper commentMapper;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private PermissionService permissionService;
    @Mock
    private SysConfigMapper configMapper;
    @Mock
    private SensitiveWordGuard sensitiveWordGuard;
    @Mock
    private SysUserMapper userMapper;

    private ArticleCommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new ArticleCommentServiceImpl(commentMapper, articleMapper, permissionService, configMapper, sensitiveWordGuard, userMapper);
        lenient().when(permissionService.isSuperAdmin(ADMIN)).thenReturn(true);
        lenient().when(userMapper.selectList(anyWrapper())).thenReturn(List.of());
    }

    @Test
    @DisplayName("评论列表应分页返回一级评论并挂载子回复")
    void pageCommentsShouldAttachReplies() {
        Article article = article(1L, 2L);
        article.setVisible(1);
        article.setStatus("PUBLISHED");
        ArticleComment root = comment(10L, 1L, 0L, 10L, "一级评论", "NORMAL");
        ArticleComment reply = comment(11L, 1L, 10L, 10L, "子回复", "NORMAL");
        ArticleComment nestedReply = comment(12L, 1L, 11L, 10L, "回复子回复", "NORMAL");
        root.setUserAvatar("/old-root-avatar.webp");
        reply.setUserAvatar("/old-reply-avatar.webp");
        IPage<ArticleComment> page = Page.of(1, 20, 1);
        page.setRecords(List.of(root));

        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);
        when(commentMapper.selectPage(any(), anyWrapper())).thenReturn(page);
        when(commentMapper.selectList(anyWrapper())).thenReturn(List.of(reply, nestedReply));
        when(userMapper.selectList(anyWrapper())).thenReturn(List.of(user(1L, "/current-admin-avatar.webp")));

        var result = commentService.pageComments(new ArticleCommentListQuery(1L, 1L, 20L), ADMIN);

        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).replies()).hasSize(2);
        assertThat(result.records().get(0).replies().get(0).content()).isEqualTo("子回复");
        assertThat(result.records().get(0).replies().get(0).replyToUserName()).isEqualTo("admin");
        assertThat(result.records().get(0).replies().get(1).replyToUserName()).isEqualTo("admin");
        assertThat(result.records().get(0).canHide()).isTrue();
        assertThat(result.records().get(0).canDelete()).isTrue();
        assertThat(result.records().get(0).mine()).isTrue();
        assertThat(result.records().get(0).userAvatar()).isEqualTo("/current-admin-avatar.webp");
        assertThat(result.records().get(0).replies().get(0).userAvatar()).isEqualTo("/current-admin-avatar.webp");
    }

    @Test
    @DisplayName("游客只能读取已发布且可见文章评论")
    void pageCommentsShouldFollowArticleVisibility() {
        Article draft = article(1L, 1L);
        draft.setVisible(1);
        draft.setStatus("DRAFT");
        when(articleMapper.selectOne(anyWrapper())).thenReturn(draft);

        assertThatThrownBy(() -> commentService.pageComments(new ArticleCommentListQuery(1L, 1L, 20L), null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权限查看该文章评论");

        AppUserPrincipal manager = new AppUserPrincipal(4L, "manager", List.of("R_MODERATOR"));
        when(commentMapper.selectPage(any(), anyWrapper())).thenReturn(Page.of(1, 20, 0));
        commentService.pageComments(new ArticleCommentListQuery(1L, 1L, 20L), manager);
    }

    @Test
    @DisplayName("隐藏和删除评论在公开响应中返回脱敏内容")
    void pageCommentsShouldMaskHiddenAndDeletedContent() {
        Article article = article(1L, 2L);
        article.setVisible(1);
        article.setStatus("PUBLISHED");
        ArticleComment hidden = comment(10L, 1L, 0L, 10L, "隐藏原文", "HIDDEN");
        ArticleComment deleted = comment(11L, 1L, 10L, 10L, "删除原文", "DELETED");
        IPage<ArticleComment> page = Page.of(1, 20, 1);
        page.setRecords(List.of(hidden));

        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);
        when(commentMapper.selectPage(any(), anyWrapper())).thenReturn(page);
        when(commentMapper.selectList(anyWrapper())).thenReturn(List.of(deleted));

        var result = commentService.pageComments(new ArticleCommentListQuery(1L, 1L, 20L), null);

        assertThat(result.records().get(0).content()).isEqualTo("该评论已隐藏");
        assertThat(result.records().get(0).replies().get(0).content()).isEqualTo("该评论已删除");
    }

    @Test
    @DisplayName("新增一级评论应写入评论并递增评论数")
    void createRootCommentShouldInsertAndIncrementCount() {
        Article article = article(1L, 0L);
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(1L, "/current-admin-avatar.webp"));

        Long id = commentService.createComment(new ArticleCommentSaveRequest(1L, 0L, " 新评论 "), ADMIN);

        ArgumentCaptor<ArticleComment> commentCaptor = ArgumentCaptor.forClass(ArticleComment.class);
        verify(commentMapper).insert(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getContent()).isEqualTo("新评论");
        assertThat(commentCaptor.getValue().getStatus()).isEqualTo("NORMAL");
        assertThat(commentCaptor.getValue().getUserName()).isEqualTo("admin");
        assertThat(commentCaptor.getValue().getUserAvatar()).isEqualTo("/current-admin-avatar.webp");
        assertThat(id).isEqualTo(commentCaptor.getValue().getId());

        verify(articleMapper).update(eq(null), anyWrapper());
    }

    @Test
    @DisplayName("新增回复应校验父评论属于同一文章")
    void createReplyShouldRejectCrossArticleParent() {
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article(1L, 0L));
        when(commentMapper.selectOne(anyWrapper())).thenReturn(comment(9L, 2L, 0L, 9L, "父评论", "NORMAL"));

        assertThatThrownBy(() -> commentService.createComment(new ArticleCommentSaveRequest(1L, 9L, "回复"), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("父评论不属于当前文章");

        verify(commentMapper, never()).insert(any(ArticleComment.class));
    }

    @Test
    @DisplayName("已删除评论不可继续回复")
    void createReplyShouldRejectDeletedParent() {
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article(1L, 0L));
        when(commentMapper.selectOne(anyWrapper())).thenReturn(comment(9L, 1L, 0L, 9L, "该评论已删除", "DELETED"));

        assertThatThrownBy(() -> commentService.createComment(new ArticleCommentSaveRequest(1L, 9L, "回复"), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已删除评论不可回复");
    }

    @Test
    @DisplayName("空评论和超长评论应拒绝")
    void createCommentShouldValidateContent() {
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article(1L, 0L));

        assertThatThrownBy(() -> commentService.createComment(new ArticleCommentSaveRequest(1L, 0L, "   "), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("评论内容不能为空");

        String longContent = "评".repeat(501);
        assertThatThrownBy(() -> commentService.createComment(new ArticleCommentSaveRequest(1L, 0L, longContent), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("评论内容不能超过500字");
    }

    @Test
    @DisplayName("隐藏评论仍计入评论数，删除评论递减评论数")
    void updateStatusShouldMaintainCommentCount() {
        Article article = article(1L, 3L);
        ArticleComment comment = comment(8L, 1L, 0L, 8L, "评论", "NORMAL");
        when(commentMapper.selectOne(anyWrapper())).thenReturn(comment);
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);

        commentService.updateStatus(8L, new ArticleCommentStatusRequest("HIDDEN"), ADMIN);
        verify(articleMapper, never()).update(eq(null), anyWrapper());

        comment.setStatus("HIDDEN");
        commentService.updateStatus(8L, new ArticleCommentStatusRequest("DELETED"), ADMIN);

        verify(articleMapper).update(eq(null), anyWrapper());
    }

    @Test
    @DisplayName("已删除评论不可恢复")
    void deletedCommentShouldNotBeRestored() {
        Article article = article(1L, 0L);
        ArticleComment comment = comment(8L, 1L, 0L, 8L, "该评论已删除", "DELETED");
        when(commentMapper.selectOne(anyWrapper())).thenReturn(comment);
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);

        assertThatThrownBy(() -> commentService.updateStatus(8L, new ArticleCommentStatusRequest("NORMAL"), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已删除评论不可恢复");

        verify(commentMapper, never()).updateById(any(ArticleComment.class));
    }

    @Test
    @DisplayName("评论作者只能删除自己的评论，不能隐藏评论")
    void authorShouldDeleteOwnCommentButCannotHide() {
        AppUserPrincipal author = new AppUserPrincipal(2L, "author", List.of("R_USER"));
        Article article = article(1L, 1L);
        ArticleComment comment = comment(8L, 1L, 0L, 8L, "评论", "NORMAL");
        comment.setUserId(2L);
        comment.setUserName("author");
        when(commentMapper.selectOne(anyWrapper())).thenReturn(comment);
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);
        denyCommentManage(author);

        assertThatThrownBy(() -> commentService.updateStatus(8L, new ArticleCommentStatusRequest("HIDDEN"), author))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权限变更该评论状态");

        commentService.deleteComment(8L, author);

        verify(articleMapper).update(eq(null), anyWrapper());
    }

    @Test
    @DisplayName("文章发布者可在隐藏配置开启时隐藏评论但不可删除他人评论")
    void publisherShouldHideWhenConfigEnabledButCannotDeleteOthers() {
        AppUserPrincipal publisher = new AppUserPrincipal(3L, "publisher", List.of("R_USER"));
        Article article = article(1L, 1L);
        article.setCreateBy("publisher");
        ArticleComment comment = comment(8L, 1L, 0L, 8L, "评论", "NORMAL");
        comment.setUserId(2L);
        when(commentMapper.selectOne(anyWrapper())).thenReturn(comment);
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);
        when(configMapper.selectOne(anyWrapper())).thenReturn(config("article_comment_hide_enabled", "true"));
        denyCommentManage(publisher);

        commentService.updateStatus(8L, new ArticleCommentStatusRequest("HIDDEN"), publisher);
        verify(commentMapper).updateById(comment);

        comment.setStatus("NORMAL");
        assertThatThrownBy(() -> commentService.deleteComment(8L, publisher))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权限删除该评论");
    }

    @Test
    @DisplayName("隐藏配置关闭后普通管理员不可隐藏，超级管理员仍可隐藏")
    void hideDisabledShouldBlockManagerButNotSuperAdmin() {
        AppUserPrincipal manager = new AppUserPrincipal(4L, "manager", List.of("R_MODERATOR"));
        Article article = article(1L, 1L);
        ArticleComment comment = comment(8L, 1L, 0L, 8L, "评论", "NORMAL");
        when(commentMapper.selectOne(anyWrapper())).thenReturn(comment);
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);
        when(configMapper.selectOne(anyWrapper())).thenReturn(config("article_comment_hide_enabled", "false"));
        assertThatThrownBy(() -> commentService.updateStatus(8L, new ArticleCommentStatusRequest("HIDDEN"), manager))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权限变更该评论状态");

        commentService.updateStatus(8L, new ArticleCommentStatusRequest("HIDDEN"), ADMIN);
        verify(commentMapper).updateById(comment);
    }

    private Article article(Long id, Long commentCount) {
        Article article = new Article();
        article.setId(id);
        article.setCommentCount(commentCount);
        article.setVisible(1);
        article.setStatus("PUBLISHED");
        article.setDeleted(0);
        return article;
    }

    private SysConfig config(String configKey, String configValue) {
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        config.setDeleted(0);
        return config;
    }

    private void denyCommentManage(AppUserPrincipal principal) {
        doThrow(new BusinessException(ApiCode.FORBIDDEN, "无权限访问：article:comment:manage"))
                .when(permissionService)
                .requirePermission(eq(principal), anyString());
    }

    private ArticleComment comment(Long id, Long articleId, Long parentId, Long rootId, String content, String status) {
        ArticleComment comment = new ArticleComment();
        comment.setId(id);
        comment.setArticleId(articleId);
        comment.setParentId(parentId);
        comment.setRootId(rootId);
        comment.setContent(content);
        comment.setStatus(status);
        comment.setUserId(1L);
        comment.setUserName("admin");
        comment.setCreateTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        comment.setDeleted(0);
        return comment;
    }

    private SysUser user(Long id, String avatar) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setAvatar(avatar);
        user.setDeleted(0);
        return user;
    }

    @SuppressWarnings("unchecked")
    private <T> Wrapper<T> anyWrapper() {
        return any(Wrapper.class);
    }
}
