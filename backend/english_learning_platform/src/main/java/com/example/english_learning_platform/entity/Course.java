package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 32)
    private String tag;            // primary / middle / college / none

    @Column(name = "cover_image", length = 255)
    private String coverImage;     // 封面图路径或 URL

    @Column(name = "video_url", length = 500)
    private String videoUrl;       // B 站视频链接

    @Column(length = 16)
    private String level;          // beginner / intermediate / advanced

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
