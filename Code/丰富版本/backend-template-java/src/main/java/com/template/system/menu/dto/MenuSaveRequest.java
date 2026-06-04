package com.template.system.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 菜单保存请求。
 *
 * @param parentId       父菜单 ID，根节点传 0
 * @param menuType       菜单类型：DIR/MENU/BUTTON/IFRAME/LINK
 * @param path           路由路径
 * @param name           路由名称
 * @param component      前端组件路径
 * @param redirect       重定向地址
 * @param title          菜单标题或国际化 Key
 * @param icon           图标
 * @param accessScope    访问端：ADMIN/PORTAL/BOTH
 * @param permissionCode 权限标识
 * @param iframeLink     iframe 或外链地址
 * @param keepAlive      是否缓存
 * @param fixedTab       是否固定标签
 * @param hidden         是否隐藏菜单
 * @param hiddenTab      是否隐藏标签
 * @param activePath     激活路径
 * @param sort           排序
 * @param enabled        是否启用
 */
public record MenuSaveRequest(
        Long parentId,

        String menuType,

        @Size(max = 255, message = "路由路径不能超过 255 个字符")
        String path,

        @NotBlank(message = "路由名称不能为空")
        @Size(max = 100, message = "路由名称不能超过 100 个字符")
        String name,

        @Size(max = 255, message = "组件路径不能超过 255 个字符")
        String component,

        @Size(max = 255, message = "重定向地址不能超过 255 个字符")
        String redirect,

        @NotBlank(message = "菜单标题不能为空")
        @Size(max = 100, message = "菜单标题不能超过 100 个字符")
        String title,

        @Size(max = 100, message = "图标不能超过 100 个字符")
        String icon,

        String accessScope,

        @Size(max = 100, message = "权限标识不能超过 100 个字符")
        String permissionCode,

        @Size(max = 500, message = "链接地址不能超过 500 个字符")
        String iframeLink,

        Boolean keepAlive,
        Boolean fixedTab,
        Boolean hidden,
        Boolean hiddenTab,

        @Size(max = 255, message = "激活路径不能超过 255 个字符")
        String activePath,

        Integer sort,
        Boolean enabled
) {
}
