package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户答题详情实体类
 * 对应数据库表: user_answer_details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_answer_details")
public class UserAnswerDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "record_id", nullable = false, length = 255)
    private String recordId;
    
    @Column(name = "question_item_id", nullable = false, length = 255)
    private String questionItemId;
    
    @Column(name = "user_answer", nullable = false, length = 500)
    private String userAnswer;
    
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
    
    @Column(name = "time_spent")
    private Integer timeSpent;
    
    @Column(name = "answered_at", updatable = false)
    private LocalDateTime answeredAt;
    
    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }
}
