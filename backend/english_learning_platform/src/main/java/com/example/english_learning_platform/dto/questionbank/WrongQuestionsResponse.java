package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题本响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongQuestionsResponse {
    private StatsDTO stats;
    private List<WrongQuestionItem> items;
    private Long total;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsDTO {
        private Long total; // 总错题数
        private Long recent; // 近7天新增
        private Integer accuracy; // 整体正确率
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongQuestionItem {
        private Long id; // user_answer_details.id
        private Long questionId; // questions.id
        private String paperName;
        private String sectionName;
        private String sectionType;
        private String category;
        private String preview; // 题目预览（content 前50字）
        private Long wrongCount; // 错误次数
        private LocalDateTime lastWrongDate;
    }
}
