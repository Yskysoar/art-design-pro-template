package com.template.system.role.dto;

/**
 * 角色列表查询参数。
 *
 * @param current     当前页码，从 1 开始
 * @param size        每页条数
 * @param roleId      角色 ID
 * @param roleName    角色名称，支持模糊查询
 * @param roleCode    角色编码，支持模糊查询
 * @param description 角色描述，支持模糊查询
 * @param enabled     是否启用
 * @param startTime   创建时间开始值，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
 * @param endTime     创建时间结束值，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
 */
public record RoleListQuery(
        Long current,
        Long size,
        Long roleId,
        String roleName,
        String roleCode,
        String description,
        Boolean enabled,
        String startTime,
        String endTime
) {
}
