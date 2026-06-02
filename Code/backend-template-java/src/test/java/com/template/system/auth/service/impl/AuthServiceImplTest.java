package com.template.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.security.jwt.JwtTokenService;
import com.template.system.auth.dto.RegisterRequest;
import com.template.system.auth.dto.ResetPasswordRequest;
import com.template.system.auth.service.CaptchaService;
import com.template.system.org.entity.SysOrg;
import com.template.system.org.mapper.SysOrgMapper;
import com.template.system.permission.mapper.SysPermissionMapper;
import com.template.system.relation.entity.SysUserOrg;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysRolePermissionMapper;
import com.template.system.relation.mapper.SysUserOrgMapper;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysOrgMapper orgMapper = mock(SysOrgMapper.class);
    private final SysPermissionMapper permissionMapper = mock(SysPermissionMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final SysUserOrgMapper userOrgMapper = mock(SysUserOrgMapper.class);
    private final SysRolePermissionMapper rolePermissionMapper = mock(SysRolePermissionMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final CaptchaService captchaService = mock(CaptchaService.class);
    private final AuthServiceImpl service = new AuthServiceImpl(
            userMapper,
            roleMapper,
            orgMapper,
            permissionMapper,
            userRoleMapper,
            userOrgMapper,
            rolePermissionMapper,
            passwordEncoder,
            jwtTokenService,
            captchaService
    );

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<SysUser> anyUserWrapper() {
        return any(LambdaQueryWrapper.class);
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<SysRole> anyRoleWrapper() {
        return any(LambdaQueryWrapper.class);
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<SysOrg> anyOrgWrapper() {
        return any(LambdaQueryWrapper.class);
    }

    @Test
    void registerCreatesNormalUserWithDefaultRoleAndOrg() {
        SysRole role = new SysRole();
        role.setId(3L);
        role.setRoleCode("R_USER");
        SysOrg org = new SysOrg();
        org.setId(1L);
        org.setOrgCode("ORG_TEMPLATE");

        when(userMapper.selectCount(anyUserWrapper())).thenReturn(0L);
        when(roleMapper.selectOne(anyRoleWrapper())).thenReturn(role);
        when(orgMapper.selectOne(anyOrgWrapper())).thenReturn(org);
        when(passwordEncoder.encode("secret123")).thenReturn("hash");

        service.register(new RegisterRequest("new_user", "secret123", "captcha-id", "a1B2"));

        verify(userMapper).insert(any(SysUser.class));
        verify(userRoleMapper).insert(any(SysUserRole.class));
        verify(userOrgMapper).insert(any(SysUserOrg.class));
    }

    @Test
    void registerRejectsDuplicateUserName() {
        when(userMapper.selectCount(anyUserWrapper())).thenReturn(1L);

        assertThatThrownBy(() -> service.register(new RegisterRequest("admin", "secret123", "captcha-id", "a1B2")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void resetPasswordUpdatesPasswordHash() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserName("admin");
        user.setPasswordHash("oldHash");

        when(userMapper.selectOne(anyUserWrapper())).thenReturn(user);
        when(passwordEncoder.matches("newSecret123", "oldHash")).thenReturn(false);
        when(passwordEncoder.encode("newSecret123")).thenReturn("newHash");

        service.resetPassword(new ResetPasswordRequest("admin", "newSecret123", "captcha-id", "a1B2"));

        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void resetPasswordRejectsSamePassword() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserName("admin");
        user.setPasswordHash("oldHash");

        when(userMapper.selectOne(anyUserWrapper())).thenReturn(user);
        when(passwordEncoder.matches("admin123", "oldHash")).thenReturn(true);

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest("admin", "admin123", "captcha-id", "a1B2")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("新密码不能与当前密码相同");
    }
}
