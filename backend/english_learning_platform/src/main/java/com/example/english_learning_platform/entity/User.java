package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表: users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "user_name", nullable = false, length = 50, unique = true)
    private String userName;
    
    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword;
    
    @Column(name = "user_email", nullable = false, length = 70, unique = true)
    private String userEmail;
    
    @Column(length = 255)
    private String avatar = "https://picsum.photos/seed/default/100/100";
    
    @Column(name = "user_status", length = 20)
    private String userStatus = "沉迷学习";
    
    @Column(length = 200)
    private String signature = "这个人很懒，什么都没写";
    
    @Column
    private Integer streak = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 创建时自动设置时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新时自动设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
