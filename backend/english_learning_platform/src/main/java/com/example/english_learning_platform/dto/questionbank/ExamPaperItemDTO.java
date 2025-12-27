package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 考试模式列表项 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperItemDTO {
    private Long id;
    private String name;
    private String category; // CET4, CET6, KY, IELTS, TOEFL
    private Integer year;
    private Integer difficulty; // 1-5
    private String status; // not_done, done, ongoing
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
