package com.example.english_learning_platform.dto.questionbank;

import lombok.Data;

/**
 * 收藏请求 DTO
 */
@Data
public class FavoriteRequest {
    private String type; // "paper" 或 "question"
    private Long id; // 试卷ID或题目ID
}
