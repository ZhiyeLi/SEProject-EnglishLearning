package com.example.english_learning_platform.dto.questionbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日统计 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatsDTO {
    private Long count; // 今日做题数
    private Long wrongCount; // 今日错题数
}
