package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 单题模式列表项 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionItemDTO {
    private Long id;
    private String title;
    private String sectionType; // listening, reading, writing, speaking
    private String sectionName;
    private Long paperId;
    private String paperName;
    private String paperCategory;
    private String status; // not_done, done
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
