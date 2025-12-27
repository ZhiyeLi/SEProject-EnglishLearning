package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * 提交答案响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerResponse {
    private Long recordId; // user_exam_records.id
    private BigDecimal score; // 总得分
    private Integer accuracy; // 正确率（百分比）
    private Integer totalQuestions; // 总题数
    private Integer correctCount; // 正确数
    private List<AnswerDetail> details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDetail {
        private Long subItemId;
        private Object userAnswer; // String 或 List<String>
        private List<String> correctAnswer;
        private Boolean isCorrect;
        private BigDecimal scoreObtained;
        private String explanation;
    }
}
