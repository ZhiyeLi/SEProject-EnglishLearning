package com.example.english_learning_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private String tag;
    private String coverImage;
    private String videoUrl;
    private String level;
    private String status;     // 当前用户的学习状态，null 表示未开始
    private boolean favorite;  // 当前用户是否收藏
}
