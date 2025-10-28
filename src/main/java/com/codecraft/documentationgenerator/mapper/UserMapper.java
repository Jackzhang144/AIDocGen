package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper接口
 * <p>
 * 定义用户数据访问接口，用于操作users表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return User 用户对象
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    /**
     * 根据邮箱查找用户
     * @param email 用户邮箱
     * @return User 用户对象
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    /**
     * 根据外部用户UID查找用户
     *
     * @param userUid 外部用户唯一标识
     * @return User 用户对象
     */
    @Select("SELECT * FROM users WHERE user_uid = #{userUid}")
    User findByUserUid(String userUid);

    /**
     * 检查用户是否存在
     *
     * @param email 用户邮箱
     * @return User 用户对象
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    /**
     * 插入新用户
     *
     * @param user 用户对象
     */
    @Insert("INSERT INTO users(user_uid, email, name, given_name, family_name, picture, password, created_at, last_active_at, last_login_at, refresh_token, plan, stripe_customer_id, updated_at) " +
            "VALUES(#{userUid}, #{email}, #{name}, #{givenName}, #{familyName}, #{picture}, #{password}, #{createdAt}, #{lastActiveAt}, #{lastLoginAt}, #{refreshToken}, #{plan}, #{stripeCustomerId}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /**
     * 更新用户登录信息
     *
     * @param user 用户对象
     */
    @Update("UPDATE users SET last_login_at = #{lastLoginAt}, last_active_at = #{lastActiveAt}, refresh_token = #{refreshToken}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateLoginInfo(User user);

    /**
     * 更新用户最后活跃时间
     *
     * @param user 用户对象
     */
    @Update("UPDATE users SET last_active_at = #{lastActiveAt}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateLastActive(User user);

    /**
     * 更新用户基础信息
     */
    @Update("UPDATE users SET user_uid = #{userUid}, name = #{name}, given_name = #{givenName}, family_name = #{familyName}, picture = #{picture}, refresh_token = #{refreshToken}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateProfile(User user);

    /**
     * 更新用户订阅信息
     *
     * @param user 用户对象
     */
    @Update("UPDATE users SET plan = #{plan}, stripe_customer_id = #{stripeCustomerId}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateSubscriptionInfo(User user);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 查找所有用户
     *
     * @return List<User> 所有用户列表
     */
    @Select("SELECT * FROM users")
    List<User> findAll();
}
