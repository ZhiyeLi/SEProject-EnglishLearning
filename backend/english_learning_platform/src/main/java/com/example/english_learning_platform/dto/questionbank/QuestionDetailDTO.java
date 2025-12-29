package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * 题目详情 DTO（单题模式）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailDTO {
    private QuestionInfo question;
    private List<SubItemDTO> subItems;
    private Boolean isFavorited;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInfo {
        private Long id;
        private Long paperId;
        private String sectionType;
        private String sectionName;
        private String title;
        private String materialText;
        private String materialImage;
        private String audioUrl;
        private Integer audioStartSec;
        private Integer audioEndSec;
        private String category; // 从 paper 表获取
        private String paperName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubItemDTO {
        private Long id;
        private Long parentQuestionId;
        private String content;
        private String itemType;
        private List<OptionDTO> options;
        private List<String> answer;
        private String explanation;
        private BigDecimal scoreValue;
        private Integer sortOrder;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private String key;
        private String value;
    }
}
