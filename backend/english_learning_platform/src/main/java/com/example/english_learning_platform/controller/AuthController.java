package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.dto.LoginRequest;
import com.example.english_learning_platform.dto.RegisterRequest;
import com.example.english_learning_platform.dto.UserDTO;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> result = authService.register(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = authService.login(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/user")
    public ApiResponse<UserDTO> getCurrentUser(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserDTO user = authService.getUserInfo(userId);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/user")
    public ApiResponse<UserDTO> updateUserInfo(HttpServletRequest request, @RequestBody User updates) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserDTO user = authService.updateUserInfo(userId, updates);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(HttpServletRequest request, @RequestBody Map<String, String> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String oldPassword = data.get("oldPassword");
            String newPassword = data.get("newPassword");
            authService.changePassword(userId, oldPassword, newPassword);
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/verify-password")
    public ApiResponse<String> verifyPassword(HttpServletRequest request, @RequestBody Map<String, String> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String password = data.get("password");
            authService.verifyPassword(userId, password);
            return ApiResponse.success("密码验证成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
