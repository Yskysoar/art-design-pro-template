package com.template.system.auth.service;

import com.template.system.auth.dto.LoginRequest;
import com.template.system.auth.dto.RegisterRequest;
import com.template.system.auth.dto.ResetPasswordRequest;
import com.template.system.auth.vo.LoginResponse;
import com.template.system.auth.vo.UserInfoResponse;

/**
 * 认证服务。
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void resetPassword(ResetPasswordRequest request);

    UserInfoResponse getUserInfo(Long userId);
}
