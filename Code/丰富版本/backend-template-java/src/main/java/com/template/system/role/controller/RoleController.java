package com.template.system.role.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.role.dto.RoleDataScopeSaveRequest;
import com.template.system.role.dto.RoleListQuery;
import com.template.system.role.dto.RolePermissionSaveRequest;
import com.template.system.role.dto.RoleSaveRequest;
import com.template.system.role.service.RoleService;
import com.template.system.role.vo.RoleDataScopeVo;
import com.template.system.role.vo.RoleListItemVo;
import com.template.system.role.vo.RolePermissionVo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理接口。
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private static final String ROLE_ADD_PERMISSION = "system:role:add";
    private static final String ROLE_EDIT_PERMISSION = "system:role:edit";
    private static final String ROLE_PERMISSION_PERMISSION = "system:role:permission";
    private static final String ROLE_VIEW_PERMISSION = "system:role:view";

    private final RoleService roleService;
    private final PermissionService permissionService;

    public RoleController(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    /**
     * 查询角色分页列表。
     *
     * @param query 查询参数
     * @return 角色分页列表
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<RoleListItemVo>> listRoles(
            @ModelAttribute RoleListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_VIEW_PERMISSION);
        return ApiResponse.success(roleService.pageRoles(query));
    }

    /**
     * 新增角色。
     *
     * @param request   角色保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> createRole(
            @Valid @RequestBody RoleSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_ADD_PERMISSION);
        roleService.createRole(request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 更新角色。
     *
     * @param id        角色 ID
     * @param request   角色保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_EDIT_PERMISSION);
        roleService.updateRole(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 删除角色。
     *
     * @param id        角色 ID
     * @param principal 当前登录用户
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_EDIT_PERMISSION);
        roleService.deleteRole(id, principal);
        return ApiResponse.success(null);
    }

    /**
     * 查询角色菜单权限。
     *
     * @param id 角色 ID
     * @return 菜单权限 ID 集合
     */
    @GetMapping("/{id}/permissions")
    public ApiResponse<RolePermissionVo> getRolePermissions(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_PERMISSION_PERMISSION);
        return ApiResponse.success(roleService.getRolePermissions(id));
    }

    /**
     * 查询角色数据权限。
     *
     * @param id 角色 ID
     * @return 数据权限范围和自定义组织 ID
     */
    @GetMapping("/{id}/data-scope")
    public ApiResponse<RoleDataScopeVo> getRoleDataScope(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_PERMISSION_PERMISSION);
        return ApiResponse.success(roleService.getRoleDataScope(id));
    }

    /**
     * 保存角色菜单权限。
     *
     * @param id        角色 ID
     * @param request   权限保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PutMapping("/{id}/permissions")
    public ApiResponse<Void> saveRolePermissions(
            @PathVariable Long id,
            @RequestBody RolePermissionSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_PERMISSION_PERMISSION);
        roleService.saveRolePermissions(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 保存角色数据权限。
     *
     * @param id        角色 ID
     * @param request   数据权限保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PutMapping("/{id}/data-scope")
    public ApiResponse<Void> saveRoleDataScope(
            @PathVariable Long id,
            @Valid @RequestBody RoleDataScopeSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ROLE_PERMISSION_PERMISSION);
        roleService.saveRoleDataScope(id, request, principal);
        return ApiResponse.success(null);
    }
}
