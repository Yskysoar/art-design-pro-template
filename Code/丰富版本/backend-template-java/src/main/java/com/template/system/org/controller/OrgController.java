package com.template.system.org.controller;

import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.org.dto.OrgSaveRequest;
import com.template.system.org.service.OrgService;
import com.template.system.org.vo.OrgTreeVo;
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
 * 组织管理接口。
 */
@RestController
@RequestMapping("/api/org")
public class OrgController {

    private static final String ORG_MANAGE_PERMISSION = "system:org:manage";

    private final OrgService orgService;
    private final PermissionService permissionService;

    public OrgController(OrgService orgService, PermissionService permissionService) {
        this.orgService = orgService;
        this.permissionService = permissionService;
    }

    /**
     * 查询组织树。
     *
     * @param principal 当前登录用户
     * @return 当前用户可见组织树
     */
    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeVo>> getOrgTree(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ORG_MANAGE_PERMISSION);
        return ApiResponse.success(orgService.getOrgTree(principal));
    }

    /**
     * 新增组织。
     *
     * @param request   组织保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> createOrg(
            @Valid @RequestBody OrgSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ORG_MANAGE_PERMISSION);
        orgService.createOrg(request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 更新组织。
     *
     * @param id        组织 ID
     * @param request   组织保存请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateOrg(
            @PathVariable Long id,
            @Valid @RequestBody OrgSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ORG_MANAGE_PERMISSION);
        orgService.updateOrg(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 删除组织。
     *
     * @param id 组织 ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrg(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ORG_MANAGE_PERMISSION);
        orgService.deleteOrg(id);
        return ApiResponse.success(null);
    }
}
