package com.template.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.pagination.PageResult;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import com.template.system.user.service.UserService;
import com.template.system.user.vo.UserListItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    public UserServiceImpl(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public PageResult<UserListItemVo> pageUsers(UserListQuery query) {
        long current = normalizeCurrent(query.current());
        long size = normalizeSize(query.size());

        IPage<SysUser> page = userMapper.selectPage(Page.of(current, size), buildQueryWrapper(query));
        Map<Long, List<String>> roleMap = getUserRoleCodeMap(page.getRecords());
        List<UserListItemVo> records = page.getRecords().stream()
                .map(user -> toVo(user, roleMap.getOrDefault(user.getId(), List.of())))
                .toList();

        return new PageResult<>(records, page.getCurrent(), page.getSize(), page.getTotal());
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

    private UserListItemVo toVo(SysUser user, List<String> roleCodes) {
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
}
