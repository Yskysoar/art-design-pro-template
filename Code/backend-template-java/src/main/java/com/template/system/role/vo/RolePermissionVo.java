package com.template.system.role.vo;

import java.util.List;

/**
 * 角色菜单权限响应。
 *
 * @param roleId  角色 ID
 * @param menuIds 已授权菜单 ID 集合
 */
public record RolePermissionVo(Long roleId, List<Long> menuIds) {
}
