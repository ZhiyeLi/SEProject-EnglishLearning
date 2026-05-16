package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.dto.LoginRequest;
import com.example.english_learning_platform.dto.RegisterRequest;
import com.example.english_learning_platform.dto.UserDTO;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.service.AuthService;
import com.example.english_learning_platform.service.IpDefenseService;
import com.example.english_learning_platform.service.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final IpDefenseService ipDefenseService;
    private final RecaptchaService recaptchaService;
    
    public AuthController(
            AuthService authService,
            IpDefenseService ipDefenseService,
            RecaptchaService recaptchaService
    ) {
        this.authService = authService;
        this.ipDefenseService = ipDefenseService;
        this.recaptchaService = recaptchaService;
    }
    
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            String ip = getClientIp(httpRequest);
            boolean requireCaptcha = ipDefenseService.recordAttemptAndCheck("register", ip);
            if (requireCaptcha) {
                RecaptchaService.RecaptchaResult verifyResult =
                        recaptchaService.verify(request.getRecaptchaToken(), ip);
                if (!verifyResult.isOk()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("requireCaptcha", true);
                    return ApiResponse.success(verifyResult.getMessage(), data);
                }
            }

            Map<String, Object> result = authService.register(request);
            ipDefenseService.clearAttempts("register", ip);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            String ip = getClientIp(httpRequest);
            boolean requireCaptcha = ipDefenseService.recordAttemptAndCheck("login", ip);
            if (requireCaptcha) {
                RecaptchaService.RecaptchaResult verifyResult =
                        recaptchaService.verify(request.getRecaptchaToken(), ip);
                if (!verifyResult.isOk()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("requireCaptcha", true);
                    return ApiResponse.success(verifyResult.getMessage(), data);
                }
            }

            Map<String, Object> result = authService.login(request);
            ipDefenseService.clearAttempts("login", ip);
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

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return normalizeIp(forwarded.split(",")[0].trim());
        }
        return normalizeIp(request.getRemoteAddr());
    }

    private String normalizeIp(String ip) {
        if (ip == null) {
            return "unknown";
        }
        if (ip.startsWith("::ffff:")) {
            return ip.substring(7);
        }
        return ip;
    }
}
