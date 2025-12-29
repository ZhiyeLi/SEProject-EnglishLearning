package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
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
    
    @Column(name = "record_id", nullable = false)
    private Long recordId;
    
    @Column(name = "sub_item_id", nullable = false)
    private Long subItemId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // 冗余字段，便于统计
    
    @Column(name = "user_content", columnDefinition = "TEXT")
    private String userContent; // 用户答案（JSON字符串）
    
    @Column(name = "is_correct")
    private Integer isCorrect; // 0-错误, 1-正确
    
    @Column(name = "score_obtained", precision = 4, scale = 1)
    private BigDecimal scoreObtained;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isCorrect == null) {
            isCorrect = 0;
        }
        if (scoreObtained == null) {
            scoreObtained = new BigDecimal("0.0");
        }
    }
}
