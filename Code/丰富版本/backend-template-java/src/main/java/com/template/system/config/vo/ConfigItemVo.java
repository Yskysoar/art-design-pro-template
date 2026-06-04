package com.template.system.config.vo;

/**
 * 配置项列表行数据。
 *
 * @param id          配置 ID
 * @param configKey   配置键
 * @param configValue 配置值
 * @param description 配置说明
 * @param editable    是否可编辑
 * @param createBy    创建人
 * @param createTime  创建时间
 * @param updateBy    更新人
 * @param updateTime  更新时间
 */
public record ConfigItemVo(
        Long id,
        String configKey,
        String configValue,
        String description,
        Boolean editable,
        String createBy,
        String createTime,
        String updateBy,
        String updateTime
) {
}
