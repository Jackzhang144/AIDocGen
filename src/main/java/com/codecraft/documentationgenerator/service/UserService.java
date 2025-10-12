package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.entity.User;

import java.util.List;

public interface UserService {

    User findById(Long id);

    User findByEmail(String email);

    void createUser(User user);

    void updateLoginInfo(User user);

    void updateSubscriptionInfo(User user);

    void deleteById(Long id);

    List<User> findAll();
}