package com.template.system.role.dto;

import java.util.List;

/**
 * 角色菜单权限保存请求。
 *
 * @param menuIds 菜单 ID 集合
 */
public record RolePermissionSaveRequest(List<Long> menuIds) {
}
