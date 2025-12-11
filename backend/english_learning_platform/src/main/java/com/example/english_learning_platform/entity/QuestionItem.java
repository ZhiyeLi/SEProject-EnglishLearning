package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 题目子项实体类
 * 对应数据库表: question_items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_items")
public class QuestionItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "question_id", nullable = false, length = 255)
    private String questionId;
    
    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;
    
    @Column(name = "question_type", nullable = false, length = 255)
    private String questionType; // single-choice, multiple-choice, fill-blank, etc.
    
    @Column(length = 500)
    private String options; // JSON格式存储选项
    
    @Column(nullable = false, length = 255)
    private String answer;
    
    @Column(length = 500)
    private String explanation;
    
    @Column(name = "order_num", nullable = false)
    private Integer orderNum;
}
