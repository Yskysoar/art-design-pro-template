package com.template.system.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 组织保存请求。
 *
 * @param parentId 父组织 ID，根组织传 0
 * @param orgName  组织名称
 * @param orgCode  组织编码
 * @param orgType  组织类型：DEPT/CLUB/GROUP/MERCHANT
 * @param sort     排序
 * @param enabled  是否启用
 */
public record OrgSaveRequest(
        Long parentId,

        @NotBlank(message = "组织名称不能为空")
        @Size(max = 100, message = "组织名称不能超过 100 个字符")
        String orgName,

        @NotBlank(message = "组织编码不能为空")
        @Size(max = 100, message = "组织编码不能超过 100 个字符")
        String orgCode,

        @Size(max = 30, message = "组织类型不能超过 30 个字符")
        String orgType,

        Integer sort,
        Boolean enabled
) {
}
