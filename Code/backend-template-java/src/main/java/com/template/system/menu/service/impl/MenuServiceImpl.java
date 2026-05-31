package com.template.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.security.auth.AppUserPrincipal;
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
}
