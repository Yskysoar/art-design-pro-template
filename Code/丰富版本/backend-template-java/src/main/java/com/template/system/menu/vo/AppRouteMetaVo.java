package com.template.system.menu.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 前端路由 meta 结构。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppRouteMetaVo(
        String title,
        String icon,
        List<String> roles,
        List<AppRouteAuthVo> authList,
        Boolean keepAlive,
        Boolean fixedTab,
        Boolean isHide,
        Boolean isHideTab,
        String activePath,
        Boolean isIframe,
        String link
) {
}
