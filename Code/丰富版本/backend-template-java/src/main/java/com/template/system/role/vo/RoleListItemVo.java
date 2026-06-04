package com.template.system.role.vo;

/**
 * 角色列表行数据。
 *
 * @param roleId      角色 ID
 * @param roleName    角色名称
 * @param roleCode    角色编码
 * @param description 角色描述
 * @param enabled     是否启用
 * @param createTime  创建时间
 */
public record RoleListItemVo(
        Long roleId,
        String roleName,
        String roleCode,
        String description,
        Boolean enabled,
        String createTime
) {
}
