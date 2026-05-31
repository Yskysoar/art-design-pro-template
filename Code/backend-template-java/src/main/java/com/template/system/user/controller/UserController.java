package com.template.system.user.controller;

import com.template.common.response.ApiResponse;
import com.template.system.auth.vo.UserInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户接口。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 当前用户信息。
     *
     * @return 用户资料、角色和按钮权限
     */
    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo() {
        UserInfoResponse response = new UserInfoResponse(
                1L,
                "admin",
                "admin@example.com",
                "/src/assets/images/avatar/avatar.webp",
                List.of("R_SUPER"),
                List.of("system:user:add", "system:user:edit", "system:user:delete")
        );
        return ApiResponse.success(response);
    }
}
