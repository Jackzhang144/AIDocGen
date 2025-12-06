package com.codecraft.controller;

import com.codecraft.entity.User;
import com.codecraft.entity.UserRole;
import com.codecraft.repository.RequestLogRepository;
import com.codecraft.repository.UserRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AdminController {

    private final UserRepository userRepository;
    private final RequestLogRepository requestLogRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           RequestLogRepository requestLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.requestLogRepository = requestLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserWithStats>> listUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User current = authUser(authHeader);
        if (current == null || current.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        List<User> users = userRepository.findAll();
        var startEpoch = java.time.LocalDateTime.of(1970, 1, 1, 0, 0);
        var endEpoch = java.time.LocalDateTime.of(9999, 12, 31, 23, 59);
        List<UserWithStats> res = users.stream().map(u -> {
            long total = requestLogRepository.countByUsernameAndCreatedAtBetween(
                    u.getUsername(),
                    startEpoch,
                    endEpoch
            );
            return new UserWithStats(u.getId(), u.getUsername(), u.getRole().name(), u.getCreatedAt(), u.getUpdatedAt(), total);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AdminUserRequest req,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User current = authUser(authHeader);
        if (current == null || current.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.valueOf(req.getRole()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody AdminUserRequest req,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User current = authUser(authHeader);
        if (current == null || current.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getUsername().equals(current.getUsername())) {
            return ResponseEntity.badRequest().body("不能修改自身管理员");
        }
        user.setUsername(req.getUsername());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        user.setRole(UserRole.valueOf(req.getRole()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User current = authUser(authHeader);
        if (current == null || current.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getUsername().equals(current.getUsername())) {
            return ResponseEntity.badRequest().body("不能删除自身管理员");
        }
        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }

    private User authUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7);
            String username = com.codecraft.security.JwtUtilStatic.parseUsername(token);
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Data
    static class AdminUserRequest {
        private String username;
        private String password;
        private String role;
    }

    @Data
    static class UserWithStats {
        private final Long id;
        private final String username;
        private final String role;
        private final java.time.LocalDateTime createdAt;
        private final java.time.LocalDateTime updatedAt;
        private final long totalRequests;
    }
}
