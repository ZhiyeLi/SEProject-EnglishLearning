package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 单词实体类
 * 对应数据库表: words
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "words")
public class Word {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Long wordId;
    
    @Column(nullable = false, length = 80)
    private String word;
    
    @Column(name = "part_of_speech", length = 20)
    private String partOfSpeech;
    
    @Column(length = 100)
    private String phonetic;
    
    @Column(columnDefinition = "TEXT")
    private String definition;
    
    @Column(columnDefinition = "TEXT")
    private String example;
    
    @Column(name = "type_id", nullable = false)
    private Long typeId;
    
    @Column(columnDefinition = "TEXT")
    private String synonyms;
    
    @Column(columnDefinition = "TEXT")
    private String antonyms;
    
    @Column(name = "usage_notes", columnDefinition = "TEXT")
    private String usageNotes;
    
    @Column(name = "audio_url", length = 500)
    private String audioUrl;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    // 如果需要关联 WordType，可以添加：
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "type_id", insertable = false, updatable = false)
    // private WordType wordType;
}
