package com.template.system.menu.service;

import com.template.security.auth.AppUserPrincipal;
import com.template.system.menu.vo.AppRouteVo;

import java.util.List;

/**
 * 菜单服务。
 */
public interface MenuService {

    List<AppRouteVo> getCurrentUserMenus(AppUserPrincipal principal);
}
