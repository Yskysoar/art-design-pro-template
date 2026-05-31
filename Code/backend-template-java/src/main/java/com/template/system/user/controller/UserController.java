package com.template.system.user.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.auth.service.AuthService;
import com.template.system.auth.vo.UserInfoResponse;
import com.template.system.user.dto.UserCreateRequest;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.dto.UserStatusRequest;
import com.template.system.user.dto.UserUpdateRequest;
import com.template.system.user.service.UserService;
import com.template.system.user.vo.UserListItemVo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * 当前用户信息。
     *
     * @return 用户资料、角色和按钮权限
     */
    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ApiResponse.success(authService.getUserInfo(principal.userId()));
    }

    /**
     * 查询用户分页列表。
     *
     * @param query 查询参数
     * @return 用户分页列表
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<UserListItemVo>> listUsers(@ModelAttribute UserListQuery query) {
        return ApiResponse.success(userService.pageUsers(query));
    }

    /**
     * 新增用户。
     *
     * @param request   新增用户请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.createUser(request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 更新用户。
     *
     * @param id        用户 ID
     * @param request   更新用户请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.updateUser(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 修改用户状态。
     *
     * @param id        用户 ID
     * @param request   状态请求
     * @param principal 当前登录用户
     * @return 空响应
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.updateStatus(id, request, principal);
        return ApiResponse.success(null);
    }

    /**
     * 删除用户。
     *
     * @param id        用户 ID
     * @param principal 当前登录用户
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.deleteUser(id, principal);
        return ApiResponse.success(null);
    }
}
