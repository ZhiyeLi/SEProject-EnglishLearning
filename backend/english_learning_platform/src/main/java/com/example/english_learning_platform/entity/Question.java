package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 题目实体类
 * 对应数据库表: questions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String type; // reading, listening, writing, speaking, vocabulary
    
    @Column(nullable = false, length = 255)
    private String difficulty; // easy, medium, hard
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(length = 500)
    private String preview;
    
    @Column(nullable = false, length = 500)
    private String content;
    
    @Column(name = "audio_url", length = 255)
    private String audioUrl;
    
    @Column(length = 500)
    private String tags;
    
    @Column(name = "related_course_id", length = 255)
    private String relatedCourseId;
    
    @Column(name = "related_chapter", length = 255)
    private String relatedChapter;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
