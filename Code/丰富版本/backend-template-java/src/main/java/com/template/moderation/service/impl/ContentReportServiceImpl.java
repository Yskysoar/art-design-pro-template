package com.template.moderation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleComment;
import com.template.article.mapper.ArticleCommentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.moderation.dto.ContentReportCreateRequest;
import com.template.moderation.dto.ContentReportHandleRequest;
import com.template.moderation.dto.ContentReportListQuery;
import com.template.moderation.entity.ContentReport;
import com.template.moderation.mapper.ContentReportMapper;
import com.template.moderation.service.ContentReportService;
import com.template.moderation.vo.ContentReportItemVo;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 内容举报审核服务实现。
 */
@Service
public class ContentReportServiceImpl implements ContentReportService {

    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final long NOT_DELETED_LONG = 0L;
    private static final int NOT_DELETED_INT = 0;
    private static final String STATUS_PENDING = "PENDING";
    private static final Set<String> TARGET_TYPES = Set.of("ARTICLE", "COMMENT", "PRIVATE_MESSAGE");
    private static final Set<String> REPORT_REASONS = Set.of(
            "SPAM",
            "HARASSMENT",
            "PERSONAL_INFO",
            "COPYRIGHT",
            "FRAUD",
            "MISLEADING",
            "FILE_RISK",
            "OUTDATED",
            "OFF_TOPIC",
            "LOW_QUALITY",
            "OTHER"
    );
    private static final Set<String> HANDLE_STATUSES = Set.of("PROCESSING", "RESOLVED", "REJECTED");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ContentReportMapper reportMapper;
    private final ArticleMapper articleMapper;
    private final ArticleCommentMapper commentMapper;
    private final SocialMessageMapper messageMapper;

    public ContentReportServiceImpl(
            ContentReportMapper reportMapper,
            ArticleMapper articleMapper,
            ArticleCommentMapper commentMapper,
            SocialMessageMapper messageMapper
    ) {
        this.reportMapper = reportMapper;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReport(ContentReportCreateRequest request, AppUserPrincipal principal) {
        String targetType = normalizeTargetType(request.targetType());
        String reasonType = normalizeReasonType(request.reasonType());
        assertTargetExists(targetType, request.targetId(), principal);
        ContentReport report = new ContentReport();
        report.setTargetType(targetType);
        report.setTargetId(request.targetId());
        report.setReasonType(reasonType);
        report.setDescription(normalizeNullable(request.description()));
        report.setReporterId(principal.userId());
        report.setReporterName(principal.userName());
        report.setStatus(STATUS_PENDING);
        report.setCreateTime(LocalDateTime.now());
        report.setDeleted(NOT_DELETED_LONG);
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public PageResult<ContentReportItemVo> pageReports(ContentReportListQuery query) {
        long current = query.current() == null || query.current() < 1 ? DEFAULT_CURRENT : query.current();
        long size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);
        String targetType = normalizeOptionalUpper(query.targetType());
        String reasonType = normalizeOptionalUpper(query.reasonType());
        String status = normalizeOptionalUpper(query.status());
        IPage<ContentReport> page = reportMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<ContentReport>()
                .eq(ContentReport::getDeleted, NOT_DELETED_LONG)
                .eq(StringUtils.hasText(targetType), ContentReport::getTargetType, targetType)
                .eq(StringUtils.hasText(reasonType), ContentReport::getReasonType, reasonType)
                .eq(StringUtils.hasText(status), ContentReport::getStatus, status)
                .eq(query.reporterId() != null, ContentReport::getReporterId, query.reporterId())
                .orderByAsc(ContentReport::getStatus)
                .orderByDesc(ContentReport::getCreateTime)
                .orderByDesc(ContentReport::getId));
        return new PageResult<>(
                page.getRecords().stream().map(this::toVo).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long id, ContentReportHandleRequest request, AppUserPrincipal principal) {
        ContentReport report = getExistingReport(id);
        String status = normalizeHandleStatus(request.status());
        report.setStatus(status);
        report.setHandlerId(principal.userId());
        report.setHandlerName(principal.userName());
        report.setHandlingRemark(normalizeNullable(request.remark()));
        report.setHandledTime(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    private ContentReport getExistingReport(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "举报ID不能为空");
        }
        ContentReport report = reportMapper.selectOne(new LambdaQueryWrapper<ContentReport>()
                .eq(ContentReport::getId, id)
                .eq(ContentReport::getDeleted, NOT_DELETED_LONG));
        if (report == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "举报不存在");
        }
        return report;
    }

    private void assertTargetExists(String targetType, Long targetId, AppUserPrincipal principal) {
        if (targetId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "举报目标ID不能为空");
        }
        boolean exists = switch (targetType) {
            case "ARTICLE" -> articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                    .eq(Article::getId, targetId)
                    .eq(Article::getDeleted, NOT_DELETED_INT)) > 0;
            case "COMMENT" -> commentMapper.selectCount(new LambdaQueryWrapper<ArticleComment>()
                    .eq(ArticleComment::getId, targetId)
                    .eq(ArticleComment::getDeleted, NOT_DELETED_INT)) > 0;
            case "PRIVATE_MESSAGE" -> messageMapper.selectCount(new LambdaQueryWrapper<SocialMessage>()
                    .eq(SocialMessage::getId, targetId)
                    .and(wrapper -> wrapper
                            .eq(SocialMessage::getSenderId, principal.userId())
                            .or()
                            .eq(SocialMessage::getReceiverId, principal.userId()))
                    .eq(SocialMessage::getDeleted, NOT_DELETED_LONG)) > 0;
            default -> false;
        };
        if (!exists) {
            throw new BusinessException(ApiCode.NOT_FOUND, "举报目标不存在");
        }
    }

    private ContentReportItemVo toVo(ContentReport report) {
        return new ContentReportItemVo(
                report.getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReasonType(),
                report.getDescription(),
                report.getReporterId(),
                report.getReporterName(),
                report.getStatus(),
                report.getHandlerId(),
                report.getHandlerName(),
                report.getHandlingRemark(),
                formatTime(report.getHandledTime()),
                formatTime(report.getCreateTime()),
                formatTime(report.getUpdateTime())
        );
    }

    private String normalizeTargetType(String targetType) {
        String normalized = normalizeRequired(targetType, "举报目标类型不能为空");
        if (!TARGET_TYPES.contains(normalized)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "不支持的举报目标类型");
        }
        return normalized;
    }

    private String normalizeReasonType(String reasonType) {
        String normalized = normalizeRequired(reasonType, "举报原因不能为空");
        if (!REPORT_REASONS.contains(normalized)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "不支持的举报原因");
        }
        return normalized;
    }

    private String normalizeHandleStatus(String status) {
        String normalized = normalizeRequired(status, "处理状态不能为空");
        if (!HANDLE_STATUSES.contains(normalized)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "不支持的处理状态");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, message);
        }
        return value.trim().toUpperCase();
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeOptionalUpper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : null;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : DATE_TIME_FORMATTER.format(time);
    }
}
