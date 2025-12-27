package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户考试记录实体类
 * 对应数据库表: user_exam_records
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_exam_records")
public class UserExamRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "paper_id", nullable = false)
    private Long paperId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Mode mode; // full_paper, single_part
    
    @Column(name = "target_question_id")
    private Long targetQuestionId; // 单题模式时记录题目ID
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status; // ongoing, completed, graded
    
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;
    
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (mode == null) {
            mode = Mode.full_paper;
        }
        if (status == null) {
            status = Status.ongoing;
        }
        if (totalScore == null) {
            totalScore = new BigDecimal("0.00");
        }
    }
    
    public enum Mode {
        full_paper, single_part
    }
    
    public enum Status {
        ongoing, completed, graded
    }
}
