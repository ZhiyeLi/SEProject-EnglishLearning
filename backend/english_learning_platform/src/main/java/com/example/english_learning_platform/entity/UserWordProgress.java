package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户单词进度实体类
 * 对应数据库表: user_word_progress
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_word_progress")
public class UserWordProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "word_id", nullable = false)
    private Long wordId;
    
    @Column(name = "type_id", nullable = false)
    private Long typeId;
    
    @Column
    private Integer stage = 0;
    
    @Column(name = "last_review_time")
    private LocalDateTime lastReviewTime;
    
    @Column(name = "next_review_time")
    private LocalDateTime nextReviewTime;
    
    @Column(name = "review_count")
    private Integer reviewCount = 0;
    
    @Column(name = "passed_date")
    private LocalDate passedDate;
}
