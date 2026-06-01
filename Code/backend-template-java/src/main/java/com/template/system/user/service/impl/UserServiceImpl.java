package com.template.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.config.entity.SysConfig;
import com.template.system.config.mapper.SysConfigMapper;
import com.template.system.enums.UserOrgRelationMode;
import com.template.system.org.entity.SysOrg;
import com.template.system.org.mapper.SysOrgMapper;
import com.template.system.relation.entity.SysUserOrg;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysUserOrgMapper;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.user.dto.UserCreateRequest;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.dto.UserStatusRequest;
import com.template.system.user.dto.UserUpdateRequest;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import com.template.system.user.service.UserService;
import com.template.system.user.vo.UserListItemVo;
import com.template.system.user.vo.UserOrgVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户业务服务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final int NOT_DELETED = 0;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final String DEFAULT_PASSWORD = "admin123";
    private static final String USER_NORMAL = "NORMAL";
    private static final String CONFIG_USER_ORG_MODE = "user_org_relation_mode";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserOrgMapper userOrgMapper;
    private final SysRoleMapper roleMapper;
    private final SysOrgMapper orgMapper;
    private final SysConfigMapper configMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            SysUserMapper userMapper,
            SysUserRoleMapper userRoleMapper,
            SysUserOrgMapper userOrgMapper,
            SysRoleMapper roleMapper,
            SysOrgMapper orgMapper,
            SysConfigMapper configMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.userOrgMapper = userOrgMapper;
        this.roleMapper = roleMapper;
        this.orgMapper = orgMapper;
        this.configMapper = configMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserListItemVo> pageUsers(UserListQuery query) {
        long current = normalizeCurrent(query.current());
        long size = normalizeSize(query.size());

        IPage<SysUser> page = userMapper.selectPage(Page.of(current, size), buildQueryWrapper(query));
        Map<Long, List<String>> roleMap = getUserRoleCodeMap(page.getRecords());
        Map<Long, List<Long>> orgMap = getUserOrgIdMap(page.getRecords());
        List<UserListItemVo> records = page.getRecords().stream()
                .map(user -> toVo(
                        user,
                        roleMap.getOrDefault(user.getId(), List.of()),
                        orgMap.getOrDefault(user.getId(), List.of())
                ))
                .toList();

        return new PageResult<>(records, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateRequest request, AppUserPrincipal principal) {
        assertUserNameUnique(request.userName(), null);
        List<SysRole> roles = getRolesByCodes(request.roleCodes());

        SysUser user = new SysUser();
        user.setUserName(request.userName());
        user.setPasswordHash(passwordEncoder.encode(getInitialPassword(request.password())));
        user.setNickName(getDefaultNickName(request.nickName(), request.userName()));
        user.setUserGender(normalizeGender(request.userGender()));
        user.setUserPhone(request.userPhone());
        user.setUserEmail(request.userEmail());
        user.setAvatar(request.avatar());
        user.setStatus(toBackendStatusWithDefault(request.status()));
        user.setCreateBy(principal.userName());
        user.setUpdateBy(principal.userName());
        user.setDeleted(NOT_DELETED);

        userMapper.insert(user);
        rewriteUserRoles(user.getId(), roles);
        rewriteUserOrgs(user.getId(), normalizeOrgIds(request.orgIds()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateRequest request, AppUserPrincipal principal) {
        SysUser user = getExistingUser(id);
        assertUserNameUnique(request.userName(), id);
        List<SysRole> roles = getRolesByCodes(request.roleCodes());

        user.setUserName(request.userName());
        user.setNickName(getDefaultNickName(request.nickName(), request.userName()));
        user.setUserGender(normalizeGender(request.userGender()));
        user.setUserPhone(request.userPhone());
        user.setUserEmail(request.userEmail());
        user.setAvatar(request.avatar());
        user.setUpdateBy(principal.userName());

        userMapper.updateById(user);
        rewriteUserRoles(id, roles);
        rewriteUserOrgs(id, normalizeOrgIds(request.orgIds()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, UserStatusRequest request, AppUserPrincipal principal) {
        SysUser user = getExistingUser(id);
        user.setStatus(toBackendStatusWithDefault(request.status()));
        user.setUpdateBy(principal.userName());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id, AppUserPrincipal principal) {
        getExistingUser(id);
        if (principal.userId().equals(id)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "不能删除当前登录用户");
        }

        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userOrgMapper.delete(new LambdaQueryWrapper<SysUserOrg>().eq(SysUserOrg::getUserId, id));
    }

    @Override
    public UserOrgVo getUserOrgs(Long userId) {
        getExistingUser(userId);
        List<Long> orgIds = userOrgMapper.selectList(new LambdaQueryWrapper<SysUserOrg>()
                        .eq(SysUserOrg::getUserId, userId))
                .stream()
                .map(SysUserOrg::getOrgId)
                .toList();
        return new UserOrgVo(userId, orgIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserOrgs(Long userId, List<Long> orgIds, AppUserPrincipal principal) {
        getExistingUser(userId);
        rewriteUserOrgs(userId, normalizeOrgIds(orgIds));
        SysUser user = getExistingUser(userId);
        user.setUpdateBy(principal.userName());
        userMapper.updateById(user);
    }

    private LambdaQueryWrapper<SysUser> buildQueryWrapper(UserListQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, NOT_DELETED)
                .eq(query.id() != null, SysUser::getId, query.id())
                .like(StringUtils.hasText(query.userName()), SysUser::getUserName, query.userName())
                .eq(StringUtils.hasText(query.userGender()), SysUser::getUserGender, query.userGender())
                .like(StringUtils.hasText(query.userPhone()), SysUser::getUserPhone, query.userPhone())
                .like(StringUtils.hasText(query.userEmail()), SysUser::getUserEmail, query.userEmail())
                .orderByDesc(SysUser::getCreateTime);

        String backendStatus = toBackendStatus(query.status());
        if (StringUtils.hasText(backendStatus)) {
            wrapper.eq(SysUser::getStatus, backendStatus);
        }
        return wrapper;
    }

    private Map<Long, List<String>> getUserRoleCodeMap(List<SysUser> users) {
        if (users.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleCodeMap = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getDeleted, NOT_DELETED))
                .stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleCode));

        return userRoles.stream()
                .filter(item -> roleCodeMap.containsKey(item.getRoleId()))
                .collect(Collectors.groupingBy(
                        SysUserRole::getUserId,
                        Collectors.mapping(item -> roleCodeMap.get(item.getRoleId()), Collectors.toList())
                ));
    }

    private Map<Long, List<Long>> getUserOrgIdMap(List<SysUser> users) {
        if (users.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<SysUserOrg> userOrgs = userOrgMapper.selectList(new LambdaQueryWrapper<SysUserOrg>()
                .in(SysUserOrg::getUserId, userIds));
        if (userOrgs.isEmpty()) {
            return Collections.emptyMap();
        }

        return userOrgs.stream().collect(Collectors.groupingBy(
                SysUserOrg::getUserId,
                Collectors.mapping(SysUserOrg::getOrgId, Collectors.toList())
        ));
    }

    private SysUser getExistingUser(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "用户ID不能为空");
        }

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .eq(SysUser::getDeleted, NOT_DELETED));
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void assertUserNameUnique(String userName, Long excludeUserId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .eq(SysUser::getDeleted, NOT_DELETED)
                .ne(excludeUserId != null, SysUser::getId, excludeUserId));
        if (count != null && count > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "用户名已存在");
        }
    }

    private List<SysRole> getRolesByCodes(List<String> roleCodes) {
        List<String> normalizedCodes = roleCodes == null ? List.of() : roleCodes.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (normalizedCodes.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "至少选择一个角色");
        }

        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, normalizedCodes)
                .eq(SysRole::getEnabled, 1)
                .eq(SysRole::getDeleted, NOT_DELETED));
        Map<String, SysRole> roleMap = roles.stream()
                .collect(Collectors.toMap(SysRole::getRoleCode, Function.identity()));
        List<String> missingCodes = normalizedCodes.stream()
                .filter(code -> !roleMap.containsKey(code))
                .toList();
        if (!missingCodes.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "角色不存在或未启用：" + String.join(",", missingCodes));
        }

        return normalizedCodes.stream().map(roleMap::get).toList();
    }

    private void rewriteUserRoles(Long userId, List<SysRole> roles) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        List<SysUserRole> relations = new ArrayList<>();
        for (SysRole role : roles) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(role.getId());
            relations.add(relation);
        }
        relations.forEach(userRoleMapper::insert);
    }

    private void rewriteUserOrgs(Long userId, List<Long> orgIds) {
        userOrgMapper.delete(new LambdaQueryWrapper<SysUserOrg>().eq(SysUserOrg::getUserId, userId));
        if (orgIds.isEmpty()) {
            return;
        }

        assertOrgRelationMode(orgIds);
        assertOrgsExist(orgIds);

        List<SysUserOrg> relations = new ArrayList<>();
        for (int index = 0; index < orgIds.size(); index++) {
            Long orgId = orgIds.get(index);
            SysUserOrg relation = new SysUserOrg();
            relation.setUserId(userId);
            relation.setOrgId(orgId);
            relation.setPrimaryOrg(index == 0 ? 1 : 0);
            relations.add(relation);
        }
        relations.forEach(userOrgMapper::insert);
    }

    private void assertOrgRelationMode(List<Long> orgIds) {
        String mode = getConfigValue(CONFIG_USER_ORG_MODE);
        if (UserOrgRelationMode.ONE_TO_ONE.name().equalsIgnoreCase(mode) && orgIds.size() > 1) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "当前系统仅允许一个用户关联一个组织");
        }
    }

    private void assertOrgsExist(List<Long> orgIds) {
        List<SysOrg> orgs = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .in(SysOrg::getId, orgIds)
                .eq(SysOrg::getDeleted, NOT_DELETED));
        Set<Long> existingIds = orgs.stream().map(SysOrg::getId).collect(Collectors.toSet());
        List<Long> missingIds = orgIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "组织不存在：" + missingIds);
        }
    }

    private List<Long> normalizeOrgIds(List<Long> orgIds) {
        if (orgIds == null) {
            return Collections.emptyList();
        }
        return orgIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }

    private UserListItemVo toVo(SysUser user, List<String> roleCodes, List<Long> orgIds) {
        return new UserListItemVo(
                user.getId(),
                user.getAvatar(),
                toFrontendStatus(user.getStatus()),
                user.getUserName(),
                user.getUserGender(),
                user.getNickName(),
                user.getUserPhone(),
                user.getUserEmail(),
                roleCodes,
                orgIds,
                user.getCreateBy(),
                formatDateTime(user.getCreateTime()),
                user.getUpdateBy(),
                formatDateTime(user.getUpdateTime())
        );
    }

    private long normalizeCurrent(Long current) {
        return current == null || current < 1 ? DEFAULT_CURRENT : current;
    }

    private long normalizeSize(Long size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String toBackendStatus(String frontendStatus) {
        if (!StringUtils.hasText(frontendStatus)) {
            return null;
        }
        return switch (frontendStatus) {
            case "1", "2" -> "NORMAL";
            case "3" -> "LOCKED";
            case "4" -> "DISABLED";
            default -> frontendStatus;
        };
    }

    private String toBackendStatusWithDefault(String status) {
        String backendStatus = toBackendStatus(status);
        return StringUtils.hasText(backendStatus) ? backendStatus : USER_NORMAL;
    }

    private String toFrontendStatus(String backendStatus) {
        if ("LOCKED".equals(backendStatus)) {
            return "3";
        }
        if ("DISABLED".equals(backendStatus)) {
            return "4";
        }
        return "1";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String getInitialPassword(String password) {
        return StringUtils.hasText(password) ? password : DEFAULT_PASSWORD;
    }

    private String getDefaultNickName(String nickName, String userName) {
        return StringUtils.hasText(nickName) ? nickName : userName;
    }

    private String normalizeGender(String gender) {
        if ("1".equals(gender)) {
            return "男";
        }
        if ("2".equals(gender)) {
            return "女";
        }
        return gender;
    }

    private String getConfigValue(String configKey) {
        return configMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, configKey)
                        .eq(SysConfig::getDeleted, NOT_DELETED))
                .stream()
                .map(SysConfig::getConfigValue)
                .findFirst()
                .orElse(UserOrgRelationMode.ONE_TO_MANY.name());
    }
}
