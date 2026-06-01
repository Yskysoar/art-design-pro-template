package com.template.system.menu.service;

import com.template.security.auth.AppUserPrincipal;
import com.template.system.menu.dto.MenuSaveRequest;
import com.template.system.menu.vo.AppRouteVo;

import java.util.List;

/**
 * 菜单服务。
 */
public interface MenuService {

    /**
     * 当前用户可访问菜单树。
     *
     * @param principal 当前登录用户
     * @return 当前用户可访问的前端路由树
     */
    List<AppRouteVo> getCurrentUserMenus(AppUserPrincipal principal);

    /**
     * 查询菜单管理页完整菜单树。
     *
     * @return 完整菜单树
     */
    List<AppRouteVo> getManageMenuTree();

    /**
     * 新增菜单。
     *
     * @param request   菜单保存请求
     * @param principal 当前登录用户
     */
    void createMenu(MenuSaveRequest request, AppUserPrincipal principal);

    /**
     * 更新菜单。
     *
     * @param id        菜单 ID
     * @param request   菜单保存请求
     * @param principal 当前登录用户
     */
    void updateMenu(Long id, MenuSaveRequest request, AppUserPrincipal principal);

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     */
    void deleteMenu(Long id);
}
