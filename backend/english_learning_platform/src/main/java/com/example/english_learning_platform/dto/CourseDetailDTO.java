package com.example.english_learning_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String tag;
    private String coverImage;
    private String videoUrl;
    private String level;
    private String status;
    private boolean favorite;
}
