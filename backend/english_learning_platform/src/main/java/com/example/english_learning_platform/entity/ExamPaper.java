package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 试卷实体类
 * 对应数据库表: exam_papers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_papers")
public class ExamPaper {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 32)
    private String category; // CET4, CET6, TOEFL, IELTS, KY
    
    @Column
    private Integer year;
    
    @Column
    private Integer difficulty; // 1-5
    
    @Column
    private Integer status; // 1-启用, 0-禁用
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (difficulty == null) {
            difficulty = 3;
        }
        if (status == null) {
            status = 1;
        }
    }
}
