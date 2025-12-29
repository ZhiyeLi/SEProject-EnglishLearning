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
    private Integer objectiveCount; // 客观题数（用于计算正确率）
    private Long duration; // 用时（秒）
    private List<AnswerDetail> details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDetail {
        private Long subItemId;
        private Long parentQuestionId; // 父题目ID
        private String materialText; // 父题目材料原文
        private String materialImage; // 父题目图片
        private String audioUrl; // 音频URL
        private Integer audioStartSec; // 音频开始秒
        private Integer audioEndSec; // 音频结束秒
        private String itemType; // 题型：choice, multi_choice, insert, essay, writing 等
        private String content; // 题目内容
        private Object userAnswer; // String 或 List<String>
        private List<String> correctAnswer;
        private Boolean isCorrect; // 客观题：true/false，主观题：null
        private Boolean isObjective; // 是否为客观题（可自动判分）
        private BigDecimal scoreObtained;
        private String explanation;
        private List<OptionItem> options; // 选项列表（选择题用）
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionItem {
        private String key;
        private String value;
    }
}
