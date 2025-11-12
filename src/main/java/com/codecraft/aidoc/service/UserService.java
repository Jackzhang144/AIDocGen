package com.codecraft.aidoc.service;

import com.codecraft.aidoc.pojo.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * User management operations for registration, authentication, and administration.
 */
public interface UserService {

    UserEntity register(String username, String email, String rawPassword);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findById(Long id);

    List<UserEntity> listUsers();

    UserEntity updateUser(Long id, String email, String role, Integer apiQuota);

    UserEntity upsertAdmin(String username, String email, String rawPassword);
}
