package com.template.security.permission.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.template.common.exception.BusinessException;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.permission.entity.SysPermission;
import com.template.system.permission.mapper.SysPermissionMapper;
import com.template.system.relation.entity.SysRolePermission;
import com.template.system.relation.mapper.SysRolePermissionMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 当前登录用户权限校验服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    private static final AppUserPrincipal SUPER_PRINCIPAL =
            new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));
    private static final AppUserPrincipal ADMIN_PRINCIPAL =
            new AppUserPrincipal(2L, "manager", List.of("R_ADMIN"));

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysPermissionMapper permissionMapper;

    @Mock
    private SysRolePermissionMapper rolePermissionMapper;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(roleMapper, permissionMapper, rolePermissionMapper);
    }

    @Test
    @DisplayName("超级管理员必须同时存在于 JWT 声明和启用角色数据中")
    void isSuperAdminShouldRequireEnabledRoleFromDatabase() {
        when(roleMapper.selectList(anyWrapper())).thenReturn(List.of(enabledRole(1L, "R_SUPER", 100)));

        assertThat(permissionService.isSuperAdmin(SUPER_PRINCIPAL)).isTrue();
    }

    @Test
    @DisplayName("JWT 包含超级管理员但数据库角色停用时不应跳过权限校验")
    void isSuperAdminShouldRejectStaleTokenWhenRoleDisabled() {
        when(roleMapper.selectList(anyWrapper())).thenReturn(List.of());

        assertThat(permissionService.isSuperAdmin(SUPER_PRINCIPAL)).isFalse();
    }

    @Test
    @DisplayName("拥有有效权限码时应通过权限校验")
    void requirePermissionShouldPassWhenRoleHasPermissionCode() {
        when(roleMapper.selectList(anyWrapper())).thenReturn(List.of(enabledRole(2L, "R_ADMIN", 80)));
        when(rolePermissionMapper.selectList(anyWrapper())).thenReturn(List.of(rolePermission(2L, 10L)));
        when(permissionMapper.selectList(anyWrapper())).thenReturn(List.of(permission(10L, "system:user:view")));

        permissionService.requirePermission(ADMIN_PRINCIPAL, "system:user:view");

        verify(permissionMapper).selectList(anyWrapper());
    }

    @Test
    @DisplayName("没有有效权限码时应拒绝访问")
    void requirePermissionShouldRejectMissingPermissionCode() {
        when(roleMapper.selectList(anyWrapper())).thenReturn(List.of(enabledRole(2L, "R_ADMIN", 80)));
        when(rolePermissionMapper.selectList(anyWrapper())).thenReturn(List.of());

        assertThatThrownBy(() -> permissionService.requirePermission(ADMIN_PRINCIPAL, "system:user:view"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权限访问：system:user:view");

        verify(permissionMapper, never()).selectList(anyWrapper());
    }

    private SysRole enabledRole(Long id, String roleCode, Integer roleLevel) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setRoleLevel(roleLevel);
        role.setEnabled(1);
        role.setDeleted(0);
        return role;
    }

    private SysRolePermission rolePermission(Long roleId, Long permissionId) {
        SysRolePermission rolePermission = new SysRolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        return rolePermission;
    }

    private SysPermission permission(Long id, String permissionCode) {
        SysPermission permission = new SysPermission();
        permission.setId(id);
        permission.setPermissionCode(permissionCode);
        permission.setEnabled(1);
        permission.setDeleted(0);
        return permission;
    }

    @SuppressWarnings("unchecked")
    private <T> Wrapper<T> anyWrapper() {
        return any(Wrapper.class);
    }
}
