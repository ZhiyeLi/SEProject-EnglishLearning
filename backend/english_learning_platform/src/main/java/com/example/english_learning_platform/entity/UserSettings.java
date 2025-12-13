package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户设置实体类
 * 对应数据库表: user_settings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_settings")
public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "daily_goal")
    private Integer dailyGoal = 50;
    
    @Column(name = "vocabulary_difficulty", length = 255)
    private String vocabularyDifficulty = "四级词汇";
    
    @Column(name = "review_strategy", length = 255)
    private String reviewStrategy = "标准模式（1,3,7,15,30天）";
    
    @Column(name = "remind_time", length = 10)
    private String remindTime = "08:00";
    
    @Column(name = "check_in_reminder")
    private Boolean checkInReminder = true;
    
    @Column(name = "suggestions_reminder")
    private Boolean suggestionsReminder = true;
    
    @Column(name = "message_reminder")
    private Boolean messageReminder = true;
    
    @Column(name = "dark_mode")
    private Boolean darkMode = false;
    
    @Column(name = "font_size", length = 50)
    private String fontSize = "normal";
    
    @Column(length = 50)
    private String language = "中文简体";
    
    @Column(name = "share_score")
    private Boolean shareScore = true;
    
    @Column(name = "profile_visibility", length = 255)
    private String profileVisibility = "公开";
    
    @Column(name = "friend_request_mode", length = 255)
    private String friendRequestMode = "所有人";
    
    @Column(columnDefinition = "JSON", name = "options")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> options;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
