package com.template.moderation.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleComment;
import com.template.article.mapper.ArticleCommentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.common.exception.BusinessException;
import com.template.moderation.dto.ContentReportCreateRequest;
import com.template.moderation.dto.ContentReportHandleRequest;
import com.template.moderation.dto.ContentReportListQuery;
import com.template.moderation.entity.ContentReport;
import com.template.moderation.mapper.ContentReportMapper;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialMessageMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 内容举报审核服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentReportServiceImplTest {

    private static final AppUserPrincipal ADMIN = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));

    @Mock
    private ContentReportMapper reportMapper;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private ArticleCommentMapper commentMapper;
    @Mock
    private SocialMessageMapper messageMapper;

    private ContentReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        initTableInfo(ContentReport.class);
        initTableInfo(Article.class);
        initTableInfo(ArticleComment.class);
        initTableInfo(SocialMessage.class);
        reportService = new ContentReportServiceImpl(reportMapper, articleMapper, commentMapper, messageMapper);
    }

    @Test
    @DisplayName("创建文章举报时应校验目标存在并写入待处理状态")
    void createReportShouldInsertPendingReport() {
        when(articleMapper.selectCount(anyArticleWrapper())).thenReturn(1L);

        reportService.createReport(new ContentReportCreateRequest("article", 10L, "spam", "广告内容"), ADMIN);

        ArgumentCaptor<ContentReport> captor = ArgumentCaptor.forClass(ContentReport.class);
        verify(reportMapper).insert(captor.capture());
        ContentReport report = captor.getValue();
        assertThat(report.getTargetType()).isEqualTo("ARTICLE");
        assertThat(report.getTargetId()).isEqualTo(10L);
        assertThat(report.getReasonType()).isEqualTo("SPAM");
        assertThat(report.getReporterName()).isEqualTo("admin");
        assertThat(report.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("创建举报时目标不存在应拒绝")
    void createReportShouldRejectMissingTarget() {
        when(commentMapper.selectCount(anyCommentWrapper())).thenReturn(0L);

        assertThatThrownBy(() -> reportService.createReport(new ContentReportCreateRequest("COMMENT", 99L, "ABUSE", null), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("举报目标不存在");

        verify(reportMapper, never()).insert(any(ContentReport.class));
    }

    @Test
    @DisplayName("分页查询举报应映射时间和处理字段")
    void pageReportsShouldMapFields() {
        ContentReport report = report(1L);
        Page<ContentReport> page = Page.of(1, 20);
        page.setRecords(List.of(report));
        page.setTotal(1);
        when(reportMapper.selectPage(anyPage(), anyReportWrapper())).thenReturn(page);

        var result = reportService.pageReports(new ContentReportListQuery(1L, 20L, "ARTICLE", null, "PENDING", null));

        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).targetType()).isEqualTo("ARTICLE");
        assertThat(result.records().get(0).createTime()).isEqualTo("2026-06-04 10:00:00");
    }

    @Test
    @DisplayName("处理举报时应校验状态并写入处理人")
    void handleReportShouldUpdateStatusAndHandler() {
        ContentReport report = report(1L);
        when(reportMapper.selectOne(anyReportWrapper())).thenReturn(report);

        reportService.handleReport(1L, new ContentReportHandleRequest("resolved", "已人工处理"), ADMIN);

        verify(reportMapper).updateById(report);
        assertThat(report.getStatus()).isEqualTo("RESOLVED");
        assertThat(report.getHandlerName()).isEqualTo("admin");
        assertThat(report.getHandlingRemark()).isEqualTo("已人工处理");
        assertThat(report.getHandledTime()).isNotNull();
    }

    @Test
    @DisplayName("处理举报时非法状态应拒绝")
    void handleReportShouldRejectInvalidStatus() {
        when(reportMapper.selectOne(anyReportWrapper())).thenReturn(report(1L));

        assertThatThrownBy(() -> reportService.handleReport(1L, new ContentReportHandleRequest("PENDING", null), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不支持的处理状态");
    }

    private ContentReport report(Long id) {
        ContentReport report = new ContentReport();
        report.setId(id);
        report.setTargetType("ARTICLE");
        report.setTargetId(10L);
        report.setReasonType("SPAM");
        report.setReporterId(2L);
        report.setReporterName("editor");
        report.setStatus("PENDING");
        report.setCreateTime(LocalDateTime.of(2026, 6, 4, 10, 0));
        report.setDeleted(0L);
        return report;
    }

    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }

    @SuppressWarnings("unchecked")
    private Page<ContentReport> anyPage() {
        return any(Page.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<ContentReport> anyReportWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<Article> anyArticleWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<ArticleComment> anyCommentWrapper() {
        return any(Wrapper.class);
    }
}
