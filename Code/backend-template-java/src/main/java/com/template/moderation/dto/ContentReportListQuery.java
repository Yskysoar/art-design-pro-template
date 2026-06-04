package com.template.moderation.dto;

/**
 * 内容举报分页查询参数。
 *
 * @param current    当前页码
 * @param size       每页条数
 * @param targetType 目标类型
 * @param reasonType 原因类型
 * @param status     状态
 * @param reporterId 举报人 ID
 */
public record ContentReportListQuery(
        Long current,
        Long size,
        String targetType,
        String reasonType,
        String status,
        Long reporterId
) {
}
