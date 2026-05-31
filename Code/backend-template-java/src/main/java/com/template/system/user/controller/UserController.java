package com.template.system.user.controller;

import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.auth.service.AuthService;
import com.template.system.auth.vo.UserInfoResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
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
}
