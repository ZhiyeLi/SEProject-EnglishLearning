package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 单词类型实体类
 * 对应数据库表: word_types
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "word_types")
public class WordType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "total_words")
    private Integer totalWords = 0;
}
