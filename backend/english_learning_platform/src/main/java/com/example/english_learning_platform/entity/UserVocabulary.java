package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户生词本实体类
 * 对应数据库表: user_vocabulary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_vocabulary")
public class UserVocabulary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "word_id", nullable = false)
    private Long wordId;
    
    @Column(length = 255)
    private String translation;
    
    @Column(name = "source_question_id", length = 255)
    private String sourceQuestionId;
    
    @Column(name = "if_mastered")
    private Boolean ifMastered = false;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
