package com.template.system.role.vo;

import java.util.List;

/**
 * 角色数据权限响应。
 *
 * @param roleId    角色 ID
 * @param dataScope 数据权限范围
 * @param orgIds    自定义组织 ID 集合
 */
public record RoleDataScopeVo(Long roleId, String dataScope, List<Long> orgIds) {
}
