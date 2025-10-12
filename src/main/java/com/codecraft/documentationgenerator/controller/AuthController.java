package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.model.AuthRequest;
import com.codecraft.documentationgenerator.model.AuthResponse;
import com.codecraft.documentationgenerator.model.RegisterRequest;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import com.codecraft.documentationgenerator.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * <p>
 * 处理用户认证相关的RESTful API请求，包括登录和注册功能
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserServiceInterface userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录接口
     * <p>
     * 验证用户凭据并生成JWT令牌
     *
     * @param authRequest 包含用户邮箱和密码的认证请求对象
     * @return ResponseEntity<?> 包含JWT令牌或错误消息的响应
     * @throws Exception 认证异常
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) throws Exception {
        log.info("Processing login request for user: {}", authRequest.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (Exception ex) {
            log.warn("Authentication failed for user: {}", authRequest.getEmail());
            return ResponseEntity.status(401).body(new AuthResponse(null, "用户名或密码错误"));
        }

        final String token = jwtUtil.generateToken(authRequest.getEmail());
        log.info("User {} logged in successfully", authRequest.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, "登录成功"));
    }

    /**
     * 用户注册接口
     * <p>
     * 创建新用户并生成JWT令牌
     *
     * @param registerRequest 包含用户注册信息的请求对象
     * @return ResponseEntity<?> 包含JWT令牌或错误消息的响应
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        log.info("Processing registration request for user: {}", registerRequest.getEmail());

        // 检查用户是否已存在
        User existingUser = userService.findByEmail(registerRequest.getEmail());
        if (existingUser != null) {
            log.warn("Registration failed: user {} already exists", registerRequest.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse(null, "用户已存在"));
        }

        // 创建新用户
        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setName(registerRequest.getName());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        userService.createUser(newUser);
        log.info("New user registered: {}", registerRequest.getEmail());

        final String token = jwtUtil.generateToken(newUser.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, "注册成功"));
    }
}