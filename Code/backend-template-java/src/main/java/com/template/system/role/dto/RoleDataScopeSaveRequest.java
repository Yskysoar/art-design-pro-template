package com.template.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 角色数据权限保存请求。
 *
 * @param dataScope 数据权限范围
 * @param orgIds    自定义组织 ID 集合，仅 dataScope=CUSTOM_ORG 时生效
 */
public record RoleDataScopeSaveRequest(
        @NotBlank(message = "数据权限范围不能为空")
        @Size(max = 30, message = "数据权限范围不能超过 30 个字符")
        String dataScope,

        List<Long> orgIds
) {
}
