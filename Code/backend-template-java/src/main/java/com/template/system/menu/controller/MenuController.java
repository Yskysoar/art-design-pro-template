package com.template.system.menu.controller;

import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.menu.service.MenuService;
import com.template.system.menu.vo.AppRouteVo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单接口。
 */
@RestController
@RequestMapping("/api/v3/system")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 当前用户可访问菜单树。
     *
     * @param principal 当前登录用户
     * @return 前端动态路由树
     */
    @GetMapping("/menus")
    public ApiResponse<List<AppRouteVo>> getCurrentUserMenus(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(menuService.getCurrentUserMenus(principal));
    }
}
