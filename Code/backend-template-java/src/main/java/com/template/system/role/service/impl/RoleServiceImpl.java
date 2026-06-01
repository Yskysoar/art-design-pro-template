package com.template.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.menu.entity.SysMenu;
import com.template.system.menu.mapper.SysMenuMapper;
import com.template.system.org.entity.SysOrg;
import com.template.system.org.mapper.SysOrgMapper;
import com.template.system.relation.entity.SysRoleMenu;
import com.template.system.relation.entity.SysRoleOrg;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysRoleMenuMapper;
import com.template.system.relation.mapper.SysRoleOrgMapper;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.dto.RoleDataScopeSaveRequest;
import com.template.system.role.dto.RoleListQuery;
import com.template.system.role.dto.RolePermissionSaveRequest;
import com.template.system.role.dto.RoleSaveRequest;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.role.service.RoleService;
import com.template.system.role.vo.RoleDataScopeVo;
import com.template.system.role.vo.RoleListItemVo;
import com.template.system.role.vo.RolePermissionVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String SYSTEM_ROLE_TYPE = "SYSTEM";
    private static final String DEFAULT_ROLE_TYPE = "BUSINESS";
    private static final String DEFAULT_ACCESS_SCOPE = "ADMIN";
    private static final String DEFAULT_DATA_SCOPE = "SELF";
    private static final String CUSTOM_ORG_DATA_SCOPE = "CUSTOM_ORG";
    private static final Set<String> ALLOWED_DATA_SCOPES = Set.of(
            "ALL",
            "SELF",
            "CURRENT_ORG",
            "CURRENT_ORG_AND_SUB",
            CUSTOM_ORG_DATA_SCOPE
    );
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysOrgMapper orgMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleOrgMapper roleOrgMapper;
    private final SysUserRoleMapper userRoleMapper;

    public RoleServiceImpl(
            SysRoleMapper roleMapper,
            SysMenuMapper menuMapper,
            SysOrgMapper orgMapper,
            SysRoleMenuMapper roleMenuMapper,
            SysRoleOrgMapper roleOrgMapper,
            SysUserRoleMapper userRoleMapper
    ) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.orgMapper = orgMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleOrgMapper = roleOrgMapper;
        this.userRoleMapper = userRoleMapper;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleSaveRequest request, AppUserPrincipal principal) {
        assertRoleCodeUnique(request.roleCode(), null);

        SysRole role = new SysRole();
        role.setRoleName(request.roleName());
        role.setRoleCode(request.roleCode());
        role.setDescription(request.description());
        role.setEnabled(toEnabledValue(request.enabled()));
        role.setRoleLevel(10);
        role.setRoleType(DEFAULT_ROLE_TYPE);
        role.setAccessScope(DEFAULT_ACCESS_SCOPE);
        role.setDataScope(DEFAULT_DATA_SCOPE);
        role.setCreateBy(principal.userName());
        role.setUpdateBy(principal.userName());
        role.setDeleted(NOT_DELETED);

        roleMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleSaveRequest request, AppUserPrincipal principal) {
        SysRole role = getExistingRole(id);
        assertSystemRoleEditable(role);
        assertRoleCodeUnique(request.roleCode(), id);

        role.setRoleName(request.roleName());
        role.setRoleCode(request.roleCode());
        role.setDescription(request.description());
        role.setEnabled(toEnabledValue(request.enabled()));
        role.setUpdateBy(principal.userName());

        roleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id, AppUserPrincipal principal) {
        SysRole role = getExistingRole(id);
        assertSystemRoleEditable(role);
        Long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, id));
        if (userCount != null && userCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "角色已分配给用户，不能删除");
        }

        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
    }

    @Override
    public RolePermissionVo getRolePermissions(Long id) {
        getExistingRole(id);
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
        return new RolePermissionVo(id, menuIds);
    }

    @Override
    public RoleDataScopeVo getRoleDataScope(Long id) {
        SysRole role = getExistingRole(id);
        List<Long> orgIds = roleOrgMapper.selectList(new LambdaQueryWrapper<SysRoleOrg>()
                        .eq(SysRoleOrg::getRoleId, id))
                .stream()
                .map(SysRoleOrg::getOrgId)
                .toList();
        return new RoleDataScopeVo(id, role.getDataScope(), orgIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermissions(Long id, RolePermissionSaveRequest request, AppUserPrincipal principal) {
        SysRole role = getExistingRole(id);
        assertSystemRoleEditable(role);
        List<Long> menuIds = normalizeMenuIds(request == null ? null : request.menuIds());
        assertMenusExist(menuIds);

        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        for (Long menuId : menuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(id);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }

        role.setUpdateBy(principal.userName());
        roleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleDataScope(Long id, RoleDataScopeSaveRequest request, AppUserPrincipal principal) {
        SysRole role = getExistingRole(id);
        assertSystemRoleEditable(role);
        String dataScope = normalizeDataScope(request.dataScope());
        List<Long> orgIds = CUSTOM_ORG_DATA_SCOPE.equals(dataScope)
                ? normalizeOrgIds(request.orgIds())
                : Collections.emptyList();
        if (CUSTOM_ORG_DATA_SCOPE.equals(dataScope)) {
            assertOrgsExist(orgIds);
        }

        role.setDataScope(dataScope);
        role.setUpdateBy(principal.userName());
        roleMapper.updateById(role);

        roleOrgMapper.delete(new LambdaQueryWrapper<SysRoleOrg>().eq(SysRoleOrg::getRoleId, id));
        for (Long orgId : orgIds) {
            SysRoleOrg relation = new SysRoleOrg();
            relation.setRoleId(id);
            relation.setOrgId(orgId);
            roleOrgMapper.insert(relation);
        }
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

    private SysRole getExistingRole(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "角色ID不能为空");
        }

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, id)
                .eq(SysRole::getDeleted, NOT_DELETED));
        if (role == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private void assertRoleCodeUnique(String roleCode, Long excludeRoleId) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDeleted, NOT_DELETED)
                .ne(excludeRoleId != null, SysRole::getId, excludeRoleId));
        if (count != null && count > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "角色编码已存在");
        }
    }

    private void assertSystemRoleEditable(SysRole role) {
        if (SYSTEM_ROLE_TYPE.equals(role.getRoleType())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "系统角色暂不允许修改或删除");
        }
    }

    private List<Long> normalizeMenuIds(List<Long> menuIds) {
        if (menuIds == null) {
            return Collections.emptyList();
        }
        return menuIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
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

    private void assertMenusExist(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return;
        }

        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getDeleted, NOT_DELETED));
        Set<Long> existingIds = menus.stream().map(SysMenu::getId).collect(Collectors.toSet());
        List<Long> missingIds = menuIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "菜单不存在：" + missingIds);
        }
    }

    private void assertOrgsExist(List<Long> orgIds) {
        if (orgIds.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "自定义组织数据权限至少选择一个组织");
        }

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

    private String normalizeDataScope(String dataScope) {
        String normalized = StringUtils.hasText(dataScope) ? dataScope.toUpperCase() : DEFAULT_DATA_SCOPE;
        if (!ALLOWED_DATA_SCOPES.contains(normalized)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "数据权限范围不支持：" + dataScope);
        }
        return normalized;
    }

    private int toEnabledValue(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? 0 : ENABLED;
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
