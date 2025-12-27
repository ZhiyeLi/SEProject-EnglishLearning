package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 对应数据库表: user_favorites
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_favorites", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_question", columnNames = {"user_id", "question_id"})
})
public class UserFavorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
