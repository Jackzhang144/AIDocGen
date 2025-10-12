package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_UserExists_ReturnsUser() {
        // Given
        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setEmail("test@example.com");
        expectedUser.setName("Test User");
        when(userMapper.findById(1L)).thenReturn(expectedUser);

        // When
        User actualUser = userService.findById(1L);

        // Then
        assertNotNull(actualUser);
        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        verify(userMapper).findById(1L);
    }

    @Test
    void findById_UserNotExists_ThrowsBusinessException() {
        // Given
        when(userMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.findById(1L));
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper).findById(1L);
    }

    @Test
    void findByEmail_UserExists_ReturnsUser() {
        // Given
        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setEmail("test@example.com");
        expectedUser.setName("Test User");
        when(userMapper.findByEmail("test@example.com")).thenReturn(expectedUser);

        // When
        User actualUser = userService.findByEmail("test@example.com");

        // Then
        assertNotNull(actualUser);
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        verify(userMapper).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_UserNotExists_ThrowsBusinessException() {
        // Given
        when(userMapper.findByEmail(anyString())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.findByEmail("nonexistent@example.com"));
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper).findByEmail("nonexistent@example.com");
    }

    @Test
    void createUser_ValidUser_CreatesUser() {
        // Given
        User user = new User();
        user.setEmail("newuser@example.com");
        user.setName("New User");
        when(userMapper.findByEmail("newuser@example.com")).thenReturn(null);

        // When
        userService.createUser(user);

        // Then
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void createUser_EmptyEmail_ThrowsBusinessException() {
        // Given
        User user = new User();
        user.setEmail("");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.createUser(user));
        assertEquals("邮箱不能为空", exception.getMessage());
    }

    @Test
    void createUser_UserAlreadyExists_ThrowsBusinessException() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("existing@example.com");
        
        User newUser = new User();
        newUser.setEmail("existing@example.com");
        
        when(userMapper.findByEmail("existing@example.com")).thenReturn(existingUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.createUser(newUser));
        assertEquals("用户已存在", exception.getMessage());
    }

    @Test
    void deleteById_UserExists_DeletesUser() {
        // Given
        User user = new User();
        user.setId(1L);
        when(userMapper.findById(1L)).thenReturn(user);

        // When
        userService.deleteById(1L);

        // Then
        verify(userMapper).deleteById(1L);
    }

    @Test
    void deleteById_UserNotExists_ThrowsBusinessException() {
        // Given
        when(userMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.deleteById(1L));
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper, never()).deleteById(anyLong());
    }

    @Test
    void findAll_ReturnsAllUsers() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        
        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(userMapper.findAll()).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.findAll();

        // Then
        assertNotNull(actualUsers);
        assertEquals(2, actualUsers.size());
        assertEquals(expectedUsers, actualUsers);
        verify(userMapper).findAll();
    }
}