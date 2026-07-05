package com.example.english_learning_platform.service;

import java.util.Map;

public interface SettingsService {

    Map<String, Object> getUserSettings(Long userId);

    Map<String, Object> getOrCreateUserSettings(Long userId);

    Map<String, Object> updateUserSettings(Long userId, Map<String, Object> updates);

    Map<String, Object> resetUserSettings(Long userId);
}