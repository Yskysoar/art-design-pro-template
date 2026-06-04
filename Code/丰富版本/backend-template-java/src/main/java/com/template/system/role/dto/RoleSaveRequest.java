package com.template.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 角色保存请求。
 *
 * @param roleName    角色名称
 * @param roleCode    角色编码
 * @param description 角色描述
 * @param enabled     是否启用
 */
public record RoleSaveRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(min = 2, max = 50, message = "角色名称长度必须在 2 到 50 个字符之间")
        String roleName,

        @NotBlank(message = "角色编码不能为空")
        @Size(min = 2, max = 50, message = "角色编码长度必须在 2 到 50 个字符之间")
        String roleCode,

        @Size(max = 255, message = "角色描述不能超过 255 个字符")
        String description,

        Boolean enabled
) {
}
