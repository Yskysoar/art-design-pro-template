package com.template.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.menu.dto.MenuSaveRequest;
import com.template.system.menu.entity.SysMenu;
import com.template.system.menu.mapper.SysMenuMapper;
import com.template.system.menu.service.MenuService;
import com.template.system.menu.vo.AppRouteMetaVo;
import com.template.system.menu.vo.AppRouteVo;
import com.template.system.relation.entity.SysRoleMenu;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysRoleMenuMapper;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现。
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final int ENABLED = 1;
    private static final int NOT_DELETED = 0;
    private static final long ROOT_PARENT_ID = 0L;
    private static final String DEFAULT_MENU_TYPE = "MENU";
    private static final String DEFAULT_ACCESS_SCOPE = "ADMIN";

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(
            SysMenuMapper menuMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            SysRoleMenuMapper roleMenuMapper
    ) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<AppRouteVo> getCurrentUserMenus(AppUserPrincipal principal) {
        List<SysRole> roles = getEnabledRoles(principal.userId());
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }

        Set<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            return List.of();
        }

        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getEnabled, ENABLED)
                .eq(SysMenu::getDeleted, NOT_DELETED));
        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .sorted(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();

        return buildRoutes(ROOT_PARENT_ID, childrenMap, roleCodes);
    }

    @Override
    public List<AppRouteVo> getManageMenuTree() {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDeleted, NOT_DELETED)
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .sorted(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        return buildRoutes(ROOT_PARENT_ID, childrenMap, List.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(MenuSaveRequest request, AppUserPrincipal principal) {
        assertParentExists(request.parentId(), null);
        assertMenuNameUnique(request.name(), null);

        SysMenu menu = new SysMenu();
        applyRequest(menu, request, principal.userName());
        menu.setDeleted(NOT_DELETED);
        menuMapper.insert(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(Long id, MenuSaveRequest request, AppUserPrincipal principal) {
        SysMenu menu = getExistingMenu(id);
        assertParentExists(request.parentId(), id);
        assertNotMoveToSelfOrChild(id, request.parentId());
        assertMenuNameUnique(request.name(), id);

        applyRequest(menu, request, principal.userName());
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        SysMenu menu = getExistingMenu(id);
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDeleted, NOT_DELETED));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "存在子菜单，不能删除");
        }

        Long relationCount = roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getMenuId, id));
        if (relationCount != null && relationCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "菜单已分配给角色，不能删除");
        }

        menu.setDeleted(1);
        menu.setEnabled(0);
        menuMapper.updateById(menu);
    }

    private List<SysRole> getEnabledRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }

        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getEnabled, ENABLED)
                .eq(SysRole::getDeleted, NOT_DELETED));
    }

    private List<AppRouteVo> buildRoutes(
            Long parentId,
            Map<Long, List<SysMenu>> childrenMap,
            List<String> roleCodes
    ) {
        return childrenMap.getOrDefault(parentId, List.of()).stream()
                .map(menu -> toRoute(menu, buildRoutes(menu.getId(), childrenMap, roleCodes), roleCodes))
                .toList();
    }

    private AppRouteVo toRoute(SysMenu menu, List<AppRouteVo> children, List<String> roleCodes) {
        AppRouteMetaVo meta = new AppRouteMetaVo(
                menu.getTitle(),
                menu.getIcon(),
                roleCodes,
                toBoolean(menu.getKeepAlive()),
                toBoolean(menu.getFixedTab()),
                toBoolean(menu.getHidden()),
                toBoolean(menu.getHiddenTab()),
                menu.getActivePath(),
                "IFRAME".equals(menu.getMenuType()),
                menu.getIframeLink()
        );

        return new AppRouteVo(
                menu.getId(),
                menu.getPath(),
                menu.getName(),
                menu.getComponent(),
                menu.getRedirect(),
                meta,
                children.isEmpty() ? null : children
        );
    }

    private Boolean toBoolean(Integer value) {
        return value == null ? null : value == 1;
    }

    private SysMenu getExistingMenu(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "菜单ID不能为空");
        }

        SysMenu menu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getId, id)
                .eq(SysMenu::getDeleted, NOT_DELETED));
        if (menu == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "菜单不存在");
        }
        return menu;
    }

    private void assertParentExists(Long parentId, Long currentId) {
        Long normalizedParentId = normalizeParentId(parentId);
        if (ROOT_PARENT_ID == normalizedParentId) {
            return;
        }
        if (currentId != null && currentId.equals(normalizedParentId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "父菜单不能是自己");
        }

        Long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getId, normalizedParentId)
                .eq(SysMenu::getDeleted, NOT_DELETED));
        if (count == null || count == 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "父菜单不存在");
        }
    }

    private void assertNotMoveToSelfOrChild(Long id, Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);
        if (ROOT_PARENT_ID == normalizedParentId) {
            return;
        }

        Long cursor = normalizedParentId;
        while (cursor != null && cursor != ROOT_PARENT_ID) {
            if (id.equals(cursor)) {
                throw new BusinessException(ApiCode.BAD_REQUEST, "父菜单不能是当前菜单或其子菜单");
            }
            SysMenu parent = menuMapper.selectById(cursor);
            cursor = parent == null ? ROOT_PARENT_ID : parent.getParentId();
        }
    }

    private void assertMenuNameUnique(String name, Long excludeMenuId) {
        Long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getName, name)
                .eq(SysMenu::getDeleted, NOT_DELETED)
                .ne(excludeMenuId != null, SysMenu::getId, excludeMenuId));
        if (count != null && count > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "菜单路由名称已存在");
        }
    }

    private void applyRequest(SysMenu menu, MenuSaveRequest request, String operator) {
        menu.setParentId(normalizeParentId(request.parentId()));
        menu.setMenuType(normalizeText(request.menuType(), DEFAULT_MENU_TYPE).toUpperCase());
        menu.setPath(request.path());
        menu.setName(request.name());
        menu.setComponent(request.component());
        menu.setRedirect(request.redirect());
        menu.setTitle(request.title());
        menu.setIcon(request.icon());
        menu.setAccessScope(normalizeText(request.accessScope(), DEFAULT_ACCESS_SCOPE).toUpperCase());
        menu.setPermissionCode(request.permissionCode());
        menu.setIframeLink(request.iframeLink());
        menu.setKeepAlive(toInt(request.keepAlive()));
        menu.setFixedTab(toInt(request.fixedTab()));
        menu.setHidden(toInt(request.hidden()));
        menu.setHiddenTab(toInt(request.hiddenTab()));
        menu.setActivePath(request.activePath());
        menu.setSort(request.sort() == null ? 0 : request.sort());
        menu.setEnabled(toIntDefaultTrue(request.enabled()));
        menu.setUpdateBy(operator);
        if (!StringUtils.hasText(menu.getCreateBy())) {
            menu.setCreateBy(operator);
        }
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null || parentId < 0 ? ROOT_PARENT_ID : parentId;
    }

    private String normalizeText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private int toInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private int toIntDefaultTrue(Boolean value) {
        return Boolean.FALSE.equals(value) ? 0 : 1;
    }
}
