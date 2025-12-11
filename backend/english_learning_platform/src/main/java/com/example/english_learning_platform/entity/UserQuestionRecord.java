package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户题目记录实体类
 * 对应数据库表: user_question_records
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_question_records")
public class UserQuestionRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "question_id", nullable = false, length = 255)
    private String questionId;
    
    @Column(length = 255)
    private String status = "not-done"; // not-done, done, correct, wrong
    
    @Column(name = "last_result", length = 255)
    private String lastResult;
    
    @Column(name = "last_attempt_date")
    private LocalDateTime lastAttemptDate;
    
    @Column(name = "correct_count")
    private Integer correctCount = 0;
    
    @Column(name = "wrong_count")
    private Integer wrongCount = 0;
    
    @Column(name = "is_favorited")
    private Boolean isFavorited = false;
}
