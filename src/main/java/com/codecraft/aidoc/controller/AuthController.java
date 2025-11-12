package com.codecraft.aidoc.controller;

import com.codecraft.aidoc.common.ApiResponse;
import com.codecraft.aidoc.pojo.entity.UserEntity;
import com.codecraft.aidoc.pojo.request.LoginRequest;
import com.codecraft.aidoc.pojo.request.RegisterRequest;
import com.codecraft.aidoc.pojo.response.AuthResponse;
import com.codecraft.aidoc.security.JwtService;
import com.codecraft.aidoc.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles registration and login flows for the Vue console.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserEntity entity = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
        return ApiResponse.ok("注册成功", buildAuthResponse(entity));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UserEntity entity = authService.authenticate(request.getUsername(), request.getPassword());
        return ApiResponse.ok("登录成功", buildAuthResponse(entity));
    }

    private AuthResponse buildAuthResponse(UserEntity entity) {
        String token = jwtService.generateToken(entity.getId(), entity.getUsername(), entity.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .apiQuota(entity.getApiQuota())
                .build();
    }
}
