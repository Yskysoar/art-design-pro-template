package com.template.system.auth.controller;

import com.template.common.response.ApiResponse;
import com.template.security.jwt.JwtTokenService;
import com.template.system.auth.dto.LoginRequest;
import com.template.system.auth.vo.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 开发期登录接口。
     *
     * <p>当前仅用于打通前后端认证结构，后续需要改为数据库用户校验和密码哈希校验。</p>
     *
     * @param request 登录参数
     * @return accessToken
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = jwtTokenService.createAccessToken(1L, request.userName(), List.of("R_SUPER"));
        return ApiResponse.success(new LoginResponse(token));
    }
}
