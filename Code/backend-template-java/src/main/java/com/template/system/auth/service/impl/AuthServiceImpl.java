package com.template.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.security.jwt.JwtTokenService;
import com.template.system.auth.dto.LoginRequest;
import com.template.system.auth.service.AuthService;
import com.template.system.auth.vo.LoginResponse;
import com.template.system.auth.vo.UserInfoResponse;
import com.template.system.permission.entity.SysPermission;
import com.template.system.permission.mapper.SysPermissionMapper;
import com.template.system.relation.entity.SysRolePermission;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysRolePermissionMapper;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 认证服务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int ENABLED = 1;
    private static final int NOT_DELETED = 0;
    private static final String USER_NORMAL = "NORMAL";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthServiceImpl(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysPermissionMapper permissionMapper,
            SysUserRoleMapper userRoleMapper,
            SysRolePermissionMapper rolePermissionMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = findActiveUser(request.userName());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "用户名或密码错误");
        }

        List<String> roles = getRoleCodes(user.getId());
        String token = jwtTokenService.createAccessToken(user.getId(), user.getUserName(), roles);
        return new LoginResponse(token);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !USER_NORMAL.equals(user.getStatus()) || !Integer.valueOf(NOT_DELETED).equals(user.getDeleted())) {
            throw new BusinessException(ApiCode.UNAUTHORIZED);
        }

        List<SysRole> roles = getEnabledRoles(userId);
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();
        List<String> permissionCodes = getPermissionCodes(roles.stream().map(SysRole::getId).toList());

        return new UserInfoResponse(
                user.getId(),
                user.getUserName(),
                user.getUserEmail(),
                user.getAvatar(),
                roleCodes,
                permissionCodes
        );
    }

    private SysUser findActiveUser(String userName) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .eq(SysUser::getStatus, USER_NORMAL)
                .eq(SysUser::getDeleted, NOT_DELETED));
        if (user == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return user;
    }

    private List<String> getRoleCodes(Long userId) {
        return getEnabledRoles(userId).stream()
                .map(SysRole::getRoleCode)
                .toList();
    }

    private List<SysRole> getEnabledRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getEnabled, ENABLED)
                .eq(SysRole::getDeleted, NOT_DELETED));
    }

    private List<String> getPermissionCodes(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, SysPermission> permissionMap = permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getId, permissionIds)
                        .eq(SysPermission::getEnabled, ENABLED)
                        .eq(SysPermission::getDeleted, NOT_DELETED))
                .stream()
                .collect(Collectors.toMap(SysPermission::getId, Function.identity()));

        return permissionIds.stream()
                .map(permissionMap::get)
                .filter(permission -> permission != null)
                .map(SysPermission::getPermissionCode)
                .distinct()
                .toList();
    }
}
