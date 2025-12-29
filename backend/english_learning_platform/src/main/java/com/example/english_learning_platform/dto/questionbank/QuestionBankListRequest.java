package com.example.english_learning_platform.dto.questionbank;

import lombok.Data;
import java.util.List;

/**
 * 题库列表请求 DTO
 */
@Data
public class QuestionBankListRequest {
    private String mode; // "exam" 或 "single"
    private Integer page = 1;
    private Integer pageSize = 10;
    private String sortBy = "year_desc"; // year_desc, year_asc, created_desc, created_asc
    private String keyword;
    private String category = "all"; // all, CET4, CET6, KY, IELTS, TOEFL
    private String sectionType = "all"; // all, listening, reading, writing, speaking
    private List<String> containsSectionType; // 考试模式：包含的题型
    private String status = "all"; // all, not_done, done, favorited
}
