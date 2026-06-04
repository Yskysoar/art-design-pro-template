package com.template.system.menu.vo;

/**
 * 前端按钮权限结构。
 *
 * @param id       按钮菜单 ID
 * @param title    权限名称
 * @param authMark 权限标识
 */
public record AppRouteAuthVo(Long id, String title, String authMark) {
}
