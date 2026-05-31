package com.template.system.menu.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 前端动态路由结构。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppRouteVo(
        Long id,
        String path,
        String name,
        String component,
        String redirect,
        AppRouteMetaVo meta,
        List<AppRouteVo> children
) {
}
