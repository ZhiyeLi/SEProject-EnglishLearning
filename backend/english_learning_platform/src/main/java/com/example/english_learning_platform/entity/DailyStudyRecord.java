package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * 每日学习记录实体类
 * 对应数据库表: daily_study_record
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_study_record")
public class DailyStudyRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;
    
    @Column(name = "new_words")
    private Integer newWords = 0;
    
    @Column(name = "review_words")
    private Integer reviewWords = 0;
    
    @Column(name = "total_words")
    private Integer totalWords = 0;
    
    @Column
    private Integer streak = 0;
    
    @Column(name = "type_id")
    private Long typeId;
}
