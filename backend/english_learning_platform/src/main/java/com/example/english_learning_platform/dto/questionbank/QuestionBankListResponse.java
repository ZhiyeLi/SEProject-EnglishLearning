package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 列表响应 DTO（分页）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankListResponse<T> {
    private List<T> items;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
