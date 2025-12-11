package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡计划实体类
 * 对应数据库表: checkin_plans
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "checkin_plans")
public class CheckinPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "type_id", nullable = false)
    private Long typeId;
    
    @Column(name = "words_per_day", nullable = false)
    private Integer wordsPerDay;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(length = 255)
    private String status = "active"; // active, paused, completed
    
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
