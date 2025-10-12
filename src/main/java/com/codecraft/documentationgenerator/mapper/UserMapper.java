package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO users(email, name, password, created_at, last_login_at, refresh_token, plan, stripe_customer_id, updated_at) " +
            "VALUES(#{email}, #{name}, #{password}, #{createdAt}, #{lastLoginAt}, #{refreshToken}, #{plan}, #{stripeCustomerId}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE users SET last_login_at = #{lastLoginAt}, refresh_token = #{refreshToken}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateLoginInfo(User user);

    @Update("UPDATE users SET plan = #{plan}, stripe_customer_id = #{stripeCustomerId}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateSubscriptionInfo(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM users")
    List<User> findAll();
}