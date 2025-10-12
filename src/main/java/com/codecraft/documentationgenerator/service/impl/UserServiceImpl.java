package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.mapper.UserMapper;
import com.codecraft.documentationgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    public User findById(Long id) {
        return userMapper.findById(id);
    }

    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public void createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    public void updateLoginInfo(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateLoginInfo(user);
    }

    public void updateSubscriptionInfo(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateSubscriptionInfo(user);
    }

    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

}
