package com.template.system.role.service;

import com.template.common.pagination.PageResult;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.role.dto.RoleListQuery;
import com.template.system.role.dto.RolePermissionSaveRequest;
import com.template.system.role.dto.RoleSaveRequest;
import com.template.system.role.vo.RoleListItemVo;
import com.template.system.role.vo.RolePermissionVo;

/**
 * 角色业务服务。
 */
public interface RoleService {

    /**
     * 分页查询角色列表。
     *
     * @param query 查询参数
     * @return 角色分页数据
     */
    PageResult<RoleListItemVo> pageRoles(RoleListQuery query);

    /**
     * 新增角色。
     *
     * @param request   角色保存请求
     * @param principal 当前登录用户
     */
    void createRole(RoleSaveRequest request, AppUserPrincipal principal);

    /**
     * 更新角色。
     *
     * @param id        角色 ID
     * @param request   角色保存请求
     * @param principal 当前登录用户
     */
    void updateRole(Long id, RoleSaveRequest request, AppUserPrincipal principal);

    /**
     * 删除角色。
     *
     * @param id        角色 ID
     * @param principal 当前登录用户
     */
    void deleteRole(Long id, AppUserPrincipal principal);

    /**
     * 查询角色菜单权限。
     *
     * @param id 角色 ID
     * @return 角色菜单权限
     */
    RolePermissionVo getRolePermissions(Long id);

    /**
     * 保存角色菜单权限。
     *
     * @param id        角色 ID
     * @param request   权限保存请求
     * @param principal 当前登录用户
     */
    void saveRolePermissions(Long id, RolePermissionSaveRequest request, AppUserPrincipal principal);
}
