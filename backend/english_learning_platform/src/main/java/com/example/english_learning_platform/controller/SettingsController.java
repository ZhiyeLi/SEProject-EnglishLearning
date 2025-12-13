package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    
    private final SettingsService settingsService;
    
    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
    
    /**
     * 获取用户设置
     */
    @GetMapping("/get")
    public ApiResponse<Map<String, Object>> getUserSettings(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> settings = settingsService.getOrCreateUserSettings(userId);
            return ApiResponse.success(settings);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户设置
     */
    @PutMapping("/update")
    public ApiResponse<Map<String, Object>> updateUserSettings(
            HttpServletRequest request,
            @RequestBody Map<String, Object> updates) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> settings = settingsService.updateUserSettings(userId, updates);
            return ApiResponse.success(settings);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 重置用户设置为默认值
     */
    @PostMapping("/reset")
    public ApiResponse<Map<String, Object>> resetUserSettings(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> settings = settingsService.resetUserSettings(userId);
            return ApiResponse.success(settings);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
