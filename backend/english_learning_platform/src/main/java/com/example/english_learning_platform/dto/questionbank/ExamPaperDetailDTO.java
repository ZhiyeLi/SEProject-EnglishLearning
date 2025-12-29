package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * 试卷详情 DTO（考试模式）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperDetailDTO {
    private PaperInfo paper;
    private List<QuestionDTO> questions;
    private List<SubItemDTO> subItems;
    private Boolean isFavorited;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaperInfo {
        private Long id;
        private String name;
        private String category;
        private Integer year;
        private Integer difficulty;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
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
        private Integer sortOrder;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubItemDTO {
        private Long id;
        private Long parentQuestionId;
        private String content;
        private String itemType; // choice, multiple, text, blank, insert
        private List<OptionDTO> options;
        private List<String> answer; // 仅用于后端校验，不直接返回给前端
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
