package com.example.english_learning_platform.dto.questionbank;

import lombok.Data;
import java.util.List;

/**
 * 提交答案请求 DTO
 */
@Data
public class SubmitAnswerRequest {
    private String mode; // "exam" 或 "single"
    private Long paperId; // 试卷ID（考试模式必填）
    private Long questionId; // 题目ID（单题模式必填）
    private List<AnswerItem> answers;
    
    @Data
    public static class AnswerItem {
        private Long subItemId;
        private Object answer; // 可以是 String 或 List<String>
    }
}
