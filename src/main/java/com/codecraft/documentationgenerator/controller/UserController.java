package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    @PostMapping
    public void createUser(@RequestBody User user) {
        userService.createUser(user);
    }

    @PutMapping("/login")
    public void updateLoginInfo(@RequestBody User user) {
        userService.updateLoginInfo(user);
    }

    @PutMapping("/subscription")
    public void updateSubscriptionInfo(@RequestBody User user) {
        userService.updateSubscriptionInfo(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
}