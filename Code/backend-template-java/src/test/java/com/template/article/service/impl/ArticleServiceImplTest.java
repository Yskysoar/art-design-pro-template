package com.template.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.template.article.dto.ArticleSaveRequest;
import com.template.article.dto.ArticleStatusRequest;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleAttachment;
import com.template.article.entity.ArticleCategory;
import com.template.article.mapper.ArticleAttachmentMapper;
import com.template.article.mapper.ArticleCategoryMapper;
import com.template.article.mapper.ArticleCommentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.article.service.HtmlSanitizerService;
import com.template.common.exception.BusinessException;
import com.template.common.security.SensitiveWordGuard;
import com.template.file.entity.FileResource;
import com.template.file.service.FileStorageService;
import com.template.security.auth.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文章业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    private static final AppUserPrincipal ADMIN = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));

    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private ArticleCategoryMapper categoryMapper;
    @Mock
    private ArticleAttachmentMapper attachmentMapper;
    @Mock
    private ArticleCommentMapper commentMapper;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private HtmlSanitizerService htmlSanitizerService;
    @Mock
    private SensitiveWordGuard sensitiveWordGuard;

    private ArticleServiceImpl articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleServiceImpl(
                articleMapper,
                categoryMapper,
                attachmentMapper,
                commentMapper,
                fileStorageService,
                htmlSanitizerService,
                sensitiveWordGuard
        );
    }

    @Test
    @DisplayName("新增文章应净化富文本并保存附件关系")
    void createArticleShouldSanitizeHtmlAndSaveAttachments() {
        when(categoryMapper.selectOne(anyWrapper())).thenReturn(category(2L));
        when(htmlSanitizerService.sanitize("<p onclick=\"x\">正文</p>")).thenReturn("<p>正文</p>");
        when(htmlSanitizerService.toPlainText("<p>正文</p>")).thenReturn("正文");
        when(fileStorageService.getExistingFile(8L)).thenReturn(file(8L));

        Long articleId = articleService.createArticle(
                new ArticleSaveRequest(
                        "文章标题",
                        2L,
                        "/api/common/files/2026/06/01/cover.png",
                        "摘要",
                        "<p onclick=\"x\">正文</p>",
                        true,
                        "PUBLISHED",
                        List.of(8L)
                ),
                ADMIN
        );

        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).insert(articleCaptor.capture());
        assertThat(articleCaptor.getValue().getContentHtml()).isEqualTo("<p>正文</p>");
        assertThat(articleCaptor.getValue().getContentText()).isEqualTo("正文");
        assertThat(articleCaptor.getValue().getCreateBy()).isEqualTo("admin");
        assertThat(articleId).isEqualTo(articleCaptor.getValue().getId());

        ArgumentCaptor<ArticleAttachment> attachmentCaptor = ArgumentCaptor.forClass(ArticleAttachment.class);
        verify(attachmentMapper).insert(attachmentCaptor.capture());
        assertThat(attachmentCaptor.getValue().getFileId()).isEqualTo(8L);
    }

    @Test
    @DisplayName("分类不存在时新增文章应拒绝")
    void createArticleShouldRejectMissingCategory() {
        when(categoryMapper.selectOne(anyWrapper())).thenReturn(null);

        assertThatThrownBy(() -> articleService.createArticle(validRequest(), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章分类不存在或已停用");

        verify(articleMapper, never()).insert(any(Article.class));
    }

    @Test
    @DisplayName("非法文章状态应拒绝")
    void createArticleShouldRejectUnsupportedStatus() {
        when(categoryMapper.selectOne(anyWrapper())).thenReturn(category(2L));

        assertThatThrownBy(() -> articleService.createArticle(
                new ArticleSaveRequest("文章标题", 2L, null, null, "<p>正文</p>", true, "INVALID", List.of()),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章状态不支持：INVALID");

        verify(articleMapper, never()).insert(any(Article.class));
    }

    @Test
    @DisplayName("富文本净化后为空应拒绝")
    void createArticleShouldRejectEmptySanitizedContent() {
        when(categoryMapper.selectOne(anyWrapper())).thenReturn(category(2L));
        when(htmlSanitizerService.sanitize("<script>alert(1)</script>")).thenReturn("");
        when(htmlSanitizerService.toPlainText("")).thenReturn("");

        assertThatThrownBy(() -> articleService.createArticle(
                new ArticleSaveRequest("文章标题", 2L, null, null, "<script>alert(1)</script>", true, "PUBLISHED", List.of()),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章内容不能为空");

        verify(articleMapper, never()).insert(any(Article.class));
    }

    @Test
    @DisplayName("附件不存在时应拒绝保存文章附件关系")
    void createArticleShouldRejectMissingAttachment() {
        when(categoryMapper.selectOne(anyWrapper())).thenReturn(category(2L));
        when(htmlSanitizerService.sanitize("<p>正文</p>")).thenReturn("<p>正文</p>");
        when(htmlSanitizerService.toPlainText("<p>正文</p>")).thenReturn("正文");
        when(fileStorageService.getExistingFile(404L)).thenThrow(new BusinessException(com.template.common.response.ApiCode.BAD_REQUEST, "附件不存在：404"));

        assertThatThrownBy(() -> articleService.createArticle(
                new ArticleSaveRequest("文章标题", 2L, null, null, "<p>正文</p>", true, "PUBLISHED", List.of(404L)),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("附件不存在：404");
    }

    @Test
    @DisplayName("更新文章状态为发布时应补充发布时间")
    void updateStatusShouldSetPublishTimeWhenPublish() {
        Article article = article(1L);
        when(articleMapper.selectOne(anyWrapper())).thenReturn(article);

        articleService.updateStatus(1L, new ArticleStatusRequest("PUBLISHED"), ADMIN);

        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PUBLISHED");
        assertThat(captor.getValue().getPublishTime()).isNotNull();
    }

    private ArticleSaveRequest validRequest() {
        return new ArticleSaveRequest("文章标题", 2L, null, null, "<p>正文</p>", true, "PUBLISHED", List.of());
    }

    private ArticleCategory category(Long id) {
        ArticleCategory category = new ArticleCategory();
        category.setId(id);
        category.setCategoryName("技术文章");
        category.setEnabled(1);
        category.setDeleted(0);
        return category;
    }

    private Article article(Long id) {
        Article article = new Article();
        article.setId(id);
        article.setCategoryId(2L);
        article.setStatus("DRAFT");
        article.setDeleted(0);
        return article;
    }

    private FileResource file(Long id) {
        FileResource file = new FileResource();
        file.setId(id);
        file.setOriginalName("demo.pdf");
        file.setUrl("/api/common/files/2026/06/01/demo.pdf");
        file.setDeleted(0);
        return file;
    }

    @SuppressWarnings("unchecked")
    private <T> Wrapper<T> anyWrapper() {
        return any(Wrapper.class);
    }
}
