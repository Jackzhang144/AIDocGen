package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.constant.MessageConstants;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.model.AuthRequest;
import com.codecraft.documentationgenerator.model.AuthResponse;
import com.codecraft.documentationgenerator.model.RegisterRequest;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import com.codecraft.documentationgenerator.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * <p>
 * 处理用户注册和登录相关的RESTful API请求
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求对象
     * @return AuthResponse 认证响应对象
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        log.info("Processing user registration for email: {}", registerRequest.getEmail());

        // 检查用户是否已存在
        if (userService.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: user already exists with email: {}", registerRequest.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse(null, MessageConstants.USER_ALREADY_EXISTS));
        }

        // 创建新用户
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setName(registerRequest.getName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userService.createUser(user);

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        log.info("User registered successfully with email: {}", registerRequest.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, MessageConstants.REGISTRATION_SUCCESS));
    }

    /**
     * 用户登录
     *
     * @param authRequest 认证请求对象
     * @return AuthResponse 认证响应对象
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        log.info("Processing user login for email: {}", authRequest.getEmail());

        // 查找用户
        User user = userService.findByEmail(authRequest.getEmail());
        if (user == null || !passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid credentials for email: {}", authRequest.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse(null, MessageConstants.INVALID_CREDENTIALS));
        }

        // 更新登录信息
        userService.updateLoginInfo(user);

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        log.info("User logged in successfully with email: {}", authRequest.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, MessageConstants.LOGIN_SUCCESS));
    }
}