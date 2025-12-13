package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选项控制器 - 提供设置页面的各种选项值
 */
@RestController
@RequestMapping("/api/options")
public class OptionsController {

    /**
     * 获取所有设置选项
     */
    @GetMapping("/all")
    public ApiResponse<Map<String, Object>> getAllOptions() {
        Map<String, Object> options = new HashMap<>();
        
        // 词汇难度选项
        options.put("vocabularyDifficulty", List.of(
            "四级词汇",
            "六级词汇",
            "考研词汇",
            "GRE词汇"
        ));
        
        // 复习策略选项
        options.put("reviewStrategy", List.of(
            "标准模式（1,3,7,15,30天）",
            "加速模式（1,2,4,7,15天）",
            "缓速模式（1,5,10,20,30天）"
        ));
        
        // 个人资料可见范围选项
        options.put("profileVisibility", List.of(
            "公开",
            "仅好友可见",
            "隐私"
        ));
        
        // 好友请求管理选项
        options.put("friendRequestMode", List.of(
            "所有人",
            "仅现有好友推荐",
            "需要通过验证"
        ));
        
        // 语言选项
        options.put("language", List.of(
            "中文简体",
            "中文繁体",
            "English"
        ));
        
        // 字体大小选项
        Map<String, String> fontSmall = new HashMap<>();
        fontSmall.put("label", "小");
        fontSmall.put("value", "small");
        
        Map<String, String> fontNormal = new HashMap<>();
        fontNormal.put("label", "标准");
        fontNormal.put("value", "normal");
        
        Map<String, String> fontLarge = new HashMap<>();
        fontLarge.put("label", "大");
        fontLarge.put("value", "large");
        
        options.put("fontSize", List.of(fontSmall, fontNormal, fontLarge));
        
        return ApiResponse.success("获取选项成功", options);
    }

    /**
     * 获取词汇难度选项
     */
    @GetMapping("/vocabulary-difficulty")
    public ApiResponse<List<String>> getVocabularyDifficultyOptions() {
        List<String> options = List.of(
            "四级词汇",
            "六级词汇",
            "考研词汇",
            "GRE词汇"
        );
        return ApiResponse.success("获取词汇难度选项成功", options);
    }

    /**
     * 获取复习策略选项
     */
    @GetMapping("/review-strategy")
    public ApiResponse<List<String>> getReviewStrategyOptions() {
        List<String> options = List.of(
            "标准模式（1,3,7,15,30天）",
            "加速模式（1,2,4,7,15天）",
            "缓速模式（1,5,10,20,30天）"
        );
        return ApiResponse.success("获取复习策略选项成功", options);
    }

    /**
     * 获取个人资料可见范围选项
     */
    @GetMapping("/profile-visibility")
    public ApiResponse<List<String>> getProfileVisibilityOptions() {
        List<String> options = List.of(
            "公开",
            "仅好友可见",
            "隐私"
        );
        return ApiResponse.success("获取个人资料可见范围选项成功", options);
    }

    /**
     * 获取好友请求管理选项
     */
    @GetMapping("/friend-request-mode")
    public ApiResponse<List<String>> getFriendRequestModeOptions() {
        List<String> options = List.of(
            "所有人",
            "仅现有好友推荐",
            "需要通过验证"
        );
        return ApiResponse.success("获取好友请求管理选项成功", options);
    }

    /**
     * 获取语言选项
     */
    @GetMapping("/language")
    public ApiResponse<List<String>> getLanguageOptions() {
        List<String> options = List.of(
            "中文简体",
            "中文繁体",
            "English"
        );
        return ApiResponse.success("获取语言选项成功", options);
    }

    /**
     * 获取字体大小选项
     */
    @GetMapping("/font-size")
    public ApiResponse<List<Map<String, String>>> getFontSizeOptions() {
        Map<String, String> small = new HashMap<>();
        small.put("label", "小");
        small.put("value", "small");
        
        Map<String, String> normal = new HashMap<>();
        normal.put("label", "标准");
        normal.put("value", "normal");
        
        Map<String, String> large = new HashMap<>();
        large.put("label", "大");
        large.put("value", "large");
        
        List<Map<String, String>> options = List.of(small, normal, large);
        return ApiResponse.success("获取字体大小选项成功", options);
    }
}
