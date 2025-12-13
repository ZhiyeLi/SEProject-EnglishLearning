package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.UserSettings;
import com.example.english_learning_platform.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SettingsService {
    
    private final UserSettingsRepository userSettingsRepository;
    
    public SettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }
    
    /**
     * 获取所有可用的选项
     */
    private Map<String, Object> getAllOptions() {
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
        
        return options;
    }
    
    /**
     * 获取用户设置
     */
    public Map<String, Object> getUserSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户设置不存在"));
        
        return convertToMap(settings);
    }
    
    /**
     * 获取或初始化用户设置
     */
    public Map<String, Object> getOrCreateUserSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings();
                    newSettings.setUserId(userId);
                    newSettings.setOptions(getAllOptions());
                    return userSettingsRepository.save(newSettings);
                });
        
        // 确保 options 字段被填充
        if (settings.getOptions() == null) {
            settings.setOptions(getAllOptions());
            settings = userSettingsRepository.save(settings);
        }
        
        return convertToMap(settings);
    }
    
    /**
     * 更新用户设置
     */
    @Transactional
    public Map<String, Object> updateUserSettings(Long userId, Map<String, Object> updates) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings();
                    newSettings.setUserId(userId);
                    newSettings.setOptions(getAllOptions());
                    return newSettings;
                });
        
        // 更新各个字段
        if (updates.containsKey("dailyGoal")) {
            settings.setDailyGoal((Integer) updates.get("dailyGoal"));
        }
        if (updates.containsKey("vocabularyDifficulty")) {
            settings.setVocabularyDifficulty((String) updates.get("vocabularyDifficulty"));
        }
        if (updates.containsKey("reviewStrategy")) {
            settings.setReviewStrategy((String) updates.get("reviewStrategy"));
        }
        if (updates.containsKey("remindTime")) {
            settings.setRemindTime((String) updates.get("remindTime"));
        }
        if (updates.containsKey("checkInReminder")) {
            settings.setCheckInReminder((Boolean) updates.get("checkInReminder"));
        }
        if (updates.containsKey("suggestionsReminder")) {
            settings.setSuggestionsReminder((Boolean) updates.get("suggestionsReminder"));
        }
        if (updates.containsKey("messageReminder")) {
            settings.setMessageReminder((Boolean) updates.get("messageReminder"));
        }
        if (updates.containsKey("darkMode")) {
            settings.setDarkMode((Boolean) updates.get("darkMode"));
        }
        if (updates.containsKey("fontSize")) {
            settings.setFontSize((String) updates.get("fontSize"));
        }
        if (updates.containsKey("language")) {
            settings.setLanguage((String) updates.get("language"));
        }
        if (updates.containsKey("shareScore")) {
            settings.setShareScore((Boolean) updates.get("shareScore"));
        }
        if (updates.containsKey("profileVisibility")) {
            settings.setProfileVisibility((String) updates.get("profileVisibility"));
        }
        if (updates.containsKey("friendRequestMode")) {
            settings.setFriendRequestMode((String) updates.get("friendRequestMode"));
        }
        
        // 确保 options 字段始终保持最新
        if (settings.getOptions() == null) {
            settings.setOptions(getAllOptions());
        }
        
        settings = userSettingsRepository.save(settings);
        return convertToMap(settings);
    }
    
    /**
     * 重置用户设置为默认值
     */
    @Transactional
    public Map<String, Object> resetUserSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings();
                    newSettings.setUserId(userId);
                    return newSettings;
                });
        
        // 重置为默认值
        settings.setDailyGoal(50);
        settings.setVocabularyDifficulty("四级词汇");
        settings.setReviewStrategy("标准模式（1,3,7,15,30天）");
        settings.setRemindTime("08:00");
        settings.setCheckInReminder(true);
        settings.setSuggestionsReminder(true);
        settings.setMessageReminder(true);
        settings.setDarkMode(false);
        settings.setFontSize("normal");
        settings.setLanguage("中文简体");
        settings.setShareScore(true);
        settings.setProfileVisibility("公开");
        settings.setFriendRequestMode("所有人");
        settings.setOptions(getAllOptions());
        
        settings = userSettingsRepository.save(settings);
        return convertToMap(settings);
    }
    
    /**
     * 将实体转换为Map
     */
    private Map<String, Object> convertToMap(UserSettings settings) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", settings.getId());
        map.put("userId", settings.getUserId());
        map.put("dailyGoal", settings.getDailyGoal());
        map.put("vocabularyDifficulty", settings.getVocabularyDifficulty());
        map.put("reviewStrategy", settings.getReviewStrategy());
        map.put("remindTime", settings.getRemindTime());
        map.put("checkInReminder", settings.getCheckInReminder());
        map.put("suggestionsReminder", settings.getSuggestionsReminder());
        map.put("messageReminder", settings.getMessageReminder());
        map.put("darkMode", settings.getDarkMode());
        map.put("fontSize", settings.getFontSize());
        map.put("language", settings.getLanguage());
        map.put("shareScore", settings.getShareScore());
        map.put("profileVisibility", settings.getProfileVisibility());
        map.put("friendRequestMode", settings.getFriendRequestMode());
        
        // 包含可用的选项
        if (settings.getOptions() == null) {
            settings.setOptions(getAllOptions());
        }
        map.put("options", settings.getOptions());
        
        map.put("createdAt", settings.getCreatedAt());
        map.put("updatedAt", settings.getUpdatedAt());
        return map;
    }
}
