package com.template.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.pagination.PageResult;
import com.template.system.role.dto.RoleListQuery;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.role.service.RoleService;
import com.template.system.role.vo.RoleListItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 角色业务服务实现。
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final int ENABLED = 1;
    private static final int NOT_DELETED = 0;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysRoleMapper roleMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public PageResult<RoleListItemVo> pageRoles(RoleListQuery query) {
        long current = normalizeCurrent(query.current());
        long size = normalizeSize(query.size());

        IPage<SysRole> page = roleMapper.selectPage(Page.of(current, size), buildQueryWrapper(query));
        return new PageResult<>(
                page.getRecords().stream().map(this::toVo).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    private LambdaQueryWrapper<SysRole> buildQueryWrapper(RoleListQuery query) {
        LocalDateTime startTime = parseStartTime(query.startTime());
        LocalDateTime endTime = parseEndTime(query.endTime());

        return new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, NOT_DELETED)
                .eq(query.roleId() != null, SysRole::getId, query.roleId())
                .like(StringUtils.hasText(query.roleName()), SysRole::getRoleName, query.roleName())
                .like(StringUtils.hasText(query.roleCode()), SysRole::getRoleCode, query.roleCode())
                .like(StringUtils.hasText(query.description()), SysRole::getDescription, query.description())
                .eq(query.enabled() != null, SysRole::getEnabled, Boolean.TRUE.equals(query.enabled()) ? ENABLED : 0)
                .ge(startTime != null, SysRole::getCreateTime, startTime)
                .le(endTime != null, SysRole::getCreateTime, endTime)
                .orderByDesc(SysRole::getCreateTime);
    }

    private RoleListItemVo toVo(SysRole role) {
        return new RoleListItemVo(
                role.getId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getDescription(),
                Integer.valueOf(ENABLED).equals(role.getEnabled()),
                formatDateTime(role.getCreateTime())
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

    private LocalDateTime parseStartTime(String value) {
        return parseDateTime(value);
    }

    private LocalDateTime parseEndTime(String value) {
        LocalDateTime dateTime = parseDateTime(value);
        if (dateTime == null || value.length() > 10) {
            return dateTime;
        }
        return dateTime.toLocalDate().atTime(23, 59, 59);
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            if (value.length() <= 10) {
                return LocalDate.parse(value).atStartOfDay();
            }
            return LocalDateTime.parse(value.replace("T", " "), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }
}
