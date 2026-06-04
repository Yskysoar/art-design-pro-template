package com.template.system.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 配置项保存请求。
 *
 * @param configKey   配置键
 * @param configValue 配置值
 * @param description 配置说明
 * @param editable    是否可编辑
 */
public record ConfigSaveRequest(
        @NotBlank(message = "配置键不能为空")
        @Size(max = 100, message = "配置键不能超过 100 个字符")
        String configKey,

        @NotBlank(message = "配置值不能为空")
        @Size(max = 500, message = "配置值不能超过 500 个字符")
        String configValue,

        @Size(max = 255, message = "配置说明不能超过 255 个字符")
        String description,

        Boolean editable
) {
}
