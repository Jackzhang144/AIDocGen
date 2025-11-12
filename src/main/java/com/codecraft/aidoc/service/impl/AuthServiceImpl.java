package com.codecraft.aidoc.service.impl;

import com.codecraft.aidoc.enums.ErrorCode;
import com.codecraft.aidoc.exception.BusinessException;
import com.codecraft.aidoc.pojo.entity.UserEntity;
import com.codecraft.aidoc.service.AuthService;
import com.codecraft.aidoc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Auth service that delegates to {@link UserService} for persistence and uses Spring encoders for verification.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity register(String username, String email, String password) {
        return userService.register(username, email, password);
    }

    @Override
    public UserEntity authenticate(String username, String password) {
        return userService.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "用户名或密码错误"));
    }
}
