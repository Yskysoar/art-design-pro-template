package com.template.system.config.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.config.dto.ConfigListQuery;
import com.template.system.config.dto.ConfigSaveRequest;
import com.template.system.config.service.ConfigService;
import com.template.system.config.vo.ConfigItemVo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置项管理接口。
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private static final String CONFIG_MANAGE_PERMISSION = "system:config:manage";

    private final ConfigService configService;
    private final PermissionService permissionService;

    public ConfigController(ConfigService configService, PermissionService permissionService) {
        this.configService = configService;
        this.permissionService = permissionService;
    }

    /**
     * 分页查询配置项。
     *
     * @param query 查询参数
     * @return 配置项分页数据
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<ConfigItemVo>> listConfigs(
            @ModelAttribute ConfigListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, CONFIG_MANAGE_PERMISSION);
        return ApiResponse.success(configService.pageConfigs(query));
    }

    /**
     * 新增配置项。
     *
     * @param request   配置项保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> createConfig(
            @Valid @RequestBody ConfigSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, CONFIG_MANAGE_PERMISSION);
        configService.createConfig(request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 更新配置项。
     *
     * @param id        配置 ID
     * @param request   配置项保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody ConfigSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, CONFIG_MANAGE_PERMISSION);
        configService.updateConfig(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 删除配置项。
     *
     * @param id 配置 ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConfig(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, CONFIG_MANAGE_PERMISSION);
        configService.deleteConfig(id);
        return ApiResponse.success(null);
    }
}
