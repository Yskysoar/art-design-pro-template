package com.template.moderation.service;

import com.template.common.pagination.PageResult;
import com.template.moderation.dto.ContentReportCreateRequest;
import com.template.moderation.dto.ContentReportHandleRequest;
import com.template.moderation.dto.ContentReportListQuery;
import com.template.moderation.vo.ContentReportItemVo;
import com.template.security.auth.AppUserPrincipal;

/**
 * 内容举报审核服务。
 */
public interface ContentReportService {

    Long createReport(ContentReportCreateRequest request, AppUserPrincipal principal);

    PageResult<ContentReportItemVo> pageReports(ContentReportListQuery query);

    void handleReport(Long id, ContentReportHandleRequest request, AppUserPrincipal principal);
}
