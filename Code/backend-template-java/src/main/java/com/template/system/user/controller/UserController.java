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
import com.template.system.user.vo.UserOrgVo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ApiResponse.success(authService.getUserInfo(principal.userId()));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<UserListItemVo>> listUsers(@ModelAttribute UserListQuery query) {
        return ApiResponse.success(userService.pageUsers(query));
    }

    @PostMapping
    public ApiResponse<Void> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.createUser(request, principal);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.updateUser(id, request, principal);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.updateStatus(id, request, principal);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.deleteUser(id, principal);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/orgs")
    public ApiResponse<UserOrgVo> getUserOrgs(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserOrgs(id));
    }

    @PutMapping("/{id}/orgs")
    public ApiResponse<Void> saveUserOrgs(
            @PathVariable Long id,
            @RequestBody List<Long> orgIds,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.saveUserOrgs(id, orgIds, principal);
        return ApiResponse.success(null);
    }
}
