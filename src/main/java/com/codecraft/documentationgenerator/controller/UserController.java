package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.aop.RequireLogin;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * <p>
 * 处理用户相关的RESTful API请求
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceInterface userService;

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return User 用户对象
     */
    @RequireLogin
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Fetching user by ID: {}", id);
        return userService.findById(id);
    }

    /**
     * 根据邮箱获取用户信息
     *
     * @param email 用户邮箱
     * @return User 用户对象
     */
    @RequireLogin
    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        log.info("Fetching user by email: {}", email);
        return userService.findByEmail(email);
    }

    /**
     * 创建新用户
     *
     * @param user 用户对象
     */
    @RequireLogin
    @PostMapping
    public void createUser(@RequestBody User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        userService.createUser(user);
    }

    /**
     * 更新用户登录信息
     *
     * @param user 用户对象
     */
    @RequireLogin
    @PutMapping("/login")
    public void updateLoginInfo(@RequestBody User user) {
        log.info("Updating login info for user ID: {}", user.getId());
        userService.updateLoginInfo(user);
    }

    /**
     * 更新用户订阅信息
     *
     * @param user 用户对象
     */
    @RequireLogin
    @PutMapping("/subscription")
    public void updateSubscriptionInfo(@RequestBody User user) {
        log.info("Updating subscription info for user ID: {}", user.getId());
        userService.updateSubscriptionInfo(user);
    }

    /**
     * 删除指定ID的用户
     *
     * @param id 用户ID
     */
    @RequireLogin
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteById(id);
    }

    /**
     * 获取所有用户列表
     *
     * @return List<User> 用户列表
     */
    @RequireLogin
    @GetMapping
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userService.findAll();
    }
}