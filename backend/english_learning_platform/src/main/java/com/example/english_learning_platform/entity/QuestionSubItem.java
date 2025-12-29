package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * 小题实体类
 * 对应数据库表: question_sub_items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_sub_items")
public class QuestionSubItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "parent_question_id", nullable = false)
    private Long parentQuestionId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "item_type", length = 32)
    private String itemType; // choice, multiple, text, blank, insert
    
    @Column(columnDefinition = "JSON")
    private String options; // JSON格式: [{"key":"A","value":"..."}]
    
    @Column(columnDefinition = "JSON")
    private String answer; // JSON格式: ["A"] 或 ["A","C"]
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Column(name = "score_value", precision = 4, scale = 1)
    private BigDecimal scoreValue;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @PrePersist
    protected void onCreate() {
        if (itemType == null) {
            itemType = "choice";
        }
        if (scoreValue == null) {
            scoreValue = new BigDecimal("1.0");
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
