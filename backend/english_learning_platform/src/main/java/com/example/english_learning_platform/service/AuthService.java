package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.LoginRequest;
import com.example.english_learning_platform.dto.RegisterRequest;
import com.example.english_learning_platform.dto.UserDTO;
import com.example.english_learning_platform.entity.User;

import java.util.Map;

public interface AuthService {

    Map<String, Object> register(RegisterRequest request);

    Map<String, Object> login(LoginRequest request);

    UserDTO getUserInfo(Long userId);

    UserDTO updateUserInfo(Long userId, User updates);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void verifyPassword(Long userId, String password);
}
