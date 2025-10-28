package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * <p>
 * 提供用户相关的业务逻辑处理
 *
 * @author CodeCraft
 * @version 1.0
 */
public interface UserServiceInterface {

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return User 用户对象
     */
    User findById(Long id);

    /**
     * 根据邮箱查找用户
     *
     * @param email 用户邮箱
     * @return User 用户对象
     */
    User findByEmail(String email);

    /**
     * 根据邮箱查找用户（可为空）
     *
     * @param email 用户邮箱
     * @return User 或 null
     */
    User findByEmailOrNull(String email);

    /**
     * 根据外部用户UID查找用户
     *
     * @param userUid 外部用户唯一标识
     * @return User 用户对象
     */
    User findByUserUid(String userUid);

    /**
     * 检查用户是否存在
     *
     * @param email 用户邮箱
     * @return boolean 用户是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 创建新用户
     *
     * @param user 用户对象
     */
    void createUser(User user);

    /**
     * 更新用户登录信息
     *
     * @param user 用户对象
     */
    void updateLoginInfo(User user);

    /**
     * 更新用户活跃状态
     *
     * @param user 用户对象
     */
    void updateLastActive(User user);

    /**
     * 更新用户基础信息
     */
    void updateProfile(User user);

    /**
     * 更新用户订阅信息
     *
     * @param user 用户对象
     */
    void updateSubscriptionInfo(User user);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 查找所有用户
     *
     * @return List<User> 所有用户列表
     */
    List<User> findAll();
}
