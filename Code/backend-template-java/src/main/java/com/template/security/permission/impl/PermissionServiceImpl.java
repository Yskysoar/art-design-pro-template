package com.template.security.permission.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.permission.entity.SysPermission;
import com.template.system.permission.mapper.SysPermissionMapper;
import com.template.system.relation.entity.SysRolePermission;
import com.template.system.relation.mapper.SysRolePermissionMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于角色和权限码的登录用户权限校验实现。
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final int ENABLED = 1;
    private static final int NOT_DELETED = 0;
    private static final String SUPER_ROLE_CODE = "R_SUPER";

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public PermissionServiceImpl(
            SysRoleMapper roleMapper,
            SysPermissionMapper permissionMapper,
            SysRolePermissionMapper rolePermissionMapper
    ) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public void requirePermission(AppUserPrincipal principal, String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN, "权限标识不能为空");
        }
        if (isSuperAdmin(principal) || getPermissionCodes(principal).contains(permissionCode)) {
            return;
        }
        throw new BusinessException(ApiCode.FORBIDDEN, "无权限访问：" + permissionCode);
    }

    @Override
    public void requireSuperAdmin(AppUserPrincipal principal) {
        if (!isSuperAdmin(principal)) {
            throw new BusinessException(ApiCode.FORBIDDEN, "仅超级管理员可操作");
        }
    }

    @Override
    public boolean isSuperAdmin(AppUserPrincipal principal) {
        return principal != null
                && principal.roles() != null
                && principal.roles().contains(SUPER_ROLE_CODE);
    }

    @Override
    public int getMaxRoleLevel(AppUserPrincipal principal) {
        List<SysRole> roles = getEnabledRoles(principal);
        return roles.stream()
                .map(SysRole::getRoleLevel)
                .filter(level -> level != null)
                .max(Integer::compareTo)
                .orElse(0);
    }

    private Set<String> getPermissionCodes(AppUserPrincipal principal) {
        List<SysRole> roles = getEnabledRoles(principal);
        if (roles.isEmpty()) {
            return Set.of();
        }

        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        List<Long> permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return Set.of();
        }

        return permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getId, permissionIds)
                        .eq(SysPermission::getEnabled, ENABLED)
                        .eq(SysPermission::getDeleted, NOT_DELETED))
                .stream()
                .map(SysPermission::getPermissionCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private List<SysRole> getEnabledRoles(AppUserPrincipal principal) {
        if (principal == null || principal.roles() == null || principal.roles().isEmpty()) {
            return List.of();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, principal.roles())
                .eq(SysRole::getEnabled, ENABLED)
                .eq(SysRole::getDeleted, NOT_DELETED));
    }
}
