package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 题库主表实体类 (篇章/大题)
 * 对应数据库表: questions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class QuestionBank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "paper_id", nullable = false)
    private Long paperId;
    
    @Column(name = "section_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SectionType sectionType; // listening, reading, writing, speaking
    
    @Column(name = "section_name", length = 100)
    private String sectionName;
    
    @Column(length = 255)
    private String title;
    
    @Column(name = "material_text", columnDefinition = "LONGTEXT")
    private String materialText;
    
    @Column(name = "material_image", length = 500)
    private String materialImage;
    
    @Column(name = "audio_url", length = 500)
    private String audioUrl;
    
    @Column(name = "audio_start_sec")
    private Integer audioStartSec;
    
    @Column(name = "audio_end_sec")
    private Integer audioEndSec;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (audioStartSec == null) {
            audioStartSec = 0;
        }
        if (audioEndSec == null) {
            audioEndSec = 0;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
    
    public enum SectionType {
        listening, reading, writing, speaking
    }
}
