package com.template.moderation.vo;

/**
 * 内容举报列表行数据。
 */
public record ContentReportItemVo(
        Long id,
        String targetType,
        Long targetId,
        String reasonType,
        String description,
        Long reporterId,
        String reporterName,
        String status,
        Long handlerId,
        String handlerName,
        String handlingRemark,
        String handledTime,
        String createTime,
        String updateTime
) {
}
