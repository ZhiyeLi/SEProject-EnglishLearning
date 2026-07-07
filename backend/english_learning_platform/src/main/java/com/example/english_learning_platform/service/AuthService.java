package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.LoginRequest;
import com.example.english_learning_platform.dto.RegisterRequest;
import com.example.english_learning_platform.dto.UserDTO;
import com.example.english_learning_platform.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface AuthService {
    @Transactional
    Map<String, Object> register(RegisterRequest request);

    Map<String, Object> login(LoginRequest request);

    UserDTO getUserInfo(Long userId);

    @Transactional
    UserDTO updateUserInfo(Long userId, User updates);

    @Transactional
    void changePassword(Long userId, String oldPassword, String newPassword);

    void verifyPassword(Long userId, String password);
}
