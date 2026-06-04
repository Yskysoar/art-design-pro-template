package com.template.moderation.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.moderation.dto.ContentReportCreateRequest;
import com.template.moderation.dto.ContentReportHandleRequest;
import com.template.moderation.dto.ContentReportListQuery;
import com.template.moderation.service.ContentReportService;
import com.template.moderation.vo.ContentReportItemVo;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容举报审核接口。
 */
@RestController
@RequestMapping("/api/moderation/reports")
public class ContentReportController {

    private static final String MODERATION_REPORT_MANAGE = "moderation:report:manage";

    private final ContentReportService reportService;
    private final PermissionService permissionService;

    public ContentReportController(ContentReportService reportService, PermissionService permissionService) {
        this.reportService = reportService;
        this.permissionService = permissionService;
    }

    @PostMapping
    public ApiResponse<Long> createReport(
            @Valid @RequestBody ContentReportCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(reportService.createReport(request, principal));
    }

    @GetMapping
    public ApiResponse<PageResult<ContentReportItemVo>> listReports(
            @ModelAttribute ContentReportListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, MODERATION_REPORT_MANAGE);
        return ApiResponse.success(reportService.pageReports(query));
    }

    @PatchMapping("/{id}/handle")
    public ApiResponse<Void> handleReport(
            @PathVariable Long id,
            @Valid @RequestBody ContentReportHandleRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, MODERATION_REPORT_MANAGE);
        reportService.handleReport(id, request, principal);
        return ApiResponse.success(null);
    }
}
