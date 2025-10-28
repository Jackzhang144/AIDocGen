package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.constant.MessageConstants;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.UserMapper;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 * <p>
 * 实现用户相关的业务逻辑
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserServiceInterface {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return User 用户对象
     */
    public User findById(Long id) {
        log.info("Finding user by ID: {}", id);
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(MessageConstants.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 根据邮箱查找用户
     *
     * @param email 用户邮箱
     * @return User 用户对象
     */
    public User findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        User user = findByEmailOrNull(email);
        if (user == null) {
            throw new BusinessException(MessageConstants.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 根据邮箱查找用户（可为空）
     *
     * @param email 用户邮箱
     * @return User 或 null
     */
    public User findByEmailOrNull(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return userMapper.findByEmail(email);
    }

    /**
     * 根据外部用户UID查找用户
     *
     * @param userUid 外部用户唯一标识
     * @return User 用户对象
     */
    public User findByUserUid(String userUid) {
        log.info("Finding user by external uid: {}", userUid);
        User user = userMapper.findByUserUid(userUid);
        if (user == null) {
            throw new BusinessException(MessageConstants.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 检查用户是否存在
     *
     * @param email 用户邮箱
     * @return boolean 用户是否存在
     */
    public boolean existsByEmail(String email) {
        log.info("Checking if user exists by email: {}", email);
        return userMapper.existsByEmail(email);
    }

    /**
     * 创建新用户
     *
     * @param user 用户对象
     */
    public void createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new BusinessException(MessageConstants.EMAIL_CANNOT_BE_EMPTY);
        }
        
        // 检查用户是否已存在
        User existingUser = userMapper.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new BusinessException(MessageConstants.USER_ALREADY_EXISTS);
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        user.setLastActiveAt(now);
        user.setLastLoginAt(user.getLastLoginAt() == null ? now : user.getLastLoginAt());
        user.setUpdatedAt(now);
        userMapper.insert(user);
    }

    /**
     * 更新用户登录信息
     *
     * @param user 用户对象
     */
    public void updateLoginInfo(User user) {
        log.info("Updating login info for user ID: {}", user.getId());
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setLastActiveAt(now);
        user.setUpdatedAt(now);
        userMapper.updateLoginInfo(user);
    }

    /**
     * 更新用户活跃状态
     *
     * @param user 用户对象
     */
    public void updateLastActive(User user) {
        log.info("Updating last active time for user ID: {}", user.getId());
        LocalDateTime now = LocalDateTime.now();
        user.setLastActiveAt(now);
        user.setUpdatedAt(now);
        userMapper.updateLastActive(user);
    }

    /**
     * 更新用户基础信息
     */
    public void updateProfile(User user) {
        log.info("Updating profile for user ID: {}", user.getId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateProfile(user);
    }

    /**
     * 更新用户订阅信息
     *
     * @param user 用户对象
     */
    public void updateSubscriptionInfo(User user) {
        log.info("Updating subscription info for user ID: {}", user.getId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateSubscriptionInfo(user);
    }

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     */
    public void deleteById(Long id) {
        log.info("Deleting user with ID: {}", id);
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(MessageConstants.USER_NOT_FOUND);
        }
        userMapper.deleteById(id);
    }

    /**
     * 查找所有用户
     *
     * @return List<User> 所有用户列表
     */
    public List<User> findAll() {
        log.info("Finding all users");
        return userMapper.findAll();
    }

}
