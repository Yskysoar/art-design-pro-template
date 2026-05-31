package com.template.system.user.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.auth.service.AuthService;
import com.template.system.auth.vo.UserInfoResponse;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.service.UserService;
import com.template.system.user.vo.UserListItemVo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
}
