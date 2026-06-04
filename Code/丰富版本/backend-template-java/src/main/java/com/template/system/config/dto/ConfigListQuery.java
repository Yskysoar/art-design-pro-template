package com.template.system.config.dto;

/**
 * 配置项列表查询参数。
 *
 * @param current     当前页码
 * @param size        每页条数
 * @param configKey   配置键，支持模糊查询
 * @param description 配置说明，支持模糊查询
 * @param editable    是否可编辑
 */
public record ConfigListQuery(
        Long current,
        Long size,
        String configKey,
        String description,
        Boolean editable
) {
}
