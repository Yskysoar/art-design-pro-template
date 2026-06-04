package com.template.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.common.security.SensitiveWordGuard;
import com.template.security.jwt.JwtTokenService;
import com.template.system.auth.dto.LoginRequest;
import com.template.system.auth.dto.RegisterRequest;
import com.template.system.auth.dto.ResetPasswordRequest;
import com.template.system.auth.service.AuthService;
import com.template.system.auth.service.CaptchaService;
import com.template.system.auth.vo.LoginResponse;
import com.template.system.auth.vo.UserInfoResponse;
import com.template.system.org.entity.SysOrg;
import com.template.system.org.mapper.SysOrgMapper;
import com.template.system.permission.entity.SysPermission;
import com.template.system.permission.mapper.SysPermissionMapper;
import com.template.system.relation.entity.SysUserOrg;
import com.template.system.relation.entity.SysRolePermission;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysUserOrgMapper;
import com.template.system.relation.mapper.SysRolePermissionMapper;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String DEFAULT_REGISTER_ROLE = "R_USER";
    private static final String DEFAULT_REGISTER_ORG = "ORG_TEMPLATE";
    private static final String SYSTEM_OPERATOR = "system";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysOrgMapper orgMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserOrgMapper userOrgMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CaptchaService captchaService;
    private final SensitiveWordGuard sensitiveWordGuard;

    public AuthServiceImpl(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysOrgMapper orgMapper,
            SysPermissionMapper permissionMapper,
            SysUserRoleMapper userRoleMapper,
            SysUserOrgMapper userOrgMapper,
            SysRolePermissionMapper rolePermissionMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            CaptchaService captchaService,
            SensitiveWordGuard sensitiveWordGuard
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.orgMapper = orgMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.userOrgMapper = userOrgMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.captchaService = captchaService;
        this.sensitiveWordGuard = sensitiveWordGuard;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        captchaService.validateAndConsume(request.captchaId(), request.captchaCode());
        SysUser user = findActiveUser(request.userName());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "用户名或密码错误");
        }

        List<String> roles = getRoleCodes(user.getId());
        String token = jwtTokenService.createAccessToken(user.getId(), user.getUserName(), roles);
        return new LoginResponse(token, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        captchaService.validateAndConsume(request.captchaId(), request.captchaCode());
        assertUserNameAvailable(request.userName());
        sensitiveWordGuard.validate("用户名", request.userName());
        sensitiveWordGuard.validate("昵称", request.nickName());
        SysRole role = getDefaultRegisterRole();
        SysOrg org = getDefaultRegisterOrg();

        SysUser user = new SysUser();
        user.setUserName(request.userName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickName(request.nickName());
        user.setStatus(USER_NORMAL);
        user.setCreateBy(SYSTEM_OPERATOR);
        user.setUpdateBy(SYSTEM_OPERATOR);
        user.setDeleted(NOT_DELETED);
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);

        SysUserOrg userOrg = new SysUserOrg();
        userOrg.setUserId(user.getId());
        userOrg.setOrgId(org.getId());
        userOrg.setPrimaryOrg(1);
        userOrgMapper.insert(userOrg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        captchaService.validateAndConsume(request.captchaId(), request.captchaCode());
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, request.userName())
                .eq(SysUser::getDeleted, NOT_DELETED));
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "用户不存在");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdateBy(SYSTEM_OPERATOR);
        userMapper.updateById(user);
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

    private void assertUserNameAvailable(String userName) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .eq(SysUser::getDeleted, NOT_DELETED));
        if (count != null && count > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "用户名已存在");
        }
    }

    private SysRole getDefaultRegisterRole() {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, DEFAULT_REGISTER_ROLE)
                .eq(SysRole::getEnabled, ENABLED)
                .eq(SysRole::getDeleted, NOT_DELETED));
        if (role == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "默认注册角色未配置");
        }
        return role;
    }

    private SysOrg getDefaultRegisterOrg() {
        SysOrg org = orgMapper.selectOne(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getOrgCode, DEFAULT_REGISTER_ORG)
                .eq(SysOrg::getEnabled, ENABLED)
                .eq(SysOrg::getDeleted, NOT_DELETED));
        if (org == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "默认注册组织未配置");
        }
        return org;
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
