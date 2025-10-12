package com.codecraft.documentationgenerator.service.impl;

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
            throw new BusinessException("用户不存在");
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
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 创建新用户
     *
     * @param user 用户对象
     */
    public void createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        
        // 检查用户是否已存在
        User existingUser = userMapper.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new BusinessException("用户已存在");
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    /**
     * 更新用户登录信息
     *
     * @param user 用户对象
     */
    public void updateLoginInfo(User user) {
        log.info("Updating login info for user ID: {}", user.getId());
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateLoginInfo(user);
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
            throw new BusinessException("用户不存在");
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