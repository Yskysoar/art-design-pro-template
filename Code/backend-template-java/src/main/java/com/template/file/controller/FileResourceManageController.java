package com.template.file.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.file.dto.FileResourceListQuery;
import com.template.file.service.FileResourceManageService;
import com.template.file.vo.FileResourceItemVo;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件资源管理接口。
 */
@RestController
@RequestMapping("/api/file-resource")
public class FileResourceManageController {

    private static final String FILE_MANAGE_PERMISSION = "system:file:manage";

    private final FileResourceManageService fileResourceManageService;
    private final PermissionService permissionService;

    public FileResourceManageController(
            FileResourceManageService fileResourceManageService,
            PermissionService permissionService
    ) {
        this.fileResourceManageService = fileResourceManageService;
        this.permissionService = permissionService;
    }

    /**
     * 分页查询文件资源台账。
     *
     * @param query     查询参数
     * @param principal 当前登录用户
     * @return 文件资源分页数据
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<FileResourceItemVo>> listResources(
            @ModelAttribute FileResourceListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, FILE_MANAGE_PERMISSION);
        return ApiResponse.success(fileResourceManageService.pageResources(query));
    }

    /**
     * 删除未引用文件资源。
     *
     * @param id        文件 ID
     * @param principal 当前登录用户
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResource(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, FILE_MANAGE_PERMISSION);
        fileResourceManageService.deleteResource(id);
        return ApiResponse.success(null);
    }
}
