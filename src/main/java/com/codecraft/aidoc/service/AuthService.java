package com.codecraft.aidoc.service;

import com.codecraft.aidoc.pojo.entity.UserEntity;

/**
 * Authentication helper for issuing JWT tokens.
 */
public interface AuthService {

    UserEntity register(String username, String email, String password);

    UserEntity authenticate(String username, String password);
}
