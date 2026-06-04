package com.template.system.menu.controller;

import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.menu.dto.MenuSaveRequest;
import com.template.system.menu.service.MenuService;
import com.template.system.menu.vo.AppRouteVo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单接口。
 */
@RestController
@RequestMapping("/api/v3/system")
public class MenuController {

    private static final String MENU_MANAGE_PERMISSION = "system:menu:manage";

    private final MenuService menuService;
    private final PermissionService permissionService;

    public MenuController(MenuService menuService, PermissionService permissionService) {
        this.menuService = menuService;
        this.permissionService = permissionService;
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

    /**
     * 菜单管理页完整菜单树。
     *
     * @return 完整菜单树
     */
    @GetMapping("/menus/manage")
    public ApiResponse<List<AppRouteVo>> getManageMenuTree(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, MENU_MANAGE_PERMISSION);
        return ApiResponse.success(menuService.getManageMenuTree());
    }

    /**
     * 新增菜单。
     *
     * @param request   菜单保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PostMapping("/menus")
    public ApiResponse<Void> createMenu(
            @Valid @RequestBody MenuSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, MENU_MANAGE_PERMISSION);
        menuService.createMenu(request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 更新菜单。
     *
     * @param id        菜单 ID
     * @param request   菜单保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PutMapping("/menus/{id}")
    public ApiResponse<Void> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody MenuSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, MENU_MANAGE_PERMISSION);
        menuService.updateMenu(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     * @return 空响应
     */
    @DeleteMapping("/menus/{id}")
    public ApiResponse<Void> deleteMenu(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, MENU_MANAGE_PERMISSION);
        menuService.deleteMenu(id);
        return ApiResponse.success(null);
    }
}
