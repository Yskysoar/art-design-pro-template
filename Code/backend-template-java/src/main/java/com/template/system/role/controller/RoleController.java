package com.template.system.role.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.system.role.dto.RoleListQuery;
import com.template.system.role.service.RoleService;
import com.template.system.role.vo.RoleListItemVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理接口。
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 查询角色分页列表。
     *
     * @param query 查询参数
     * @return 角色分页列表
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<RoleListItemVo>> listRoles(@ModelAttribute RoleListQuery query) {
        return ApiResponse.success(roleService.pageRoles(query));
    }
}
