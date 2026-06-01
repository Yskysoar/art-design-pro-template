package com.template.security.permission;

import com.template.security.auth.AppUserPrincipal;

/**
 * 当前登录用户权限校验服务。
 */
public interface PermissionService {

    /**
     * 校验当前用户是否拥有指定权限。
     *
     * @param principal      当前登录用户
     * @param permissionCode 权限标识
     */
    void requirePermission(AppUserPrincipal principal, String permissionCode);

    /**
     * 校验当前用户是否为超级管理员。
     *
     * @param principal 当前登录用户
     */
    void requireSuperAdmin(AppUserPrincipal principal);

    /**
     * 判断当前用户是否为超级管理员。
     *
     * @param principal 当前登录用户
     * @return 是超级管理员返回 true
     */
    boolean isSuperAdmin(AppUserPrincipal principal);

    /**
     * 获取当前用户最高角色层级。
     *
     * @param principal 当前登录用户
     * @return 最高角色层级
     */
    int getMaxRoleLevel(AppUserPrincipal principal);
}
