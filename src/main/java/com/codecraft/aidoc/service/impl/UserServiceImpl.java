package com.codecraft.aidoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codecraft.aidoc.enums.UserRole;
import com.codecraft.aidoc.exception.BusinessException;
import com.codecraft.aidoc.mapper.UserMapper;
import com.codecraft.aidoc.pojo.entity.UserEntity;
import com.codecraft.aidoc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.codecraft.aidoc.enums.ErrorCode.VALIDATION_FAILED;

/**
 * Default implementation backed by MyBatis-Plus.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity register(String username, String email, String rawPassword) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
            throw new BusinessException(VALIDATION_FAILED, "用户名和密码不能为空");
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username.trim());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(VALIDATION_FAILED, "用户名已存在");
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(username.trim());
        entity.setEmail(email);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setRole(UserRole.STANDARD);
        entity.setApiQuota(100);
        userMapper.insert(entity);
        return entity;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return Optional.empty();
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username.trim());
        return Optional.ofNullable(userMapper.selectOne(wrapper));
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public List<UserEntity> listUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public UserEntity updateUser(Long id, String email, String role, Integer apiQuota) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(VALIDATION_FAILED, "用户不存在");
        }
        if (StringUtils.hasText(email)) {
            entity.setEmail(email);
        }
        if (StringUtils.hasText(role)) {
            try {
                entity.setRole(UserRole.valueOf(role));
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(VALIDATION_FAILED, "不支持的角色: " + role);
            }
        }
        if (apiQuota != null) {
            entity.setApiQuota(apiQuota);
        }
        userMapper.updateById(entity);
        return entity;
    }

    @Override
    public UserEntity upsertAdmin(String username, String email, String rawPassword) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
            throw new BusinessException(VALIDATION_FAILED, "管理员用户名或密码不能为空");
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username.trim());
        UserEntity entity = userMapper.selectOne(wrapper);
        if (entity == null) {
            entity = new UserEntity();
            entity.setUsername(username.trim());
            entity.setEmail(email);
            entity.setPasswordHash(passwordEncoder.encode(rawPassword));
            entity.setRole(UserRole.ADMIN);
            entity.setApiQuota(-1);
            userMapper.insert(entity);
            return entity;
        }
        entity.setRole(UserRole.ADMIN);
        entity.setApiQuota(-1);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        if (StringUtils.hasText(email)) {
            entity.setEmail(email);
        }
        userMapper.updateById(entity);
        return entity;
    }
}
