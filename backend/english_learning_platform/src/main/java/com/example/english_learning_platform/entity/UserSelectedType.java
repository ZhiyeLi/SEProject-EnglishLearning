package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户选择的单词类型实体类
 * 对应数据库表: user_selected_types
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_selected_types")
public class UserSelectedType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "type_id", nullable = false)
    private Long typeId;
    
    @Column(name = "selected_date", updatable = false)
    private LocalDateTime selectedDate;
    
    @PrePersist
    protected void onCreate() {
        selectedDate = LocalDateTime.now();
    }
}
